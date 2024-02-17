
/*
 *  Copyright (c) 1999 - 2006 by Matthias Pfisterer
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

package org.tritonus.midi.device.fluidsynth;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.spi.MidiDeviceProvider;

import org.tritonus.share.GlobalInfo;
import org.tritonus.share.midi.TMidiDevice;

import static java.lang.System.getLogger;


public class FluidSynthesizerProvider extends MidiDeviceProvider {

    private static final Logger logger = getLogger("org.tritonus.TraceMidiDeviceProvider");

    private static MidiDevice.Info sm_info;

    public FluidSynthesizerProvider() {
        logger.log(Level.TRACE, "FluidSynthesizerProvider.<init>(): begin");

        synchronized (FluidSynthesizerProvider.class) {
            if (sm_info == null) {
                sm_info = new TMidiDevice.Info(
                        "Tritonus fluidsynth Synthesizer",
                        GlobalInfo.getVendor(),
                        "a synthesizer based on fluidsynth",
                        GlobalInfo.getVersion());
            }
        }

        logger.log(Level.TRACE, "FluidSynthesizerProvider.<init>(): end");
    }

    @Override
    public MidiDevice.Info[] getDeviceInfo() {
        logger.log(Level.TRACE, "FluidSynthesizerProvider.getDeviceInfo(): begin");

        MidiDevice.Info[] infos = new MidiDevice.Info[1];
        infos[0] = sm_info;

        logger.log(Level.TRACE, "FluidSynthesizerProvider.getDeviceInfo(): end");

        return infos;
    }

    @Override
    public MidiDevice getDevice(MidiDevice.Info info) {
        logger.log(Level.TRACE, "FluidSynthesizerProvider.getDevice(): begin");

        MidiDevice device;
        if (info != null && info.equals(sm_info)) {
            try {
                device = new FluidSynthesizer(sm_info);
            } catch (Exception e) {
                throw new IllegalArgumentException("unable to create device for " + info, e);
            }
        } else {
            throw new IllegalArgumentException("no device for " + info);
        }

        logger.log(Level.TRACE, "FluidSynthesizerProvider.getDevice(): end");

        return device;
    }
}
