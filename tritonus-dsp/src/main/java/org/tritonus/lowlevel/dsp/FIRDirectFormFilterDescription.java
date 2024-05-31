/*
 * This file is part of Tritonus: http://www.tritonus.org/
 */
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

/**
 * Description of a direct form Finite Impulse Response (FIR) filter.
 */
public class FIRDirectFormFilterDescription implements FilterDescription {

    /**
     * The filter coefficients.
     */
    private final float[] coefficients;

    /**
     * Constructor with filter coefficients.
     *
     * @param coefficients The array of filter coefficients
     */
    public FIRDirectFormFilterDescription(float[] coefficients) {
        this.coefficients = new float[coefficients.length];
        System.arraycopy(coefficients, 0, this.coefficients, 0, coefficients.length);
    }

    public float[] getCoefficients() {
        return coefficients;
    }

    /**
     * Returns the length of the filter. This returns the length of the filter
     * (the number of coefficients). Note that this is not the same as the order
     * of the filter. Commonly, the 'order' of a FIR filter is said to be the
     * number of coefficients minus 1: Since a single coefficient is only an
     * amplifier/attenuator, this is considered order zero.
     *
     * @return The length of the filter (the number of coefficients).
     */
    private int getLength() {
        return coefficients.length;
    }

    @Override
    public double getFrequencyResponse(double omega) {
        double real = 0.0;
        double imag = 0.0;
        for (int i = 0; i < getLength(); i++) {
            real += coefficients[i] * Math.cos(i * omega);
            imag += coefficients[i] * Math.sin(i * omega);
        }
        double result = Math.sqrt(real * real + imag * imag);
        return result;
    }

    @Override
    public double getPhaseResponse(double omega) {
        double real = 0.0;
        double imag = 0.0;
        for (int i = 0; i < getLength(); i++) {
            real += coefficients[i] * Math.cos(i * omega);
            imag += coefficients[i] * Math.sin(i * omega);
        }
        double result = Math.atan2(imag, real);
        return result;
    }
}
