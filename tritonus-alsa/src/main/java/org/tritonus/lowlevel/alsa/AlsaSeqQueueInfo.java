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


public class AlsaSeqQueueInfo {

    private static final Logger logger = getLogger("org.tritonus.TraceAlsaSeqNative");

    static {
        Alsa.loadNativeLibrary();
    }

    /**
     * Holds the pointer to snd_seq_queue_info_t
     * for the native code.
     * This must be long to be 64bit-clean.
     */
    /* private */ long nativeHandle;

    public AlsaSeqQueueInfo() {
        logger.log(Level.TRACE, "AlsaSeq.QueueInfo.<init>(): begin");

        int ret = malloc();
        if (ret < 0) {
            throw new RuntimeException("malloc of port_info failed");
        }

        logger.log(Level.TRACE, "AlsaSeq.QueueInfo.<init>(): end");
    }

    private native int malloc();

    public native void free();

    public native int getQueue();

    public native String getName();

    public native int getOwner();

    public native boolean getLocked();

    public native int getFlags();

    public native void setName(String name);

    public native void setOwner(int owner);

    public native void setLocked(boolean locked);

    public native void setFlags(int flags);

    private static native void setTrace(boolean trace);
}
