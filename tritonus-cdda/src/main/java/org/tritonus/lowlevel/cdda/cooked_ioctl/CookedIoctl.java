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

package org.tritonus.lowlevel.cdda.cooked_ioctl;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


/**
 * Reading audio CDs using the 'cooked ioctl' interface.
 */
public class CookedIoctl {

    private static final Logger logger = getLogger("org.tritonus.TraceCdda");

    static {
        logger.log(Level.TRACE, "loading native library tritonuscooked_ioctl");

        System.loadLibrary("tritonuscooked_ioctl");
        logger.log(Level.TRACE, "loaded");
    }

    /**
     * This holds a file descriptor for the native code -
     * do not touch!
     */
    @SuppressWarnings("unused")
    private long nativeHandle;

    // TODO parameter devicename (or something else sensible)
    public CookedIoctl(String device) {
        logger.log(Level.TRACE, "begin");
        int result = open(device);
        if (result < 0) {
            throw new RuntimeException("cannot open" + device);
        }
        logger.log(Level.TRACE, "end");
    }

    /**
     * Opens the device.
     */
    private native int open(String device);

    /**
     * Closes the device.
     */
    public native void close();

    /**
     * values[0] first track
     * values[1] last track
     *
     * anStartTrack[x] start sector of the track x.
     * type[x] type of track x.
     */
    public native int readTOC(int[] values,
                              int[] startFrame,
                              int[] length,
                              int[] type,
                              boolean[] copy,
                              boolean[] pre,
                              int[] channels);

    /**
     * Reads one or more raw frames from the CD.
     * This call reads <CODE>count</CODE> frames starting at
     * lba position <CODE>frame</CODE>.
     * <CODE>data</CODE>  has to be big enough to hold the
     * amount of data requested (<CODE>2352 * count</CODE> bytes).
     */
    public native int readFrame(int frame, int count, byte[] data);

    private static native void setTrace(boolean trace);
}
