/*
 *  Copyright (c) 2001 by Matthias Pfisterer
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

package org.tritonus.lowlevel.cdda.cdparanoia;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


/**
 * Reading audio CDs using libcdparanoia.
 */
public class Cdparanoia {

    private static final Logger logger = getLogger("org.tritonus.TraceCdda");

    static {
        logger.log(Level.TRACE, "loading native library tritonuscdparanoia");

        System.loadLibrary("tritonuscdparanoia");
        logger.log(Level.TRACE, "loaded");

        if (Boolean.getBoolean("tritonus.DisableParanoia")) {
            setParanoiaMode(false);
        }
    }

    /**
     * This holds a pointer for the native code -
     * do not touch!
     */
    @SuppressWarnings("unused")
    private long nativeHandle;

    public Cdparanoia(String device) {
        logger.log(Level.TRACE, "begin");

        int result = open(device);
        if (result < 0) {
            throw new RuntimeException("cannot open device '" + device + "'");
        }

        logger.log(Level.TRACE, "end");
    }

    /**
     * Searches the device.
     * Calls cdda_identify().
     *
     * @return 0 on success, negative values on error.
     */
    private native int find(String device);

    /**
     * Opens and initializes the device.
     * Calls cdda_open(), paranoia_init()
     * and paranoia_modeset().
     *
     * @return 0 on success, negative values on error.
     */
    private native int open(String device);

    /**
     * Closes the device.
     * Calls cdda_close().
     */
    public native void close();

    /**
     * Read the table of contents.
     * values[0] first track
     * values[1] last track
     * <p>
     * anStartTrack[x] start sector of the track x.
     * type[x] type of track x.
     */
    public native int readTOC(int[] values,
                              int[] startFrame,
                              int[] length,
                              int[] type,
                              boolean[] audio,
                              boolean[] copy,
                              boolean[] pre,
                              int[] channels);

    public native int prepareTrack(int track);

    /**
     * Reads one or more raw frames from the CD.
     * This call reads <CODE>count</CODE> frames from
     * the track that has been set by
     * <CODE>prepareTrack()</CODE>.
     * <CODE>data</CODE>  has to be big enough to hold the
     * amount of data requested (<CODE>2352 * count</CODE> bytes).
     */
    public native int readNextFrame(int count, byte[] data);

    private static native void setTrace(boolean trace);

    /**
     * Set the paranoia level.
     * This setting influences the value that is used in the call
     * 'paranoia_modeset(cdrom_paranoia*, int [mode])'.
     * If set to true a hard-coded default value will be used.
     * (Currently 'PARANOIA_MODE_FULL ^ PARANOIA_MODE_NEVERSKIP', but
     * for definitive answers, look it up in src/lib/cdparanoia/org_tritonus_lowlevel_cdda_cdparanoia_Cdparanoia.c).
     * If set to false, 'PARANOIA_MODE_DISABLE' will be used.
     * Note that currently, changing this value only has an effect prior
     * to opening the device.
     */
    private static native void setParanoiaMode(boolean poranoiaMode);
}
