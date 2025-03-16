/*
 *  Copyright (c) 1999 - 2001 by Matthias Pfisterer
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

package org.tritonus.lowlevel.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


/**
 * Event for the sequencer.
 * This class encapsulates an instance of
 * snd_seq_event_t.
 */
public class AlsaSeqEvent {

    private static final Logger logger = getLogger("org.tritonus.TraceAlsaSeqNative");

    static {
        Alsa.loadNativeLibrary();
    }

    /**
     * Holds the pointer to snd_seq_event_t
     * for the native code.
     * This must be long to be 64bit-clean.
     */
    /* private */ long nativeHandle;

    public AlsaSeqEvent() {
        logger.log(Level.TRACE, "begin");

        int ret = malloc();
        if (ret < 0) {
            throw new RuntimeException("malloc of event failed");
        }

        logger.log(Level.TRACE, "end");
    }

    /**
     * Allocates memory for a snd_seq_event_t.
     * <p>
     * The native part of this method uses calloc() to
     * allocate the memory (so the allocated memory is
     * zero'ed).  The memory reference is stored in {@link
     * #nativeHandle nativeHandle}.  Memory allocated
     * with this call should be freed by calling {@link
     * #free() free()}.
     */
    private native int malloc();

    /**
     * Frees memory for a snd_seq_event_t.
     */
    public native void free();

    // TODO implement natively
    public native int getLength();

    public native int getType();

    public native int getFlags();

    public native int getTag();

    public native int getQueue();

    public native long getTimestamp();

    public native int getSourceClient();

    public native int getSourcePort();

    public native int getDestClient();

    public native int getDestPort();

    /**
     * Retrieves the parameters of a note event.
     * This method is suitable for the following event types:
     * SND_SEQ_EVENT_NOTE
     * SND_SEQ_EVENT_NOTEON
     * SND_SEQ_EVENT_NOTEOFF
     * SND_SEQ_EVENT_KEYPRESS
     *
     * After return, the array will contain:
     * values[0] channel
     * values[1] note
     * values[2] velocity
     * values[3] off_velocity
     * values[4] duration
     */
    public native void getNote(int[] values);

    /**
     * Retrieves the parameters of a control event.
     * This method is suitable for the following event types:
     * SND_SEQ_EVENT_CONTROLLER
     * SND_SEQ_EVENT_PGMCHANGE
     * SND_SEQ_EVENT_CHANPRESS
     * SND_SEQ_EVENT_PITCHBEND
     * SND_SEQ_EVENT_CONTROL14
     * SND_SEQ_EVENT_NONREGPARAM
     * SND_SEQ_EVENT_REGPARAM
     * SND_SEQ_EVENT_SONGPOS
     * SND_SEQ_EVENT_SONGSEL
     * SND_SEQ_EVENT_QFRAME
     * SND_SEQ_EVENT_TIMESIGN
     * SND_SEQ_EVENT_KEYSIGN
     *
     * After return, the array will contain:
     * values[0] channel
     * values[1] param
     * values[2] value
     */
    public native void getControl(int[] values);

    /**
     * Retrieves the parameters of a queue control event.
     * This method is suitable for the following event types:
     * SND_SEQ_EVENT_START
     * SND_SEQ_EVENT_CONTINUE
     * SND_SEQ_EVENT_STOP
     * SND_SEQ_EVENT_SETPOS_TICK
     * SND_SEQ_EVENT_SETPOS_TIME
     * SND_SEQ_EVENT_TEMPO
     * SND_SEQ_EVENT_CLOCK
     * SND_SEQ_EVENT_TICK
     * SND_SEQ_EVENT_SYNC
     * SND_SEQ_EVENT_SYNC_POS
     *
     * After return, the array will contain:
     * values[0] queue
     * values[1] value
     * valuesL[0] time
     */
    public native void getQueueControl(int[] values, long[] valuesL);

    /**
     * Retrieves the parameters of a variable-length event.
     * This method is suitable for the following event types:
     * SND_SEQ_EVENT_SYSEX
     * SND_SEQ_EVENT_BOUNCE
     * SND_SEQ_EVENT_USR_VAR0
     * SND_SEQ_EVENT_USR_VAR1
     * SND_SEQ_EVENT_USR_VAR2
     * SND_SEQ_EVENT_USR_VAR3
     * SND_SEQ_EVENT_USR_VAR4
     */
    public native byte[] getVar();

    public native void setCommon(int type, int flags, int tag, int queue, long timestamp, int sourceClient, int sourcePort, int destClient, int destPort);

    public native void setTimestamp(long timestamp);

    public native void setNote(int channel, int key, int velocity, int offVelocity, int duration);

    public native void setControl(int channel, int param, int value);

    public native void setQueueControl(int controlQueue, int controlValue, long controlTime);

    public native void setVar(byte[] data, int offset, int length);

    private static native void setTrace(boolean trace);
}
