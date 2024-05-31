/*
 *  Copyright (c) 1999 - 2006 by Matthias Pfisterer
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

package org.tritonus.share.midi;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import static java.lang.System.getLogger;


/**
 * Base class for MidiDevice implementations.
 * The goal of this class is to supply the common functionality for
 * classes that implement the interface MidiDevice.
 */
public abstract class TMidiDevice implements MidiDevice {

    private static final Logger logger= getLogger("org.tritonus.TraceMidiDevice");

    /**
     * The Info object for a certain instance of MidiDevice.
     */
    private final MidiDevice.Info info;

    /**
     * A flag to store whether the device is "open".
     */
    private boolean deviceOpen;

    /**
     * Whether to handle input from the physical port
     * and to allow Transmitters.
     */
    private final boolean useTransmitter;

    /**
     * Whether to handle output to the physical port
     * and to allow Receivers.
     */
    private final boolean useReceiver;

    /**
     * The list of Receiver objects that belong to this
     * MidiDevice.
     *
     * @see #addReceiver
     * @see #removeReceiver
     */
    private final List<Receiver> receivers;

    /**
     * The list of Transmitter objects that belong to this
     * MidiDevice.
     *
     * @see #addTransmitter
     * @see #removeTransmitter
     */
    private final List<Transmitter> transmitters;

    /**
     * Initialize this class.
     * This sets the info from the passed one, sets the open status
     * to false, the number of Receivers to zero and the collection
     * of Transmitters to be empty.
     *
     * @param info The info object that describes this instance.
     */
    public TMidiDevice(MidiDevice.Info info) {
        this(info, true, true);
    }

    /**
     * Initialize this class.
     * This sets the info from the passed one, sets the open status
     * to false, the number of Receivers to zero and the collection
     * of Transmitters to be empty.
     *
     * @param info The info object that describes this instance.
     */
    public TMidiDevice(MidiDevice.Info info, boolean useTransmitter, boolean useReceiver) {
        this.info = info;
        this.useTransmitter = useTransmitter;
        this.useReceiver = useReceiver;
        deviceOpen = false;
        receivers = new ArrayList<>();
        transmitters = new ArrayList<>();
    }

    /**
     * Retrieves a description of this instance.
     * This returns the info object passed to the constructor.
     *
     * @return the description
     * @see #TMidiDevice
     */
    @Override
    public MidiDevice.Info getDeviceInfo() {
        return info;
    }

    @Override
    public synchronized void open() throws MidiUnavailableException {
        logger.log(Level.TRACE, "begin");

        if (!isOpen()) {
            openImpl();
            // If openImpl() throws a MidiUnavailableException, deviceOpen
            // remains false.
            deviceOpen = true;
        }

        logger.log(Level.TRACE, "end");
    }

    /**
     * Subclasses have to override this method to be notified of
     * opening.
     */
    protected void openImpl() throws MidiUnavailableException {
        logger.log(Level.TRACE, "begin");

        logger.log(Level.TRACE, "end");
    }

    @Override
    public synchronized void close() {
        logger.log(Level.TRACE, "begin");

        if (isOpen()) {
            closeImpl();
            // TODO close all Receivers and Transmitters
            deviceOpen = false;
        }

        logger.log(Level.TRACE, "end");
    }

    /**
     * Subclasses have to override this method to be notified of
     * closeing.
     */
    protected void closeImpl() {
        logger.log(Level.TRACE, "begin");

        logger.log(Level.TRACE, "end");
    }

    @Override
    public boolean isOpen() {
        return deviceOpen;
    }

    /**
     * Returns whether to handle input.
     * If this is true, retrieving Transmitters is possible
     * and input from the physical port is passed to them.
     *
     * @see #getUseReceiver
     */
    protected boolean getUseTransmitter() {
        return useTransmitter;
    }

    /**
     * Returns whether to handle output.
     * If this is true, retrieving Receivers is possible
     * and output to them is passed to the physical port.
     *
     * @see #getUseTransmitter
     */
    protected boolean getUseReceiver() {
        return useReceiver;
    }

    /**
     * Returns the device time in microseconds.
     * This is a default implementation, telling the application
     * program that the device doesn't track time. If a device wants
     * to give timing information, it has to override this method.
     */
    @Override
    public long getMicrosecondPosition() {
        return -1;
    }

    @Override
    public int getMaxReceivers() {
        int maxReceivers = 0;
        if (getUseReceiver()) {
            // The value -1 means unlimited.
            maxReceivers = -1;
        }
        return maxReceivers;
    }

    @Override
    public int getMaxTransmitters() {
        int maxTransmitters = 0;
        if (getUseTransmitter()) {
            // The value -1 means unlimited.
            maxTransmitters = -1;
        }
        return maxTransmitters;
    }

    /**
     * Creates a new Receiver object associated with this instance.
     * In this implementation, an unlimited number of Receivers
     * per MidiDevice can be created.
     */
    @Override
    public Receiver getReceiver() throws MidiUnavailableException {
        if (!getUseReceiver()) {
            throw new MidiUnavailableException("Receivers are not supported by this device");
        }
        return new TReceiver();
    }

