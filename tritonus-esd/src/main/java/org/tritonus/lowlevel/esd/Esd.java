/*
 *  Copyright (c) 1999 - 2002 by Matthias Pfisterer
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

package org.tritonus.lowlevel.esd;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


public class Esd {

    private static final Logger logger = getLogger("org.tritonus.TraceEsdNative");

    public static final int ESD_STREAM = 0x0000;
    public static final int ESD_PLAY = 0x1000;
    public static final int ESD_BITS8 = 0x0000;
    public static final int ESD_BITS16 = 0x0001;
    public static final int ESD_MONO = 0x0010;
    public static final int ESD_STEREO = 0x0020;

    private static boolean isLibraryAvailable = false;

    static {
        Esd.loadNativeLibrary();
    }

    public static void loadNativeLibrary() {
        logger.log(Level.TRACE, "loading native library tritonusesd");

        try {
            System.loadLibrary("tritonusesd");
            isLibraryAvailable = true;
        } catch (Throwable t) {
            logger.log(Level.ERROR, t.getMessage(), t);
        }

        logger.log(Level.TRACE, "loaded");
    }

    /**
     * Returns whether the libraries are installed correctly.
     */
    public static boolean isLibraryAvailable() {
        return isLibraryAvailable;
    }
}
