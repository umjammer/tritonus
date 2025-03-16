//    $Id$

//    This file is part of the GSM 6.10 audio decoder library for Java
//    Copyright (C) 1998 Steven Pickles (pix@test.at)

//    This library is free software; you can redistribute it and/or
//    modify it under the terms of the GNU Library General Public
//    License as published by the Free Software Foundation; either
//    version 2 of the License, or (at your option) any later version.

//    This library is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//    Library General Public License for more details.

//    You should have received a copy of the GNU Library General Public
//    License along with this library; if not, write to the Free
//    Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

//  This software is a port of the GSM Library provided by
//  Jutta Degener (jutta@cs.tu-berlin.de) and 
//  Carsten Bormann (cabo@cs.tu-berlin.de), 
//  Technische Universitaet Berlin

package org.tritonus.lowlevel.gsm;

import org.tritonus.lowlevel.gsm.BitDecoder.AllocationMode;

import static org.tritonus.share.sampled.TConversionTool.intToBytes16;


public final class GSMDecoder {

    private static final byte GSM_MAGIC = 0x0d;

    private static final int[] FAC = {18431, 20479, 22527, 24575, 26623, 28671, 30719, 32767};

    private static final int[] QLB = {3277, 11469, 21299, 32767};

    private static final int MIN_WORD = -32767 - 1;
    private static final int MAX_WORD = 32767;

    private final GsmFrameFormat gsmFrameFormat;

    private final BitDecoder bitDecoder = new BitDecoder(null, 0, AllocationMode.LSBitFirst);

    private final int[] dp0 = new int[280];

    //    private int[] u = new int[8];
    private final int[][] larPP = new int[2][8];
    private int j;

    private int nrp;
    private final int[] v = new int[9];
    private int msr;

    private final GsmFrameParameters gsmFrameParameters = new GsmFrameParameters();

    private final int[] erp = new int[40];
    private final int[] wt = new int[160];

    private final int[] xMp = new int[13];

    private final int[] result = new int[2];

    private final int[] larP = new int[8];

    private final int[] s = new int[160];

    public GSMDecoder() {
        this(GsmFrameFormat.TOAST);
    }

    public GSMDecoder(GsmFrameFormat gsmFrameFormat) {
        this.gsmFrameFormat = gsmFrameFormat;
    }

    public void GSM() {
        nrp = 40;
    }

    /**
     * This is how the method call should look like.
     *
     * @param frame       the array that contains the GSM frame (encoded data)
     * @param frameStart  that number of the byte that should be used as
     *                    starting point for the GSM frame inside frame
     * @param buffer      the array where the decoded data should be written to.
     *                    The data are written as 16 bit linear samples (actually using the lowest
     *                    13 bit), either big or little endian, depending on the value of
     *                    bigEndian.
     * @param bufferStart the byte number where the data should be written.
     * @param bigEndian   whether the decoded data should be written big endian
     *                    or little endian.
     */
    public void decode(byte[] frame, int frameStart, byte[] buffer, int bufferStart, boolean bigEndian)
            throws InvalidGSMFrameException {
        int[] decodedData;
        switch (gsmFrameFormat) {
            case TOAST:
                decodedData = decode(frame, frameStart);
                for (int i = 0; i < 160; i++) {
                    intToBytes16(decodedData[i], buffer, i * 2 + bufferStart, bigEndian);
                }
                break;
            case MICROSOFT:
                decodedData = decode(frame, frameStart);
                for (int i = 0; i < 160; i++) {
                    intToBytes16(decodedData[i], buffer, i * 2 + bufferStart, bigEndian);
                }
                decodedData = decode(frame, frameStart + 33);
                for (int i = 0; i < 160; i++) {
                    intToBytes16(decodedData[i], buffer, i * 2 + bufferStart + 160 * 2, bigEndian);
                }
                break;
            default:
                throw new RuntimeException("unknown GsmFrameFormat");
        }
    }

    /**
     * Decodes a single GSM frame.
     *
     * @param c                 byte array containing the coded frame
     * @param bufferStartOffset offset into the array for the coded frame
     * @return an array containing the decoded samples
     * @throws InvalidGSMFrameException c is larger than 33
     */
    private int[] decode(byte[] c, int bufferStartOffset) throws InvalidGSMFrameException {
        switch (gsmFrameFormat) {
            case TOAST:
                explodeFrameToast(c, bufferStartOffset, gsmFrameParameters);
                break;
            case MICROSOFT:
                explodeFrameMicrosoft(c, bufferStartOffset, gsmFrameParameters);
                break;
        }

        return decoder(gsmFrameParameters);
    }

