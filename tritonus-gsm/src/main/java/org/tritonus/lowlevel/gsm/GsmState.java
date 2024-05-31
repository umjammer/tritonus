/*
 * State control for encoding.
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

import java.io.PrintWriter;
import java.io.StringWriter;


public class GsmState {

    private short[] dp0;
    /** preprocessing, Offset_com. */
    private short z1;
    /** Offset_com. */
    private int l_z2;
    /** Preemphasis */
    private int mp;

    /** short_term.java */
    private short[] u;
    /** */
    private short[][] larPP;
    /** */
    private short j;

    /** long_term.java, synthesis */
    private short nrp;
    /** short_term.java, synthesis */
    private short[] v;
    /** Gsm_Decoder.java, Postprocessing */
    private short msr;

    public GsmState() {
        short[] Dp0 = new short[280];
        short[] U = new short[8];
        short[][] LARpp = new short[2][8];
        short[] V = new short[9];

        this.setDp0(Dp0);
        this.setZ1((short) 0);
        this.setL_z2(0);
        this.setMp(0);
        this.setU(U);
        this.setLARpp(LARpp);
        this.setJ((short) 0);
        this.setNrp((short) 40);
        this.setV(V);
        this.setMsr((short) 0);
    }

    @Override
    public String toString() {
        StringWriter sw = new StringWriter();
        PrintWriter ps = new PrintWriter(sw);

        ps.println("\ndp0[]: ");

//        for (int i = 0; i < dp0.length; ++i) {
//            ps.print("[" + i + "] " + dp0[i]);
//            if (i < dp0.length - 1)
//                ps.print(", ");
//        }

        ps.println("\nz1: " + z1);
        ps.println("\nl_z2: " + l_z2);
        ps.println("\nmp: " + mp);
        ps.println("\nu[]: ");
        for (int i = 0; i < u.length; ++i) {
            ps.print("[" + i + "] " + u[i]);
            if (i < u.length - 1)
                ps.print(", ");
        }
        ps.print("\n");
        ps.println("\nLARpp[]: ");
        for (int i = 0; i < 2; ++i) {
            for (int col = 0; col < 8; ++col) {
                ps.print("[" + i + "][" + col + "] " + larPP[i][col]);
                ps.print(", ");
            }
            ps.print("\n");
        }
        ps.print("\n");
        ps.println("\nj: " + j);
        ps.println("\nnrp: " + nrp);
        ps.println("\nv[]: ");
        for (int i = 0; i < v.length; ++i) {
            ps.print("[" + i + "] " + v[i]);
            if (i < v.length - 1)
                ps.print(", ");
        }
        ps.print("\n");
        ps.println("\nmsr: " + msr);
//        ps.println("\nverbose: " + verbose);
//        ps.println("\nfast: " + fast);
        return sw.toString();
    }

    public void setDp0(short[] value) {
        dp0 = value;
    }

    public void setDp0Indexed(int ix, short value) {
        dp0[ix] = value;
    }

    public short[] getDp0() {
        return dp0;
    }

    public short getDp0Indexed(int ix) {
        return dp0[ix];
    }

    public void setZ1(short value) {
        z1 = value;
    }

    public short getZ1() {
        return z1;
    }

    public void setL_z2(int value) {
        l_z2 = value;
    }

    public int getL_z2() {
        return l_z2;
    }

    public void setMp(int value) {
        mp = value;
    }

    public int getMp() {
        return mp;
    }

    public void setU(short[] value) {
        u = value;
    }

    public void setUIndexed(int ix, short value) {
        u[ix] = value;
    }

    public short[] getU() {
        return u;
    }

    public short getUIndexed(int ix) {
        return u[ix];
    }

    public void setLARpp(short[][] value) {
        larPP = value;
    }

    public void setLARppIndexed(int ix, short[] value) {
        larPP[ix] = value;
    }

    public short[][] getLARpp() {
        return larPP;
    }

    public short[] getLARppIndexed(int ix) {
        return larPP[ix];
    }

    public void setJ(short value) {
        j = value;
    }

    public short getJ() {
        return j;
    }

    public void setNrp(short value) {
        nrp = value;
    }

    public short getNrp() {
        return nrp;
    }

    public void setV(short[] value) {
        v = value;
    }

    public void setVIndexed(int ix, short value) {
        v[ix] = value;
    }

    public short[] getV() {
        return v;
    }

    public short getVIndexed(int ix) {
        return v[ix];
    }

    public void setMsr(short value) {
        msr = value;
    }

    public short getMsr() {
        return msr;
    }
}
