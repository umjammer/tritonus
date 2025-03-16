/*
 * Long term port to Java.
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

public class LongTerm {

    /**
     *
     * @param d [0..39] residual signal IN
     * @param k d entry point, which 40
     * @param e [0..39] add 5 to index OUT
     * @param nc correlation lag OUT
     * @param bc gain factor OUT
     */
    public void predictLongTerm(
            short[] d, int k, short[] e, short[] dp, short[] dpp, int dp_dpp_point_dp0,
            short[] nc, short[] bc, int nc_bc_index) {
        calculateLtpParameters(d, k, dp, dp_dpp_point_dp0, bc, nc, nc_bc_index);
        analyzeFiltering(bc[nc_bc_index], nc[nc_bc_index], dp, d, k, dpp, e, dp_dpp_point_dp0);
    }

    /**
     *
     * @param d [0..39] IN
     * @param dp [-120..-1] IN
     * @param bc_out OUT
     * @param nc_out OUT
     */
    private static void calculateLtpParameters(
            short[] d, int d_index, short[] dp, int dp_start,
            short[] bc_out, short[] nc_out, int nc_bc_index) throws IllegalArgumentException {

        int lambda;
        short nc;
        short[] wt = new short[40];

        int max, power;
        short R, S, dmax = 0, scal;
        short temp;

        //
        // Search of the optimum scaling of d[0..39].
        //
        for (int k = 0; k <= 39; k++) {
            temp = d[k + d_index];
            temp = GsmMath.abs(temp);
            if (temp > dmax) {
                dmax = temp;
            }
        }

        temp = 0;

        if (dmax == 0) {
            scal = 0;
        } else {
            if (!(dmax > 0)) {
                throw new IllegalArgumentException("dmax = " + dmax + " should be > 0.");
            }
            temp = GsmMath.norm(dmax << 16);
        }

        if (temp > 6) {
            scal = 0;
        } else {
            scal = (short) (6 - temp);
        }

        if (!(scal >= 0)) {
            throw new IllegalArgumentException("scal = " + scal + " should be >= 0.");
        }

        //
        // Initialization of a working array wt
        //

        for (int k = 0; k <= 39; k++) {
            wt[k] = GsmMath.sasr(d[k + d_index], scal);
        }

        //
        // Search for the maximum cross-correlation and coding of the LTP lag
        //
        max = 0;
        nc = 40; // index for the maximum cross-correlation

        for (lambda = 40; lambda <= 120; lambda++) {
            int result;
            int step = 1;

            result = step(0, wt, dp, dp_start - lambda);
            result += step(1, wt, dp, step + dp_start - lambda);
            step++;
            result += step(2, wt, dp, step + dp_start - lambda);
            step++;
            result += step(3, wt, dp, step + dp_start - lambda);
            step++;
            result += step(4, wt, dp, step + dp_start - lambda);
            step++;
            result += step(5, wt, dp, step + dp_start - lambda);
            step++;
            result += step(6, wt, dp, step + dp_start - lambda);
            step++;
            result += step(7, wt, dp, step + dp_start - lambda);
            step++;
            result += step(8, wt, dp, step + dp_start - lambda);
            step++;
            result += step(9, wt, dp, step + dp_start - lambda);
            step++;
            result += step(10, wt, dp, step + dp_start - lambda);
            step++;
            result += step(11, wt, dp, step + dp_start - lambda);
            step++;
            result += step(12, wt, dp, step + dp_start - lambda);
            step++;
            result += step(13, wt, dp, step + dp_start - lambda);
            step++;
            result += step(14, wt, dp, step + dp_start - lambda);
            step++;
            result += step(15, wt, dp, step + dp_start - lambda);
            step++;
            result += step(16, wt, dp, step + dp_start - lambda);
            step++;
            result += step(17, wt, dp, step + dp_start - lambda);
            step++;
            result += step(18, wt, dp, step + dp_start - lambda);
            step++;
            result += step(19, wt, dp, step + dp_start - lambda);
            step++;
            result += step(20, wt, dp, step + dp_start - lambda);
            step++;
            result += step(21, wt, dp, step + dp_start - lambda);
            step++;
            result += step(22, wt, dp, step + dp_start - lambda);
            step++;
            result += step(23, wt, dp, step + dp_start - lambda);
            step++;
            result += step(24, wt, dp, step + dp_start - lambda);
            step++;
            result += step(25, wt, dp, step + dp_start - lambda);
            step++;
            result += step(26, wt, dp, step + dp_start - lambda);
            step++;
            result += step(27, wt, dp, step + dp_start - lambda);
            step++;
            result += step(28, wt, dp, step + dp_start - lambda);
            step++;
            result += step(29, wt, dp, step + dp_start - lambda);
            step++;
            result += step(30, wt, dp, step + dp_start - lambda);
            step++;
            result += step(31, wt, dp, step + dp_start - lambda);
            step++;
            result += step(32, wt, dp, step + dp_start - lambda);
            step++;
            result += step(33, wt, dp, step + dp_start - lambda);
            step++;
            result += step(34, wt, dp, step + dp_start - lambda);
            step++;
            result += step(35, wt, dp, step + dp_start - lambda);
            step++;
            result += step(36, wt, dp, step + dp_start - lambda);
            step++;
            result += step(37, wt, dp, step + dp_start - lambda);
            step++;
            result += step(38, wt, dp, step + dp_start - lambda);
            step++;
            result += step(39, wt, dp, step + dp_start - lambda);
            step++;

            if (result > max) {
                nc = (short) lambda;
                max = result;
            }
        }

        nc_out[nc_bc_index] = nc;

        max <<= 1;

        //
        // Rescaling of max
        //
        if (!(scal <= 100 && scal >= -100)) {
            throw new IllegalArgumentException("scal = " + scal + " should be >= -100 and <= 100.");
        }

        max = max >> (6 - scal); // sub(6, scal)

        if (!(nc <= 120 && nc >= 40)) {
            throw new IllegalArgumentException("nc = " + nc + " should be >= 40 and <= 120.");
        }

        //
        // Compute the power of the reconstructed short term residual signal dp[..]
        //
        power = 0;
        for (int k = 0; k <= 39; k++) {
            int L_temp;

            L_temp = GsmMath.sasr(dp[k - nc + dp_start], 3);
            power += L_temp * L_temp;
        }
        power <<= 1; // from L_MULT

        //
        // Normalization of max and power
        //

        if (max <= 0) {
            bc_out[nc_bc_index] = 0;
            return;
        }
        if (max >= power) {
            bc_out[nc_bc_index] = 3;
            return;
        }

        temp = GsmMath.norm(power);

        R = GsmMath.sasr(max << temp, 16);
        S = GsmMath.sasr(power << temp, 16);

        //
        // Coding of the LTP gain
        //

        //
        // Table 4.3a must be used to obtain the level DLB[i] for the
        // quantization of the LTP gain b to get the coded version bc.
        //
        for (int bc = 0; bc <= 2; bc++) {
            if (R <= GsmMath.mult(S, GsmDef.gsm_DLB[bc])) {
                break;
            }
            bc_out[nc_bc_index] = (short) bc;
        }
    }

    private static int step(int k, short[] wt, short[] dp, int dp_i) {
        return (wt[k] * dp[dp_i]);
    }

    /**
     * In this part, we have to decode the bc parameter to compute the samples
     * of the estimate dpp[0..39]. The decoding of bc needs the use of table
     * 4.3b. The long term residual signal e[0..39] is then calculated to be fed
     * to the RPE encoding section.
     *
     * @param bc IN
     * @param nc IN
     * @param dp previous d [-120..-1] IN
     * @param d d [0..39] IN
     * @param dpp estimate [0..39] OUT
     * @param e long term res. signal [0..39] OUT
     */
    static void analyzeFiltering(
            short bc, short nc, short[] dp, short[] d, int d_index, short[] dpp, short[] e, int dp_dpp_index) {

        short bp;

        switch (bc) {
        case 0:
            bp = (short) 3277;
            for (int k = 0; k <= 39; k++) {
                dpp[k + dp_dpp_index] = GsmMath.multR(bp, dp[k - nc + dp_dpp_index]);
                e[k + 5] = GsmMath.sub(d[k + d_index], dpp[k + dp_dpp_index]);
            }
            break;

        case 1:
            bp = (short) 11469;
            for (int k = 0; k <= 39; k++) {
                dpp[k + dp_dpp_index] = GsmMath.multR(bp, dp[k - nc + dp_dpp_index]);
                e[k + 5] = GsmMath.sub(d[k + d_index], dpp[k + dp_dpp_index]);
            }
            break;

        case 2:
            bp = (short) 21299;
            for (int k = 0; k <= 39; k++) {
                dpp[k + dp_dpp_index] = GsmMath.multR(bp, dp[k - nc + dp_dpp_index]);
                e[k + 5] = GsmMath.sub(d[k + d_index], dpp[k + dp_dpp_index]);
            }
            break;

        case 3:
            bp = (short) 32767;
            for (int k = 0; k <= 39; k++) {
                dpp[k + dp_dpp_index] = GsmMath.multR(bp, dp[k - nc + dp_dpp_index]);
                e[k + 5] = GsmMath.sub(d[k + d_index], dpp[k + dp_dpp_index]);
            }
            break;
        }
    }

    /**
     * This procedure uses the bcr and ncr parameter to realize the long term
     * synthesis filtering. The decoding of bcr needs table 4.3b.
     *
     * @param erp [0..39] IN
     * @param dp0_index_start_drp [-120..-1] IN, [0..40] OUT
     * drp is a pointer into the GsmState dp0 short array.
     */
    public static void synthesizeFiltering(GsmState s, short ncr, short bcr, short[] erp, int dp0_index_start_drp) {

        short brp, drpp, nr;
        short[] drp = s.getDp0();

        //
        // Check the limits of nr.
        //
        nr = ncr < 40 || ncr > 120 ? s.getNrp() : ncr;

        s.setNrp(nr);

        if (!(nr >= 40 && nr <= 120)) {
            throw new IllegalArgumentException("nr = " + nr + " is out of range. Should be >= 40 and <= 120");
        }

        //
        // Decoding of the LTP gain bcr
        //
        brp = GsmDef.gsm_QLB[bcr];

        //
        // Computation of the reconstructed short term residual signal drp[0..39]
        //
        if (brp == GsmDef.MIN_WORD) {
            throw new IllegalArgumentException("brp = " + brp + " is out of range. Should be = " + GsmDef.MIN_WORD);
        }

        for (int k = 0; k <= 39; k++) {
            drpp = GsmMath.multR(brp, drp[k - nr + dp0_index_start_drp]);
            drp[k + dp0_index_start_drp] = GsmMath.add(erp[k], drpp);
        }

        //
        // Update of the reconstructed short term residual signal
        //
        // drp[ -1..-120 ]
        //
        System.arraycopy(drp, (dp0_index_start_drp - 80), drp, (dp0_index_start_drp - 120), 120);

        s.setDp0(drp);
    }
}