    private static void explodeFrameToast(byte[] c, int bufferStartIndex, GsmFrameParameters gsmFrameParameters)
            throws InvalidGSMFrameException {
        if (c.length != 33) {
            throw new InvalidGSMFrameException();
        }

        int i = bufferStartIndex;

        if (((c[i] >> 4) & 0xf) != GSM_MAGIC) {
            throw new InvalidGSMFrameException();
        }

        gsmFrameParameters.larC[0] = ((c[i++] & 0xF) << 2); /* 1 */
        gsmFrameParameters.larC[0] |= ((c[i] >> 6) & 0x3);
        gsmFrameParameters.larC[1] = (c[i++] & 0x3F);
        gsmFrameParameters.larC[2] = ((c[i] >> 3) & 0x1F);
        gsmFrameParameters.larC[3] = ((c[i++] & 0x7) << 2);
        gsmFrameParameters.larC[3] |= ((c[i] >> 6) & 0x3);
        gsmFrameParameters.larC[4] = ((c[i] >> 2) & 0xF);
        gsmFrameParameters.larC[5] = ((c[i++] & 0x3) << 2);
        gsmFrameParameters.larC[5] |= ((c[i] >> 6) & 0x3);
        gsmFrameParameters.larC[6] = ((c[i] >> 3) & 0x7);
        gsmFrameParameters.larC[7] = (c[i++] & 0x7);
        gsmFrameParameters.nc[0] = ((c[i] >> 1) & 0x7F);
        gsmFrameParameters.bc[0] = ((c[i++] & 0x1) << 1);
        gsmFrameParameters.bc[0] |= ((c[i] >> 7) & 0x1);
        gsmFrameParameters.mc[0] = ((c[i] >> 5) & 0x3);
        gsmFrameParameters.xMaxC[0] = ((c[i++] & 0x1F) << 1);
        gsmFrameParameters.xMaxC[0] |= ((c[i] >> 7) & 0x1);
        gsmFrameParameters.xmc[0] = ((c[i] >> 4) & 0x7);
        gsmFrameParameters.xmc[1] = ((c[i] >> 1) & 0x7);
        gsmFrameParameters.xmc[2] = ((c[i++] & 0x1) << 2);
        gsmFrameParameters.xmc[2] |= ((c[i] >> 6) & 0x3);
        gsmFrameParameters.xmc[3] = ((c[i] >> 3) & 0x7);
        gsmFrameParameters.xmc[4] = (c[i++] & 0x7);
        gsmFrameParameters.xmc[5] = ((c[i] >> 5) & 0x7);
        gsmFrameParameters.xmc[6] = ((c[i] >> 2) & 0x7);
        gsmFrameParameters.xmc[7] = ((c[i++] & 0x3) << 1); /* 10 */
        gsmFrameParameters.xmc[7] |= ((c[i] >> 7) & 0x1);
        gsmFrameParameters.xmc[8] = ((c[i] >> 4) & 0x7);
        gsmFrameParameters.xmc[9] = ((c[i] >> 1) & 0x7);
        gsmFrameParameters.xmc[10] = ((c[i++] & 0x1) << 2);
        gsmFrameParameters.xmc[10] |= ((c[i] >> 6) & 0x3);
        gsmFrameParameters.xmc[11] = ((c[i] >> 3) & 0x7);
        gsmFrameParameters.xmc[12] = (c[i++] & 0x7);
        gsmFrameParameters.nc[1] = ((c[i] >> 1) & 0x7F);
        gsmFrameParameters.bc[1] = ((c[i++] & 0x1) << 1);
        gsmFrameParameters.bc[1] |= ((c[i] >> 7) & 0x1);
        gsmFrameParameters.mc[1] = ((c[i] >> 5) & 0x3);
        gsmFrameParameters.xMaxC[1] = ((c[i++] & 0x1F) << 1);
        gsmFrameParameters.xMaxC[1] |= ((c[i] >> 7) & 0x1);
        gsmFrameParameters.xmc[13] = ((c[i] >> 4) & 0x7);
        gsmFrameParameters.xmc[14] = ((c[i] >> 1) & 0x7);
        gsmFrameParameters.xmc[15] = ((c[i++] & 0x1) << 2);
        gsmFrameParameters.xmc[15] |= ((c[i] >> 6) & 0x3);
        gsmFrameParameters.xmc[16] = ((c[i] >> 3) & 0x7);
        gsmFrameParameters.xmc[17] = (c[i++] & 0x7);
        gsmFrameParameters.xmc[18] = ((c[i] >> 5) & 0x7);
        gsmFrameParameters.xmc[19] = ((c[i] >> 2) & 0x7);
        gsmFrameParameters.xmc[20] = ((c[i++] & 0x3) << 1);
        gsmFrameParameters.xmc[20] |= ((c[i] >> 7) & 0x1);
        gsmFrameParameters.xmc[21] = ((c[i] >> 4) & 0x7);
        gsmFrameParameters.xmc[22] = ((c[i] >> 1) & 0x7);
        gsmFrameParameters.xmc[23] = ((c[i++] & 0x1) << 2);
        gsmFrameParameters.xmc[23] |= ((c[i] >> 6) & 0x3);
        gsmFrameParameters.xmc[24] = ((c[i] >> 3) & 0x7);
        gsmFrameParameters.xmc[25] = (c[i++] & 0x7);
        gsmFrameParameters.nc[2] = ((c[i] >> 1) & 0x7F);
        gsmFrameParameters.bc[2] = ((c[i++] & 0x1) << 1); /* 20 */
        gsmFrameParameters.bc[2] |= ((c[i] >> 7) & 0x1);
        gsmFrameParameters.mc[2] = ((c[i] >> 5) & 0x3);
        gsmFrameParameters.xMaxC[2] = ((c[i++] & 0x1F) << 1);
        gsmFrameParameters.xMaxC[2] |= ((c[i] >> 7) & 0x1);
        gsmFrameParameters.xmc[26] = ((c[i] >> 4) & 0x7);
        gsmFrameParameters.xmc[27] = ((c[i] >> 1) & 0x7);
        gsmFrameParameters.xmc[28] = ((c[i++] & 0x1) << 2);
        gsmFrameParameters.xmc[28] |= ((c[i] >> 6) & 0x3);
        gsmFrameParameters.xmc[29] = ((c[i] >> 3) & 0x7);
        gsmFrameParameters.xmc[30] = (c[i++] & 0x7);
        gsmFrameParameters.xmc[31] = ((c[i] >> 5) & 0x7);
        gsmFrameParameters.xmc[32] = ((c[i] >> 2) & 0x7);
        gsmFrameParameters.xmc[33] = ((c[i++] & 0x3) << 1);
        gsmFrameParameters.xmc[33] |= ((c[i] >> 7) & 0x1);
        gsmFrameParameters.xmc[34] = ((c[i] >> 4) & 0x7);
        gsmFrameParameters.xmc[35] = ((c[i] >> 1) & 0x7);
        gsmFrameParameters.xmc[36] = ((c[i++] & 0x1) << 2);
        gsmFrameParameters.xmc[36] |= ((c[i] >> 6) & 0x3);
        gsmFrameParameters.xmc[37] = ((c[i] >> 3) & 0x7);
        gsmFrameParameters.xmc[38] = (c[i++] & 0x7);
        gsmFrameParameters.nc[3] = ((c[i] >> 1) & 0x7F);
        gsmFrameParameters.bc[3] = ((c[i++] & 0x1) << 1);
        gsmFrameParameters.bc[3] |= ((c[i] >> 7) & 0x1);
        gsmFrameParameters.mc[3] = ((c[i] >> 5) & 0x3);
        gsmFrameParameters.xMaxC[3] = ((c[i++] & 0x1F) << 1);
        gsmFrameParameters.xMaxC[3] |= ((c[i] >> 7) & 0x1);
        gsmFrameParameters.xmc[39] = ((c[i] >> 4) & 0x7);
        gsmFrameParameters.xmc[40] = ((c[i] >> 1) & 0x7);
        gsmFrameParameters.xmc[41] = ((c[i++] & 0x1) << 2);
        gsmFrameParameters.xmc[41] |= ((c[i] >> 6) & 0x3);
        gsmFrameParameters.xmc[42] = ((c[i] >> 3) & 0x7);
        gsmFrameParameters.xmc[43] = (c[i++] & 0x7); /* 30 */
        gsmFrameParameters.xmc[44] = ((c[i] >> 5) & 0x7);
        gsmFrameParameters.xmc[45] = ((c[i] >> 2) & 0x7);
        gsmFrameParameters.xmc[46] = ((c[i++] & 0x3) << 1);
        gsmFrameParameters.xmc[46] |= ((c[i] >> 7) & 0x1);
        gsmFrameParameters.xmc[47] = ((c[i] >> 4) & 0x7);
        gsmFrameParameters.xmc[48] = ((c[i] >> 1) & 0x7);
        gsmFrameParameters.xmc[49] = ((c[i++] & 0x1) << 2);
        gsmFrameParameters.xmc[49] |= ((c[i] >> 6) & 0x3);
        gsmFrameParameters.xmc[50] = ((c[i] >> 3) & 0x7);
        gsmFrameParameters.xmc[51] = (c[i] & 0x7); /* 33 */
    }

