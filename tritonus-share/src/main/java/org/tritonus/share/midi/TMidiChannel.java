/*
 *  Copyright (c) 2006 by Matthias Pfisterer
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

package org.tritonus.share.midi;

import javax.sound.midi.MidiChannel;


/**
 * Base class for MidiChannel implementations.
 *
 * <p>This base class serves two purposes:</p>
 *
 * <ol>
 * <li>It contains a channel number property so that the MidiChannel
 * object knows its own MIDI channel number.</li>
 *
 * <li>It maps some of the methods to others.</li>
 * </ol>
 *
 * @author Matthias Pfisterer
 */
public abstract class TMidiChannel implements MidiChannel {

    private final int channel;

    protected TMidiChannel(int channel) {
        this.channel = channel;
    }

    protected int getChannel() {
        return channel;
    }

    @Override
    public void noteOff(int noteNumber) {
        noteOff(noteNumber, 0);
    }

    @Override
    public void programChange(int bank, int program) {
        int bankMSB = bank >> 7;
        int bankLSB = bank & 0x7F;
        controlChange(0, bankMSB);
        controlChange(32, bankLSB);
        programChange(program);
    }

    @Override
    public void resetAllControllers() {
        controlChange(121, 0);
    }

    @Override
    public void allNotesOff() {
        controlChange(123, 0);
    }

    @Override
    public void allSoundOff() {
        controlChange(120, 0);
    }

    @Override
    public boolean localControl(boolean on) {
        controlChange(122, on ? 127 : 0);
        return getController(122) >= 64;
    }

    @Override
    public void setMono(boolean mono) {
        // TODO check this
        controlChange(mono ? 126 : 127, 0);
    }

    @Override
    public boolean getMono() {
        // TODO check this
        return getController(126) == 0;
    }

    @Override
    public void setOmni(boolean omni) {
        controlChange(omni ? 125 : 124, 0);
    }

    @Override
    public boolean getOmni() {
        // TODO check this
        return getController(125) == 0;
    }
}
