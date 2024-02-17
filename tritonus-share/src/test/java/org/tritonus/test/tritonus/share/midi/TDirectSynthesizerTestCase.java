/*
 * TDirectSynthesizerTestCase.java
 */

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

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.VoiceStatus;

import org.junit.jupiter.api.Test;
import org.tritonus.share.midi.TDirectSynthesizer;

import static org.junit.jupiter.api.Assertions.assertEquals;


class TDirectSynthesizerTestCase {

    @Test
    public void testNoteOn() throws Exception {
        checkMessage2(Type.NOTEON);
    }

    @Test
    public void testNoteOff() throws Exception {
        checkMessage2(Type.NOTEOFF);
    }

    @Test
    public void testPolyPressure() throws Exception {
        checkMessage2(Type.POLY_PRESSURE);
    }

    @Test
    public void testControlChange() throws Exception {
        checkMessage2(Type.CONTROL_CHANGE);
    }

    @Test
    public void testProgramChange() throws Exception {
        checkMessage1(Type.PROGRAM);
    }

    @Test
    public void testChannelPressure() throws Exception {
        checkMessage1(Type.CHANNEL_PRESSURE);
    }

    @Test
    public void testPitchbend() throws Exception {
        Synthesizer synth = new TestSynthesizer();
        synth.open();
        TestSynthesizer.TestChannel[] channels = (TestSynthesizer.TestChannel[]) synth.getChannels();
        try {
            Receiver r = synth.getReceiver();
            checkPitchbend(channels, r, 0, 0);
            checkPitchbend(channels, r, 5, 127);
            checkPitchbend(channels, r, 7, 128);
            checkPitchbend(channels, r, 15, 16383);
        } finally {
            synth.close();
        }
    }

    private void checkPitchbend(TestSynthesizer.TestChannel[] channels, Receiver r, int nChannel, int nBend)
            throws Exception {
        Type type = Type.PITCHBEND;
        ShortMessage shMsg = new ShortMessage();
        shMsg.setMessage(type.getCommand(), nChannel, nBend & 0x7F, nBend >> 7);
        resetResults(channels);
        r.send(shMsg, -1);
        checkResult(channels, nChannel, type, nBend, -1);
    }

    /**
     * @param type if true, note on is tested. If false, note off is tested.
     * @throws Exception
     */
    private void checkMessage2(Type type) throws Exception {
        Synthesizer synth = new TestSynthesizer();
        synth.open();
        TestSynthesizer.TestChannel[] channels = (TestSynthesizer.TestChannel[]) synth.getChannels();
        try {
            Receiver r = synth.getReceiver();
            checkMessage(type, channels, r, 0, 17, 55);
            checkMessage(type, channels, r, 15, 0, 0);
            checkMessage(type, channels, r, 5, 127, 127);
        } finally {
            synth.close();
        }
    }

    /**
     * @param type
     * @throws Exception
     */
    private void checkMessage1(Type type) throws Exception {
        Synthesizer synth = new TestSynthesizer();
        synth.open();
        TestSynthesizer.TestChannel[] channels = (TestSynthesizer.TestChannel[]) synth.getChannels();
        try {
            Receiver r = synth.getReceiver();
            checkMessage(type, channels, r, 0, 57, 0);
            checkMessage(type, channels, r, 15, 0, 0);
            checkMessage(type, channels, r, 5, 127, 0);
        } finally {
            synth.close();
        }
    }

    private void checkMessage(Type type,
                              TestSynthesizer.TestChannel[] channels,
                              Receiver r, int nChannel, int nValue1, int nValue2) throws Exception {
        ShortMessage shMsg = new ShortMessage();
        shMsg.setMessage(type.getCommand(), nChannel, nValue1, nValue2);
        resetResults(channels);
        r.send(shMsg, -1);
        checkResult(channels, nChannel, type, nValue1, nValue2);
    }

    private void resetResults(TestSynthesizer.TestChannel[] channels) {
        for (TestSynthesizer.TestChannel channel : channels) {
            channel.resetValues();
        }
    }

    private void checkResult(TestSynthesizer.TestChannel[] channels,
                             int channel, Type type, int value1, int value2) {
        for (int i = 0; i < channels.length; i++) {
            TestSynthesizer.TestChannel ch = channels[i];
            if (i == channel) {
                assertEquals(type, ch.getType(), "affected channel: type");
                assertEquals(value1, ch.getValue1(), "affected channel: value1");
                assertEquals(value2, ch.getValue2(), "affected channel: value2");
            } else {
                assertEquals(Type.NONE, ch.getType(), "unaffected channel: type");
                assertEquals(-1, ch.getValue1(), "unaffected channel: value1");
                assertEquals(-1, ch.getValue2(), "unaffected channel: value2");
            }
        }
    }

    private static class TestSynthesizer extends TDirectSynthesizer {

        private MidiChannel[] m_channels;

        public TestSynthesizer() {
            // no MidiDevice.Info
            super(null);
            m_channels = new TestChannel[16];
            for (int i = 0; i < 16; i++) {
                m_channels[i] = new TestChannel(i);
            }
        }

        @Override
        public int getMaxPolyphony() {
            return 16;
        }

