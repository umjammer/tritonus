/*
 *  Copyright (c) 2001 by Matthias Pfisterer
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

/**
 * Object carrying a snd_mixer_t.
 */
public class AlsaMixer {

    /**
     * not private because needed to be accessed by AlsaMixerElement.
     * (Better solution: inner classes)
     */
    /* private */ long nativeHandle;

    static {
        Alsa.loadNativeLibrary();
    }

    public AlsaMixer(String mixerName) throws Exception {
        if (open(0) < 0) {
            throw new IllegalStateException("open");
        }
        if (attach(mixerName) < 0) {
            close();
            throw new IllegalStateException("attach");
        }
        if (register() < 0) {
            close();
            throw new IllegalStateException("register");
        }
        if (load() < 0) {
            close();
            throw new IllegalStateException("load");
        }
    }

    /**
     * Calls snd_mixer_open().
     */
    private native int open(int mode);

    /**
     * Calls snd_mixer_attach().
     */
    private native int attach(String cardName);

    /**
     * Calls snd_mixer_selem_register(.., NULL, NULL).
     * This is a hack, taken over from alsamixer.
     */
    private native int register();

    /**
     * Calls snd_mixer_load().
     */
    private native int load();

    /**
     * Calls snd_mixer_free().
     */
    private native int free();

    /**
     * Calls snd_mixer_close().
     */
    public native int close();

    // getCount() ??

    /**
     * The caller has to allocate the indices and names arrays big
     * enough to hold information on all controls. If the retrieving
     * of controls is successful, a positive number (the number of
     * controls) is returned. If the arrays are not big enough, -1
     * is returned. In this case, it's the task of the caller to allocate
     * bigger arrays and try again.
     * Both arrays must be of the same size.
     * <p>
     * Calls snd_mixer_first_elem() and snd_mixer_elem_next().
     */
    public native int readControlList(int[] indices, String[] names);

    public static native void setTrace(boolean trace);
}
