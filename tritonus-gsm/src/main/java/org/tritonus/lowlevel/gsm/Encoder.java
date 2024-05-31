/*
 * Encoder is the base class for all GSM encoding.
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

import org.tritonus.lowlevel.gsm.BitEncoder.AllocationMode;


/**
 * Encoder for GSM 06.10.
 *
 * @author Christopher Edwards
 * @author Matthias Pfisterer
 */
public class Encoder { /* Every Encoder has a state through completion */

    private final GsmState gsm = new GsmState();
    private final LongTerm longTerm = new LongTerm();
    private final Lpc lpc = new Lpc();
    private final Rpe rpe = new Rpe();
    private final ShortTerm shortTerm = new ShortTerm();

    /* [0..7] LAR coefficients OUT */
    private final short[] larC = new short[8];
    /* [0..3] LTP lag OUT */
    private final short[] nc = new short[4];
    /* [0..3] coded LTP gain OUT */
    private final short[] mc = new short[4];
    /* [0..3] RPE grid selection OUT */
    private final short[] bc = new short[4];
    /* [0..3] Coded maximum amplitude OUT */
    private final short[] xMaxC = new short[4];
    /* [13*4] normalized RPE samples OUT */
    private final short[] xmc = new short[13 * 4];

    /* Reads 160 bytes */
    private final int[] inputSignal = new int[160]; /* [0..159] OUT */

    private final GsmFrameFormat gsmFrameFormat;

    /**
     * Constructor.
     *
     * @param gsmFrameFormat the format of the GSM frames to produce
     */
    public Encoder(GsmFrameFormat gsmFrameFormat) {
        this.gsmFrameFormat = gsmFrameFormat;
    }

    /**
     * Encodes a block of data.
     *
     * @param buffer an 160-element (for "toast" frame format) or 320-element array
     *                 (for Microsoft frame format) with the data to encode in PCM
     *                 signed 16 bit format
     * @param frame  the encoded GSM frame (33 or 65 bytes, depending on the frame
     *                 format). Note that the contents of this array is overwritten
     *                 by this method.
     */
    public void encode(short[] buffer, byte[] frame) {
        BitEncoder bitEncoder;
        switch (gsmFrameFormat) {
        case TOAST:
            bitEncoder = new BitEncoder(frame, AllocationMode.MSBitFirst);
            bitEncoder.addBits(0xD, 4);
            break;
        case MICROSOFT:
            bitEncoder = new BitEncoder(frame, AllocationMode.LSBitFirst);
            break;
        default:
            throw new RuntimeException("Unhandled GSM frame format");
        }
        for (int i = 0; i < 160; i++) {
            inputSignal[i] = buffer[i];
        }
        encode();
        implodeFrameGeneric(bitEncoder);

        if (gsmFrameFormat == GsmFrameFormat.MICROSOFT) {
            for (int i = 0; i < 160; i++) {
                inputSignal[i] = buffer[i + 160];
            }
            encode();
            implodeFrameGeneric(bitEncoder);
        }
    }

