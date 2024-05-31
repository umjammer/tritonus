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

import org.junit.jupiter.api.Assertions;


/**
 * Tests for class javax.sound.midi.Synthesizer.
 */
public class IllegalStateTestCase extends BaseSynthesizerTestCase {

    @Override
    protected void checkSynthesizer(Synthesizer synth) throws Exception {
        // Synthesizer is closed
        checkMethods(synth, false);

        // Synthesizer open
        synth.open();

        checkMethods(synth, true);

        // clean up
        synth.close();
    }

    private static void checkMethods(Synthesizer synth, boolean open) throws Exception {
        boolean expectingException = false;
        checkMethod(synth, "getMaxPolyphony()", expectingException, open);
        checkMethod(synth, "getLatency()", expectingException, open);
        checkMethod(synth, "getChannels()", expectingException, open);
        checkMethod(synth, "getVoiceStatus()", expectingException, open);
        checkMethod(synth, "getDefaultSoundbank()", expectingException, open);
        checkMethod(synth, "getAvailableInstruments()", expectingException, open);
        checkMethod(synth, "getLoadedInstruments()", expectingException, open);
    }

    private static void checkMethod(
            Synthesizer synth, String methodName, boolean exceptionExpected, boolean open) throws Exception {
        try {
            if ("getMaxPolyphony()".equals(methodName))
                synth.getMaxPolyphony();
            else if ("getLatency()".equals(methodName))
                synth.getLatency();
            else if ("getChannels()".equals(methodName))
                synth.getChannels();
            else if ("getVoiceStatus()".equals(methodName))
                synth.getVoiceStatus();
            else if ("getDefaultSoundbank()".equals(methodName))
                synth.getDefaultSoundbank();
            else if ("getAvailableInstruments()".equals(methodName))
                synth.getAvailableInstruments();
            else if ("getLoadedInstruments()".equals(methodName))
                synth.getLoadedInstruments();
            else
                throw new RuntimeException("unknown method name");
            if (exceptionExpected) {
                Assertions.fail(constructErrorMessage(synth, methodName, exceptionExpected, open));
            }
        } catch (IllegalStateException e) {
            if (!exceptionExpected) {
                Assertions.fail(constructErrorMessage(synth, methodName, exceptionExpected, open));
            }
        }
    }

    private static String constructErrorMessage(Synthesizer synth,
                                                String methodName,
                                                boolean exceptionExpected,
                                                boolean open) {
        String message = ": IllegalStateException ";
        message += (exceptionExpected ? "not thrown" : "thrown");
        message += " on " + methodName;
        return BaseSynthesizerTestCase.errmsg(synth, message, open);
    }
}