    /**
     * Creates a new Transmitter object associated with this instance.
     * In this implementation, an unlimited number of Transmitters
     * per MidiDevice can be created.
     */
    @Override
    public Transmitter getTransmitter() throws MidiUnavailableException {
        if (!getUseTransmitter()) {
            throw new MidiUnavailableException("Transmitters are not supported by this device");
        }
        return new TTransmitter();
    }

    @Override
    public List<Receiver> getReceivers() {
        return Collections.unmodifiableList(receivers);
    }

    @Override
    public List<Transmitter> getTransmitters() {
        return Collections.unmodifiableList(transmitters);
    }

    /**
     * Intended for overriding by subclasses to receive messages.
     * This method is called by TMidiDevice.Receiver object on
     * receipt of a MidiMessage.
     */
    protected void receive(MidiMessage message, long timeStamp) {
        logger.log(Level.TRACE, "### [should be overridden] message " + message);
    }

    protected void addReceiver(Receiver receiver) {
        synchronized (receivers) {
            receivers.add(receiver);
        }
    }

    protected void removeReceiver(Receiver receiver) {
        synchronized (receivers) {
            receivers.remove(receiver);
        }
    }

    protected void addTransmitter(Transmitter transmitter) {
        synchronized (transmitters) {
            transmitters.add(transmitter);
        }
    }

    protected void removeTransmitter(Transmitter transmitter) {
        synchronized (transmitters) {
            transmitters.remove(transmitter);
        }
    }

    /**
     * Send a MidiMessage to all Transmitters.
     * This method should be called by subclasses when they get a
     * message from a physical MIDI port.
     */
    protected void sendImpl(MidiMessage message, long timeStamp) {
        logger.log(Level.TRACE, "begin");

        for (Transmitter _transmitter : transmitters) {
            TTransmitter transmitter = (TTransmitter) _transmitter;
            // due to a bug in the Sun jdk1.3, we cannot use
            // clone() for MetaMessages. So we have to do the
            // equivalent ourselves.
            //MidiMessage copiedMessage = (MidiMessage) message.clone();
            MidiMessage copiedMessage;
            if (message instanceof MetaMessage origMessage) {
                MetaMessage metaMessage = new MetaMessage();
                try {
                    metaMessage.setMessage(origMessage.getType(), origMessage.getData(), origMessage.getData().length);
                } catch (InvalidMidiDataException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
                copiedMessage = metaMessage;
            } else {
                copiedMessage = (MidiMessage) message.clone();
            }

            if (message instanceof MetaMessage) {
                logger.log(Level.TRACE, "MetaMessage.getData().length (original): " + ((MetaMessage) message).getData().length);

                logger.log(Level.TRACE, "MetaMessage.getData().length (cloned): " + ((MetaMessage) copiedMessage).getData().length);
            }
            transmitter.send(copiedMessage, timeStamp);
        }

        logger.log(Level.TRACE, "end");
    }

    // INNER CLASSES

    /**
     * Receiver proxy class.
     * This class' objects are handed out on calls to
     * TMidiDevice.getReceiver().
     */
    public class TReceiver implements Receiver {

        private boolean open;

        public TReceiver() {
            TMidiDevice.this.addReceiver(this);
            open = true;
        }

        protected boolean isOpen() {
            return open;
        }

        /**
         * Receive a MidiMessage.
         */
        @Override
        public void send(MidiMessage message, long timeStamp) {
            logger.log(Level.TRACE, "message " + message);

            if (open) {
                TMidiDevice.this.receive(message, timeStamp);
            } else {
                throw new IllegalStateException("receiver is not open");
            }
        }

        /**
         * Closes the receiver.
         * After a receiver has been closed, it does no longer
         * propagate MidiMessages to its associated MidiDevice.
         */
        @Override
        public void close() {
            TMidiDevice.this.removeReceiver(this);
            open = false;
        }
    }

    public class TTransmitter implements Transmitter {

        private boolean open;
        private Receiver receiver;

        public TTransmitter() {
            open = true;
            TMidiDevice.this.addTransmitter(this);
        }

        @Override
        public void setReceiver(Receiver receiver) {
            synchronized (this) {
                this.receiver = receiver;
            }
        }

        @Override
        public Receiver getReceiver() {
            return receiver;
        }

        public void send(MidiMessage message, long timeStamp) {
            if (getReceiver() != null && open) {
                getReceiver().send(message, timeStamp);
            }
        }

        /**
         * Closes the transmitter.
         * After a transmitter has been closed, it no longer
         * passes MidiMessages to a Receiver previously set for
         * it.
         */
        @Override
        public void close() {
            TMidiDevice.this.removeTransmitter(this);
            open = false;
            // Previously, this method just set receiver to null
            // instead of maintaining an open flag. This allows to exploit
            // the behaviour of calling close(), the setReceiver() again,
            // and the Transmitter is "reopened".
            // TODO write a test case for this scenario.
        }
    }

    /**
     * This is needed only because MidiDevice.Info's
     * constructor is protected (in the Sun jdk1.3).
     */
    public static class Info extends MidiDevice.Info {

        public Info(String a, String b, String c, String d) {
            super(a, b, c, d);
        }
    }
}

