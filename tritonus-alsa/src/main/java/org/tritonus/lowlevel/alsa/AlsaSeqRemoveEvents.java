/*
 *  Copyright (c) 1999 - 2002 by Matthias Pfisterer
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
 * TODO
 */
public class AlsaSeqRemoveEvents {

    private static final Logger logger = getLogger("org.tritonus.TraceAlsaSeqNative");

    static {
        Alsa.loadNativeLibrary();
    }

    /**
     * Holds the pointer to snd_seq_queue_timer_t
     * for the native code.
     * This must be long to be 64bit-clean.
     */
    /* private */ long nativeHandle;

    static {
        Alsa.loadNativeLibrary();
    }

    public AlsaSeqRemoveEvents() {
        logger.log(Level.TRACE, "begin");

        int ret = malloc();
        if (ret < 0) {
            throw new RuntimeException("malloc of port_info failed");
        }

        logger.log(Level.TRACE, "end");
    }

    private native int malloc();

    public native void free();

    public native int getCondition();

    public native int getQueue();

    public native long getTime();

    public native int getDestClient();

    public native int getDestPort();

    public native int getChannel();

    public native int getEventType();

    public native int getTag();

    public native void setCondition(int condition);

    public native void setQueue(int queue);

    public native void setTime(long time);

    public native void setDest(int client, int port);

    public native void setChannel(int channel);

    public native void setEventType(int eventType);

    public native void setTag(int tag);

    private static native void setTrace(boolean trace);
}
