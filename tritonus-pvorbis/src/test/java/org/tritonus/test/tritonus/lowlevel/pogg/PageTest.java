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
 * <p>
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
        byte[] header = new byte[1024];
        for (int i = 0; i < header.length; i++) {
            header[i] = (byte) (i + 128);
        }
        byte[] body = new byte[1024];
        for (int i = 0; i < body.length; i++) {
            body[i] = (byte) i;
        }
        p.header_base = header;
        p.header = 0;
        p.header_len = header.length;
        p.body_base = body;
        p.body = 0;
        p.body_len = body.length;
        checkData(p, "set data test", header, body);
    }

    @Test
    public void testSetDataOffset() throws Exception {
        Page p = new Page();
        byte[] header = new byte[102];
        for (int i = 0; i < header.length; i++) {
            header[i] = (byte) (i + 128);
        }
        byte[] body = new byte[1024];
        for (int i = 0; i < body.length; i++) {
            body[i] = (byte) i;
        }
        p.header_base = header;
        p.header = 12;
        p.header_len = 88;
        p.body_base = body;
        p.body = 511;
        p.body_len = 513;
        byte[] headerCompare = new byte[88];
        System.arraycopy(header, 12, headerCompare, 0, 88);
        byte[] bodyCompare = new byte[513];
        System.arraycopy(body, 511, bodyCompare, 0, 513);
        checkData(p, "set data offset test", headerCompare, bodyCompare);
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

    private static void checkHeaderProperties(
            String message, byte[] header,
            int versionExpected, boolean continuedExpected, int packetsExpected,
            boolean bosExpected, boolean eosExpected, long granulePosExpected,
            int serialNoExpected, int pageNoExpected) throws Exception {
        Page p = new Page();
        byte[] data = new byte[12];
        p.header_base = header;
        p.header = 0;
        p.header_len = header.length;
        p.body_base = data;
        p.body = 0;
        p.body_len = data.length;

        assertEquals(versionExpected, p.version(), constructErrorMessage(message, "version"));
        assertEquals(continuedExpected, p.continued(), constructErrorMessage(message, "continued flag"));
        assertEquals(packetsExpected, p.getPackets(), constructErrorMessage(message, "packets"));
        assertEquals(bosExpected, p.bos(), constructErrorMessage(message, "bos flag"));
        assertEquals(eosExpected, p.eos(), constructErrorMessage(message, "eos flag"));
        assertEquals(granulePosExpected, p.granulePos(), constructErrorMessage(message, "granulepos"));
        assertEquals(serialNoExpected, p.serialNo(), constructErrorMessage(message, "serialno"));
        assertEquals(pageNoExpected, p.pageNo(), constructErrorMessage(message, "pageno"));
    }

    private static void checkData(Page p, String message, byte[] headerExpected, byte[] bodyExpected) throws Exception {
        assertArrayEquals(headerExpected, Arrays.copyOfRange(p.header_base, p.header, p.header + p.header_len), constructErrorMessage(message, "header content"));
        assertArrayEquals(bodyExpected, Arrays.copyOfRange(p.body_base, p.body, p.body + p.body_len), constructErrorMessage(message, "body content"));
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
