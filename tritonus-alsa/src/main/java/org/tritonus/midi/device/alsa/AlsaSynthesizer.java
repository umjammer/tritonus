
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
import org.tritonus.share.TDebug;
import org.tritonus.share.midi.TMidiDevice;


public class AlsaSynthesizer
        extends AlsaMidiDevice
        implements Synthesizer {

    private static final MidiChannel[] EMPTY_MIDICHANNEL_ARRAY = new MidiChannel[0];
    private static final VoiceStatus[] EMPTY_VOICESTATUS_ARRAY = new VoiceStatus[0];

    private List<MidiChannel> m_channels;
    private int m_nVoices;

    public AlsaSynthesizer(int nClient, int nPort, int nVoices) {
        super(
                new TMidiDevice.Info(
                        "ALSA Synthesizer (" + nClient + ":" + nPort + ")",
                        GlobalInfo.getVendor(),
                        "Synthesizer based on the ALSA sequencer",
                        GlobalInfo.getVersion()),
                nClient, nPort, false, true);
        m_nVoices = nVoices;
        m_channels = new ArrayList<>();
    }

    @Override
    protected void openImpl() {
        super.openImpl();
        // TDebug.out("AlsaSynthesizer.openImpl(): called");
        // necessary? thread-safe?
        m_channels.clear();
        Receiver receiver = null;
        try {
            receiver = this.getReceiver();
        } catch (MidiUnavailableException e) {
            if (TDebug.TraceAllExceptions) {
                TDebug.out(e);
            }
        }
        for (int i = 0; i < 16; i++) {
            MidiChannel channel = new AlsaMidiChannel(
                    receiver, i);
            m_channels.add(channel);
        }
    }

    @Override
    protected void closeImpl() {
        super.closeImpl();
    }

    @Override
    public int getMaxPolyphony() {
        return m_nVoices;
    }

    @Override
    public long getLatency() {
        return -1L;
    }

    @Override
    public MidiChannel[] getChannels() {
        return m_channels.toArray(EMPTY_MIDICHANNEL_ARRAY);
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
    public boolean loadInstruments(Soundbank soundbank, Patch[] aPatches) {
        return false;
    }

    @Override
    public void unloadInstruments(Soundbank soundbank, Patch[] aPatches) {
    }


}



