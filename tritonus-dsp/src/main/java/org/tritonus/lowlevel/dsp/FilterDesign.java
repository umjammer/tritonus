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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


/**
 * Several methods to design digital filters. This is a design method for FIR
 * filters.
 */
public class FilterDesign {

    private static final Logger logger = getLogger(FilterDesign.class.getName());

    /**
     * Shared instance of {@link RectangularWindow}.
     */
    public static final FIRWindow RECTANGULAR_WINDOW = new RectangularWindow();
    /**
     * Shared instance of {@link HammingWindow}.
     */
    public static final FIRWindow HAMMING_WINDOW = new HammingWindow();

    public static FIRDirectFormFilterDescription getFirDirectFormFilterDescription(double[] coefficients) {
        float[] coefficientsF = new float[coefficients.length];
        for (int i = 0; i < coefficients.length; i++) {
            coefficientsF[i] = (float) coefficients[i];
        }
        return new FIRDirectFormFilterDescription(coefficientsF);
    }

    /**
     * Filter design by frequency sampling. This is a design method for FIR
     * filters. It allows to design filters with arbitrary frequency response.
     */
    public static double[] designFrequencySampling(double[] frequencyResponse) {
        int halfLength = frequencyResponse.length;
        int fullLength = halfLength * 2;
        Complex[] _frequencyResponse = new Complex[fullLength];
        // double scaleFactor = (double) (fullLength - 1) / (double) fullLength;
        for (int k = 0; k < halfLength; k++) {
            // double phase = -Math.PI * k * scaleFactor;
        }
        // TODO middle point has to be 0
        // TODO check loop bounds
        for (int k = halfLength; k < fullLength; k++) {
            // double phase = Math.PI - Math.PI * k * scaleFactor;
        }
        Complex[] complexCoefficients = Util.IDFT(_frequencyResponse);
        double[] realCoefficients = new double[fullLength];
        for (int i = 0; i < fullLength; i++) {
            realCoefficients[i] = complexCoefficients[i].real();
            logger.log(Level.DEBUG, "coefficient, imaginary part: " + complexCoefficients[i].imag());
        }
        return realCoefficients;
    }

    //
    // Rectangular Window methods
    //

    /**
     * order should be odd.
     */
    public static double[] designRectangularLowPass(int order, double cornerOmega) {
        double[] h = new double[order];
        int middle = order / 2;
        for (int n = 0; n < order; n++) {
            int k = (n - middle);
            if (k == 0) {
                h[n] = cornerOmega / Math.PI;
            } else {
                double a = cornerOmega * k;
                double sin = Math.sin(a);
                h[n] = sin / (Math.PI * k);
            }
        }
        return h;
    }

    /**
     * order should be odd.
     */
    public static double[] designRectangularHighPass(int order, double cornerOmega) {
        double[] h = new double[order];
        int middle = order / 2;
        for (int n = 0; n < order; n++) {
            h[n] = 1.0 - Math.sin(cornerOmega * (n - middle)) / (Math.PI * (n - middle));
        }
        return h;
    }

    /**
     * order should be odd. o1 < o2 required
     */
    public static double[] designRectangularBandPass(int order, double cornerOmega1, double cornerOmega2) {
        double[] h = new double[order];
        int middle = order / 2;
        for (int n = 0; n < order; n++) {
            h[n] = (Math.sin(cornerOmega2 * (n - middle)) -
                    Math.sin(cornerOmega1 * (n - middle))) / (Math.PI * (n - middle));
        }
        return h;
    }

    /**
     * order should be odd.
     */
    public static double[] designRectangularBandStop(int order, double cornerOmega1, double cornerOmega2) {
        double[] h = new double[order];
        int middle = order / 2;
        for (int n = 0; n < order; n++) {
            h[n] = 1.0 - (Math.sin(cornerOmega2 * (n - middle)) -
                    Math.sin(cornerOmega1 * (n - middle))) / (Math.PI * (n - middle));
        }
        return h;
    }

    //
    // Window methods
    //

    public static double[] designWindowLowPass(int order, double cornerOmega, FIRWindow window) {
        double[] rectangular = designRectangularLowPass(order, cornerOmega);
        return applyWindow(rectangular, window);
    }

    public static double[] designWindowHighPass(int order, double cornerOmega, FIRWindow window) {
        double[] rectangular = designRectangularHighPass(order, cornerOmega);
        return applyWindow(rectangular, window);
    }

    public static double[] designWindowBandPass(int order, double cornerOmega1, double cornerOmega2, FIRWindow window) {
        double[] rectangular = designRectangularBandPass(order, cornerOmega1, cornerOmega2);
        return applyWindow(rectangular, window);
    }

    public static double[] designWindowBandStop(int order, double cornerOmega1, double cornerOmega2, FIRWindow window) {
        double[] rectangular = designRectangularBandStop(order, cornerOmega1, cornerOmega2);
        return applyWindow(rectangular, window);
    }

    private static double[] applyWindow(double[] rectangular, FIRWindow window) {
        double[] windows = window.getWindow(rectangular.length);
        double[] h = Util.multiply(rectangular, windows);
        return h;
    }
}
