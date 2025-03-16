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

import javax.sound.midi.Synthesizer;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test for javax.sound.midi.Synthesizer.getLatency().
 */
public class GetLatencyTestCase extends BaseSynthesizerTestCase {

    @Override
    protected void checkSynthesizer(Synthesizer synth) throws Exception {
        synth.open();
        try (synth) {
            long latency = synth.getLatency();
            assertTrue(latency >= 0, constructErrorMessage(synth, true));
        }
    }

    private static String constructErrorMessage(Synthesizer synth, boolean open) {
        return errmsg(synth, "getLatency() result not positive", open);
    }
}
