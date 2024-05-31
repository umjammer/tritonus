/*
 * TCircularBufferTestCase.java
 */
/*
 *  Copyright (c) 2001 - 2002 by Matthias Pfisterer
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

package org.tritonus.test;

import org.junit.jupiter.api.Test;
import org.tritonus.share.TCircularBuffer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TCircularBufferTestCase {

    @Test
    public void testBufferSize() {
        int size = 45678;
        TCircularBuffer buffer = new TCircularBuffer(size, false, false, null);
        assertEquals(size, buffer.availableWrite(), "buffer size");
        size = 0;
        buffer = new TCircularBuffer(size, false, false, null);
        assertEquals(size, buffer.availableWrite(), "buffer size");
    }

    @Test
    public void testAvailable() {
        int bufferSize = 45678;
        int writeSize1 = bufferSize / 2;
        int writeSize2 = bufferSize / 5;
        int readSize1 = bufferSize / 10;
        int readSize2 = bufferSize / 3;
        TCircularBuffer buffer = new TCircularBuffer(bufferSize, true, true, null);
        assertEquals(bufferSize, buffer.availableWrite(), "availableWrite()");
        assertEquals(0, buffer.availableRead(), "availableRead()");
        buffer.write(new byte[bufferSize]);
        assertEquals(0, buffer.availableWrite(), "availableWrite()");
        assertEquals(bufferSize, buffer.availableRead(), "availableRead()");
        buffer.read(new byte[bufferSize]);
        assertEquals(bufferSize, buffer.availableWrite(), "availableWrite()");
        assertEquals(0, buffer.availableRead(), "availableRead()");

        buffer.write(new byte[writeSize1]);
        assertEquals(bufferSize - writeSize1, buffer.availableWrite(), "availableWrite()");
        assertEquals(writeSize1, buffer.availableRead(), "availableRead()");
        buffer.write(new byte[writeSize2]);
        assertEquals(bufferSize - writeSize1 - writeSize2, buffer.availableWrite(), "availableWrite()");
        assertEquals(writeSize1 + writeSize2, buffer.availableRead(), "availableRead()");
        buffer.read(new byte[readSize1]);
        assertEquals(bufferSize - writeSize1 - writeSize2 + readSize1, buffer.availableWrite(), "availableWrite()");
        assertEquals(writeSize1 + writeSize2 - readSize1, buffer.availableRead(), "availableRead()");
        buffer.read(new byte[readSize2]);
        assertEquals(bufferSize - writeSize1 - writeSize2 + readSize1 + readSize2, buffer.availableWrite(), "availableWrite()");
        assertEquals(writeSize1 + writeSize2 - readSize1 - readSize2, buffer.availableRead(), "availableRead()");
    }

    @Test
    public void testReadWrite() {
        int bufferSize = 8901 * 4;
        int result;
        byte[] writeArray = new byte[bufferSize];
        byte[] readArray = new byte[bufferSize];
        TCircularBuffer buffer = new TCircularBuffer(bufferSize, true, true, null);
        for (int i = 0; i < writeArray.length; i++) {
            writeArray[i] = (byte) (i % 256);
        }
        result = buffer.write(writeArray);
        assertEquals(writeArray.length, result, "written length");
        result = buffer.read(readArray);
        assertEquals(readArray.length, result, "read length");
        assertTrue(Util.compareByteArrays(readArray, 0, writeArray, 0, readArray.length), "data content");

        buffer.write(new byte[bufferSize / 3]);
        result = buffer.write(writeArray, bufferSize / 4, bufferSize / 2);
        assertEquals(bufferSize / 2, result, "written length");
        buffer.read(new byte[bufferSize / 3]);
        result = buffer.read(readArray, 0, bufferSize / 2);
        assertEquals(bufferSize / 2, result, "read length");
        assertTrue(Util.compareByteArrays(readArray, 0, writeArray, bufferSize / 4, bufferSize / 2), "data content");
    }

    @Test
    public void testTrigger() {
        TestTrigger trigger = new TestTrigger();

        int bufferSize = 45678;
        TCircularBuffer buffer = new TCircularBuffer(bufferSize, false, true, trigger);
        buffer.read(new byte[10]);
        assertTrue(trigger.isCalled(), "trigger called");

        trigger.reset();
        buffer.write(new byte[bufferSize / 3]);
        buffer.read(new byte[bufferSize / 2]);
        assertTrue(trigger.isCalled(), "trigger called");
    }

    @Test
    public void testClose() {
        int result;
        int bufferSize = 45678;
        TestTrigger trigger = new TestTrigger();
        TCircularBuffer buffer = new TCircularBuffer(bufferSize, true, true, trigger);
        buffer.write(new byte[bufferSize / 2]);
        assertEquals(bufferSize / 2, buffer.availableWrite(), "availableWrite()");
        assertEquals(bufferSize / 2, buffer.availableRead(), "availableRead()");
        buffer.close();
        assertEquals(bufferSize / 2, buffer.availableWrite(), "availableWrite()");
        assertEquals(bufferSize / 2, buffer.availableRead(), "availableRead()");
        result = buffer.read(new byte[bufferSize / 2]);
        assertEquals(bufferSize / 2, result, "read length");
        assertEquals(bufferSize, buffer.availableWrite(), "availableWrite()");
        assertEquals(0, buffer.availableRead(), "availableRead()");
        result = buffer.read(new byte[bufferSize / 2]);
        assertEquals(-1, result, "read length");
        assertFalse(trigger.isCalled(), "trigger invocation");
    }

    private static class TestTrigger implements TCircularBuffer.Trigger {

        private boolean called = false;

        @Override
        public void execute() {
            called = true;
        }

        public boolean isCalled() {
            return called;
        }

        public void reset() {
            called = false;
        }
    }
}
