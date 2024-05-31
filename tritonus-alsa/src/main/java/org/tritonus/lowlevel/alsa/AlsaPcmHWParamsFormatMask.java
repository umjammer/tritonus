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
public class AlsaPcmHWParamsFormatMask {

    private static final Logger logger = getLogger("org.tritonus.TraceAlsaPcmNative");

    /**
     * Holds the pointer to snd_pcm_format_mask_t
     * for the native code.
     * This must be long to be 64bit-clean.
     */
    @SuppressWarnings("unused")
    private long nativeHandle;

    public AlsaPcmHWParamsFormatMask() {
        logger.log(Level.TRACE, "AlsaPcmHWParamsFormatMask.<init>(): begin");

        int ret = malloc();
        if (ret < 0) {
            throw new RuntimeException("malloc of format_mask failed");
        }

        logger.log(Level.TRACE, "AlsaPcmHWParamsFormatMask.<init>(): end");
    }

    /**
     * Calls snd_pcm_format_mask_malloc().
     */
    private native int malloc();

    /**
     * Calls snd_pcm_format_mask_free().
     */
    public native void free();

    /**
     * Calls snd_pcm_format_mask_none().
     */
    public native void none();

    /**
     * Calls snd_pcm_format_mask_any().
     */
    public native void any();

    /**
     * Calls snd_pcm_format_mask_test().
     */
    public native boolean test(int format);

    /**
     * Calls snd_pcm_format_mask_set().
     */
    public native void set(int format);

    /**
     * Calls snd_pcm_format_mask_reset().
     */
    public native void reset(int format);
}
