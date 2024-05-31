/*
 *  Copyright (c) 2003 by Matthias Pfisterer
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

package org.tritonus.test.tritonus.share.midi;

import org.junit.jupiter.api.Test;
import org.tritonus.share.midi.TMidiChannel;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * TDirectSynthesizerTestCase.
 */
public class TMidiChannelTestCase {

    @Test
    public void testChannelNumber() {
        int CHANNEL = 19;
        TestMidiChannel channel = new TestMidiChannel(CHANNEL);
        assertEquals(CHANNEL, channel.getChannelNumber(), "channel number");
    }

    @Test
    public void testNoteOff() {
        TestMidiChannel channel = new TestMidiChannel(0);
        int KEY;
        channel.resetCachedValues();
        KEY = 0;
        channel.noteOff(KEY);
        assertEquals(KEY, channel.getNoteOffKey(), "noteOff() key");
        assertEquals(0, channel.getNoteOffVelocity(), "noteOff() velocity");

        channel.resetCachedValues();
        KEY = 11;
        channel.noteOff(KEY);
        assertEquals(KEY, channel.getNoteOffKey(), "noteOff() key");
        assertEquals(0, channel.getNoteOffVelocity(), "noteOff() velocity");

        channel.resetCachedValues();
        KEY = 127;
        channel.noteOff(KEY);
        assertEquals(KEY, channel.getNoteOffKey(), "noteOff() key");
        assertEquals(0, channel.getNoteOffVelocity(), "noteOff() velocity");
    }

    @Test
    public void testProgramChange() {
        TestMidiChannel channel = new TestMidiChannel(0);
        doTestProgramChange(channel, 0, 0, 0);
        doTestProgramChange(channel, 127, 127, 127);
    }

    private static void doTestProgramChange(TestMidiChannel channel, int bankHigh, int bankLow, int program) {
        channel.resetCachedValues();
        int bank = (bankHigh << 7) | bankLow;
        channel.programChange(bank, program);
        System.out.println("(c)" + channel.getSetControllerNumber());
        System.out.println("(v)" + channel.getSetControllerValue());
        System.out.println("(c2)" + channel.getSetControllerNumber2());
        System.out.println("(v2)" + channel.getSetControllerValue2());

        assertEquals(0, channel.getSetControllerNumber(), "programChange() bank high (c)");
        assertEquals(bankHigh, channel.getSetControllerValue(), "programChange() bank high (v)");
        assertEquals(32, channel.getSetControllerNumber2(), "programChange() bank low (c)");
        assertEquals(bankLow, channel.getSetControllerValue2(), "programChange() bank low (v)");
        assertEquals(program, channel.getProgramChangeValue(), "programChange() program");
    }

    @Test
    public void testResetAllControllers() {
        TestMidiChannel channel = new TestMidiChannel(0);
        channel.resetAllControllers();
        assertEquals(121, channel.getSetControllerNumber(), "resetAllControllers(): controller");
        assertEquals(0, channel.getSetControllerValue(), "resetAllControllers(): value");
    }

    @Test
    public void testAllNotesOff() {
        TestMidiChannel channel = new TestMidiChannel(0);
        channel.allNotesOff();
        assertEquals(123, channel.getSetControllerNumber(), "allNotesOff(): controller");
        assertEquals(0, channel.getSetControllerValue(), "allNotesOff(): value");
    }

    @Test
    public void testAllSoundOff() {
        TestMidiChannel channel = new TestMidiChannel(0);
        channel.allSoundOff();
        assertEquals(120, channel.getSetControllerNumber(), "allSoundOff(): controller");
        assertEquals(0, channel.getSetControllerValue(), "allSoundOff(): value");
    }

    @Test
    public void testLocalControl() {
        TestMidiChannel channel = new TestMidiChannel(0);
        channel.localControl(true);
        assertEquals(122, channel.getSetControllerNumber(), "localControl(true): controller");
        assertEquals(127, channel.getSetControllerValue(), "localControl(true): value");
        channel.resetCachedValues();

        channel.localControl(false);
        assertEquals(122, channel.getSetControllerNumber(), "localControl(false): controller");
        assertEquals(0, channel.getSetControllerValue(), "localControl(false): value");
    }

    private static class TestMidiChannel
            extends TMidiChannel {

        private int noteOffKey;
        private int noteOffVelocity;
        private int setControllerNumber;
        private int setControllerValue;
        private int setControllerNumber2;
        private int setControllerValue2;
//        private int getControllerNumber;
        private int programChangeValue;

        public TestMidiChannel(int channel) {
            super(channel);
            resetCachedValues();
        }

        /**
         * Used to obtain the return value of the protected super class method.
         *
         * @return
         */
        public int getChannelNumber() {
            return getChannel();
        }

        public void resetCachedValues() {
            noteOffKey = -1;
            noteOffVelocity = -1;
            setControllerNumber = -1;
            setControllerValue = -1;
            setControllerNumber2 = -1;
            setControllerValue2 = -1;
//   m_nGetControllerNumber = -1;
            programChangeValue = -1;
        }

        public int getNoteOffKey() {
            return noteOffKey;
        }

        public int getNoteOffVelocity() {
            return noteOffVelocity;
        }

        public int getSetControllerNumber() {
            return setControllerNumber;
        }

        public int getSetControllerValue() {
            return setControllerValue;
        }

        public int getSetControllerNumber2() {
            return setControllerNumber2;
        }

        public int getSetControllerValue2() {
            return setControllerValue2;
        }

        public int getProgramChangeValue() {
            return programChangeValue;
        }

        /**
         * Records the passed values.
         */
        @Override
        public void controlChange(int controller, int value) {
            System.out.println("CC: " + controller + ": " + value);
            if (setControllerNumber != -1) {
                setControllerNumber2 = controller;
                setControllerValue2 = value;
            } else {
                setControllerNumber = controller;
                setControllerValue = value;
            }
        }

        @Override
        public int getChannelPressure() {
            return 0;
        }

        @Override
        public int getController(int controller) {
            return 0;
        }

        @Override
        public boolean getMute() {
            return false;
        }

        @Override
        public int getPitchBend() {
            return 0;
        }

        @Override
        public int getPolyPressure(int noteNumber) {
            return 0;
        }

        @Override
        public int getProgram() {
            return 0;
        }

        @Override
        public boolean getSolo() {
            return false;
        }

        @Override
        public void noteOff(int noteNumber, int velocity) {
            noteOffKey = noteNumber;
            noteOffVelocity = velocity;
        }

        @Override
        public void noteOn(int noteNumber, int velocity) {
        }

        @Override
        public void programChange(int program) {
            programChangeValue = program;
        }

        @Override
        public void setChannelPressure(int pressure) {
        }

        @Override
        public void setMute(boolean mute) {
        }

        @Override
        public void setPitchBend(int bend) {
        }

        @Override
        public void setPolyPressure(int noteNumber, int pressure) {
        }

        @Override
        public void setSolo(boolean solo) {
        }
    }
}
