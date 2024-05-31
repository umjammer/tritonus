/*
 *  Copyright (c) 1999 - 2001 by Matthias Pfisterer
 *
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.tritonus.midi.device.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import org.tritonus.lowlevel.alsa.AlsaSeq;
import org.tritonus.lowlevel.alsa.AlsaSeqEvent;
import org.tritonus.lowlevel.alsa.AlsaSeqPortSubscribe;
import org.tritonus.lowlevel.alsa.AlsaSeqQueueStatus;
import org.tritonus.share.midi.TMidiDevice;

import static java.lang.System.getLogger;
import static org.tritonus.share.GlobalInfo.getVendor;
import static org.tritonus.share.GlobalInfo.getVersion;


/**
 * A representation of a physical MIDI port based on the ALSA sequencer.
 */
public class AlsaMidiDevice extends TMidiDevice implements AlsaMidiIn.AlsaMidiInListener {

    private static final Logger logger = getLogger("org.tritonus.TraceMidiDevice");

    /**
     * ALSA client id of the physical port.
     */
    private final int physicalClient;

    /**
     * ALSA port id of the physical port.
     */
    private final int physicalPort;

    /**
     * The object interfacing to the ALSA sequencer.
     */
    private AlsaSeq alsaSeq;

    /**
     * The object used for getting timestamps.
     */
    private AlsaSeqQueueStatus queueStatus;

    /**
     * The ALSA port id of the handler.
     * This is used by alsaSeq.
     */
    private int ownPort;

    /**
     * Handler for input from the physical MIDI port.
     */
    private AlsaMidiIn alsaMidiIn;

    /**
     * Handler for output to the physical MIDI port.
     */
    private AlsaMidiOut alsaMidiOut;

    /**
     * ALSA queue number used to timestamp incoming events.
     */
    private int timestampingQueue;

    /**
     * The event used for starting and stopping the queue.
     */
    private final AlsaSeqEvent event = new AlsaSeqEvent();

    public AlsaMidiDevice(int client, int port, boolean useIn, boolean useOut) {
        this(new TMidiDevice.Info("ALSA MIDI port (" + client + ":" + port + ")",
                        getVendor(), "ALSA MIDI port (" + client + ":" + port + ")", getVersion()),
                client, port, useIn, useOut);
    }

    protected AlsaMidiDevice(MidiDevice.Info info, int client, int port, boolean useIn, boolean useOut) {
        super(info, useIn, useOut);
        logger.log(Level.TRACE, "begin");

        physicalClient = client;
        physicalPort = port;

        logger.log(Level.TRACE, "end");
    }

    protected AlsaSeq getAlsaSeq() {
        return alsaSeq;
    }

    protected int getOwnPort() {
        return ownPort;
    }

    protected int getPhysicalClient() {
        return physicalClient;
    }

    protected int getPhysicalPort() {
        return physicalPort;
    }

    private AlsaSeqQueueStatus getQueueStatus() {
        return queueStatus;
    }

    @Override
    protected void openImpl() {
        logger.log(Level.TRACE, "begin");

        // create an ALSA client...
        alsaSeq = new AlsaSeq("Tritonus Midi port handler");
        // ...and an ALSA port
        ownPort = getAlsaSeq().createPort(
                "handler port",
                AlsaSeq.SND_SEQ_PORT_CAP_WRITE | AlsaSeq.SND_SEQ_PORT_CAP_SUBS_WRITE | AlsaSeq.SND_SEQ_PORT_CAP_READ | AlsaSeq.SND_SEQ_PORT_CAP_SUBS_READ,
                0,
                AlsaSeq.SND_SEQ_PORT_TYPE_APPLICATION,
                0, 0, 0);
        if (getUseTransmitter()) {
            // AlsaMidiIn listens to incoming event on the
            // MIDI port.
            // It calls this.dequeueEvent() if
            // it receives an event.
            timestampingQueue = getAlsaSeq().allocQueue();
            queueStatus = new AlsaSeqQueueStatus();
            // TODO stop queue
            startQueue();
            alsaMidiIn = new AlsaMidiIn(
                    getAlsaSeq(), getOwnPort(),
                    getPhysicalClient(), getPhysicalPort(),
                    getTimestampingQueue(), true,
                    this);
            alsaMidiIn.start();
        }
        if (getUseReceiver()) {
            // uses subscribers, immediately
            alsaMidiOut = new AlsaMidiOut(getAlsaSeq(), getOwnPort());
            AlsaSeqPortSubscribe portSubscribe = new AlsaSeqPortSubscribe();
            portSubscribe.setSender(getAlsaSeq().getClientId(), getOwnPort());
            portSubscribe.setDest(getPhysicalClient(), getPhysicalPort());
            getAlsaSeq().subscribePort(portSubscribe);
            portSubscribe.free();
        }

        logger.log(Level.TRACE, "end");
    }

