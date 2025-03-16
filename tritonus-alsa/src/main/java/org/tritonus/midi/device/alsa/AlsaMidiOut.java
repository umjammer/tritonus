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
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;

import org.tritonus.lowlevel.alsa.AlsaSeq;
import org.tritonus.lowlevel.alsa.AlsaSeqEvent;

import static java.lang.System.getLogger;
import static org.tritonus.lowlevel.alsa.AlsaSeq.SND_SEQ_ADDRESS_SUBSCRIBERS;
import static org.tritonus.lowlevel.alsa.AlsaSeq.SND_SEQ_ADDRESS_UNKNOWN;
import static org.tritonus.lowlevel.alsa.AlsaSeq.SND_SEQ_QUEUE_DIRECT;
import static org.tritonus.lowlevel.alsa.AlsaSeq.SND_SEQ_TIME_MODE_REL;
import static org.tritonus.lowlevel.alsa.AlsaSeq.SND_SEQ_TIME_STAMP_REAL;
import static org.tritonus.lowlevel.alsa.AlsaSeq.SND_SEQ_TIME_STAMP_TICK;


/**
 * This class sends events always to clients that have subscribed to the client
 * passed as AlsaSeq object and the source port number passed.
 * This class doesn't establish any subscriptions. They have to be
 * established elsewhere.
 */
public class AlsaMidiOut {

    private static final Logger logger = getLogger("org.tritonus.TraceAlsaMidiOut");

    /**
     * The low-level object to interface to the ALSA sequencer.
     */
    private final AlsaSeq alsaSeq;

    /**
     * The source port to use for sending messages via the ALSA sequencer.
     */
    private final int sourcePort;

    /**
     * The sequencer queue to use inside the ALSA sequencer.
     * This value is only used (and valid) if immediately
     * false. Otherwise, events are sent directely to the destination
     * client, circumventing queues.
     */
    private final int queue;

    private final boolean immediately;

    private boolean handleMetaMessages;

    private final AlsaSeqEvent event = new AlsaSeqEvent();

    /*
     * Sends to all subscribers via queue.
     */
    public AlsaMidiOut(AlsaSeq sequencer, int sourcePort, int queue) {
        this(sequencer, sourcePort, queue, false);
    }

    /*
     * Sends to all subscribers immediately.
     */
    public AlsaMidiOut(AlsaSeq sequencer, int sourcePort) {
        this(sequencer, sourcePort, -1, true);
    }

    private AlsaMidiOut(AlsaSeq sequencer, int sourcePort, int queue, boolean immediately) {
        logger.log(Level.TRACE, "begin");

        alsaSeq = sequencer;
        this.sourcePort = sourcePort;
        this.queue = queue;
        this.immediately = immediately;
        handleMetaMessages = false;

        logger.log(Level.TRACE, "end");
    }

    private AlsaSeq getAlsaSeq() {
        return alsaSeq;
    }

    private int getSourcePort() {
        return sourcePort;
    }

    private int getQueue() {
        return queue;
    }

    private boolean getImmediately() {
        return immediately;
    }

    public boolean getHandleMetaMessages() {
        return handleMetaMessages;
    }

    public void setHandleMetaMessages(boolean handleMetaMessages) {
        this.handleMetaMessages = handleMetaMessages;
    }

    public synchronized void enqueueMessage(MidiMessage event, long tick) {
        logger.log(Level.TRACE, "begin");

        if (event instanceof ShortMessage) {
            enqueueShortMessage((ShortMessage) event, tick);
        } else if (event instanceof SysexMessage) {
            enqueueSysexMessage((SysexMessage) event, tick);
        } else if (event instanceof MetaMessage && getHandleMetaMessages()) {
            enqueueMetaMessage((MetaMessage) event, tick);
        } else {
            // Ignore it.
        }

        logger.log(Level.TRACE, "end");
    }

