/*
 * Lpc port to Java.
 * Copyright (C) 1999  Christopher Edwards
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package org.tritonus.lowlevel.gsm;

import static org.tritonus.lowlevel.gsm.GsmDef.MIN_WORD;


public class Lpc {

    private final int[] L_ACF = new int[9];

    /**
     * @param so 0..159 signals IN/OUT
     * @param LARc 0..7 LARc's OUT
     */
    public void analyzeLPC(short[] so, short[] LARc) {
        correlateAuto(so);
        reflectCoefficients(LARc);
        transformeToLogAreaRatios(LARc);
        quantizeAndCode(LARc);
    }

    /**
     * @param so [0..159] IN/OUT
     */
    private void correlateAuto(short[] so) throws IllegalArgumentException {
        int i, sp_index = 0;
        short smax = 0, scalauto;

        // Dynamic scaling of the array s[0..159]

        // Search for the maximum.
        for (int k = 0; k <= 159; k++) {
            short temp = GsmMath.add(so[k], (short) 0);
            if (temp > smax)
                smax = temp;
        }

        // Computation of the scaling factor.
        if (smax == 0) {
            scalauto = 0;
        } else {
            if (!(smax > 0)) {
                throw new IllegalArgumentException("smax = " + smax + " should be > 0.");
            }
            scalauto = (short) (4 - GsmMath.norm(smax << 16)); // sub(4, ..)
        }

        // Scaling of the array s[0...159]

        if (scalauto > 0) {
            if (!(scalauto <= 4)) {
                throw new IllegalArgumentException("scalauto = " + scalauto + " should be <= 4.");
            }
            switch (scalauto) {
            case 1:
                for (int k = 0; k <= 159; k++) {
                    so[k] = GsmMath.multR(so[k], (short) 16384);
                }
                break;

            case 2:
                for (int k = 0; k <= 159; k++) {
                    so[k] = GsmMath.multR(so[k], (short) (16384 >> 1));
                }
                break;

            case 3:
                for (int k = 0; k <= 159; k++) {
                    so[k] = GsmMath.multR(so[k], (short) (16384 >> 2));
                }
                break;

            case 4:
                for (int k = 0; k <= 159; k++) {
                    so[k] = GsmMath.multR(so[k], (short) (16384 >> 3));
                }
                break;
            }
        }

        // Compute the L_ACF[..].

        short[] sp = so;
        short sl = sp[sp_index];

        // Zero out L_ACF
        int[] temp = {0, 0, 0, 0, 0, 0, 0, 0, 0};
        System.arraycopy(temp, 0, L_ACF, 0, L_ACF.length);

        L_ACF[0] += sl * sp[(sp_index - 0)];

        sl = sp[++sp_index];
        L_ACF[0] += sl * sp[(sp_index - 0)];
        L_ACF[1] += sl * sp[(sp_index - 1)];

        sl = sp[++sp_index];
        L_ACF[0] += sl * sp[(sp_index - 0)];
        L_ACF[1] += sl * sp[(sp_index - 1)];
        L_ACF[2] += sl * sp[(sp_index - 2)];

        sl = sp[++sp_index];
        L_ACF[0] += sl * sp[(sp_index - 0)];
        L_ACF[1] += sl * sp[(sp_index - 1)];
        L_ACF[2] += sl * sp[(sp_index - 2)];
        L_ACF[3] += sl * sp[(sp_index - 3)];

        sl = sp[++sp_index];
        L_ACF[0] += sl * sp[(sp_index - 0)];
        L_ACF[1] += sl * sp[(sp_index - 1)];
        L_ACF[2] += sl * sp[(sp_index - 2)];
        L_ACF[3] += sl * sp[(sp_index - 3)];
        L_ACF[4] += sl * sp[(sp_index - 4)];

        sl = sp[++sp_index];
        L_ACF[0] += sl * sp[(sp_index - 0)];
        L_ACF[1] += sl * sp[(sp_index - 1)];
        L_ACF[2] += sl * sp[(sp_index - 2)];
        L_ACF[3] += sl * sp[(sp_index - 3)];
        L_ACF[4] += sl * sp[(sp_index - 4)];
        L_ACF[5] += sl * sp[(sp_index - 5)];

        sl = sp[++sp_index];
        L_ACF[0] += sl * sp[(sp_index - 0)];
        L_ACF[1] += sl * sp[(sp_index - 1)];
        L_ACF[2] += sl * sp[(sp_index - 2)];
        L_ACF[3] += sl * sp[(sp_index - 3)];
        L_ACF[4] += sl * sp[(sp_index - 4)];
        L_ACF[5] += sl * sp[(sp_index - 5)];
        L_ACF[6] += sl * sp[(sp_index - 6)];

        sl = sp[++sp_index];
        L_ACF[0] += sl * sp[(sp_index - 0)];
        L_ACF[1] += sl * sp[(sp_index - 1)];
        L_ACF[2] += sl * sp[(sp_index - 2)];
        L_ACF[3] += sl * sp[(sp_index - 3)];
        L_ACF[4] += sl * sp[(sp_index - 4)];
        L_ACF[5] += sl * sp[(sp_index - 5)];
        L_ACF[6] += sl * sp[(sp_index - 6)];
        L_ACF[7] += sl * sp[(sp_index - 7)];

        sl = sp[++sp_index];

        for (i = sp_index; i < 160; ++i) {

            sl = sp[i];

            L_ACF[0] += sl * sp[(i - 0)];
            L_ACF[1] += sl * sp[(i - 1)];
            L_ACF[2] += sl * sp[(i - 2)];
            L_ACF[3] += sl * sp[(i - 3)];
            L_ACF[4] += sl * sp[(i - 4)];
            L_ACF[5] += sl * sp[(i - 5)];
            L_ACF[6] += sl * sp[(i - 6)];
            L_ACF[7] += sl * sp[(i - 7)];
            L_ACF[8] += sl * sp[(i - 8)];
        }

        for (int k = 0; k < 9; k++) {
            L_ACF[k] <<= 1;
        }

        // Rescaling of the array s[0..159]
        if (scalauto > 0) {
            if (!(scalauto <= 4)) {
                throw new IllegalArgumentException("scalauto = " + scalauto + " should be <= 4.");
            }

            for (int k = 0; k < 160; k++) {
                so[k] = (short) (so[k] << scalauto);
            }
        }
    }

    /** @param r OUT 0...7 */
    private void reflectCoefficients(short[] r) throws IllegalArgumentException {
        short temp;
        int rIndex = 0;

        short[] ACF = new short[9]; // 0..8
        short[] P = new short[9]; // 0..8
        short[] K = new short[9]; // 2..8

        // Schur recursion with 16 bits arithmetic.

        if (L_ACF[0] == 0) { // everything is the same.
            for (int i = 0; i < 8; i++) {
                r[i] = 0;
            }
            return;
        }

        if (L_ACF[0] == 0) {
            throw new IllegalArgumentException("L_ACF[0] = " + L_ACF[0] + " should not = 0.");
        }

        temp = GsmMath.norm(L_ACF[0]);

        if (!(temp >= 0 && temp < 32)) {
            throw new IllegalArgumentException("temp = " + temp + " should be >= 0 and < 32.");
        }

        // ? overflow ?
        for (int i = 0; i <= 8; i++) {
            ACF[i] = GsmMath.sasr(L_ACF[i] << temp, 16);
        }

        // Initialize array P[..] and K[..] for the recursion.

        System.arraycopy(ACF, 0, K, 0, 7);

        System.arraycopy(ACF, 0, P, 0, 8);

        // Compute reflection coefficients
        for (int n = 1; n <= 8; n++, rIndex++) {

            temp = P[1];
            temp = GsmMath.abs(temp);
            if (P[0] < temp) {
                for (int i = n; i < 8; i++) {
                    r[i] = 0;
                }
                return;
            }

            r[rIndex] = GsmMath.div(temp, P[0]);

            if (!(r[rIndex] >= 0)) {
                throw new IllegalArgumentException("r[" + rIndex + "] = " + r[rIndex] + " should be >= 0");
            }

            if (P[1] > 0) {
                // r[n] = sub(0, r[n])
                r[rIndex] = (short) (-(r[rIndex]));
            }

            if (r[rIndex] == MIN_WORD) {
                throw new IllegalArgumentException("r[" + rIndex + "] = " + r[rIndex] + " should not be " + MIN_WORD);
            }
            if (n == 8)
                return;

            // Schur recursion
            temp = GsmMath.multR(P[1], r[rIndex]);
            P[0] = GsmMath.add(P[0], temp);

            for (int m = 1; m <= 8 - n; m++) {
                temp = GsmMath.multR(K[m], r[rIndex]);
                P[m] = GsmMath.add(P[m + 1], temp);

                temp = GsmMath.multR(P[m + 1], r[rIndex]);
                K[m] = GsmMath.add(K[m], temp);
            }
        }
    }

    /**
     * The following scaling for r[..] and LAR[..] has been used:
     * <p>
     * r[..] = integer( real_r[..]*32768. ); -1 <= real_r < 1. LAR[..] =
     * integer( real_LAR[..] * 16384 ); with -1.625 <= real_LAR <= 1.625
     *
     * @param r IN/OUT 0..7
     * @since 4.2.6
     */
    private static void transformeToLogAreaRatios(short[] r) throws IllegalArgumentException {

        // Computation of the LAR[0..7] from the r[0..7]
        for (int i = 0; i < 8; i++) {

            short temp = r[i];
            temp = GsmMath.abs(temp);

            if (!(temp >= 0)) {
                throw new IllegalArgumentException("temp = " + temp + " should be >= 0 ");
            }

            if (temp < 22118) {
                temp = (short) (temp >> 1);
            } else if (temp < 31130) {

                if (!(temp >= 11059)) {
                    throw new IllegalArgumentException("temp = " + temp + " should be >= 11059 ");
                }

                temp = (short) (temp - 11059);
            } else {
                if (!(temp >= 26112)) {
                    throw new IllegalArgumentException("temp = " + temp + " should be >= 26112 ");
                }

                temp = (short) (temp - 26112);
                temp = (short) (temp << 2);
            }

            r[i] = (short) (r[i] < 0 ? -temp : temp);

            if (r[i] == MIN_WORD) {
                throw new IllegalArgumentException("r[" + i + "] = " + r[i] + " should not be = " + MIN_WORD);
            }
        }
    }

    /**
     * This procedure needs four tables; the following equations give the
     * optimum scaling for the constants:
     * <p>
     * A[0..7] = integer( real_A[0..7] * 1024 ) B[0..7] = integer( real_B[0..7]
     * * 512 ) MAC[0..7] = maximum of the LARc[0..7] MIC[0..7] = minimum of the
     * LARc[0..7]
     *
     * @param LAR IN/OUT [0..7]
     * @since 4.2.7
     */
    private static void quantizeAndCode(short[] LAR) {
        int index = 0;

        step2(20480, 0, 31, -32, LAR, index++);
        step2(20480, 0, 31, -32, LAR, index++);
        step2(20480, 2048, 15, -16, LAR, index++);
        step2(20480, -2560, 15, -16, LAR, index++);

        step2(13964, 94, 7, -8, LAR, index++);
        step2(15360, -1792, 7, -8, LAR, index++);
        step2(8534, -341, 3, -4, LAR, index++);
        step2(9036, -1144, 3, -4, LAR, index++);
    }

    private static void step2(int A, int B, int MAC, int MIC, short[] LAR, int index) {
        short temp;

        temp = GsmMath.mult((short) A, LAR[index]);
        temp = GsmMath.add(temp, (short) B);
        temp = GsmMath.add(temp, (short) 256);
        temp = GsmMath.sasr(temp, 9);
        LAR[index] = (short) (temp > MAC ? MAC - MIC : (temp < MIC ? 0 : temp - MIC));
    }
}
