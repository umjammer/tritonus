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
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

import org.tritonus.lowlevel.alsa.AlsaSeq;
import org.tritonus.lowlevel.alsa.AlsaSeqEvent;
import org.tritonus.lowlevel.alsa.AlsaSeqPortSubscribe;

import static java.lang.System.getLogger;


/**
 * Handles input from an ALSA port.
 */
public class AlsaMidiIn extends Thread {

    private static final Logger logger = getLogger("org.tritonus.TraceAlsaMidiIn");

    /**
     * ALSA client used to receive events.
     */
    private AlsaSeq alsaSeq;

    /**
     * ALSA port number (belonging to the client represented be
     * alsaSeq) used to receive events.
     */
    private int destPort;

    /**
     * ALSA client number to subscribe to receive events.
     */
    private int sourceClient;

    /**
     * ALSA port number (belonging to sourceClient) to
     * subscribe to receive events.
     */
    private int sourcePort;

    private final AlsaMidiInListener listener;

    private final AlsaSeqEvent event = new AlsaSeqEvent();

    // used to query event for detailed information
    private final int[] values = new int[5];
    private final long[] valuesL = new long[1];

    /**
     * Receives events without timestamping them.
     * Does establish a subscription where events are routed directely
     * (not getting a timestamp).
     *
     * @param alsaSeq       The client that should be used to receive
     *                      events.
     * @param destPort     The port number that should be used to receive
     *                      events. This port has to exist on the client represented by
     *                      alsaSeq.
     * @param sourceClient The client number that should be listened
     *                      to. This and sourcePort must exist prior to calling this
     *                      constructor. The port has to allow read subscriptions.
     * @param sourcePort   The port number that should be listened
     *                      to. This and sourceClient must exist prior to calling this
     *                      constructor. The port has to allow read subscriptions.
     * @param listener      The listener that should receive the
     *                      MidiMessage objects created here from received events.
     */
    public AlsaMidiIn(AlsaSeq alsaSeq,
                      int destPort,
                      int sourceClient,
                      int sourcePort,
                      AlsaMidiInListener listener) {
        this(alsaSeq,
                destPort,
                sourceClient,
                sourcePort,
                -1, false,  // signals: do not do timestamping
                listener);
    }

    /**
     * Does establish a subscription where events are routed through
     * a queue to get a timestamp.
     */
    public AlsaMidiIn(AlsaSeq alsaSeq,
                      int destPort,
                      int sourceClient,
                      int sourcePort,
                      int timestampingQueue,
                      boolean realtime,
                      AlsaMidiInListener listener) {
        this.sourceClient = sourceClient;
        this.sourcePort = sourcePort;
        this.listener = listener;
        this.alsaSeq = alsaSeq;
        this.destPort = destPort;
        if (timestampingQueue >= 0) {
            AlsaSeqPortSubscribe portSubscribe = new AlsaSeqPortSubscribe();
            portSubscribe.setSender(sourceClient, sourcePort);
            portSubscribe.setDest(getAlsaSeq().getClientId(), destPort);
            portSubscribe.setQueue(timestampingQueue);
            portSubscribe.setExclusive(false);
            portSubscribe.setTimeUpdate(true);
            portSubscribe.setTimeReal(realtime);
            getAlsaSeq().subscribePort(portSubscribe);
            portSubscribe.free();
        } else {
            AlsaSeqPortSubscribe portSubscribe = new AlsaSeqPortSubscribe();
            portSubscribe.setSender(sourceClient, sourcePort);
            portSubscribe.setDest(getAlsaSeq().getClientId(), destPort);
            getAlsaSeq().subscribePort(portSubscribe);
            portSubscribe.free();
        }
        setDaemon(true);
    }

    private AlsaSeq getAlsaSeq() {
        return alsaSeq;
    }

