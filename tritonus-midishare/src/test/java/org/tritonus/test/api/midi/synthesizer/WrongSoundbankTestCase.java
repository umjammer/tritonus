/*
 *  Copyright (c) 2006 by Matthias Pfisterer
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

package org.tritonus.test.api.midi.synthesizer;

import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;
import javax.sound.midi.Synthesizer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Test for javax.sound.midi.Synthesizer.getLatency().
 */
public class WrongSoundbankTestCase extends BaseSynthesizerTestCase {

    @Override
    protected void checkSynthesizer(Synthesizer synth) throws Exception {
        WrongSoundbank sb = new WrongSoundbank();
        Instrument instr = sb.new WrongInstrument();
        Patch[] patchList = new Patch[1];
        patchList[0] = new Patch(0, 0);

        synth.open();
        try (synth) {
            boolean open = true;
            assertFalse(synth.isSoundbankSupported(sb),
                    errmsg(synth, "isSoundbankSupported() result wrong", true));

            assertThrows(IllegalArgumentException.class, () -> synth.loadInstrument(instr),
                    errmsg(synth, "loadInstrument()", open));

            assertThrows(IllegalArgumentException.class, () -> synth.unloadInstrument(instr),
                    errmsg(synth, "unloadInstrument()", open));

            assertThrows(IllegalArgumentException.class, () -> synth.remapInstrument(instr, instr),
                    errmsg(synth, "remapInstrument()", open));

            assertThrows(IllegalArgumentException.class, () -> synth.loadAllInstruments(sb),
                    errmsg(synth, "loadAllInstruments()", open));

            assertThrows(IllegalArgumentException.class, () -> synth.unloadAllInstruments(sb),
                    errmsg(synth, "unloadAllInstruments()", open));

            assertThrows(IllegalArgumentException.class, () -> synth.loadInstruments(sb, patchList),
                    errmsg(synth, "loadInstruments()", open));

            assertThrows(IllegalArgumentException.class, () -> synth.unloadInstruments(sb, patchList),
                    errmsg(synth, "unloadInstruments()", open));
        }
    }

    protected static String errmsg(Synthesizer synth, String methodName, boolean open) {
        String message = ": " + "IllegalArgumentException not thrown";
        message += " on " + methodName;
        return BaseSynthesizerTestCase.errmsg(synth, message, open);
    }

    private static class WrongSoundbank implements Soundbank {

        public class WrongInstrument extends Instrument {

            public WrongInstrument() {
                super(WrongSoundbank.this, null, null, null);
            }

            @Override
            public Object getData() {
                return null;
            }
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public Instrument getInstrument(Patch patch) {
            return new WrongInstrument();
        }

        @Override
        public Instrument[] getInstruments() {
            Instrument[] instruments = new Instrument[1];
            instruments[0] = new WrongInstrument();
            return instruments;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public SoundbankResource[] getResources() {
            return null;
        }

        @Override
        public String getVendor() {
            return null;
        }

        @Override
        public String getVersion() {
            return null;
        }
    }
}