    private void explodeFrameMicrosoft(byte[] c, int bufferStartIndex, GsmFrameParameters gsmFrameParameters)
            throws InvalidGSMFrameException {
        bitDecoder.setCodedFrame(c, bufferStartIndex);

        gsmFrameParameters.larC[0] = bitDecoder.getNextBits(6);
        gsmFrameParameters.larC[1] = bitDecoder.getNextBits(6);
        gsmFrameParameters.larC[2] = bitDecoder.getNextBits(5);
        gsmFrameParameters.larC[3] = bitDecoder.getNextBits(5);
        gsmFrameParameters.larC[4] = bitDecoder.getNextBits(4);
        gsmFrameParameters.larC[5] = bitDecoder.getNextBits(4);
        gsmFrameParameters.larC[6] = bitDecoder.getNextBits(3);
        gsmFrameParameters.larC[7] = bitDecoder.getNextBits(3);
        gsmFrameParameters.nc[0] = bitDecoder.getNextBits(7);
        gsmFrameParameters.bc[0] = bitDecoder.getNextBits(2);
        gsmFrameParameters.mc[0] = bitDecoder.getNextBits(2);
        gsmFrameParameters.xMaxC[0] = bitDecoder.getNextBits(6);
        gsmFrameParameters.xmc[0] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[1] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[2] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[3] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[4] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[5] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[6] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[7] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[8] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[9] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[10] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[11] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[12] = bitDecoder.getNextBits(3);
        gsmFrameParameters.nc[1] = bitDecoder.getNextBits(7);
        gsmFrameParameters.bc[1] = bitDecoder.getNextBits(2);
        gsmFrameParameters.mc[1] = bitDecoder.getNextBits(2);
        gsmFrameParameters.xMaxC[1] = bitDecoder.getNextBits(6);
        gsmFrameParameters.xmc[13] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[14] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[15] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[16] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[17] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[18] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[19] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[20] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[21] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[22] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[23] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[24] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[25] = bitDecoder.getNextBits(3);
        gsmFrameParameters.nc[2] = bitDecoder.getNextBits(7);
        gsmFrameParameters.bc[2] = bitDecoder.getNextBits(2);
        gsmFrameParameters.mc[2] = bitDecoder.getNextBits(2);
        gsmFrameParameters.xMaxC[2] = bitDecoder.getNextBits(6);
        gsmFrameParameters.xmc[26] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[27] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[28] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[29] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[30] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[31] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[32] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[33] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[34] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[35] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[36] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[37] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[38] = bitDecoder.getNextBits(3);
        gsmFrameParameters.nc[3] = bitDecoder.getNextBits(7);
        gsmFrameParameters.bc[3] = bitDecoder.getNextBits(2);
        gsmFrameParameters.mc[3] = bitDecoder.getNextBits(2);
        gsmFrameParameters.xMaxC[3] = bitDecoder.getNextBits(6);
        gsmFrameParameters.xmc[39] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[40] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[41] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[42] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[43] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[44] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[45] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[46] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[47] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[48] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[49] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[50] = bitDecoder.getNextBits(3);
        gsmFrameParameters.xmc[51] = bitDecoder.getNextBits(3);
    }

