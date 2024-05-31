/*
 *  Copyright (c) 1999 by Matthias Pfisterer
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

package org.tritonus.midi.device.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.spi.MidiDeviceProvider;

import org.tritonus.share.GlobalInfo;
import org.tritonus.share.midi.TMidiDevice;

import static java.lang.System.getLogger;


public class AlsaSequencerProvider extends MidiDeviceProvider {

    private static final Logger logger = getLogger("org.tritonus.TraceMidiDeviceProvider");

    private static MidiDevice.Info info;

    public AlsaSequencerProvider() {
        logger.log(Level.TRACE, "begin");

        synchronized (AlsaSequencerProvider.class) {
            if (info == null) {
                info = new TMidiDevice.Info(
                        "Tritonus ALSA Sequencer",
                        GlobalInfo.getVendor(),
                        "this sequencer uses the ALSA sequencer",
                        GlobalInfo.getVersion());
            }
        }

        logger.log(Level.TRACE, "end");
    }

    @Override
    public MidiDevice.Info[] getDeviceInfo() {
        logger.log(Level.TRACE, "begin");

        MidiDevice.Info[] infos = new MidiDevice.Info[1];
        infos[0] = info;

        logger.log(Level.TRACE, "end");

        return infos;
    }

    @Override
    public MidiDevice getDevice(MidiDevice.Info info) {
        logger.log(Level.TRACE, "begin");

        MidiDevice device = null;
        if (info != null && info.equals(AlsaSequencerProvider.info)) {
            device = new AlsaSequencer(AlsaSequencerProvider.info);
        }
        if (device == null) {
            throw new IllegalArgumentException("no device for " + info);
        }

        logger.log(Level.TRACE, "end");

        return device;
    }
}
