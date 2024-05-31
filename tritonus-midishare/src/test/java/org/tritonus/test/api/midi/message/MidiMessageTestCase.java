/*
 * MidiMessageTestCase.java
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

package org.tritonus.test.api.midi.message;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;

import org.junit.jupiter.api.Test;
import org.tritonus.test.Util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Tests for class javax.sound.midi.MidiMessage.
 */
public class MidiMessageTestCase {

    /**
     * Checks the constructor.
     * The test checks for four things:
     * <ol>
     * <li>if the content of data follows the passed array (note that
     * it is legal for data to be longer than the passed array).</li>
     * <li>if the value of length follows the length of the passed array.</li>
     * <li>if the constructor makes a copy of the passed array.</li>
     * <li>if the constructor does (not) use setMessage().</li>
     * </ol>
     */
    @Test
    public void testConstructor() throws Exception {
        byte[] data = new byte[] {(byte) 144, 127, 0};
        TestMidiMessage message = new TestMidiMessage(data);
        assertTrue(Util.compareByteArrays(data, 0, message.getDataField(), 0, data.length), "data content");
        assertEquals(data.length, message.getLengthField(), "length field");
        assertSame(data, message.getDataField(), "array copying"); // not copied!
        assertFalse(message.getSetMessageUsed(), "setMessage() usage");
    }

    /**
     * Checks setMessage(byte[], int).
     * The test checks for three things:
     * <ol>
     * <li>if the content of data follows the passed array (note that
     * it is legal for data to be longer than the passed array).</li>
     * <li>if the value of length follows the length of the passed array.</li>
     * <li>if the method makes a copy of the passed array.</li>
     * </ol>
     */
    @Test
    public void testSetMessage() throws Exception {
        byte[] data = new byte[] {(byte) 144, 127, 0};
        TestMidiMessage message = new TestMidiMessage(data);
        byte[] data2 = new byte[] {(byte) 128, 31, 1};
        message.setMessage(data2, data2.length);
        assertTrue(Util.compareByteArrays(data2, 0, message.getDataField(), 0, data.length), "data content");
        assertEquals(data2.length, message.getLengthField(), "length field");
        assertNotSame(data2, message.getDataField(), "array copying");
        byte[] data3 = new byte[] {(byte) 128, 31, 1, 55, 55, 55};
        int desiredLength = 3;
        message.setMessage(data3, desiredLength);
        assertTrue(Util.compareByteArrays(data3, 0, message.getDataField(), 0, desiredLength), "data content");
        assertEquals(desiredLength, message.getLengthField(), "length field");
    }

    /**
     * Checks getMessage().
     * The test checks for three things:
     * <ol>
     * <li>if the returned array has the correct length.</li>
     * <li>if the returned array has the correct content (note that
     * it is legal for stored data to be longer than the returned array).</li>
     * <li>if the returned array is a copy of the stored array.</li>
     * </ol>
     */
    @Test
    public void testGetMessage() throws Exception {
        byte[] data = new byte[] {(byte) 144, 127, 0};
        TestMidiMessage message = new TestMidiMessage(data);
        byte[] returned = message.getMessage();
        assertEquals(data.length, returned.length, "length");
        assertTrue(Util.compareByteArrays(data, 0, returned, 0, data.length), "data content");
        assertNotSame(returned, message.getDataField(), "array copying");
    }

    /**
     * Checks getStatus().
     * The test checks if the returned status byte is correct.
     */
    @Test
    public void testGetStatus() throws Exception {
        int status = 144;
        byte[] data = new byte[] {(byte) status, 127, 0};
        TestMidiMessage message = new TestMidiMessage(data);
        int returnedStatus = message.getStatus();
        assertEquals(status, returnedStatus, "status byte");
    }

    /**
     * Checks setMessage(byte[], int).
     * The test checks if the returned length is correct.
     */
    @Test
    public void testGetLength() throws Exception {
        byte[] data = new byte[] {(byte) 144, 127, 0};
        TestMidiMessage message = new TestMidiMessage(data);
        int returnedLength = message.getLength();
        assertEquals(data.length, returnedLength, "length");
    }

    /**
     * Inner class used to access protected fields of MidiMessage.
     */
    private static class TestMidiMessage extends MidiMessage {

        private boolean setMessageUsed;

        public TestMidiMessage(byte[] data) {
            super(data);
        }

        public byte[] getDataField() {
            return data;
        }

        public int getLengthField() {
            return length;
        }

        public boolean getSetMessageUsed() {
            return setMessageUsed;
        }

        @Override
        protected void setMessage(byte[] data, int length) throws InvalidMidiDataException {
            super.setMessage(data, length);
            setMessageUsed = true;
        }

        /**
         * Not used here.
         */
        @Override
        public Object clone() {
            return null;
        }
    }
}