    private void implodeFrameGeneric(BitEncoder bitEncoder) {
        bitEncoder.addBits(larC[0], 6);
        bitEncoder.addBits(larC[1], 6);
        bitEncoder.addBits(larC[2], 5);
        bitEncoder.addBits(larC[3], 5);
        bitEncoder.addBits(larC[4], 4);
        bitEncoder.addBits(larC[5], 4);
        bitEncoder.addBits(larC[6], 3);
        bitEncoder.addBits(larC[7], 3);

        bitEncoder.addBits(nc[0], 7);
        bitEncoder.addBits(bc[0], 2);
        bitEncoder.addBits(mc[0], 2);
        bitEncoder.addBits(xMaxC[0], 6);
        bitEncoder.addBits(xmc[0], 3);
        bitEncoder.addBits(xmc[1], 3);
        bitEncoder.addBits(xmc[2], 3);
        bitEncoder.addBits(xmc[3], 3);
        bitEncoder.addBits(xmc[4], 3);
        bitEncoder.addBits(xmc[5], 3);
        bitEncoder.addBits(xmc[6], 3);
        bitEncoder.addBits(xmc[7], 3);
        bitEncoder.addBits(xmc[8], 3);
        bitEncoder.addBits(xmc[9], 3);
        bitEncoder.addBits(xmc[10], 3);
        bitEncoder.addBits(xmc[11], 3);
        bitEncoder.addBits(xmc[12], 3);

        bitEncoder.addBits(nc[1], 7);
        bitEncoder.addBits(bc[1], 2);
        bitEncoder.addBits(mc[1], 2);
        bitEncoder.addBits(xMaxC[1], 6);
        bitEncoder.addBits(xmc[13], 3);
        bitEncoder.addBits(xmc[14], 3);
        bitEncoder.addBits(xmc[15], 3);
        bitEncoder.addBits(xmc[16], 3);
        bitEncoder.addBits(xmc[17], 3);
        bitEncoder.addBits(xmc[18], 3);
        bitEncoder.addBits(xmc[19], 3);
        bitEncoder.addBits(xmc[20], 3);
        bitEncoder.addBits(xmc[21], 3);
        bitEncoder.addBits(xmc[22], 3);
        bitEncoder.addBits(xmc[23], 3);
        bitEncoder.addBits(xmc[24], 3);
        bitEncoder.addBits(xmc[25], 3);

        bitEncoder.addBits(nc[2], 7);
        bitEncoder.addBits(bc[2], 2);
        bitEncoder.addBits(mc[2], 2);
        bitEncoder.addBits(xMaxC[2], 6);
        bitEncoder.addBits(xmc[26], 3);
        bitEncoder.addBits(xmc[27], 3);
        bitEncoder.addBits(xmc[28], 3);
        bitEncoder.addBits(xmc[29], 3);
        bitEncoder.addBits(xmc[30], 3);
        bitEncoder.addBits(xmc[31], 3);
        bitEncoder.addBits(xmc[32], 3);
        bitEncoder.addBits(xmc[33], 3);
        bitEncoder.addBits(xmc[34], 3);
        bitEncoder.addBits(xmc[35], 3);
        bitEncoder.addBits(xmc[36], 3);
        bitEncoder.addBits(xmc[37], 3);
        bitEncoder.addBits(xmc[38], 3);

        bitEncoder.addBits(nc[3], 7);
        bitEncoder.addBits(bc[3], 2);
        bitEncoder.addBits(mc[3], 2);
        bitEncoder.addBits(xMaxC[3], 6);
        bitEncoder.addBits(xmc[39], 3);
        bitEncoder.addBits(xmc[40], 3);
        bitEncoder.addBits(xmc[41], 3);
        bitEncoder.addBits(xmc[42], 3);
        bitEncoder.addBits(xmc[43], 3);
        bitEncoder.addBits(xmc[44], 3);
        bitEncoder.addBits(xmc[45], 3);
        bitEncoder.addBits(xmc[46], 3);
        bitEncoder.addBits(xmc[47], 3);
        bitEncoder.addBits(xmc[48], 3);
        bitEncoder.addBits(xmc[49], 3);
        bitEncoder.addBits(xmc[50], 3);
        bitEncoder.addBits(xmc[51], 3);
    }

