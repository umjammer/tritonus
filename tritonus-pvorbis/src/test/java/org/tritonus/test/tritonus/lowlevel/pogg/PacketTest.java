/*
 *  Copyright (c) 2005 by Matthias Pfisterer
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

package org.tritonus.test.tritonus.lowlevel.pogg;

import java.util.Arrays;

import biniu.ogg.Packet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * PacketTest.
 * <p>
 * Tests for classes org.tritonus.lowlevel.pogg.Packet.
 */
public class PacketTest {

//    public void testClear() throws Exception {
//        Packet p = new Packet();
//        p.clear();
//        checkPacket(p, "clear test", false, false, 0, 0, null);
//    }

    @Test
    public void testClear() throws Exception {
        Packet p = new Packet();
        p.packetByte = new byte[3];
        p.b_o_s = true;
        p.e_o_s = false;
        p.granulePos = 99;
        p.packetNo = 100;
        p.clear();
        checkPacket(p, "clear test", false, false, 0, 0, null);
    }

    @Test
    public void testSetData() throws Exception {
        Packet p = new Packet();
        byte[] data = new byte[1024];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        p.packetByte = data;
        checkPacket(p, "set data test", false, false, 0, 0, data);
    }

    @Test
    public void testSetDataTruncated() throws Exception {
        Packet p = new Packet();
        byte[] data = new byte[1024];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        p.packetByte = Arrays.copyOfRange(data, 0, data.length / 2);
        byte[] compare = new byte[data.length / 2];
        System.arraycopy(data, 0, compare, 0, data.length / 2);
        checkPacket(p, "set data truncated test", false, false, 0, 0, compare);
    }

    @Test
    public void testSetFlags() throws Exception {
        checkFlags("set flags test 1", true, false, 65555L, 0);
        checkFlags("set flags test 2", false, true, 0, 0);
        checkFlags("set flags test 3", false, false, Long.MAX_VALUE, 0);
        checkFlags("set flags test 4", true, true, Long.MIN_VALUE, 0);
    }

    private static void checkFlags(String message, boolean bos, boolean eos, long granulePos,
                                   long packetNo) throws Exception {
        Packet p = new Packet();
        byte[] data = new byte[0];
        p.packetByte = data;
        p.b_o_s = bos;
        p.e_o_s = eos;
        p.granulePos = granulePos;
        p.packetNo = packetNo;
        checkPacket(p, message, bos, eos, granulePos, packetNo, data);
    }

    private static void checkPacket(Packet p, String message, boolean bosExpected,
                                    boolean eosExpected, long granulePosExpected,
                                    long packetNoExpected, byte[] dataExpected) throws Exception {
        assertEquals(bosExpected, p.isBos(), constructErrorMessage(message, "bos flag"));
        assertEquals(eosExpected, p.isEos(), constructErrorMessage(message, "eos flag"));
        assertEquals(granulePosExpected, p.granulePos, constructErrorMessage(message, "granulepos"));
        assertEquals(packetNoExpected, p.packetNo, constructErrorMessage(message, "packetno"));
        System.out.println("data: " + Arrays.toString(p.packetByte));
        assertTrue(equals(dataExpected, p.packetByte), constructErrorMessage(message, "data content"));
    }

    private static boolean equals(Packet p1, Packet p2) {
        return Arrays.equals(p1.packetByte, p2.packetByte) &&
                p1.isBos() == p2.isBos() &&
                p1.isEos() == p2.isEos() &&
                p1.granulePos == p2.granulePos &&
                p1.packetNo == p2.packetNo;
    }

    private static boolean equals(byte[] b1, byte[] b2) {
        if (b1 == null && b2 == null)
            return true;
        if (b1 != null)
            return equals(b1, 0, b2, 0, b1.length);
        return false;
    }

    private static boolean equals(byte[] b1, int offset1, byte[] b2, int offset2, int length) {
        if (b1 == null && b2 == null)
            return true;
        if (offset1 + length > b1.length || offset2 + length > b2.length)
            return false;
        for (int i = 0; i < length; i++) {
            if (b1[offset1 + i] != b2[offset2 + i])
                return false;
        }
        return true;
    }

    private static String constructErrorMessage(String s1, String s2) {
        return s1 + ": " + s2;
    }
}
