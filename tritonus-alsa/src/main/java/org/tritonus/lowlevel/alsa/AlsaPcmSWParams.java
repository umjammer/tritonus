/*
 *  Copyright (c) 2000 - 2001 by Matthias Pfisterer
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
 */

package org.tritonus.lowlevel.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


/**
 * TODO
 */
public class AlsaPcmSWParams {

    private static final Logger logger = getLogger("org.tritonus.TraceAlsaPcmNative");

    /**
     * Holds the pointer to snd_pcm_sw_params_t
     * for the native code.
     * This must be long to be 64bit-clean.
     */
    @SuppressWarnings("unused")
    private long nativeHandle;

    public AlsaPcmSWParams() {
        logger.log(Level.TRACE, "begin");

        int ret = malloc();
        if (ret < 0) {
            throw new RuntimeException("malloc of hw_params failed");
        }

        logger.log(Level.TRACE, "end");
    }

    private native int malloc();

    public native void free();

    public native int getStartMode();

    public native int getXrunMode();

    public native int getTStampMode();

    public native int getSleepMin();

    public native int getAvailMin();

    public native int getXferAlign();

    public native int getStartThreshold();

    public native int getStopThreshold();

    public native int getSilenceThreshold();

    public native int getSilenceSize();
}