        @Override
        public long getLatency() {
            return 0;
        }

        @Override
        public MidiChannel[] getChannels() {
            return m_channels;
        }

        @Override
        public VoiceStatus[] getVoiceStatus() {
            return null;
        }

        @Override
        public boolean isSoundbankSupported(Soundbank soundbank) {
            return false;
        }

        @Override
        public boolean loadInstrument(Instrument instrument) {
            return false;
        }

        @Override
        public void unloadInstrument(Instrument instrument) {
        }

        @Override
        public boolean remapInstrument(Instrument from, Instrument to) {
            return false;
        }

        @Override
        public Soundbank getDefaultSoundbank() {
            return null;
        }

        @Override
        public Instrument[] getAvailableInstruments() {
            return null;
        }

        @Override
        public Instrument[] getLoadedInstruments() {
            return null;
        }

        @Override
        public boolean loadAllInstruments(Soundbank soundbank) {
            return false;
        }

        @Override
        public void unloadAllInstruments(Soundbank soundbank) {
        }

        @Override
        public boolean loadInstruments(Soundbank soundbank, Patch[] patchList) {
            return false;
        }

        @Override
        public void unloadInstruments(Soundbank soundbank, Patch[] patchList) {
        }

        public static class TestChannel implements MidiChannel {

            private Type m_nType;
            private int m_nValue1;
            private int m_nValue2;

            public TestChannel(int nChannel) {
            }

            public void resetValues() {
                m_nType = Type.NONE;
                m_nValue1 = -1;
                m_nValue2 = -1;
            }

            public Type getType() {
                return m_nType;
            }

            public int getValue1() {
                return m_nValue1;
            }

            public int getValue2() {
                return m_nValue2;
            }

            @Override
            public void allNotesOff() {
            }

            @Override
            public void allSoundOff() {
            }

            @Override
            public void controlChange(int nController, int nValue) {
                m_nType = Type.CONTROL_CHANGE;
                m_nValue1 = nController;
                m_nValue2 = nValue;
            }

            @Override
            public int getChannelPressure() {
                return 0;
            }

            @Override
            public int getController(int nController) {
                return 0;
            }

            @Override
            public boolean getMono() {
                return false;
            }

            @Override
            public boolean getMute() {
                return false;
            }

            @Override
            public boolean getOmni() {
                return false;
            }

            @Override
            public int getPitchBend() {
                return 0;
            }

            @Override
            public int getPolyPressure(int nNoteNumber) {
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
            public boolean localControl(boolean bOn) {
                return false;
            }

            @Override
            public void noteOff(int nNoteNumber, int nVelocity) {
                m_nType = Type.NOTEOFF;
                m_nValue1 = nNoteNumber;
                m_nValue2 = nVelocity;
            }

            @Override
            public void noteOff(int nNoteNumber) {
            }

            @Override
            public void noteOn(int nNoteNumber, int nVelocity) {
                m_nType = Type.NOTEON;
                m_nValue1 = nNoteNumber;
                m_nValue2 = nVelocity;
            }

            @Override
            public void programChange(int nBank, int nProgram) {
                m_nType = Type.BANK_PROGRAM;
                m_nValue1 = nBank;
                m_nValue2 = nProgram;
            }

            @Override
            public void programChange(int nProgram) {
                m_nType = Type.PROGRAM;
                m_nValue1 = nProgram;
                m_nValue2 = 0;
            }

            @Override
            public void resetAllControllers() {
            }

            @Override
            public void setChannelPressure(int nPressure) {
                m_nType = Type.CHANNEL_PRESSURE;
                m_nValue1 = nPressure;
                m_nValue2 = 0;
            }

            @Override
            public void setMono(boolean bMono) {
            }

            @Override
            public void setMute(boolean bMute) {
            }

            @Override
            public void setOmni(boolean bOmni) {
            }

            @Override
            public void setPitchBend(int nBend) {
                m_nType = Type.PITCHBEND;
                m_nValue1 = nBend;
            }

            @Override
            public void setPolyPressure(int nNoteNumber, int nPressure) {
                m_nType = Type.POLY_PRESSURE;
                m_nValue1 = nNoteNumber;
                m_nValue2 = nPressure;
            }

            @Override
            public void setSolo(boolean bSolo) {
            }
        }
    }

    public enum Type {
        NONE,
        CONTROL_CHANGE(ShortMessage.CONTROL_CHANGE),
        NOTEON(ShortMessage.NOTE_ON),
        NOTEOFF(ShortMessage.NOTE_OFF),
        PROGRAM(ShortMessage.PROGRAM_CHANGE),
        BANK_PROGRAM(ShortMessage.PROGRAM_CHANGE),
        PITCHBEND(ShortMessage.PITCH_BEND),
        POLY_PRESSURE(ShortMessage.POLY_PRESSURE),
        CHANNEL_PRESSURE(ShortMessage.CHANNEL_PRESSURE);

        private final int m_nCommand;

        Type() {
            this(0);
        }

        Type(int nCommand) {
            m_nCommand = nCommand;
        }

        public int getCommand() {
            return m_nCommand;
        }
    }
}


