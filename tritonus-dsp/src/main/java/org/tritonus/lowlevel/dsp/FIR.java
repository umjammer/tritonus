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
 * A Finite Impulse Response (FIR) filter.
 */
public class FIR implements Filter {

    /**
     * The length of the filter (number of coefficients).
     */
    private int length;

    /**
     * The filter coefficients.
     */
    private float[] coefficients;

    /**
     * The buffer for past input values. This stores the input values needed for
     * convolution. The buffer is used as a circular buffer.
     */
    private float[] buffer;

    /**
     * The index into buffer. Since buffer is used as a circular buffer,
     * a buffer pointer is needed.
     */
    private int bufferIndex;

    /**
     * Constructor with filter coefficients.
     *
     * @param filterDescription filter description containing the new coefficients
     */
    public FIR(FIRDirectFormFilterDescription filterDescription) {
        float[] coefficients = filterDescription.getCoefficients();
        length = coefficients.length;
        this.coefficients = new float[length];
        System.arraycopy(coefficients, 0, this.coefficients, 0, length);
        buffer = new float[length];
        bufferIndex = 0;
    }

    /**
     * Change filter coefficients on the fly.
     *
     * <p>
     * Note that this method does not allow to change the order of the filter
     * (by passing a different number of filter coefficients) from the value set
     * in the constructor.
     * </p>
     *
     * @param filterDescription filter description containing the new coefficients
     * @throws IllegalArgumentException if the number of coefficients is different from the current
     *                                  number of coefficients
     */
    public void setFilterDescription(FIRDirectFormFilterDescription filterDescription) {
        float[] coefficients = filterDescription.getCoefficients();
        if (coefficients.length != length) {
            throw new IllegalArgumentException("cannot change length of filter");
        }
        System.arraycopy(coefficients, 0, this.coefficients, 0, length);
    }

    @Override
    public float process(float sample) {
        buffer[bufferIndex] = sample;
        int bi = bufferIndex;
        float output = 0.0F;
        for (int i = 0; i < length; i++) {
            output += coefficients[i] * buffer[bi];
            bi--;
            if (bi < 0) {
                bi += length;
            }
        }
        bufferIndex = (bufferIndex + 1) % length;
        return output;
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
    public int getLength() {
        return length;
    }
}
