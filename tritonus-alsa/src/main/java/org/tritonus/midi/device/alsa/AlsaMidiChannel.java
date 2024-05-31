/*
 *  Copyright (c) 1999 by Matthias Pfisterer
 *
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
 *
 */

package org.tritonus.midi.device.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.tritonus.share.midi.MidiUtils;

import static java.lang.System.getLogger;


// idea: put things that can implemented with "pure MIDI" into a base class TMidiChannel
public class AlsaMidiChannel implements MidiChannel {

    private static final Logger logger = getLogger("org.tritonus.TraceAlsaMidiChannel");

    private final Receiver receiver;
    private final int channel;

    public AlsaMidiChannel(Receiver receiver, int channel) {
        this.receiver = receiver;
        this.channel = channel;
    }

    protected int getChannel() {
        return channel;
    }

    protected void sendMessage(MidiMessage message) {
        receiver.send(message, -1);
    }

    @Override
    public void noteOn(int noteNumber, int velocity) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.NOTE_ON, getChannel(), noteNumber, velocity);
        } catch (InvalidMidiDataException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        sendMessage(message);
    }

    @Override
    public void noteOff(int noteNumber, int velocity) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.NOTE_OFF, getChannel(), noteNumber, velocity);
        } catch (InvalidMidiDataException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        sendMessage(message);
    }

    @Override
    public void noteOff(int noteNumber) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.NOTE_OFF, getChannel(), noteNumber, 0);
        } catch (InvalidMidiDataException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        sendMessage(message);
    }

    @Override
    public void setPolyPressure(int noteNumber, int pressure) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.POLY_PRESSURE, pressure, 0);
        } catch (InvalidMidiDataException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        sendMessage(message);
    }

    @Override
    public int getPolyPressure(int noteNumber) {
        return -1;
    }

    @Override
    public void setChannelPressure(int pressure) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.CHANNEL_PRESSURE, getChannel(), pressure, 0);
        } catch (InvalidMidiDataException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        sendMessage(message);
    }

    @Override
    public int getChannelPressure() {
        return -1;
    }

    @Override
    public void controlChange(int controller, int value) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.CONTROL_CHANGE, getChannel(), controller, value);
        } catch (InvalidMidiDataException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        sendMessage(message);
    }

    @Override
    public int getController(int controller) {
        return -1;
    }

    @Override
    public void programChange(int program) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.PROGRAM_CHANGE, getChannel(), program, 0);
        } catch (InvalidMidiDataException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        sendMessage(message);
    }

    @Override
    public void programChange(int bank, int program) {
        ShortMessage message = new ShortMessage();
        try {
            // TODO what about the bank?
            message.setMessage(ShortMessage.PROGRAM_CHANGE, getChannel(), program, 0);
        } catch (InvalidMidiDataException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        sendMessage(message);
    }

    @Override
    public int getProgram() {
        return -1;
    }

    @Override
    public void setPitchBend(int bend) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.PITCH_BEND, MidiUtils.get14bitLSB(bend), MidiUtils.get14bitMSB(bend));
        } catch (InvalidMidiDataException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        sendMessage(message);
    }

    @Override
    public int getPitchBend() {
        return -1;
    }

    @Override
    public void resetAllControllers() {
    }

    @Override
    public void allNotesOff() {
    }

    @Override
    public void allSoundOff() {
    }

    @Override
    public boolean localControl(boolean on) {
        return false;
    }

    @Override
    public void setMono(boolean mono) {
    }

    @Override
    public boolean getMono() {
        return false;
    }

    @Override
    public void setOmni(boolean omni) {
    }

    @Override
    public boolean getOmni() {
        return false;
    }

    @Override
    public void setMute(boolean mute) {
    }

    @Override
    public boolean getMute() {
        return false;
    }

    @Override
    public void setSolo(boolean solo) {
    }

    @Override
    public boolean getSolo() {
        return false;
    }
}