    public static void print(String name, int[] data) {
        System.out.print("[" + name + ":");
        for (int i = 0; i < data.length; i++) {
            System.out.print("" + data[i]);
            if (i < data.length - 1) {
                System.out.print(",");
            } else {
                System.out.println("]");
            }
        }
    }

    public static void print(String name, int data) {
        System.out.println("[" + name + ":" + data + "]");
    }

    private int[] decoder(GsmFrameParameters gsmFrameParameters) {
        return decoder(gsmFrameParameters.larC, gsmFrameParameters.nc,
                gsmFrameParameters.bc, gsmFrameParameters.mc,
                gsmFrameParameters.xMaxC, gsmFrameParameters.xmc);
    }

    private int[] decoder(int[] LARcr, int[] Ncr, int[] bcr, int[] Mcr,
                          int[] xmaxcr, int[] xMcr) {
        int j, k;

        // drp is just dp0+120

        // print("LARcr",LARcr);
        // print("Ncr",Ncr);
        // print("bcr",bcr);
        // print("Mcr",Mcr);
        // print("xmaxcr",xmaxcr);
        // print("xMcr",xMcr);

        for (j = 0; j < 4; j++) {
            // find out what is done with xMcr
            RPEDecoding(xmaxcr[j], Mcr[j], xMcr, j * 13, erp);

            // print("erp",erp);

            longTermSynthesisFiltering(Ncr[j], bcr[j], erp, dp0);

            for (k = 0; k < 40; k++) {
                wt[j * 40 + k] = dp0[120 + k];
            }

        }

        // print("LARcr",LARcr);

        // print("wt",wt);

        int[] s = shortTermSynthesisFilter(LARcr, wt);

        // print("s",s);

        postprocessing(s);

        return s;
    }