    /**
     * Main part of encoding.
     *
     * <p>Uses array inputSignal as input (160 samples are expected there).</p>
     *
     * <p>Output is in the arrays xmc, larC, etc.</p>
     */
    private void encode() {
        int xmcPoint = 0;
        int nc_bc_index = 0;
        int xmaxc_Mc_index = 0;
        int dp_dpp_point_dp0 = 120;

        // short[] ep = new short[40];
        short[] e = new short[50];
        short[] so = new short[160];

        Gsm_Preprocess(so);
        lpc.analyzeLPC(so, larC);
        shortTerm.Gsm_Short_Term_Analysis_Filter(gsm, larC, so);

        short[] dp = gsm.getDp0();
        short[] dpp = dp;

        for (int k = 0; k <= 3; k++, xmcPoint += 13) {
            longTerm.predictLongTerm(so, /* d [0..39] IN */
                    k * 40, /* so entry point */
                    e, /* e+5 [0..39] OUT */
                    dp, /* Referance to GsmState dp0 */
                    dpp, /* Referance to GsmState dp0 */
                    dp_dpp_point_dp0, /* Where to start the dp0 ref */
                    nc, /* [0..3] coded LTP gain OUT */
                    bc, /* [0..3] RPE grid selection OUT */
                    nc_bc_index++ /* The current referance point for nc & bc */
            );

            rpe.Gsm_RPE_Encoding(e, /* e + 5 ][0..39][ IN/OUT */
                    xMaxC, /* [0..3] Coded maximum amplitude OUT */
                    mc, /* [0..3] coded LTP gain OUT */
                    xmaxc_Mc_index++, /* The current referance point */
                    xmc, /* [13*4] normalized RPE samples OUT */
                    xmcPoint /* The current referance point for xmc */);

            for (int i = 0; i <= 39; i++) {
                dp[i + dp_dpp_point_dp0] = GsmMath.add(e[5 + i], dpp[i
                        + dp_dpp_point_dp0]);
            }

            gsm.setDp0(dp);
            dp_dpp_point_dp0 += 40;
        }

        for (int i = 0; i < 120; i++) {
            gsm.setDp0Indexed(i, gsm.getDp0Indexed((160 + i)));
        }
    }

    private void Gsm_Preprocess(short[] so) /* [0..159] IN/OUT */
            throws IllegalArgumentException {
        int index = 0, so_index = 0;

        short z1 = gsm.getZ1();
        int L_z2 = gsm.getL_z2();
        int mp = gsm.getMp();

        short s1, msp, lsp, SO;
        int L_s2, L_temp;
        int k = 160;

        while (k != 0) {
            k--;

            /*
             * 4.2.1 Downscaling of the input signal
             */
            SO = (short) (GsmMath.sasr((short) inputSignal[index++], (short) 3) << 2);

            if (!(SO >= -0x4000)) { /* downscaled by */
                throw new IllegalArgumentException("Gsm_Preprocess: SO = " + SO
                        + " is out of range. Sould be >= -0x4000 ");
            }

            if (!(SO <= 0x3FFC)) { /* previous routine. */
                throw new IllegalArgumentException("Gsm_Preprocess: SO = " + SO
                        + " is out of range. Sould be <= 0x3FFC ");
            }

            /*
             * 4.2.2 Offset compensation
             *
             * This part implements a high-pass filter and requires extended
             * arithmetic precision for the recursive part of this filter. The
             * input of this procedure is the array so[0...159] and the output
             * the array sof[ 0...159 ].
             */

            /*
             * Compute the non-recursive part
             */
            s1 = (short) (SO - z1); /* s1 = gsm_sub( *so, z1 ); */
            z1 = SO;

            if (s1 == GsmDef.MIN_WORD) {
                throw new IllegalArgumentException("Gsm_Preprocess: s1 = " + s1
                        + " is out of range. ");
            }

            /*
             * Compute the recursive part
             */
            L_s2 = s1;
            L_s2 <<= 15;

            /*
             * Execution of a 31 bv 16 bits multiplication
             */

            msp = GsmMath.sasr(L_z2, 15);

            /* gsm_L_sub(L_z2,(msp<<15)); */
            lsp = (short) (L_z2 - (msp << 15));

            L_s2 += GsmMath.multR(lsp, (short) 32735);
            L_temp = (int) msp * 32735; /* GSM_L_MULT(msp,32735) >> 1; */
            L_z2 = GsmMath.addL(L_temp, L_s2);

            /*
             * Compute sof[k] with rounding
             */
            L_temp = GsmMath.addL(L_z2, 16384);

            /*
             * 4.2.3 Preemphasis
             */
            msp = GsmMath.multR((short) mp, (short) -28180);
            mp = GsmMath.sasr(L_temp, 15);
            so[so_index++] = GsmMath.add((short) mp, msp);
        }
        gsm.setZ1(z1);
        gsm.setL_z2(L_z2);
        gsm.setMp(mp);
    }
}
