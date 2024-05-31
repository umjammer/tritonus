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


/**
 * TDirectSynthesizerTestCase.
 */
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
        try (synth) {
            synth.open();
            TestSynthesizer.TestChannel[] channels = (TestSynthesizer.TestChannel[]) synth.getChannels();
            Receiver r = synth.getReceiver();
            checkPitchbend(channels, r, 0, 0);
            checkPitchbend(channels, r, 5, 127);
            checkPitchbend(channels, r, 7, 128);
            checkPitchbend(channels, r, 15, 16383);
        }
    }

    private static void checkPitchbend(TestSynthesizer.TestChannel[] channels, Receiver r, int channel, int bend)
            throws Exception {
        Type type = Type.PITCHBEND;
        ShortMessage shMsg = new ShortMessage();
        shMsg.setMessage(type.getCommand(), channel, bend & 0x7F, bend >> 7);
        resetResults(channels);
        r.send(shMsg, -1);
        checkResult(channels, channel, type, bend, -1);
    }

    /**
     * @param type if true, note on is tested. If false, note off is tested.
     */
    private static void checkMessage2(Type type) throws Exception {
        Synthesizer synth = new TestSynthesizer();
        try (synth) {
            synth.open();
            TestSynthesizer.TestChannel[] channels = (TestSynthesizer.TestChannel[]) synth.getChannels();
            Receiver r = synth.getReceiver();
            checkMessage(type, channels, r, 0, 17, 55);
            checkMessage(type, channels, r, 15, 0, 0);
            checkMessage(type, channels, r, 5, 127, 127);
        }
    }

    /**
     * @param type
     */
    private static void checkMessage1(Type type) throws Exception {
        Synthesizer synth = new TestSynthesizer();
        try (synth) {
            synth.open();
            TestSynthesizer.TestChannel[] channels = (TestSynthesizer.TestChannel[]) synth.getChannels();
            Receiver r = synth.getReceiver();
            checkMessage(type, channels, r, 0, 57, 0);
            checkMessage(type, channels, r, 15, 0, 0);
            checkMessage(type, channels, r, 5, 127, 0);
        }
    }

    private static void checkMessage(Type type, TestSynthesizer.TestChannel[] channels,
                                     Receiver r, int channel, int value1, int value2) throws Exception {
        ShortMessage shortMessage = new ShortMessage();
        shortMessage.setMessage(type.getCommand(), channel, value1, value2);
        resetResults(channels);
        r.send(shortMessage, -1);
        checkResult(channels, channel, type, value1, value2);
    }

    private static void resetResults(TestSynthesizer.TestChannel[] channels) {
        for (TestSynthesizer.TestChannel channel : channels) {
            channel.resetValues();
        }
    }

    private static void checkResult(TestSynthesizer.TestChannel[] channels,
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

        private final MidiChannel[] channels;

        public TestSynthesizer() {
            // no MidiDevice.Info
            super(null);
            channels = new TestChannel[16];
            for (int i = 0; i < 16; i++) {
                channels[i] = new TestChannel(i);
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
            return channels;
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

            private Type type;
            private int value1;
            private int value2;

            public TestChannel(int channel) {
            }

            public void resetValues() {
                type = Type.NONE;
                value1 = -1;
                value2 = -1;
            }

            public Type getType() {
                return type;
            }

            public int getValue1() {
                return value1;
            }

            public int getValue2() {
                return value2;
            }

            @Override
            public void allNotesOff() {
            }

            @Override
            public void allSoundOff() {
            }

            @Override
            public void controlChange(int controller, int value) {
                type = Type.CONTROL_CHANGE;
                value1 = controller;
                value2 = value;
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
            public boolean localControl(boolean on) {
                return false;
            }

            @Override
            public void noteOff(int noteNumber, int velocity) {
                type = Type.NOTEOFF;
                value1 = noteNumber;
                value2 = velocity;
            }

            @Override
            public void noteOff(int noteNumber) {
            }

            @Override
            public void noteOn(int noteNumber, int velocity) {
                type = Type.NOTEON;
                value1 = noteNumber;
                value2 = velocity;
            }

            @Override
            public void programChange(int bank, int program) {
                type = Type.BANK_PROGRAM;
                value1 = bank;
                value2 = program;
            }

            @Override
            public void programChange(int program) {
                type = Type.PROGRAM;
                value1 = program;
                value2 = 0;
            }

            @Override
            public void resetAllControllers() {
            }

            @Override
            public void setChannelPressure(int pressure) {
                type = Type.CHANNEL_PRESSURE;
                value1 = pressure;
                value2 = 0;
            }

            @Override
            public void setMono(boolean mono) {
            }

            @Override
            public void setMute(boolean mute) {
            }

            @Override
            public void setOmni(boolean omni) {
            }

            @Override
            public void setPitchBend(int bend) {
                type = Type.PITCHBEND;
                value1 = bend;
            }

            @Override
            public void setPolyPressure(int noteNumber, int pressure) {
                type = Type.POLY_PRESSURE;
                value1 = noteNumber;
                value2 = pressure;
            }

            @Override
            public void setSolo(boolean solo) {
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

        private final int command;

        Type() {
            this(0);
        }

        Type(int command) {
            this.command = command;
        }

        public int getCommand() {
            return command;
        }
    }
}
