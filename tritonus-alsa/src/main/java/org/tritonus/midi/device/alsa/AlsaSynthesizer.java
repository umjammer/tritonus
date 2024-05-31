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
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.VoiceStatus;

import org.tritonus.share.GlobalInfo;
import org.tritonus.share.midi.TMidiDevice;

import static java.lang.System.getLogger;


public class AlsaSynthesizer extends AlsaMidiDevice implements Synthesizer {

    private static final Logger logger = getLogger("org.tritonus.TraceAllExceptions");

    private static final MidiChannel[] EMPTY_MIDICHANNEL_ARRAY = new MidiChannel[0];
    private static final VoiceStatus[] EMPTY_VOICESTATUS_ARRAY = new VoiceStatus[0];

    private final List<MidiChannel> channels;
    private final int voices;

    public AlsaSynthesizer(int client, int port, int voices) {
        super(new TMidiDevice.Info(
                        "ALSA Synthesizer (" + client + ":" + port + ")",
                        GlobalInfo.getVendor(),
                        "Synthesizer based on the ALSA sequencer",
                        GlobalInfo.getVersion()),
                client, port, false, true);
        this.voices = voices;
        channels = new ArrayList<>();
    }

    @Override
    protected void openImpl() {
        super.openImpl();
//logger.log(Level.DEBUG, "AlsaSynthesizer.openImpl(): called");
        // necessary? thread-safe?
        channels.clear();
        Receiver receiver = null;
        try {
            receiver = this.getReceiver();
        } catch (MidiUnavailableException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        for (int i = 0; i < 16; i++) {
            MidiChannel channel = new AlsaMidiChannel(receiver, i);
            channels.add(channel);
        }
    }

    @Override
    protected void closeImpl() {
        super.closeImpl();
    }

    @Override
    public int getMaxPolyphony() {
        return voices;
    }

    @Override
    public long getLatency() {
        return -1L;
    }

    @Override
    public MidiChannel[] getChannels() {
        return channels.toArray(EMPTY_MIDICHANNEL_ARRAY);
    }

    @Override
    public VoiceStatus[] getVoiceStatus() {
        return EMPTY_VOICESTATUS_ARRAY;
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
    public boolean loadInstruments(Soundbank soundbank, Patch[] patches) {
        return false;
    }

    @Override
    public void unloadInstruments(Soundbank soundbank, Patch[] patches) {
    }
}