    private void enqueueShortMessage(ShortMessage shortMessage, long time) {
        int channel = shortMessage.getChannel();
        switch (shortMessage.getCommand()) {
        case ShortMessage.NOTE_OFF:
            sendNoteOffEvent(time, channel, shortMessage.getData1(), shortMessage.getData2());
            break;

        case ShortMessage.NOTE_ON:
            sendNoteOnEvent(time, channel, shortMessage.getData1(), shortMessage.getData2());
            break;

        case ShortMessage.POLY_PRESSURE:
            sendKeyPressureEvent(time, channel, shortMessage.getData1(), shortMessage.getData2());
            break;

        case ShortMessage.CONTROL_CHANGE:
            sendControlChangeEvent(time, channel, shortMessage.getData1(), shortMessage.getData2());
            break;

        case ShortMessage.PROGRAM_CHANGE:
            sendProgramChangeEvent(time, channel, shortMessage.getData1());
            break;

        case ShortMessage.CHANNEL_PRESSURE:
            sendChannelPressureEvent(time, channel, shortMessage.getData1());
            break;

        case ShortMessage.PITCH_BEND:
            sendPitchBendEvent(time, channel, get14bitValue(shortMessage.getData1(), shortMessage.getData2()));
            break;

        case 0xF0:
            switch (shortMessage.getStatus()) {
            case ShortMessage.MIDI_TIME_CODE:
                sendMTCEvent(time, shortMessage.getData1());
                break;

            case ShortMessage.SONG_POSITION_POINTER:
                sendSongPositionPointerEvent(time, get14bitValue(shortMessage.getData1(), shortMessage.getData2()));
                break;

            case ShortMessage.SONG_SELECT:
                sendSongSelectEvent(time, shortMessage.getData1());
                break;

            case ShortMessage.TUNE_REQUEST:
                sendTuneRequestEvent(time);
                break;

            case ShortMessage.TIMING_CLOCK:
                sendMidiClockEvent(time);
                break;

            case ShortMessage.START:
                sendStartEvent(time);
                break;

            case ShortMessage.CONTINUE:
                sendContinueEvent(time);
                break;

            case ShortMessage.STOP:
                sendStopEvent(time);
                break;

            case ShortMessage.ACTIVE_SENSING:
                sendActiveSensingEvent(time);
                break;

            case ShortMessage.SYSTEM_RESET:
                sendSystemResetEvent(time);
                break;

            default:
                logger.log(Level.TRACE, "UNKNOWN EVENT TYPE: " + shortMessage.getStatus());
            }
            break;

        default:
            logger.log(Level.TRACE, "UNKNOWN EVENT TYPE: " + shortMessage.getStatus());
        }
    }

    private static int get14bitValue(int lsb, int msb) {
        return (lsb & 0x7F) | ((msb & 0x7F) << 7);
    }

    private void sendNoteOffEvent(long time, int channel, int note, int velocity) {
        sendNoteEvent(AlsaSeq.SND_SEQ_EVENT_NOTEOFF, time, channel, note, velocity);
    }

    private void sendNoteOnEvent(long time, int channel, int note, int velocity) {
        sendNoteEvent(AlsaSeq.SND_SEQ_EVENT_NOTEON, time, channel, note, velocity);
    }

    private void sendNoteEvent(int type, long time, int channel, int note, int velocity) {
        setCommon(type, 0, time);
        event.setNote(channel, note, velocity, 0, 0);
        sendEvent();
    }

    private void sendKeyPressureEvent(long time, int channel, int note, int pressure) {
        sendControlEvent(AlsaSeq.SND_SEQ_EVENT_KEYPRESS, time, channel, note, pressure);
    }

    private void sendControlChangeEvent(long time, int channel, int control, int value) {
        sendControlEvent(AlsaSeq.SND_SEQ_EVENT_CONTROLLER, time, channel, control, value);
    }

    private void sendProgramChangeEvent(long time, int channel, int program) {
        sendControlEvent(AlsaSeq.SND_SEQ_EVENT_PGMCHANGE, time, channel, 0, program);
    }

    private void sendChannelPressureEvent(long time, int channel, int pressure) {
        sendControlEvent(AlsaSeq.SND_SEQ_EVENT_CHANPRESS, time, channel, 0, pressure);
    }

    // TODO recheck!!!!
    private void sendPitchBendEvent(long time, int channel, int pitch) {
        sendControlEvent(AlsaSeq.SND_SEQ_EVENT_PITCHBEND, time, channel, 0, pitch);
    }

    private void sendControlEvent(int type, long time, int channel, int param, int value) {
        setCommon(type, 0, time);
        event.setControl(channel, param, value);
        sendEvent();
    }

    private void sendMTCEvent(long time, int data) {
        sendControlEvent(AlsaSeq.SND_SEQ_EVENT_QFRAME, time, 0, 0, data);
    }

    private void sendSongPositionPointerEvent(long time, int position) {
        sendControlEvent(AlsaSeq.SND_SEQ_EVENT_SONGPOS, time, 0, 0, position);
    }

