/*
 *  Copyright (c) 1999 - 2002 by Matthias Pfisterer
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

package org.tritonus.sampled.mixer.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.tritonus.lowlevel.alsa.Alsa;
import org.tritonus.lowlevel.alsa.AlsaCtl;
import org.tritonus.lowlevel.alsa.AlsaCtlCardInfo;
import org.tritonus.share.sampled.mixer.TMixerProvider;

import static java.lang.System.getLogger;


public class AlsaDataLineMixerProvider extends TMixerProvider {

    private static final Logger logger = getLogger("org.tritonus.TraceMixerProvider");

    private static boolean initialized = false;

    public AlsaDataLineMixerProvider() {
        super();
        logger.log(Level.TRACE, "begin");

        if (!initialized && !isDisabled()) {
            if (!Alsa.isLibraryAvailable()) {
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

        int[] cards = AlsaCtl.getCards();
        logger.log(Level.TRACE, "num cards: " + cards.length);

        for (int i = 0; i < cards.length; i++) {
            logger.log(Level.TRACE, "AlsaDataLineMixerProvider.staticInit():card #" + i + ": " + cards[i]);

            logger.log(Level.TRACE, "creating Ctl object...");

            String pcmName = "hw:" + cards[i];
//            String pcmName = AlsaDataLineMixer.getPcmName(cards[i]);
            AlsaCtl ctl;
            try {
                ctl = new AlsaCtl(pcmName, 0);
            } catch (Exception e) {
                logger.log(Level.TRACE, e);
                continue;
            }
            logger.log(Level.TRACE, "calling getCardInfo()...");

            AlsaCtlCardInfo cardInfo = new AlsaCtlCardInfo();
            ctl.getCardInfo(cardInfo);
            logger.log(Level.TRACE, "ALSA sound card:");
            logger.log(Level.TRACE, "card: " + cardInfo.getCard());
            logger.log(Level.TRACE, "id: " + cardInfo.getId());
            int[] devices = ctl.getPcmDevices();
            logger.log(Level.TRACE, "num devices: " + devices.length);

            // TODO combine devices into one AlsaDataLineMixer?
            // pass device number to AlsaDataLineMixer constructor?
            for (int device = 0; device < devices.length; device++) {
                logger.log(Level.TRACE, "device #" + device + ": " + devices[device]);
            }
//            ctl.close();

            // We do not use pcmName because the mixer may choose to open as 'plughw',
            // while for ctl, the device name always has to be 'hw'.
            AlsaDataLineMixer mixer = new AlsaDataLineMixer(cards[i]);
            super.addMixer(mixer);
        }

        logger.log(Level.TRACE, "end");
    }
}
