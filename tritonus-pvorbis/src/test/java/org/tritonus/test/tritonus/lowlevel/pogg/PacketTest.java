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
 *
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
        byte[] abData = new byte[1024];
        for (int i = 0; i < abData.length; i++) {
            abData[i] = (byte) i;
        }
        p.packetByte = abData;
        checkPacket(p, "set data test", false, false, 0, 0, abData);
    }

    @Test
    public void testSetDataTruncated() throws Exception {
        Packet p = new Packet();
        byte[] abData = new byte[1024];
        for (int i = 0; i < abData.length; i++) {
            abData[i] = (byte) i;
        }
        p.packetByte = Arrays.copyOfRange(abData, 0, abData.length / 2);
        byte[] abCompare = new byte[abData.length / 2];
        System.arraycopy(abData, 0, abCompare, 0, abData.length / 2);
        checkPacket(p, "set data truncated test", false, false, 0, 0, abCompare);
    }

    @Test
    public void testSetFlags() throws Exception {
        checkFlags("set flags test 1", true, false, 65555L, 0);
        checkFlags("set flags test 2", false, true, 0, 0);
        checkFlags("set flags test 3", false, false, Long.MAX_VALUE, 0);
        checkFlags("set flags test 4", true, true, Long.MIN_VALUE, 0);
    }

    private void checkFlags(String strMessage, boolean bBos, boolean bEos,
                            long lGranulePos, long lPacketNo) throws Exception {
        Packet p = new Packet();
        byte[] abData = new byte[0];
        p.packetByte = abData;
        p.b_o_s = bBos;
        p.e_o_s = bEos;
        p.granulePos = lGranulePos;
        p.packetNo = lPacketNo;
        checkPacket(p, strMessage, bBos, bEos, lGranulePos, lPacketNo, abData);
    }

    private void checkPacket(Packet p, String strMessage, boolean bBosExpected,
                             boolean bEosExpected, long lGranulePosExpected,
                             long lPacketNoExpected, byte[] abDataExpected) throws Exception {
        assertEquals(bBosExpected, p.isBos(), constructErrorMessage(strMessage, "bos flag"));
        assertEquals(bEosExpected, p.isEos(), constructErrorMessage(strMessage, "eos flag"));
        assertEquals(lGranulePosExpected, p.granulePos, constructErrorMessage(strMessage, "granulepos"));
        assertEquals(lPacketNoExpected, p.packetNo, constructErrorMessage(strMessage, "packetno"));
        System.out.println("data: " + Arrays.toString(p.packetByte));
        assertTrue(equals(abDataExpected, p.packetByte), constructErrorMessage(strMessage, "data content"));
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

    private static boolean equals(byte[] b1, int nOffset1,
                                  byte[] b2, int nOffset2,
                                  int nLength) {
        if (b1 == null && b2 == null)
            return true;
        if (nOffset1 + nLength > b1.length || nOffset2 + nLength > b2.length)
            return false;
        for (int i = 0; i < nLength; i++) {
            if (b1[nOffset1 + i] != b2[nOffset2 + i])
                return false;
        }
        return true;
    }

    private static String constructErrorMessage(String s1, String s2) {
        return s1 + ": " + s2;
    }
}
