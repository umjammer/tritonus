/*
 *  Copyright (c) 2001 - 2004 by Matthias Pfisterer
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

package org.tritonus.test.tritonus.share.sampled.file;

import org.junit.jupiter.api.Test;
import org.tritonus.share.sampled.file.TDataOutputStream;
import org.tritonus.test.Util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * BaseDataOutputStreamTestCase.
 */
public abstract class BaseDataOutputStreamTestCase {

    private boolean seekable;

    protected BaseDataOutputStreamTestCase(boolean seekable) {
        this.seekable = seekable;
    }

    protected abstract TDataOutputStream createDataOutputStream() throws Exception;

    protected abstract byte[] getWrittenData() throws Exception;

    @Test
    public void testWriting() throws Exception {
        TDataOutputStream dataOutputStream = createDataOutputStream();
        checkTDataOutputStream(dataOutputStream);
        byte[] resultingData = getWrittenData();
        // Util.dumpByteArray(resultingData);
        checkTDataOutputStream2(resultingData);
    }

    @Test
    public void testSupportsSeek() throws Exception {
        TDataOutputStream dataOutputStream = createDataOutputStream();
        assertEquals(seekable, dataOutputStream.supportsSeek(), "seekability");
    }

    private static void checkTDataOutputStream(TDataOutputStream dataOutputStream) throws Exception {
        dataOutputStream.writeLittleEndian32(0x12345678);
        dataOutputStream.writeLittleEndian16((short) 0x2345);
        dataOutputStream.close();
    }

    private static void checkTDataOutputStream2(byte[] resultingData) throws Exception {
        byte[] expectedData = new byte[] {0x78, 0x56, 0x34, 0x12, 0x45, 0x23};
        assertTrue(Util.compareByteArrays(expectedData, 0, resultingData, 0, expectedData.length));
    }
}
