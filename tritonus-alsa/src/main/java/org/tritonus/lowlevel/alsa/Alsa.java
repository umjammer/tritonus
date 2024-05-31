/*
 *  Copyright (c) 2000 - 2001 by Matthias Pfisterer
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

package org.tritonus.lowlevel.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


/**
 * Common ALSA functions.
 * Used only for the functions that do neither belong to the
 * ctl sections nor to any specific interface section
 * (like pcm, rawmidi, etc.).
 * Currently, there is only one function remaining.
 */
public class Alsa {

    private static final Logger logger = getLogger("org.tritonus.TraceAlsaNative");

    private static boolean isLibraryAvailable = false;

    static {
        Alsa.loadNativeLibrary();
    }

    public static void loadNativeLibrary() {
        logger.log(Level.TRACE, "begin");

        if (!isLibraryAvailable()) {
            loadNativeLibraryImpl();
        }

        logger.log(Level.TRACE, "end");
    }

    /**
     * Load the native library for alsa.
     * <p>
     * This method actually does the loading of the library.  Unlike
     * {@link #loadNativeLibrary() loadNativeLibrary()}, it does not
     * check if the library is already loaded.
     */
    private static void loadNativeLibraryImpl() {
        logger.log(Level.TRACE, "loading native library tritonusalsa");

        try {
            System.loadLibrary("tritonusalsa");
            // only reached if no exception occures
            isLibraryAvailable = true;
        } catch (Error e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }

        logger.log(Level.TRACE, "loaded");
    }

    /**
     * Returns whether the libraries are installed correctly.
     */
    public static boolean isLibraryAvailable() {
        return isLibraryAvailable;
    }

    public static native String getStringError(int errNum);
}
