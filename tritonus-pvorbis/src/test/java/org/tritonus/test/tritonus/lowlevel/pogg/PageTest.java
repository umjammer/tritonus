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

import biniu.ogg.Page;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * PageTest.
 *
 * Tests for classes org.tritonus.lowlevel.pogg.Page.
 */
public class PageTest {

    /**
     * First and last, uncontinued, pos 0, serial 0x04030201,
     * page 0, 1 segment, 1 packet
     */
    private static final byte[] HEADER1 = new byte[] {
            0x4f, 0x67, 0x67, 0x53, 0, 0x06,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 0, 0, 0, 0,
            0x15, (byte) 0xed, (byte) 0xec, (byte) 0x91,
            1, 17
    };

    /**
     * First , uncontinued, pos -1, serial 0x04030201,
     * page 8, 7 segments, 1 packet
     */
    private static final byte[] HEADER2 = new byte[] {
            0x4f, 0x67, 0x67, 0x53, 0, 0x02,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,
            0x01, 0x02, 0x03, 0x04, 8, 0, 0, 0,
            0x15, (byte) 0xed, (byte) 0xec, (byte) 0x91,
            7, (byte) 255, (byte) 255, (byte) 255, (byte) 255,
            (byte) 255, (byte) 255, 12
    };

    @Test
    public void testSetData() throws Exception {
        Page p = new Page();
        byte[] abHeader = new byte[1024];
        for (int i = 0; i < abHeader.length; i++) {
            abHeader[i] = (byte) (i + 128);
        }
        byte[] abBody = new byte[1024];
        for (int i = 0; i < abBody.length; i++) {
            abBody[i] = (byte) i;
        }
        p.header_base = abHeader;
        p.header = 0;
        p.header_len = abHeader.length;
        p.body_base = abBody;
        p.body = 0;
        p.body_len = abBody.length;
        checkData(p, "set data test", abHeader, abBody);
    }

    @Test
    public void testSetDataOffset() throws Exception {
        Page p = new Page();
        byte[] abHeader = new byte[102];
        for (int i = 0; i < abHeader.length; i++) {
            abHeader[i] = (byte) (i + 128);
        }
        byte[] abBody = new byte[1024];
        for (int i = 0; i < abBody.length; i++) {
            abBody[i] = (byte) i;
        }
        p.header_base = abHeader;
        p.header = 12;
        p.header_len = 88;
        p.body_base = abBody;
        p.body = 511;
        p.body_len = 513;
        byte[] abHeaderCompare = new byte[88];
        System.arraycopy(abHeader, 12, abHeaderCompare, 0, 88);
        byte[] abBodyCompare = new byte[513];
        System.arraycopy(abBody, 511, abBodyCompare, 0, 513);
        checkData(p, "set data offset test", abHeaderCompare, abBodyCompare);
    }

    @Test
    public void testHeaderProperties() throws Exception {
        checkHeaderProperties("header properties test 1", HEADER1,
                0, false, 1,
                true, true, 0L,
                0x04030201, 0);
        checkHeaderProperties("header properties test 2", HEADER2,
                0, false, 1,
                true, false, -1L,
                0x04030201, 8);
    }

    private void checkHeaderProperties(
            String strMessage, byte[] abHeader,
            int nVersionExpected, boolean bContinuedExpected, int nPacketsExpected,
            boolean bBosExpected, boolean bEosExpected, long lGranulePosExpected,
            int nSerialNoExpected, int nPageNoExpected) throws Exception {
        Page p = new Page();
        byte[] abData = new byte[12];
        p.header_base = abHeader;
        p.header = 0;
        p.header_len = abHeader.length;
        p.body_base = abData;
        p.body = 0;
        p.body_len = abData.length;

        assertEquals(nVersionExpected, p.version(), constructErrorMessage(strMessage, "version"));
        assertEquals(bContinuedExpected, p.continued(), constructErrorMessage(strMessage, "continued flag"));
        assertEquals(nPacketsExpected, p.getPackets(), constructErrorMessage(strMessage, "packets"));
        assertEquals(bBosExpected, p.bos(), constructErrorMessage(strMessage, "bos flag"));
        assertEquals(bEosExpected, p.eos(), constructErrorMessage(strMessage, "eos flag"));
        assertEquals(lGranulePosExpected, p.granulePos(), constructErrorMessage(strMessage, "granulepos"));
        assertEquals(nSerialNoExpected, p.serialNo(), constructErrorMessage(strMessage, "serialno"));
        assertEquals(nPageNoExpected, p.pageNo(), constructErrorMessage(strMessage, "pageno"));
    }

    private void checkData(Page p, String strMessage, byte[] abHeaderExpected, byte[] abBodyExpected) throws Exception {
        assertArrayEquals(abHeaderExpected, Arrays.copyOfRange(p.header_base, p.header, p.header + p.header_len), constructErrorMessage(strMessage, "header content"));
        assertArrayEquals(abBodyExpected, Arrays.copyOfRange(p.body_base, p.body, p.body + p.body_len), constructErrorMessage(strMessage, "body content"));
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
