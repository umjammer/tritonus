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

/**
 * TODO
 */
public class AlsaCtl {

    /**
     * Contains a pointer to snd_ctl_t.
     */
    @SuppressWarnings("unused")
    private long nativeHandle;

    static {
        Alsa.loadNativeLibrary();
    }

    public static native int loadCard(int card);

    // this encapsulates snd_card_next()
    public static native int[] getCards();

    public static native int getCardIndex(String name);

    public static native String getCardName(int card);

    public static native String getCardLongName(int card);

    /**
     * Open a ctl.
     * <p>
     * Objects created with this constructor have to be
     * closed by calling {@link #close() close()}. This is
     * necessary to free native resources.
     *
     * @param name The name of the sound card. For
     *                instance, "hw:0", or an identifier you gave the
     *                card ("CARD1").
     * @param mode   Special modes for the low-level opening
     *                like SND_CTL_NONBLOCK, SND_CTL_ASYNC. Normally, set
     *                this to 0.
     */
    public AlsaCtl(String name, int mode) throws Exception {
        if (open(name, mode) < 0) {
            throw new IllegalStateException("open");
        }
    }

    public AlsaCtl(int card) throws Exception {
        this("hw:" + card, 0);
    }

    /**
     * Calls snd_ctl_open().
     */
    private native int open(String name, int mode);

    /**
     * Calls snd_ctl_close().
     */
    public native int close();

    /**
     * Calls snd_ctl_card_info().
     */
    public native int getCardInfo(AlsaCtlCardInfo cardInfo);

    // TODO ??
    public native int[] getPcmDevices();

    // TODO remove

    /**
     * values[0] device (inout)
     * values[1] subdevice (inout)
     * values[2] stream (inout)
     * values[3] card (out)
     * values[4] class (out)
     * values[5] subclass (out)
     * values[6] subdevice count (out)
     * values[7] subdevice available (out)
     * <p>
     * strings[0] id (out)
     * strings[1] name (out)
     * strings[2] subdevice name (out)
     */
    public native int getPcmInfo(int[] values, String[] strings);

    private static native void setTrace(boolean trace);
}
