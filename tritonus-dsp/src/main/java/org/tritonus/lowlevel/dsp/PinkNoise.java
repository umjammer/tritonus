/*
 *  Copyright (c) 2002 by Matthias Pfisterer
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

package org.tritonus.lowlevel.dsp;

import java.util.Random;


/**
 * for 44.1 kHz only !!!!
 * <pre>
 * [0.05 dB ripple:]
 *
 * b0 = 0.99886 * b0 + white * 0.0555179;
 * b1 = 0.99332 * b1 + white * 0.0750759;
 * b2 = 0.96900 * b2 + white * 0.1538520;
 * b3 = 0.86650 * b3 + white * 0.3104856;
 * b4 = 0.55000 * b4 + white * 0.5329522;
 * b5 = -0.7616 * b5 - white * 0.0168980;
 * pink = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362;
 * b6 = white * 0.115926;
 *
 * An 'economy' version with accuracy of +/-0.5dB is also available.
 *
 * b0 = 0.99765 * b0 + white * 0.0990460;
 * b1 = 0.96300 * b1 + white * 0.2965164;
 * b2 = 0.57000 * b2 + white * 1.0526913;
 * pink = b0 + b1 + b2 + white * 0.1848;
 * </pre>
 *
 * @author paul.kellett@maxim.abel.co.uk http://www.abel.co.uk/~maxim/
 */
public class PinkNoise implements Source {

    private final Source whiteNoiseSource;
    private float b0, b1, b2, b3, b4, b5, b6;

    public PinkNoise(float sampleRate) {
        this(sampleRate, new WhiteNoise());
    }

    public PinkNoise(float sampleRate, Random random) {
        this(sampleRate, new WhiteNoise(random));
    }

    private PinkNoise(float sampleRate, Source whiteNoiseSource) {
        // TODO scale filter for sample rate
        this.whiteNoiseSource = whiteNoiseSource;
    }

    @Override
    public float process() {
        float white = whiteNoiseSource.process();

        b0 = 0.99886f * b0 + white * 0.0555179f;
        b1 = 0.99332f * b1 + white * 0.0750759f;
        b2 = 0.96900f * b2 + white * 0.1538520f;
        b3 = 0.86650f * b3 + white * 0.3104856f;
        b4 = 0.55000f * b4 + white * 0.5329522f;
        b5 = -0.7616f * b5 - white * 0.0168980f;
        float pink = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362f;
        b6 = white * 0.115926f;
        return pink;
    }
}