    /**
     * The working part of the class.
     * Here, the thread repeats in blocking in a call to
     * getEvent() and calling the listener's
     * dequeueEvent() method.
     */
    @Override
    public void run() {
        // TODO recheck interupt mechanism
        while (!interrupted()) {
            MidiEvent event = getEvent();
            logger.log(Level.TRACE, "got event: " + event);

            if (event != null) {
                MidiMessage message = event.getMessage();
                long timestamp = event.getTick();
                if (message instanceof MetaMessage me) {
                    logger.log(Level.TRACE, "MetaMessage.getData().length: " + me.getData().length);
                }
                listener.dequeueEvent(message, timestamp);
            } else {
                logger.log(Level.TRACE, "received null from getEvent()");
            }
        }
    }

    private MidiEvent getEvent() {
        logger.log(Level.TRACE, "before eventInput()");

        while (true) {
            int ret = getAlsaSeq().eventInput(event);
            if (ret >= 0) {
                break;
            }

            /*
             * Sleep for 1 ms to enable scheduling.
             */
            logger.log(Level.TRACE, "sleeping because got no event");
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }
        MidiMessage message = null;
        int type = event.getType();
        switch (type) {
        case AlsaSeq.SND_SEQ_EVENT_NOTEON:
        case AlsaSeq.SND_SEQ_EVENT_NOTEOFF:
        case AlsaSeq.SND_SEQ_EVENT_KEYPRESS: {
            logger.log(Level.TRACE, "note/aftertouch event");

            event.getNote(values);
            ShortMessage shortMessage = new ShortMessage();
            int command = switch (type) {
                case AlsaSeq.SND_SEQ_EVENT_NOTEON -> ShortMessage.NOTE_ON;
                case AlsaSeq.SND_SEQ_EVENT_NOTEOFF -> ShortMessage.NOTE_OFF;
                case AlsaSeq.SND_SEQ_EVENT_KEYPRESS -> ShortMessage.POLY_PRESSURE;
                default -> -1;
            };
            int channel = values[0] & 0xF;
            int key = values[1] & 0x7F;
            int velocity = values[2] & 0x7F;
            try {
                shortMessage.setMessage(command, channel, key, velocity);
            } catch (InvalidMidiDataException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            message = shortMessage;
            break;
        }

        // all event types that use snd_seq_ev_ctrl_t
        // TODO more
        case AlsaSeq.SND_SEQ_EVENT_CONTROLLER:
        case AlsaSeq.SND_SEQ_EVENT_PGMCHANGE:
        case AlsaSeq.SND_SEQ_EVENT_CHANPRESS:
        case AlsaSeq.SND_SEQ_EVENT_PITCHBEND:
        case AlsaSeq.SND_SEQ_EVENT_QFRAME:
        case AlsaSeq.SND_SEQ_EVENT_SONGPOS:
        case AlsaSeq.SND_SEQ_EVENT_SONGSEL: {
            event.getControl(values);
            int command = -1;
            int channel = values[0] & 0xF;
            int data1 = -1;
            int data2 = -1;
            switch (type) {
            case AlsaSeq.SND_SEQ_EVENT_CONTROLLER:
                logger.log(Level.TRACE, "controller event");

                command = ShortMessage.CONTROL_CHANGE;
                data1 = values[1] & 0x7F;
                data2 = values[2] & 0x7F;
                break;

            case AlsaSeq.SND_SEQ_EVENT_PGMCHANGE:
                logger.log(Level.TRACE, "program change event");

                command = ShortMessage.PROGRAM_CHANGE;
                data1 = values[2] & 0x7F;
                data2 = 0;
                break;

            case AlsaSeq.SND_SEQ_EVENT_CHANPRESS:
                logger.log(Level.TRACE, "channel pressure event");

                command = ShortMessage.CHANNEL_PRESSURE;
                data1 = values[2] & 0x7F;
                data2 = 0;
                break;

            case AlsaSeq.SND_SEQ_EVENT_PITCHBEND:
                logger.log(Level.TRACE, "pitchbend event");

                command = ShortMessage.PITCH_BEND;
                data1 = values[2] & 0x7F;
                data2 = (values[2] >> 7) & 0x7F;
                break;

            case AlsaSeq.SND_SEQ_EVENT_QFRAME:
                logger.log(Level.TRACE, "MTC event");

                command = ShortMessage.MIDI_TIME_CODE;
                data1 = values[2] & 0x7F;
                data2 = 0;
                break;

            case AlsaSeq.SND_SEQ_EVENT_SONGPOS:
                logger.log(Level.TRACE, "song position event");

                command = ShortMessage.SONG_POSITION_POINTER;
                data1 = values[2] & 0x7F;
                data2 = (values[2] >> 7) & 0x7F;
                break;

            case AlsaSeq.SND_SEQ_EVENT_SONGSEL:
                logger.log(Level.TRACE, "song select event");

                command = ShortMessage.SONG_SELECT;
                data1 = values[2] & 0x7F;
                data2 = 0;
                break;
            }
            ShortMessage shortMessage = new ShortMessage();
            try {
                shortMessage.setMessage(command, channel, data1, data2);
            } catch (InvalidMidiDataException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            message = shortMessage;
        }
        break;

        // status-only events
        case AlsaSeq.SND_SEQ_EVENT_TUNE_REQUEST:
        case AlsaSeq.SND_SEQ_EVENT_CLOCK:
        case AlsaSeq.SND_SEQ_EVENT_START:
        case AlsaSeq.SND_SEQ_EVENT_CONTINUE:
        case AlsaSeq.SND_SEQ_EVENT_STOP:
        case AlsaSeq.SND_SEQ_EVENT_SENSING:
        case AlsaSeq.SND_SEQ_EVENT_RESET: {
            int status = switch (type) {
                case AlsaSeq.SND_SEQ_EVENT_TUNE_REQUEST -> ShortMessage.TUNE_REQUEST;
                case AlsaSeq.SND_SEQ_EVENT_CLOCK -> {
                    logger.log(Level.TRACE, "clock event");

                    yield ShortMessage.TIMING_CLOCK;
                }
                case AlsaSeq.SND_SEQ_EVENT_START -> ShortMessage.START;
                case AlsaSeq.SND_SEQ_EVENT_CONTINUE -> ShortMessage.CONTINUE;
                case AlsaSeq.SND_SEQ_EVENT_STOP -> ShortMessage.STOP;
                case AlsaSeq.SND_SEQ_EVENT_SENSING -> ShortMessage.ACTIVE_SENSING;
                case AlsaSeq.SND_SEQ_EVENT_RESET -> ShortMessage.SYSTEM_RESET;
                default -> -1;
            };
            ShortMessage shortMessage = new ShortMessage();
            try {
                shortMessage.setMessage(status);
            } catch (InvalidMidiDataException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            message = shortMessage;
            break;
        }

        case AlsaSeq.SND_SEQ_EVENT_USR_VAR4: {
            logger.log(Level.TRACE, "meta event");

            MetaMessage metaMessage = new MetaMessage();
            byte[] transferData = event.getVar();
            int metaType = transferData[0];
            byte[] data = new byte[transferData.length - 1];
            System.arraycopy(transferData, 1, data, 0, transferData.length - 1);
            try {
                metaMessage.setMessage(metaType, data, data.length);
            } catch (InvalidMidiDataException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            message = metaMessage;
            break;
        }

        case AlsaSeq.SND_SEQ_EVENT_SYSEX: {
            logger.log(Level.TRACE, "sysex event");

            SysexMessage sysexMessage = new SysexMessage();
            byte[] data = event.getVar();
            try {
                sysexMessage.setMessage(data, data.length);
            } catch (InvalidMidiDataException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            message = sysexMessage;
            break;
        }

        default:
            logger.log(Level.TRACE, "unknown event");
        }
        if (message != null) {
            // If the timestamp is in ticks, ticks in the MidiEvent
            // gets this value.
            // Otherwise, if the timestamp is in realtime (ns),
            // we put us in the tick value.
            long timestamp = event.getTimestamp();
            if ((event.getFlags() & AlsaSeq.SND_SEQ_TIME_STAMP_MASK) == AlsaSeq.SND_SEQ_TIME_STAMP_REAL) {
                // ns -> us
                timestamp /= 1000;
            }
            MidiEvent event = new MidiEvent(message, timestamp);
            return event;
        } else {
            return null;
        }
    }

    /**
     *
     */
    public interface AlsaMidiInListener {

        void dequeueEvent(MidiMessage message, long timestamp);
    }
}
