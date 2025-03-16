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

package org.tritonus.sampled.mixer.esd;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.tritonus.lowlevel.esd.Esd;
import org.tritonus.share.sampled.mixer.TMixerProvider;

import static java.lang.System.getLogger;


public class EsdMixerProvider extends TMixerProvider {

    private static final Logger logger = getLogger("org.tritonus.TraceMixerProvider");

    private static boolean initialized = false;

    public EsdMixerProvider() {
        super();
        logger.log(Level.TRACE, "begin");

        if (!initialized && !isDisabled()) {
            /// TODO adapt!
            if (!Esd.isLibraryAvailable()) {
                disable();
            } else {
                staticInit();
                initialized = true;
            }
        } else {
            logger.log(Level.TRACE, "already initialized or disabled");

        }

        logger.log(Level.TRACE, "end");
    }

    @Override
    protected void staticInit() {
        logger.log(Level.TRACE, "begin");

        addMixer(new EsdMixer());

        logger.log(Level.TRACE, "end");
    }
}
