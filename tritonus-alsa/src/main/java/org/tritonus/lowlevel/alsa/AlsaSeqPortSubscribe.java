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


public class AlsaSeqPortSubscribe {

    private static final Logger logger = getLogger("org.tritonus.TraceAlsaSeqNative");

    static {
        Alsa.loadNativeLibrary();
    }

    /**
     * Holds the pointer to snd_seq_port_info_t
     * for the native code.
     * This must be long to be 64bit-clean.
     */
    /* private */ long m_lNativeHandle;

    public AlsaSeqPortSubscribe() {
        logger.log(Level.TRACE, "AlsaSeq.PortSubscribe.<init>(): begin");

        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of port_info failed");
        }

        logger.log(Level.TRACE, "AlsaSeq.PortSubscribe.<init>(): end");
    }

    private native int malloc();

    public native void free();

    public native int getSenderClient();

    public native int getSenderPort();

    public native int getDestClient();

    public native int getDestPort();

    public native int getQueue();

    public native boolean getExclusive();

    public native boolean getTimeUpdate();

    public native boolean getTimeReal();

    public native void setSender(int nClient, int nPort);

    public native void setDest(int nClient, int nPort);

    public native void setQueue(int nQueue);

    public native void setExclusive(boolean bExclusive);

    public native void setTimeUpdate(boolean bUpdate);

    public native void setTimeReal(boolean bReal);

    private static native void setTrace(boolean bTrace);
}