    private void RPEDecoding(int xmaxcr, int Mcr, int[] xMcr,
                             int xMcrOffset, int[] erp) {
        int[] expAndMant;

        expAndMant = xmaxcToExpAndMant(xmaxcr);

        // System.out.println("[e&m:"+expAndMant[0]+","+expAndMant[1]+"]");

        APCMInverseQuantization(xMcr, xMcrOffset, expAndMant[0], expAndMant[1],
                xMp);

        // print("xMp",xMp);

        RPE_grid_positioning(Mcr, xMp, erp);
    }

    private int[] xmaxcToExpAndMant(int xmaxc) {
        int exp, mant;

        exp = 0;
        if (xmaxc > 15) {
            exp = ((xmaxc >> 3) - 1);
        }
        mant = (xmaxc - (exp << 3));

        if (mant == 0) {
            exp = -4;
            mant = 7;
        } else {
            while (mant <= 7) {
                mant = (mant << 1 | 1);
                exp--;
            }
            mant -= 8;
        }

        // assert(exp>=-4 && exp <= 6);
        // assert(mant>=0 && mant<=7);

        result[0] = exp;
        result[1] = mant;

        return result;
    }

    // private void assert(boolean test) {
    // if (!test) {
    // System.out.println("assertion error");
    // }
    // }

    private void APCMInverseQuantization(int[] xMc, int xMcOffset,
                                         int exp, int mant, int[] xMp) {
        int i, p;
        int temp, temp1, temp2, temp3;

        // assert(mant >0 && mant <= 7 );

        temp1 = FAC[mant];
        temp2 = sub(6, exp);
        temp3 = asl(1, sub(temp2, 1));

        // System.out.println("temp1="+temp1);
        // System.out.println("temp2="+temp2);
        // System.out.println("temp3="+temp3);

        p = 0;

        for (i = 13; i-- > 0; ) {
            // assert(xMc[xMcOffset] <= 7 && xMc[xMcOffset] >= 0);

            temp = ((xMc[xMcOffset++] << 1) - 7);

            // System.out.println("s1:temp="+temp);

            // assert(temp<=7 && temp >= -7);

            temp = (temp << 12);// &0xffff;

            // System.out.println("s2:temp="+temp);

            temp = mult_r(temp1, temp);

            // System.out.println("s3:temp="+temp);

            temp = add(temp, temp3);

            // System.out.println("s4:temp="+temp);

            xMp[p++] = asr(temp, temp2);
        }
    }

    private static int saturate(int x) {
        return (x < MIN_WORD ? MIN_WORD : (Math.min(x, MAX_WORD)));
    }

    private static int sub(int a, int b) {
        int diff = a - b;
        return saturate(diff);
    }

