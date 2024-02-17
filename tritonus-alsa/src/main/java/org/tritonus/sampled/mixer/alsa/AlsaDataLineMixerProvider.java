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

    private static boolean sm_bInitialized = false;

    public AlsaDataLineMixerProvider() {
        super();
        logger.log(Level.TRACE, "AlsaDataLineMixerProvider.<init>(): begin");

        if (!sm_bInitialized && !isDisabled()) {
            if (!Alsa.isLibraryAvailable()) {
                disable();
            } else {
                staticInit();
                sm_bInitialized = true;
            }
        } else {
            logger.log(Level.TRACE, "AlsaDataLineMixerProvider.<init>(): already initialized or disabled");
        }

        logger.log(Level.TRACE, "AlsaDataLineMixerProvider.<init>(): end");
    }

    @Override
    protected void staticInit() {
        logger.log(Level.TRACE, "AlsaDataLineMixerProvider.staticInit(): begin");

        int[] anCards = AlsaCtl.getCards();
        logger.log(Level.TRACE, "AlsaDataLineMixerProvider.staticInit(): num cards: " + anCards.length);

        for (int i = 0; i < anCards.length; i++) {
            logger.log(Level.TRACE, "AlsaDataLineMixerProvider.staticInit():card #" + i + ": " + anCards[i]);

            logger.log(Level.TRACE, "AlsaDataLineMixerProvider.staticInit(): creating Ctl object...");

            String strPcmName = "hw:" + anCards[i];
//            String strPcmName = AlsaDataLineMixer.getPcmName(anCards[i]);
            AlsaCtl ctl;
            try {
                ctl = new AlsaCtl(strPcmName, 0);
            } catch (Exception e) {
                logger.log(Level.TRACE, e);
                continue;
            }
            logger.log(Level.TRACE, "AlsaDataLineMixerProvider.staticInit(): calling getCardInfo()...");

            AlsaCtlCardInfo cardInfo = new AlsaCtlCardInfo();
            ctl.getCardInfo(cardInfo);
            logger.log(Level.TRACE, "AlsaDataLineMixerProvider.staticInit(): ALSA sound card:");
            logger.log(Level.TRACE, "AlsaDataLineMixerProvider.staticInit(): card: " + cardInfo.getCard());
            logger.log(Level.TRACE, "AlsaDataLineMixerProvider.staticInit(): id: " + cardInfo.getId());
            int[] anDevices = ctl.getPcmDevices();
            logger.log(Level.TRACE, "AlsaDataLineMixerProvider.staticInit(): num devices: " + anDevices.length);

            // TODO combine devices into one AlsaDataLineMixer?
            // pass device number to AlsaDataLineMixer constructor?
            for (int nDevice = 0; nDevice < anDevices.length; nDevice++) {
                logger.log(Level.TRACE, "AlsaDataLineMixerProvider.staticInit(): device #" + nDevice + ": " + anDevices[nDevice]);
            }
//            ctl.close();

            // We do not use strPcmName because the mixer may choose to open as 'plughw',
            // while for ctl, the device name always has to be 'hw'.
            AlsaDataLineMixer mixer = new AlsaDataLineMixer(anCards[i]);
            super.addMixer(mixer);
        }

        logger.log(Level.TRACE, "AlsaDataLineMixerProvider.staticInit(): end");
    }
}