    @Override
    protected void closeImpl() {
        logger.log(Level.TRACE, "begin");

        if (getUseTransmitter()) {
            alsaMidiIn.interrupt();
            alsaMidiIn = null;
            stopQueue();
            // TODO release timestamping queue
            queueStatus.free();
            queueStatus = null;
        }
        // TODO
//        getAlsaSeq().destroyPort(getOwnPort());
        getAlsaSeq().close();
        alsaSeq = null;

        logger.log(Level.TRACE, "end");
    }

    public long getMicroSecondPosition() {
        logger.log(Level.TRACE, "begin");

        long position = 0;
        if (queueStatus != null) {
            getAlsaSeq().getQueueStatus(getTimestampingQueue(), getQueueStatus());
            long nanoSeconds = getQueueStatus().getRealTime();
            position = nanoSeconds / 1000;
        }

        logger.log(Level.TRACE, "end");

        return position;
    }

    private void startQueue() {
        controlQueue(AlsaSeq.SND_SEQ_EVENT_START);
    }

    private void stopQueue() {
        controlQueue(AlsaSeq.SND_SEQ_EVENT_STOP);
    }

    private void controlQueue(int type) {
        event.setCommon(type,
                AlsaSeq.SND_SEQ_TIME_STAMP_REAL | AlsaSeq.SND_SEQ_TIME_MODE_REL,
                0, AlsaSeq.SND_SEQ_QUEUE_DIRECT, 0L,
                0, getOwnPort(),
                AlsaSeq.SND_SEQ_CLIENT_SYSTEM, AlsaSeq.SND_SEQ_PORT_SYSTEM_TIMER);
        event.setQueueControl(getTimestampingQueue(), 0, 0);
        getAlsaSeq().eventOutputDirect(event);
    }

    /**
     * Pass MidiMessage from Receivers to physical MIDI port.
     */
    @Override
    protected void receive(MidiMessage message, long timeStamp) {
        if (isOpen()) {
            alsaMidiOut.enqueueMessage(message, timeStamp);
        }
    }

    // for AlsaMidiInListener

    /** passes events read from the device to the Transmitters */
    @Override
    public void dequeueEvent(MidiMessage message, long timestamp) {
        logger.log(Level.TRACE, "message: " + message);

        logger.log(Level.TRACE, "tick: " + timestamp);

        // send via superclass method
        sendImpl(message, timestamp);
    }

    private int getTimestampingQueue() {
        return timestampingQueue;
    }

    @Override
    public Receiver getReceiver() throws MidiUnavailableException {
        if (!getUseReceiver()) {
            throw new MidiUnavailableException("Receivers are not supported by this device");
        }
        return new AlsaMidiDeviceReceiver();
    }

    @Override
    public Transmitter getTransmitter() throws MidiUnavailableException {
        if (!getUseTransmitter()) {
            throw new MidiUnavailableException("Transmitters are not supported by this device");
        }
        return new AlsaMidiDeviceTransmitter();
    }

    // inner classes ----

    private class AlsaMidiDeviceReceiver extends TReceiver implements AlsaReceiver {

        public AlsaMidiDeviceReceiver() {
            super();
        }

        /**
         * Subscribe to the passed port.
         * This establishes a subscription in the ALSA sequencer
         * so that the device this Receiver belongs to receives
         * event from the client:port passed as parameters.
         *
         * @return true if subscription was established,
         * false otherwise
         */
        @Override
        public boolean subscribeTo(int client, int port) {
            try {
                AlsaSeqPortSubscribe portSubscribe = new AlsaSeqPortSubscribe();
                portSubscribe.setSender(client, port);
                portSubscribe.setDest(AlsaMidiDevice.this.getPhysicalClient(), AlsaMidiDevice.this.getPhysicalPort());
                AlsaMidiDevice.this.getAlsaSeq().subscribePort(portSubscribe);
                portSubscribe.free();
                return true;
            } catch (RuntimeException e) {
                logger.log(Level.ERROR, e.getMessage(), e);

                return false;
            }
        }
    }

    private class AlsaMidiDeviceTransmitter extends TTransmitter {

        private boolean receiverSubscribed;

        public AlsaMidiDeviceTransmitter() {
            receiverSubscribed = false;
        }

        /**
         * Try to establish a subscription of the Receiver
         * to the ALSA sequencer client of the device this
         * Transmitter belongs to.
         */
        @Override
        public void setReceiver(Receiver receiver) {
            super.setReceiver(receiver);
            if (receiver instanceof AlsaReceiver alsaReceiver) {
                receiverSubscribed = alsaReceiver.subscribeTo(getPhysicalClient(), getPhysicalPort());
            }
        }

        /**
         * Send message via Java methods only if no
         * subscription was established. If there is a
         * subscription, the message is routed inside
         * the ALSA sequencer.
         */
        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (!receiverSubscribed) {
                super.send(message, timeStamp);
            }
        }

        @Override
        public void close() {
            super.close();
            // TODO remove subscription
        }
    }
}