    private static int add(int a, int b) {
        int sum = a + b;
        return saturate(sum);
    }

    private static int asl(int a, int n) {
        if (n >= 16)
            return 0;
        if (n <= -16)
            return (a < 0 ? -1 : 0);
        if (n < 0)
            return asr(a, -n);
        return (a << n);
    }

    private static int asr(int a, int n) {
        if (n >= 16)
            return (a < 0 ? -1 : 0);
        if (n <= -16)
            return 0;
        if (n < 0)
            return (a << -n);// &0xffff;
        return (a >> n);
    }

    private static int mult_r(int a, int b) {
        if (b == MIN_WORD && a == MIN_WORD)
            return MAX_WORD;
        else {
            int prod = a * b + 16384;
            // prod >>= 15;
            return saturate(prod >> 15);// &0xffff;
            // return (prod & 0xffff);
        }
    }

    private void longTermSynthesisFiltering(int Ncr, int bcr, int[] erp,
                                            int[] dp0) {
        int brp, drpp, Nr;

        Nr = Ncr < 40 || Ncr > 120 ? nrp : Ncr;
        nrp = Nr;

        brp = QLB[bcr];

        for (int k = 0; k <= 39; k++) {
            drpp = mult_r(brp, dp0[120 + (k - Nr)]);
            dp0[120 + k] = add(erp[k], drpp);
        }

        for (int k = 0; k <= 119; k++) {
            dp0[k] = dp0[40 + k];
        }
    }

    private int[] shortTermSynthesisFilter(int[] LARcr, int[] wt) {

        // print("wt",wt);

        int[] LARpp_j = larPP[j];
        int[] LARpp_j_1 = larPP[j ^= 1];

        decodingOfTheCodedLogAreaRatios(LARcr, LARpp_j);

        // print("LARpp_j",LARpp_j);

        Coefficients_0_12(LARpp_j_1, LARpp_j, larP);
        LARp_to_rp(larP);
        shortTermSynthesisFiltering(larP, 13, wt, s, 0);

        Coefficients_13_26(LARpp_j_1, LARpp_j, larP);
        LARp_to_rp(larP);
        shortTermSynthesisFiltering(larP, 14, wt, s, 13);

        Coefficients_27_39(LARpp_j_1, LARpp_j, larP);
        LARp_to_rp(larP);
        shortTermSynthesisFiltering(larP, 13, wt, s, 27);

        Coefficients_40_159(LARpp_j, larP);
        LARp_to_rp(larP);
        shortTermSynthesisFiltering(larP, 120, wt, s, 40);

        return s;

    }

    public static void decodingOfTheCodedLogAreaRatios(int[] LARc,
                                                       int[] LARpp) {
        int temp1;

        // STEP( 0, -32, 13107 );

        temp1 = (add(LARc[0], -32) << 10);
        // temp1 = (sub(temp1, 0));
        temp1 = (mult_r(13107, temp1));
        LARpp[0] = (add(temp1, temp1));

        // STEP( 0, -32, 13107 );

        temp1 = (add(LARc[1], -32) << 10);
        // temp1 = (sub(temp1, 0));
        temp1 = (mult_r(13107, temp1));
        LARpp[1] = (add(temp1, temp1));

        // STEP( 2048, -16, 13107 );

        temp1 = (add(LARc[2], -16) << 10);
        temp1 = (sub(temp1, 4096));
        temp1 = (mult_r(13107, temp1));
        LARpp[2] = (add(temp1, temp1));

        // STEP( -2560, -16, 13107 );

        temp1 = (add(LARc[3], (-16)) << 10);
        temp1 = (sub(temp1, -5120));
        temp1 = (mult_r(13107, temp1));
        LARpp[3] = (add(temp1, temp1));

        // STEP( 94, -8, 19223 );

        temp1 = (add(LARc[4], -8) << 10);
        temp1 = (sub(temp1, 188));
        temp1 = (mult_r(19223, temp1));
        LARpp[4] = (add(temp1, temp1));

        // STEP( -1792, -8, 17476 );

        temp1 = (add(LARc[5], (-8)) << 10);
        temp1 = (sub(temp1, -3584));
        temp1 = (mult_r(17476, temp1));
        LARpp[5] = (add(temp1, temp1));

        // STEP( -341, -4, 31454 );

        temp1 = (add(LARc[6], (-4)) << 10);
        temp1 = (sub(temp1, -682));
        temp1 = (mult_r(31454, temp1));
        LARpp[6] = (add(temp1, temp1));

        // STEP( -1144, -4, 29708 );

        temp1 = (add(LARc[7], -4) << 10);
        temp1 = (sub(temp1, -2288));
        temp1 = (mult_r(29708, temp1));
        LARpp[7] = (add(temp1, temp1));

    }

