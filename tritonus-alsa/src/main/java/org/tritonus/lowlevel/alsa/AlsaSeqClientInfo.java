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


public class AlsaSeqClientInfo {

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

    public AlsaSeqClientInfo() {
        logger.log(Level.TRACE, "AlsaSeq.ClientInfo.<init>(): begin");

        int nReturn = malloc();
        logger.log(Level.TRACE, "AlsaSeq.ClientInfo.<init>(): malloc() returns: " + nReturn);

        if (nReturn < 0) {
            throw new RuntimeException("malloc of client_info failed");
        }

        logger.log(Level.TRACE, "AlsaSeq.ClientInfo.<init>(): end");
    }

    private native int malloc();

    public native void free();

    public native int getClient();

    public native int getType();

    public native String getName();

    public native int getBroadcastFilter();

    public native int getErrorBounce();

    // TODO event filter

    public native int getNumPorts();

    public native int getEventLost();

    public native void setClient(int nClient);

    public native void setName(String strName);

    public native void setBroadcastFilter(int nBroadcastFilter);

    public native void setErrorBounce(int nErrorBounce);

    private static native void setTrace(boolean bTrace);

    // TODO event filter
}
