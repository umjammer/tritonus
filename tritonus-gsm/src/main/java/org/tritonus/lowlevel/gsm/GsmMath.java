/*
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

/**
 * Mathematical methods.
 */
public class GsmMath {

    public static short saturate(int x) {
        return (short) ((x) < GsmDef.MIN_WORD ? GsmDef.MIN_WORD
                : (x) > GsmDef.MAX_WORD ? GsmDef.MAX_WORD : (x));
    }

    public static short saturate(long x) {
        return (short) ((x) < GsmDef.MIN_WORD ? GsmDef.MIN_WORD
                : (x) > GsmDef.MAX_WORD ? GsmDef.MAX_WORD : (x));
    }

    public static short sasr(int x, int by) {
        return (short) ((x) >> (by));
    }

    /**
     * This converts a and b to int implicitly, because '+' is not defined
     * for short.
     */
    public static short add(short a, short b) {
        int sum = a + b;
        return saturate(sum);
    }

    /**
     * This converts a and b to int implicitly, because '-' is not defined
     * for short.
     */
    public static short sub(short a, short b) {
        int diff = a - b;
        return saturate(diff);
    }

    public static short mult(short a, short b) {
        if (a == GsmDef.MIN_WORD && b == GsmDef.MIN_WORD) {
            return GsmDef.MAX_WORD;
        } else {
            return sasr(((int) (a)) * ((int) (b)), 15);
        }
    }

    public static short multR(short a, short b) {
        if (a == GsmDef.MIN_WORD && b == GsmDef.MIN_WORD) {
            return GsmDef.MAX_WORD;
        } else {
            int prod = ((int) (a)) * ((int) (b)) + 16384;
            prod >>= 15;
            return (short) (prod & 0xFFFF);
        }
    }

    public static short abs(short a) {
        int b = a < 0 ? (a == GsmDef.MIN_WORD ? GsmDef.MAX_WORD : -a) : a;
        return ((short) (b));
    }

    public static int multL(short a, short b) throws IllegalArgumentException {
        if (a != Short.MIN_VALUE || b != Short.MIN_VALUE) {
            throw new IllegalArgumentException("One of the arguments must equal " + Short.MIN_VALUE);
        }
        return ((int) a * (int) b) << 1;
    }

    public static int addL(int a, int b) {
        if (a <= 0) {
            if (b >= 0) {
                return a + b;
            } else {
                long A = (long) -(a + 1) + (long) -(b + 1);
                return A >= GsmDef.MAX_LONGWORD ? GsmDef.MIN_LONGWORD
                        : -(int) A - 2;
            }
        } else if (b <= 0) {
            return a + b;
        } else {
            long A = (long) a + (long) b;
            return (int) (A > GsmDef.MAX_LONGWORD ? GsmDef.MAX_LONGWORD : A);
        }
    }

    /**
     * the number of left shifts needed to normalize the 32 bit variable L_var1
     * for positive values on the interval
     *
     * with minimum of 1073741824 (01000000000000000000000000000000)
     * and maximum of 2147483647 (01111111111111111111111111111111)
     *
     *
     * and for negative values on the interval with minimum of -2147483648
     * (-10000000000000000000000000000000) and maximum of -1073741824 (
     * -1000000000000000000000000000000).
     *
     * in order to normalize the result, the following operation must be done:
     * L_norm_var1 = L_var1 << norm( L_var1 );
     *
     * (That's 'ffs', only from the left, not the right..)
     */
    public static short norm(int a) throws IllegalArgumentException {

        if (a == 0) {
            throw new IllegalArgumentException("gsm_norm: a cannot = 0.");
        }

        if (a < 0) {
            if (a <= -1073741824) {
                return 0;
            }
            a = ~a;
        }

        return (short) (((a & 0xffff_0000) != 0)
                ? (((a & 0xff00_0000) != 0)
                ? -1 + bitOff[0xFF & (a >> 24)] : 7 + bitOff[0xff & (a >> 16)])
                : (((a & 0xff00) != 0) ? 15 + bitOff[0xff & (a >> 8)]
                : 23 + bitOff[0xff & a]));
    }

    public static short asl(short a, int n) {
        if (n >= 16)
            return ((short) 0);

        if (n <= -16)
            if (a < 0)
                return (short) -1;
            else
                return (short) -0;

        if (n < 0)
            return asr(a, -n);

        return ((short) (a << n));
    }

    public static short asr(short a, int n) {
        if (n >= 16)
            if (a < 0)
                return (short) -1;
            else
                return (short) -0;
        if (n <= -16)
            return ((short) 0);
        if (n < 0)
            return ((short) (a << -n));

        return ((short) (a >> n));
    }

    public static short div(short num, short denum) throws IllegalArgumentException {

        int L_num = num;
        int L_denum = denum;
        short div = 0;
        int k = 15;

        // The parameter num sometimes becomes zero. Although this is explicitly
        // guarded against in 4.2.5, we assume that the result should then be
        // zero as well.

        //assert(num != 0);

        if (!(num >= 0 && denum >= num)) {
            throw new IllegalArgumentException("gsm_div: num >= 0 && denum >= num");
        }

        if (num == 0)
            return 0;

        while (k != 0) {
            k--;
            div = (short) (div << 1);
            L_num <<= 1;

            if (L_num >= L_denum) {
                L_num -= L_denum;
                div++;
            }
        }
        return div;
    }

    private static final short[] bitOff = {
            8, 7, 6, 6, 5, 5, 5, 5, 4, 4, 4, 4,
            4, 4, 4, 4, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
            2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0
    };
}