    private void sendSongSelectEvent(long time, int song) {
        sendControlEvent(AlsaSeq.SND_SEQ_EVENT_SONGSEL, time, 0, 0, song);
    }

    private void sendTuneRequestEvent(long time) {
        sendEvent(AlsaSeq.SND_SEQ_EVENT_TUNE_REQUEST, time);
    }

    private void sendMidiClockEvent(long time) {
        sendQueueControlEvent(AlsaSeq.SND_SEQ_EVENT_CLOCK, time, 0, 0, 0);
    }

    private void sendStartEvent(long time) {
        sendQueueControlEvent(AlsaSeq.SND_SEQ_EVENT_START, time, 0, 0, 0);
    }

    private void sendContinueEvent(long time) {
        sendQueueControlEvent(AlsaSeq.SND_SEQ_EVENT_CONTINUE, time, 0, 0, 0);
    }

    private void sendStopEvent(long time) {
        sendQueueControlEvent(AlsaSeq.SND_SEQ_EVENT_STOP, time, 0, 0, 0);
    }

    private void sendActiveSensingEvent(long time) {
        sendEvent(AlsaSeq.SND_SEQ_EVENT_SENSING, time);
    }

    private void sendSystemResetEvent(long time) {
        sendEvent(AlsaSeq.SND_SEQ_EVENT_RESET, time);
    }

    private void sendQueueControlEvent(int type, long time, int queue, int value, long controlTime) {
        setCommon(type, 0, time);
        event.setQueueControl(queue, value, controlTime);
        sendEvent();
    }

    private void sendEvent(int type, long time) {
        setCommon(type, 0, time);
        sendEvent();
    }

    private void enqueueSysexMessage(SysexMessage message, long tick) {
//logger.log(Level.TRACE, "enqueueSysexMessage()");
        byte[] data = message.getMessage();
        int length = message.getLength();
//logger.log(Level.TRACE, "sysex len:" + length);
//logger.log(Level.TRACE, "data[0]:" + (data[0] & 255));
        if ((data[0] & 0xFF) == SysexMessage.SYSTEM_EXCLUSIVE) {
//logger.log(Level.TRACE, "standard sysex branch");
            sendVarEvent(AlsaSeq.SND_SEQ_EVENT_SYSEX, tick, data, 0, length);
        } else { // SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE
//logger.log(Level.TRACE, "special sysex branch");
            sendVarEvent(AlsaSeq.SND_SEQ_EVENT_SYSEX, tick, data, 1, length - 1);
        }
    }

    /**
     * We pack the type byte in front of the data bytes.
     */
    private void enqueueMetaMessage(MetaMessage message, long tick) {
        byte[] data = message.getData();
        byte[] transferData = new byte[data.length + 1];
        transferData[0] = (byte) message.getType();
        System.arraycopy(data, 0, transferData, 1, data.length);
//logger.log(Level.TRACE, "message data length: " + transferData.length);
//logger.log(Level.TRACE, "message length: " + message.getLength());
        sendVarEvent(AlsaSeq.SND_SEQ_EVENT_USR_VAR4, tick, transferData, 0, transferData.length);
    }

    private void sendVarEvent(int type, long time, byte[] data, int offset, int length) {
        setCommon(type, AlsaSeq.SND_SEQ_EVENT_LENGTH_VARIABLE, time);
        event.setVar(data, 0, length);
        sendEvent();
    }

    private void setCommon(int type, int additionalFlags, long time) {
        if (getImmediately()) {
            logger.log(Level.TRACE, "sending noteoff message (immediately)");

            event.setCommon(type, SND_SEQ_TIME_STAMP_REAL | SND_SEQ_TIME_MODE_REL | additionalFlags,
                    0, SND_SEQ_QUEUE_DIRECT, 0L, 0, getSourcePort(),
                    SND_SEQ_ADDRESS_SUBSCRIBERS, SND_SEQ_ADDRESS_UNKNOWN);
        } else { // send via queue
            logger.log(Level.TRACE, "sending noteoff message (timed)");

            event.setCommon(type, SND_SEQ_TIME_STAMP_TICK | AlsaSeq.SND_SEQ_TIME_MODE_ABS | additionalFlags,
                    0, getQueue(), time, 0, getSourcePort(),
                    SND_SEQ_ADDRESS_SUBSCRIBERS, SND_SEQ_ADDRESS_UNKNOWN);
        }
    }

    /**
     * Puts the event into the queue.
     */
    private void sendEvent() {
        getAlsaSeq().eventOutput(event);
        getAlsaSeq().drainOutput();
    }
}
