
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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import org.tritonus.share.TDebug;
import org.tritonus.share.midi.MidiUtils;


// idea: put things that can implemented with "pure MIDI" into a base class TMidiChannel
public class AlsaMidiChannel
        implements MidiChannel {

    private Receiver m_receiver;
    private int m_nChannel;

    public AlsaMidiChannel(Receiver receiver, int nChannel) {
        m_receiver = receiver;
        m_nChannel = nChannel;
    }

    protected int getChannel() {
        return m_nChannel;
    }

    protected void sendMessage(MidiMessage message) {
        m_receiver.send(message, -1);
    }

    @Override
    public void noteOn(int nNoteNumber, int nVelocity) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.NOTE_ON, getChannel(), nNoteNumber, nVelocity);
        } catch (InvalidMidiDataException e) {
            if (TDebug.TraceAlsaMidiChannel || TDebug.TraceAllExceptions) {
                TDebug.out(e);
            }
        }
        sendMessage(message);
    }

    @Override
    public void noteOff(int nNoteNumber, int nVelocity) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.NOTE_OFF, getChannel(), nNoteNumber, nVelocity);
        } catch (InvalidMidiDataException e) {
            if (TDebug.TraceAlsaMidiChannel || TDebug.TraceAllExceptions) {
                TDebug.out(e);
            }
        }
        sendMessage(message);
    }

    @Override
    public void noteOff(int nNoteNumber) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.NOTE_OFF, getChannel(), nNoteNumber, 0);
        } catch (InvalidMidiDataException e) {
            if (TDebug.TraceAlsaMidiChannel || TDebug.TraceAllExceptions) {
                TDebug.out(e);
            }
        }
        sendMessage(message);
    }

    @Override
    public void setPolyPressure(int nNoteNumber, int nPressure) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.POLY_PRESSURE, nPressure, 0);
        } catch (InvalidMidiDataException e) {
            if (TDebug.TraceAlsaMidiChannel || TDebug.TraceAllExceptions) {
                TDebug.out(e);
            }
        }
        sendMessage(message);
    }

    @Override
    public int getPolyPressure(int nNoteNumber) {
        return -1;
    }

    @Override
    public void setChannelPressure(int nPressure) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.CHANNEL_PRESSURE, getChannel(), nPressure, 0);
        } catch (InvalidMidiDataException e) {
            if (TDebug.TraceAlsaMidiChannel || TDebug.TraceAllExceptions) {
                TDebug.out(e);
            }
        }
        sendMessage(message);
    }

    @Override
    public int getChannelPressure() {
        return -1;
    }

    @Override
    public void controlChange(int nController, int nValue) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.CONTROL_CHANGE, getChannel(), nController, nValue);
        } catch (InvalidMidiDataException e) {
            if (TDebug.TraceAlsaMidiChannel || TDebug.TraceAllExceptions) {
                TDebug.out(e);
            }
        }
        sendMessage(message);
    }

    @Override
    public int getController(int nController) {
        return -1;
    }

    @Override
    public void programChange(int nProgram) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.PROGRAM_CHANGE, getChannel(), nProgram, 0);
        } catch (InvalidMidiDataException e) {
            if (TDebug.TraceAlsaMidiChannel || TDebug.TraceAllExceptions) {
                TDebug.out(e);
            }
        }
        sendMessage(message);
    }

    @Override
    public void programChange(int nBank, int nProgram) {
        ShortMessage message = new ShortMessage();
        try {
            // TODO: what about the bank?
            message.setMessage(ShortMessage.PROGRAM_CHANGE, getChannel(), nProgram, 0);
        } catch (InvalidMidiDataException e) {
            if (TDebug.TraceAlsaMidiChannel || TDebug.TraceAllExceptions) {
                TDebug.out(e);
            }
        }
        sendMessage(message);
    }

    @Override
    public int getProgram() {
        return -1;
    }

    @Override
    public void setPitchBend(int nBend) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.PITCH_BEND, MidiUtils.get14bitLSB(nBend), MidiUtils.get14bitMSB(nBend));
        } catch (InvalidMidiDataException e) {
            if (TDebug.TraceAlsaMidiChannel || TDebug.TraceAllExceptions) {
                TDebug.out(e);
            }
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
    public boolean localControl(boolean bOn) {
        return false;
    }

    @Override
    public void setMono(boolean bMono) {
    }

    @Override
    public boolean getMono() {
        return false;
    }

    @Override
    public void setOmni(boolean bOmni) {
    }

    @Override
    public boolean getOmni() {
        return false;
    }

    @Override
    public void setMute(boolean bMute) {
    }

    @Override
    public boolean getMute() {
        return false;
    }

    @Override
    public void setSolo(boolean bSolo) {
    }

    @Override
    public boolean getSolo() {
        return false;
    }
}



