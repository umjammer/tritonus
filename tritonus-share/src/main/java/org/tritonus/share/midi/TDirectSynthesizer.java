/*
 *  Copyright (c) 2004 by Matthias Pfisterer
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
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;


/**
 * Base class for Synthesizer implementations.
 *
 * <p>
 * This base class is for Synthesizer implementations that do not itself operate
 * on MIDI, but instread implement the MidiChannel interface. For these
 * implementations, MIDI behaviour is simulated on top of MidiChannel.
 * </p>
 *
 * @author Matthias Pfisterer
 * @see javax.sound.midi.MidiChannel
 */
public abstract class TDirectSynthesizer extends TMidiDevice implements Synthesizer {

    /**
     * Initialize this class.
     * This sets the info from the passed one, sets the open status
     * to false, the number of Receivers to zero and the collection
     * of Transmitters to be empty.
     *
     * @param info The info object that describes this instance.
     */
    public TDirectSynthesizer(MidiDevice.Info info) {
        // no Transmitters, only Receivers
        super(info, false, true);
    }

    /**
     * Obtains the MidiChannel with the specified number.
     *
     * @param channel the requested channel number (0..15)
     * @return the respective <code>MidiChannel</code> object
     */
    private MidiChannel getChannel(int channel) {
        return getChannels()[channel];
    }

    /**
     * Handles MIDI messages coming in from Receivers.
     */
    @Override
    protected void receive(MidiMessage message, long timeStamp) {
        if (message instanceof ShortMessage shortMessage) {
            int channel = shortMessage.getChannel();
            int command = shortMessage.getCommand();
            int data1 = shortMessage.getData1();
            int data2 = shortMessage.getData2();
            switch (command) {
            case ShortMessage.NOTE_OFF:
                getChannel(channel).noteOff(data1, data2);
                break;

            case ShortMessage.NOTE_ON:
                getChannel(channel).noteOn(data1, data2);
                break;

            case ShortMessage.POLY_PRESSURE:
                getChannel(channel).setPolyPressure(data1, data2);
                break;

            case ShortMessage.CONTROL_CHANGE:
                getChannel(channel).controlChange(data1, data2);
                break;

            case ShortMessage.PROGRAM_CHANGE:
                getChannel(channel).programChange(data1);
                break;

            case ShortMessage.CHANNEL_PRESSURE:
                getChannel(channel).setChannelPressure(data1);
                break;

            case ShortMessage.PITCH_BEND:
                getChannel(channel).setPitchBend(data1 | (data2 << 7));
                break;

            default:
            }
        }
    }
}
