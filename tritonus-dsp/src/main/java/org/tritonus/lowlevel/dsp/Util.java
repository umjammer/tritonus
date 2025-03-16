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
 * Helper methods used for filter design.
 */
public class Util {

    /**
     * Modified Bessel function of first kind and 0th order.
     */
    public static double I0(double x) {
        double eps = 10E-9;
        int n = 1;
        double S = 1.0;
        double D = 1.0;
        while (D > eps * S) {
            double T = x / (2.0 * n);
            n++;
            D *= (T * T);
            S += D;
        }
        return S;
    }

    /**
     * Compute an inverse discrete fourier transform (IDFT).
     * This implementation works fully complex. It is not
     * optimized for speed. I.e., it does not use a 'fast'
     * algorithm.
     *
     * @param frequencyDomain The array containing the frequency
     *                         domain factors.
     * @return The reconstructed time domain values (returned as
     * complex numbers for full generality).
     */
    public static Complex[] IDFT(Complex[] frequencyDomain) {
        int N = frequencyDomain.length;
        Complex[] timeDomain = new Complex[N];
        double oneOverN = 1.0 / N;
        for (int n = 0; n < N; n++) {
            timeDomain[n] = new Complex(0.0, 0.0);
            for (int k = 0; k < N; k++) {
                Complex exponent = new Complex(0.0, 2.0 * Math.PI * k * n * oneOverN);
                Complex term = Complex.times(frequencyDomain[k], Complex.exp(exponent));
                timeDomain[n] = Complex.plus(timeDomain[n], term);
            }
            timeDomain[n] = Complex.times(timeDomain[n], oneOverN);
        }
        return timeDomain;
    }

    /**
     * Multiplication of two arrays.
     */
    public static double[] multiply(double[] ad1, double[] ad2) {
        int length = Math.min(ad1.length, ad2.length);
        double[] result = new double[length];
        for (int i = 0; i < length; i++) {
            result[i] = ad1[i] * ad2[i];
        }
        return result;
    }

    /**
     * Converts frequency representation from omega to relative.
     * This method converts a frequency represented in
     * omega ([-PI .. +PI]) to relative (f/fs).
     *
     * @param omega The frequency represented in omega
     *               ([-PI .. +PI]).
     * @return The frequency represented relative to the sample rate
     * (f/fs).
     */
    public static double omega2relative(double omega) {
        double relative = omega / (2.0 * Math.PI);
        return relative;
    }

    /**
     * Converts frequency representation from relative to omega.
     * This method converts a frequency represented relative to
     * the sample rate (f/fs) to omega ([-PI .. +PI]).
     *
     * @param relative The frequency represented relative to the
     *                  sample rate (f/fs).
     * @return The frequency represented in omega
     * ([-PI .. +PI]).
     */
    public static double relative2omega(double relative) {
        double omega = relative * 2.0 * Math.PI;
        return omega;
    }

    /**
     * Converts frequency representation from omega to absolute.
     * This method converts a frequency represented in
     * omega ([-PI .. +PI]) to absolute frequency (f).
     *
     * @param omega      The frequency represented in omega
     *                    ([-PI .. +PI]).
     * @param sampleRate The sample rate (fs).
     * @return The absolute frequency represented in Hz (f).
     */
    public static double omega2absolute(double omega, double sampleRate) {
        double absolute = omega2relative(omega) * sampleRate;
        return absolute;
    }

    /**
     * Converts frequency representation from absolute to omega.
     * This method converts a frequency represented relative to
     * the sample rate (f/fs) to omega ([-PI .. +PI]).
     *
     * @param absolute   The absolute frequency expressed in Hz (f).
     * @param sampleRate The sample rate (fs).
     * @return The frequency represented in omega
     * ([-PI .. +PI]).
     */
    public static double absolute2omega(double absolute, double sampleRate) {
        double omega = relative2omega(absolute / sampleRate);
        return omega;
    }

    /**
     * Quantize constants from double to float.
     */
    public static float[] quantizeToFloat(double[] constants) {
        float[] constantsF = new float[constants.length];
        for (int i = 0; i < constants.length; i++) {
            constantsF[i] = (float) constants[i];
        }
        return constantsF;
    }
}
