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

import java.lang.System.Logger;
import java.util.Arrays;

import biniu.ogg.Packet;
import biniu.ogg.Page;
import biniu.ogg.StreamState;
import biniu.ogg.SyncState;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static java.lang.System.getLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * FramingTest.
 * <p>
 * Tests for classes org.tritonus.lowlevel.pogg.* except Buffer.
 */
@Disabled("wip")
public class FramingTest {

    private static final Logger logger = getLogger(FramingTest.class.getName());

    // 17 only

    private static final int[] head1_0 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x06,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 0, 0, 0, 0,
            0x15, 0xed, 0xec, 0x91,
            1,
            17
    };

    // 17, 254, 255, 256, 500, 510, 600 byte, pad

    private static final int[] head1_1 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x02,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 0, 0, 0, 0,
            0x59, 0x10, 0x6c, 0x2c,
            1,
            17
    };
    private static final int[] head2_1 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x04,
            0x07, 0x18, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 1, 0, 0, 0,
            0x89, 0x33, 0x85, 0xce,
            13,
            254, 255, 0, 255, 1, 255, 245, 255, 255, 0,
            255, 255, 90
    };

    // nil packets; beginning,middle,end

    private static final int[] head1_2 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x02,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 0, 0, 0, 0,
            0xff, 0x7b, 0x23, 0x17,
            1,
            0
    };
    private static final int[] head2_2 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x04,
            0x07, 0x28, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 1, 0, 0, 0,
            0x5c, 0x3f, 0x66, 0xcb,
            17,
            17, 254, 255, 0, 0, 255, 1, 0, 255, 245, 255, 255, 0,
            255, 255, 90, 0
    };

    // large initial packet

    private static final int[] head1_3 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x02,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 0, 0, 0, 0,
            0x01, 0x27, 0x31, 0xaa,
            18,
            255, 255, 255, 255, 255, 255, 255, 255,
            255, 255, 255, 255, 255, 255, 255, 255, 255, 10
    };

    private static final int[] head2_3 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x04,
            0x07, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 1, 0, 0, 0,
            0x7f, 0x4e, 0x8a, 0xd2,
            4,
            255, 4, 255, 0
    };

    // continuing packet test

    private static final int[] head1_4 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x02,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 0, 0, 0, 0,
            0xff, 0x7b, 0x23, 0x17,
            1,
            0
    };

    private static final int[] head2_4 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x00,
            0x07, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 1, 0, 0, 0,
            0x34, 0x24, 0xd5, 0x29,
            17,
            255, 255, 255, 255, 255, 255, 255, 255,
            255, 255, 255, 255, 255, 255, 255, 255, 255
    };

    private static final int[] head3_4 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x05,
            0x07, 0x0c, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 2, 0, 0, 0,
            0xc8, 0xc3, 0xcb, 0xed,
            5,
            10, 255, 4, 255, 0
    };

    // page with the 255 segment limit

    private static final int[] head1_5 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x02,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 0, 0, 0, 0,
            0xff, 0x7b, 0x23, 0x17,
            1,
            0
    };

    private static final int[] head2_5 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x00,
            0x07, 0xfc, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 1, 0, 0, 0,
            0xed, 0x2a, 0x2e, 0xa7,
            255,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10, 10,
            10, 10, 10, 10, 10, 10, 10
    };

    private static final int[] head3_5 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x04,
            0x07, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 2, 0, 0, 0,
            0x6c, 0x3b, 0x82, 0x3d,
            1,
            50
    };

    /* packet that overspans over an entire page */
    private static final int[] head1_6 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x02,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 0, 0, 0, 0,
            0xff, 0x7b, 0x23, 0x17,
            1,
            0
    };

    private static final int[] head2_6 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x00,
            0x07, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 1, 0, 0, 0,
            0x3c, 0xd9, 0x4d, 0x3f,
            17,
            100, 255, 255, 255, 255, 255, 255, 255, 255,
            255, 255, 255, 255, 255, 255, 255, 255
    };

    private static final int[] head3_6 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x01,
            0x07, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 2, 0, 0, 0,
            0xbd, 0xd5, 0xb5, 0x8b,
            17,
            255, 255, 255, 255, 255, 255, 255, 255,
            255, 255, 255, 255, 255, 255, 255, 255, 255
    };

    private static final int[] head4_6 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x05,
            0x07, 0x10, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 3, 0, 0, 0,
            0xef, 0xdd, 0x88, 0xde,
            7,
            255, 255, 75, 255, 4, 255, 0
    };

    /* packet that overspans over an entire page */
    private static final int[] head1_7 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x02,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 0, 0, 0, 0,
            0xff, 0x7b, 0x23, 0x17,
            1,
            0
    };

    private static final int[] head2_7 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x00,
            0x07, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 1, 0, 0, 0,
            0x3c, 0xd9, 0x4d, 0x3f,
            17,
            100, 255, 255, 255, 255, 255, 255, 255, 255,
            255, 255, 255, 255, 255, 255, 255, 255
    };

    private static final int[] head3_7 = {
            0x4f, 0x67, 0x67, 0x53, 0, 0x05,
            0x07, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x01, 0x02, 0x03, 0x04, 2, 0, 0, 0,
            0xd4, 0xe0, 0x60, 0xe5,
            1, 0
    };

    private static int sequence = 0;
    private static int lastno = 0;

    private void checkpacket(Packet op, int len, int no, int pos) {
        assertEquals(len, op.packetByte.length, "incorrect packet length!");
        assertEquals(pos, op.granulePos, "incorrect packet position!");
        // packet number just follows sequence/gap; adjust the input number
        // for that
        if (no == 0) {
            sequence = 0;
        } else {
            sequence++;
            if (no > lastno + 1)
                sequence++;
        }
        lastno = no;
        assertEquals(sequence, op.packetNo, "incorrect packet sequence");

        // Test data
        byte[] content = op.packetByte;
        for (int j = 0; j < content.length; j++) {
            assertEquals((j + no) & 0xFF, content[j] & 0xFF, "body data mismatch (1) at pos " + j + ":");
        }
    }

    void check_page(byte[] data, int offset, int[] header, Page og) {
        byte[] _header = og.header_base;
        byte[] body = og.body_base;

        // Test data
        for (int j = 0; j < og.body_len; j++) {
            assertEquals(data[j + offset], body[og.body + j], "body data mismatch (2) at pos %ld:");
        }

        // Test header
        for (int j = 0; j < og.header_len; j++) {
            assertEquals(header[j], (_header[og.header + j] & 0xFF), "header content mismatch at pos " + j + ":");
        }
        assertEquals(header[26] + 27, og.header_len, "header length incorrect! (%ld!=%d)");
    }

    private static void copyPage(Page og) {
        og.header_base = Arrays.copyOf(og.header_base, og.header_len);
        og.body_base = Arrays.copyOf(og.body_base, og.body_len);
    }

    private static void writePageToSyncState(Page page, SyncState sync) {
        byte[] pageHeader = page.header_base;
        byte[] pageBody = page.body_base;
        sync.write(pageHeader, page.header_len);
        sync.write(pageBody, page.body_len);
    }

    private static void writeToSyncState(byte[] data, int offset, int length, SyncState sync) {
        if (offset > 0) {
            byte[] tmp = new byte[length];
            System.arraycopy(data, offset, tmp, 0, tmp.length);
            data = tmp;
        }
        sync.write(data, length);
    }

    private static boolean equals(Packet p1, Packet p2) {
        return Arrays.equals(p1.packetByte, p2.packetByte) &&
                p1.isBos() == p2.isBos() &&
                p1.isEos() == p2.isEos() &&
                p1.granulePos == p2.granulePos &&
                p1.packetNo == p2.packetNo;
    }

    private static boolean equals(byte[] b1, int offset1, byte[] b2, int offset2, int length) {
        if (offset1 + length > b1.length || offset2 + length > b2.length)
            return false;
        for (int i = 0; i < length; i++) {
            if (b1[offset1 + i] != b2[offset2 + i])
                return false;
        }
        return true;
    }

    private void testPack(int[] pl, int[][] headers) {
        StreamState os_en = new StreamState();
        StreamState os_de = new StreamState();
        SyncState oy = new SyncState();
        byte[] data = new byte[1024 * 1024]; // for scripted test cases only
        int inptr = 0;
        int outptr = 0;
        int deptr = 0;
        int depacket = 0;
        long granule_pos = 7;
        int pageno = 0;
        int i, j, packets;
        int pageout = 0;
        boolean eosflag = false;
        boolean bosflag = false;

        os_en.init(0x04030201);
        os_de.init(0x04030201);
        oy.init();
        os_en.reset();
        os_de.reset();
        oy.reset();

        packets = pl.length;
//logger.log(Level.DEBUG, "packets: " + packets);
        for (i = 0; i < packets; i++) {
            // construct a test packet
            Packet op = new Packet();
            int len = pl[i];

            byte[] packetData = new byte[len];
            for (j = 0; j < len; j++)
                packetData[j] = (byte) (i + j);
            System.arraycopy(packetData, 0, data, inptr, len);
            inptr += len;
            op.packetByte = Arrays.copyOfRange(packetData, 0, len);
            op.b_o_s = false;
            op.e_o_s = i + 1 == packets;
            op.granulePos = granule_pos;
            op.packetNo = 0;

            granule_pos += 1024;

            // submit the test packet
            os_en.packetIn(op);

            // retrieve any finished pages
            Page og = new Page();

            while (os_en.pageOut(og)) {
                // We have a page.  Check it carefully

//logger.log(Level.DEBUG, String.format("%ld, ", pageno));

                assertTrue(pageno < headers.length, "coded too many pages!");

                check_page(data, outptr, headers[pageno], og);

                outptr += og.body_len;
                pageno++;

                // have a complete page; submit it to sync/decode

                Page og_de = new Page();
                Packet op_de = new Packet();
                Packet op_de2 = new Packet();
                writePageToSyncState(og, oy);

                while (oy.pageOut(og_de) > 0) {
                    // got a page.  Happy happy.  Verify that it's good.
                    // temporarily inserted
                    og_de.checksum();
                    // end insertion
                    check_page(data, deptr, headers[pageout], og_de);
                    deptr += og_de.body_len;
                    pageout++;

                    // submit it to deconstitution
                    os_de.pageIn(og_de);

                    // packets out?
                    while (os_de.packetPeek(op_de2) > 0) {
                        os_de.packetPeek(null);
                        os_de.packetOut(op_de); // just catching them all

                        // verify peek and out match
                        assertTrue(equals(op_de, op_de2), "packetout != packetpeek! pos=%ld");

                        // verify the packet!
                        // check data
                        assertTrue(equals(data, depacket, op_de.packetByte, 0, op_de.packetByte.length),
                                "packet data mismatch in decode! pos=%ld");
                        // check bos flag
                        if (bosflag) {
                            assertFalse(op_de.isBos(), "b_o_s flag incorrectly set on packet!");
                        } else {
                            assertTrue(op_de.isBos(), "b_o_s flag not set on packet!");
                        }
                        bosflag = true;
                        depacket += op_de.packetByte.length;

                        // check eos flag
                        assertFalse(eosflag, "Multiple decoded packets with eos flag!");

                        if (op_de.isEos())
                            eosflag = true;

                        // check granulepos flag
// 						if (op_de.granulepos != -1) {
// 							fprintf(stderr," granule:%ld ",(long)op_de.granulepos);
// 						}
                    }
                }
            }
        }
        data = null;
        assertEquals(pageno, headers.length, "did not write last page!");
        assertEquals(pageout, headers.length, "did not decode last page!");
        assertEquals(inptr, outptr, "encoded page data incomplete!");
        assertEquals(inptr, deptr, "decoded page data incomplete!");
        assertEquals(inptr, depacket, "decoded packet data incomplete!");
        assertTrue(eosflag, "Never got a packet with EOS set!");
    }

    @Test
    public void testStreamEncoding0() throws Exception {
        // 17 only
        // fprintf(stderr,"testing single page encoding... ");
        testPack(new int[] {17}, new int[][] {head1_0});
    }

    @Test
    public void testStreamEncoding1() throws Exception {
        // 17, 254, 255, 256, 500, 510, 600 byte, pad
        // fprintf(stderr,"testing basic page encoding... ");
        testPack(new int[] {17, 254, 255, 256, 500, 510, 600}, new int[][] {head1_1, head2_1});
    }

    @Test
    public void testStreamEncoding2() throws Exception {
        // nil packets; beginning,middle,end
        // fprintf(stderr,"testing basic nil packets... ");
        testPack(new int[] {0, 17, 254, 255, 0, 256, 0, 500, 510, 600, 0}, new int[][] {head1_2, head2_2});
    }

    @Test
    public void testStreamEncoding3() throws Exception {
        // large initial packet
        // fprintf(stderr,"testing initial-packet lacing > 4k... ");
        testPack(new int[] {4345, 259, 255}, new int[][] {head1_3, head2_3});
    }

    @Test
    public void testStreamEncoding4() throws Exception {
        // continuing packet test
        // fprintf(stderr,"testing single packet page span... ");
        testPack(new int[] {0, 4345, 259, 255}, new int[][] {head1_4, head2_4, head3_4});
    }

    @Test
    public void testStreamEncoding5() throws Exception {
        // page with the 255 segment limit
        int[] packets = new int[] {
                0, 10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 10,
                10, 10, 10, 10, 10, 10, 10, 50
        };
        // testing max packet segments...
        testPack(packets, new int[][] {head1_5, head2_5, head3_5});
    }

    @Test
    public void testStreamEncoding6() throws Exception {
        // packet that overspans over an entire page
        // testing very large packets...
        testPack(new int[] {0, 100, 9000, 259, 255}, new int[][] {head1_6, head2_6, head3_6, head4_6});
    }

    @Test
    public void testStreamEncoding7() throws Exception {
        // term only page.  why not?
        // testing zero data page (1 nil packet)...
        testPack(new int[] {0, 100, 4080}, new int[][] {head1_7, head2_7, head3_7});
    }

    /**
     * Create six pages containing 12 packets for testing.
     */
    private static Page[] createPages() throws Exception {
        StreamState os_en = new StreamState();
        os_en.init(0x04030201);

        // build a bunch of pages for testing
        int[] pl = new int[] {0, 100, 4079, 2956, 2057, 76, 34, 912, 0, 234, 1000, 1000, 1000, 300};
        Page[] og = new Page[5];
        for (int i = 0; i < og.length; i++)
            og[i] = new Page();

        os_en.reset();

        for (int i = 0; i < pl.length; i++) {
            Packet op = new Packet();
            int len = pl[i];

            byte[] packetData = new byte[len];
            for (int j = 0; j < len; j++) {
                packetData[j] = (byte) (i + j);
            }
            op.packetByte = Arrays.copyOf(packetData, len);
            op.b_o_s = false;
            op.e_o_s = i + 1 == pl.length;
            op.granulePos = (i + 1) * 1000;
            op.packetNo = 0;

            os_en.packetIn(op);
        }

        // retrieve finished pages
        for (int i = 0; i < 5; i++) {
            assertTrue(os_en.pageOut(og[i]), "Too few pages output building sync tests");
            copyPage(og[i]);
        }
        return og;
    }

    @Test
    public void testFraming() throws Exception {
        StreamState os_de = new StreamState();
        SyncState oy = new SyncState();
        os_de.init(0x04030201);
        oy.init();

        Page[] og = createPages();
    }

    /** Test lost pages on pagein/packetout: no rollback */
    @Test
    public void testFraming1() throws Exception {
        StreamState os_de = new StreamState();
        SyncState oy = new SyncState();
        os_de.init(0x04030201);
        oy.init();

        Page[] og = createPages();

        Page temp = new Page();
        Packet test = new Packet();

        // Testing loss of pages...

        oy.reset();
        os_de.reset();
        for (Page page : og) {
            writePageToSyncState(page, oy);
        }

        oy.pageOut(temp);
        os_de.pageIn(temp);
        oy.pageOut(temp);
        os_de.pageIn(temp);
        oy.pageOut(temp);
        // skip
        oy.pageOut(temp);
        os_de.pageIn(temp);

        // do we get the expected results/packets?

        assertEquals(1, os_de.packetOut(test), "retrieving packet after lost page");
        checkpacket(test, 0, 0, 0);
        assertEquals(1, os_de.packetOut(test), "retrieving packet after lost page");
        checkpacket(test, 100, 1, -1);
        assertEquals(1, os_de.packetOut(test), "retrieving packet after lost page");
        checkpacket(test, 4079, 2, 3000);
        assertEquals(-1, os_de.packetOut(test), "loss of page did not return error");
        assertEquals(1, os_de.packetOut(test), "retrieving packet after lost page");
        checkpacket(test, 76, 5, -1);
        assertEquals(1, os_de.packetOut(test), "retrieving packet after lost page");
        checkpacket(test, 34, 6, -1);
    }

    /** Test lost pages on pagein/packetout: rollback with continuation */
    @Test
    public void testFraming2() throws Exception {
        StreamState os_de = new StreamState();
        SyncState oy = new SyncState();
        os_de.init(0x04030201);
        oy.init();

        Page[] og = createPages();

        Page temp = new Page();
        Packet test = new Packet();

        // Testing loss of pages (rollback required)...

        oy.reset();
        os_de.reset();
        for (int i = 0; i < 5; i++) {
            writePageToSyncState(og[i], oy);
        }

        oy.pageOut(temp);
        os_de.pageIn(temp);
        oy.pageOut(temp);
        os_de.pageIn(temp);
        oy.pageOut(temp);
        os_de.pageIn(temp);
        oy.pageOut(temp);
        // skip
        oy.pageOut(temp);
        os_de.pageIn(temp);

        // do we get the expected results/packets?
        assertEquals(1, os_de.packetOut(test), "retrieving packet after lost page");
        checkpacket(test, 0, 0, 0);
        assertEquals(1, os_de.packetOut(test), "retrieving packet after lost page");
        checkpacket(test, 100, 1, -1);
        assertEquals(1, os_de.packetOut(test), "retrieving packet after lost page");
        checkpacket(test, 4079, 2, 3000);
        assertEquals(1, os_de.packetOut(test), "retrieving packet after lost page");
        checkpacket(test, 2956, 3, 4000);
        assertEquals(-1, os_de.packetOut(test), "retrieving packet after lost page did not return error");
        assertEquals(1, os_de.packetOut(test), "retrieving packet after lost page");
        checkpacket(test, 300, 13, 14000);
    }

    /** the rest only test sync */
    @Test
    public void testFraming3() throws Exception {
        StreamState os_de = new StreamState();
        SyncState oy = new SyncState();
        os_de.init(0x04030201);
        oy.init();

        Page[] og = createPages();

        Page og_de = new Page();
        // Test fractional page inputs: incomplete capture
        // Testing sync on partial inputs...
        oy.reset();
        byte[] header = og[1].header_base;
        oy.write(header, 3);
        assertTrue(oy.pageOut(og_de) <= 0, "sync on incomplete capture");

        // Test fractional page inputs: incomplete fixed header
        writeToSyncState(header, 3, 20, oy);
        assertTrue(oy.pageOut(og_de) <= 0, "sync on incomplete fixed header");

        // Test fractional page inputs: incomplete header
        writeToSyncState(header, 23, 5, oy);
        assertTrue(oy.pageOut(og_de) <= 0, "sync on incomplete header");

        // Test fractional page inputs: incomplete body

        writeToSyncState(header, 28, header.length - 28, oy);
        assertTrue(oy.pageOut(og_de) <= 0, "sync on incomplete body");

        byte[] body = og[1].body_base;
        oy.write(body, 1000);
        assertTrue(oy.pageOut(og_de) <= 0, "sync on incomplete body");

        writeToSyncState(body, 1000, body.length - 1000, oy);
        assertTrue(oy.pageOut(og_de) > 0, "sync on complete body");
    }

    /** Test fractional page inputs: page + incomplete capture */
    @Test
    public void testFraming4() throws Exception {
        StreamState os_de = new StreamState();
        SyncState oy = new SyncState();
        os_de.init(0x04030201);
        oy.init();

        Page[] og = createPages();

        Page og_de = new Page();
        // Testing sync on 1+partial inputs...
        oy.reset();

        writePageToSyncState(og[1], oy);

        byte[] header = og[1].header_base;
        oy.write(header, og[1].header_len);
        assertTrue(oy.pageOut(og_de) > 0);
        assertTrue(oy.pageOut(og_de) <= 0);

        writeToSyncState(header, 20, header.length - 20, oy);
        byte[] body = og[1].body_base;
        oy.write(body, og[1].body_len);
        assertTrue(oy.pageOut(og_de) > 0);
    }

    /** Test recapture: garbage + page */
    @Test
    public void testFraming5() throws Exception {
        StreamState os_de = new StreamState();
        SyncState oy = new SyncState();
        os_de.init(0x04030201);
        oy.init();

        Page[] og = createPages();

        Page og_de = new Page();
        // Testing search for capture...
        oy.reset();

        // 'garbage'
        byte[] body = og[1].body_base;
        writeToSyncState(body, 0, og[1].body_len, oy);

        writePageToSyncState(og[1], oy);

        byte[] header = og[2].header_base;
        writeToSyncState(header, 0, 20, oy);

        assertTrue(oy.pageOut(og_de) <= 0);
        assertTrue(oy.pageOut(og_de) > 0);
        assertTrue(oy.pageOut(og_de) <= 0);

        writeToSyncState(header, 20, header.length - 20, oy);
        body = og[2].body_base;
        writeToSyncState(body, 0, og[2].body_len, oy);
        assertTrue(oy.pageOut(og_de) > 0);
    }

    /** Test recapture: page + garbage + page */
    @Test
    public void testFraming6() throws Exception {
        StreamState os_de = new StreamState();
        SyncState oy = new SyncState();
        os_de.init(0x04030201);
        oy.init();

        Page[] og = createPages();

        Page og_de = new Page();
        // Testing recapture...
        oy.reset();

        writePageToSyncState(og[1], oy);

        byte[] header = og[2].header_base;
        writeToSyncState(header, 0, og[2].header_len, oy);

        assertTrue(oy.pageOut(og_de) > 0);

        byte[] body = og[2].body_base;
        writeToSyncState(body, 0, og[2].body_len - 5, oy);

        writePageToSyncState(og[3], oy);

        assertTrue(oy.pageOut(og_de) <= 0);
        assertTrue(oy.pageOut(og_de) > 0);
    }
}