    private static void Coefficients_0_12(int[] LARpp_j_1, int[] LARpp_j,
                                          int[] LARp) {
        for (int i = 0; i < 8; i++) {
            LARp[i] = add((LARpp_j_1[i] >> 2), (LARpp_j[i] >> 2));
            LARp[i] = add(LARp[i], (LARpp_j_1[i] >> 1));
        }
    }

    private static void Coefficients_13_26(int[] LARpp_j_1,
                                           int[] LARpp_j, int[] LARp) {
        for (int i = 0; i < 8; i++) {
            LARp[i] = add((LARpp_j_1[i] >> 1), (LARpp_j[i] >> 1));
        }
    }

    private static void Coefficients_27_39(int[] LARpp_j_1,
                                           int[] LARpp_j, int[] LARp) {
        for (int i = 0; i < 8; i++) {
            LARp[i] = add((LARpp_j_1[i] >> 2), (LARpp_j[i] >> 2));
            LARp[i] = add(LARp[i], (LARpp_j[i] >> 1));
        }
    }

    private static void Coefficients_40_159(int[] LARpp_j, int[] LARp) {
        System.arraycopy(LARpp_j, 0, LARp, 0, 8);
    }

    private static void LARp_to_rp(int[] LARp) {

        int temp;

        for (int i = 0; i < 8; i++) {
            if (LARp[i] < 0) {
                temp = ((LARp[i] == MIN_WORD) ? MAX_WORD : -LARp[i]);
                LARp[i] = (-((temp < 11059) ? temp << 1
                        : ((temp < 20070) ? temp + 11059 : add((temp >> 2),
                        26112))));
            } else {
                temp = LARp[i];
                LARp[i] = ((temp < 11059) ? temp << 1
                        : ((temp < 20070) ? temp + 11059 : add((temp >> 2),
                        26112)));
            }
        }
    }

    // shortTermSynthesisFiltering(LARp,13,wt,s,0);
    private void shortTermSynthesisFiltering(int[] rrp, int k, int[] wt,
                                             int[] sr, int off) {
        int sri, tmp1, tmp2;
        int woff = off;
        int soff = off;

        while (k-- > 0) {
            sri = wt[woff++];
            for (int i = 8; i-- > 0; ) {
                tmp1 = rrp[i];
                tmp2 = v[i];
                tmp2 = ((tmp1 == MIN_WORD && tmp2 == MIN_WORD ? MAX_WORD
                        : saturate((tmp1 * tmp2 + 16384) >> 15)));
                sri = sub(sri, tmp2);

                tmp1 = ((tmp1 == MIN_WORD && sri == MIN_WORD ? MAX_WORD
                        : saturate((tmp1 * sri + 16384) >> 15)));
                v[i + 1] = add(v[i], tmp1);
            }
            sr[soff++] = v[0] = sri;
        }
    }

    private void postprocessing(int[] s) {
        int soff = 0;
        int tmp;
        for (int k = 160; k-- > 0; soff++) {
            tmp = mult_r(msr, (28180));
            msr = add(s[soff], tmp);
            // s[soff]=(add(msr,msr) & 0xfff8);
            s[soff] = saturate(add(msr, msr) & ~0x7);
        }
    }

    private static void RPE_grid_positioning(int Mc, int[] xMp, int[] ep) {
        int i = 13;

        int epo = 0;
        int po = 0;

        switch (Mc) {
            case 3:
                ep[epo++] = 0;
            case 2:
                ep[epo++] = 0;
            case 1:
                ep[epo++] = 0;
            case 0:
                ep[epo++] = xMp[po++];
                i--;
        }

        do {
            ep[epo++] = 0;
            ep[epo++] = 0;
            ep[epo++] = xMp[po++];
        }
        while (--i > 0);

        while (++Mc < 4) {
            ep[epo++] = 0;
        }
    }

}

