/*
 * Copyright (c) 2006 by Henri Manson
 * Copyright (c) 2006 by Matthias Pfisterer
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.tritonus.midi.device.fluidsynth;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.VoiceStatus;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.tritonus.midi.sb.fluidsynth.FluidSoundbank;
import org.tritonus.share.midi.TDirectSynthesizer;
import org.tritonus.share.midi.TMidiChannel;
import vavi.sound.midi.fluidsynth.jna.audio.AudioLibrary;
import vavi.sound.midi.fluidsynth.jna.settings.SettingsLibrary;
import vavi.sound.midi.fluidsynth.jna.synth.SynthLibrary;

import static java.lang.System.getLogger;


/*
 * FluidSynthesizer.java
 *
 * This file is part of Tritonus: http://www.tritonus.org/
 */
public class FluidSynthesizer extends TDirectSynthesizer implements Synthesizer {

    private static final Logger logger = getLogger("org.tritonus.TraceSynthesizer");

    private MidiChannel[] channels;
    private FluidSoundbank defaultSoundbank;

    private int defaultbankSfontID;

    // native pointers
    private PointerByReference /* fluid_settings_t */ settings;
    private PointerByReference /* fluid_synth_t */ synth;
    private PointerByReference /* fluid_audio_driver_t */ audioDriver;

    public PointerByReference getSynthesizer() {
        return synth;
    }

    /**
     * Constructor.
     */
    public FluidSynthesizer(MidiDevice.Info info) throws Exception {
        super(info);
    }

    @Override
    protected void openImpl() throws MidiUnavailableException {
        newSynth();

        logger.log(Level.TRACE, "FluidSynthesizer: " + Long.toHexString(Pointer.nativeValue(synth.getValue())));

        channels = new MidiChannel[16];
        for (int i = 0; i < 16; i++) {
            channels[i] = new NewFluidMidiChannel(i);
        }

        String sfontFile = System.getProperty("tritonus.fluidsynth.defaultsoundbank");
        if (sfontFile != null && !sfontFile.isEmpty()) {
            int sfontID = loadSoundFont(sfontFile);
            setDefaultSoundBank(sfontID);
            String strBankOffset = System.getProperty("tritonus.fluidsynth.defaultsoundbankoffset");
            if (strBankOffset != null && !strBankOffset.isEmpty()) {
                setBankOffset(sfontID, Integer.parseInt(strBankOffset));
            }
        }
    }

    @Override
    protected void closeImpl() {
        logger.log(Level.TRACE, "FluidSynthesizer.closeImpl(): " + Long.toHexString(Pointer.nativeValue(synth.getValue())));

        deleteSynth();
        super.closeImpl();
    }

    public void setDefaultSoundBank(int sfontID) {
        defaultSoundbank = new FluidSoundbank(this, sfontID);
        defaultbankSfontID = sfontID;
    }

    public int loadSoundFont(String filename) {
        int sfont_id;
        if (synth == null) {
            sfont_id = -1;
        } else {
            sfont_id = SynthLibrary.INSTANCE.fluid_synth_sfload(synth, filename, 1);
        }

        return sfont_id;
    }

    public void setBankOffset(int sfontID, int offset) {
        SynthLibrary.INSTANCE.fluid_synth_set_bank_offset(synth, sfontID, offset);
    }

    public void setGain(float gain) {
        SynthLibrary.INSTANCE.fluid_synth_set_gain(synth, gain);
    }

    /**
     * $$mp: currently not functional because fluid_synth_set_reverb_preset()
     * is not present in fluidsynth 1.0.6.
     */
    public void setReverbPreset(int reverbPreset) {
        // $$mp: currently not functional because fluid_synth_set_reverb_preset() is not
        // present in fluidsynth 1.0.6
//        fluid_synth_set_reverb_preset(synth, (int) reverbPreset);
    }

    @Override
    public int getMaxPolyphony() {
        return SynthLibrary.INSTANCE.fluid_synth_get_polyphony(synth);
    }

    protected void newSynth() throws MidiUnavailableException {
        if (synth == null) {
            this.settings = SettingsLibrary.INSTANCE.new_fluid_settings();
            if (settings == null) {
                fluid_jni_delete_synth();
                throw new MidiUnavailableException("Low-level initialization of the settings failed");
            }

            this.synth = SynthLibrary.INSTANCE.new_fluid_synth(settings);
            if (synth == null) {
                fluid_jni_delete_synth();
                throw new MidiUnavailableException("Low-level initialization of the synthesizer failed");
            }

            logger.log(Level.TRACE, "newSynth: synth: " + synth);

            this.audioDriver = AudioLibrary.INSTANCE.new_fluid_audio_driver(settings, synth);
            if (audioDriver == null) {
                fluid_jni_delete_synth();
                throw new MidiUnavailableException("Low-level initialization of the audioDriver failed");
            }
        }
    }

    protected void deleteSynth() {
        logger.log(Level.TRACE, "deleteSynth: synth: " + synth);

        fluid_jni_delete_synth();
    }

    void fluid_jni_delete_synth() {
        if (audioDriver != null) {
            AudioLibrary.INSTANCE.delete_fluid_audio_driver(audioDriver);
            this.audioDriver = null;
        }
        if (synth != null) {
            SynthLibrary.INSTANCE.delete_fluid_synth(synth);
            this.synth = null;
        }
        if (settings != null) {
            SettingsLibrary.INSTANCE.delete_fluid_settings(settings);
            this.settings = null;
        }
    }

    /**
     * Turns a note on.
     * <p>
     * The implementation calls fluid_synth_noteoff().
     *
     * @param nChannel    the channel
     * @param nNoteNumber the note
     * @param nVelocity   the velocity
     */
    void noteOn(int nChannel, int nNoteNumber, int nVelocity) {
        if (synth != null) {
            SynthLibrary.INSTANCE.fluid_synth_noteon(synth, nChannel, nNoteNumber, nVelocity);
        }
    }

    /**
     * Turns a note off.
     * <p>
     * The implementation calls fluid_synth_noteon().
     *
     * @param nChannel    the channel
     * @param nNoteNumber the note
     * @param nVelocity   the velocity
     */
    void noteOff(int nChannel, int nNoteNumber, int nVelocity) {
        if (synth != null) {
            // There is no method noteoff that takes a velocity param.
//            fluid_synth_noteoff(synth, channel, key, velocity);
            SynthLibrary.INSTANCE.fluid_synth_noteoff(synth, nChannel, nNoteNumber);
        }
    }

    /**
     * Changes a controller on the synthesizer.
     * <p>
     * The implementation calls fluid_synth_cc().
     *
     * @param nChannel    the channel
     * @param nController the controller number
     * @param nValue      the controller value
     */
    void controlChange(int nChannel, int nController, int nValue) {
        if (synth != null) {
            SynthLibrary.INSTANCE.fluid_synth_cc(synth, nChannel, nController, nValue);
        }
    }

    /**
     * Obtains the value of a controller.
     * <p>
     * The implementation calls fluid_synth_get_cc().
     *
     * @param nChannel    the channel
     * @param nController the controller number
     * @return the controller value
     */
    int getController(int nChannel, int nController) {
        IntByReference value = new IntByReference();
        if (synth != null) {
            SynthLibrary.INSTANCE.fluid_synth_get_cc(synth, nChannel, nController, value);
        }
        return value.getValue();
    }

    /**
     * Sets the program for a channel.
     * <p>
     * The implementation calls fluid_synth_program_change().
     *
     * @param nChannel the channel
     * @param nProgram the program number
     */
    void programChange(int nChannel, int nProgram) {
        if (synth != null) {
            SynthLibrary.INSTANCE.fluid_synth_program_change(synth, nChannel, nProgram);
        }
    }

    /**
     * Obtains the program set for a channel.
     * <p>
     * The implementation calls fluid_synth_get_program().
     *
     * @param nChannel the channel
     * @return the program number set for this channel
     */
    int getProgram(int nChannel) {
        IntByReference sfont = new IntByReference();
        IntByReference bank = new IntByReference();
        IntByReference program = new IntByReference();
        if (synth != null) {
            SynthLibrary.INSTANCE.fluid_synth_get_program(synth, nChannel, sfont, bank, program);
        }
        return program.getValue();
    }

    /**
     * Sets the pitch bend for a channel.
     * <p>
     * The implementation calls fluid_synth_pitch_bend().
     *
     * @param nChannel the channel
     * @param nBend    the pitch bend value
     */
    void setPitchBend(int nChannel, int nBend) {
        if (synth != null) {
            SynthLibrary.INSTANCE.fluid_synth_pitch_bend(synth, nChannel, nBend);
        }
    }

    /**
     * Obtains the pitch bend for a channel.
     * <p>
     * The implementations calls fluid_synth_get_pitch_bend().
     *
     * @param nChannel the channel
     * @return the pitch bend value.
     */
    int getPitchBend(int nChannel) {
        IntByReference bend = new IntByReference();
        if (synth != null) {
            SynthLibrary.INSTANCE.fluid_synth_get_pitch_bend(synth, nChannel, bend);
        }
        return bend.getValue();
    }

    @Override
    public boolean isSoundbankSupported(Soundbank soundbank) {
        return (soundbank instanceof FluidSoundbank);
    }

    @Override
    public boolean loadAllInstruments(Soundbank soundbank) {
        checkSoundbank(soundbank);
        return true;
    }

    @Override
    public void unloadAllInstruments(Soundbank soundbank) {
        checkSoundbank(soundbank);
    }

    @Override
    public void unloadInstruments(Soundbank soundbank, Patch[] patchList) {
        checkSoundbank(soundbank);
    }

    @Override
    public boolean loadInstruments(Soundbank soundbank, Patch[] patchList) {
        checkSoundbank(soundbank);
        return true;
    }

    @Override
    public void unloadInstrument(Instrument instrument) {
        checkInstrument(instrument);
    }

    @Override
    public boolean loadInstrument(Instrument instrument) {
        checkInstrument(instrument);
        return true;
    }

    @Override
    public Instrument[] getAvailableInstruments() {
        return null;
    }

    @Override
    public MidiChannel[] getChannels() {
        return channels;
    }

    @Override
    public Soundbank getDefaultSoundbank() {
        return defaultSoundbank;
    }

    @Override
    public long getLatency() {
        return 0L;
    }

    @Override
    public Instrument[] getLoadedInstruments() {
        return null;
    }

    @Override
    public VoiceStatus[] getVoiceStatus() {
        return new VoiceStatus[0];
    }

    @Override
    public boolean remapInstrument(Instrument from, Instrument to) {
        checkInstrument(from);
        checkInstrument(to);
        return true;
    }

    /**
     * Checks if the soundbank is supported by this synthesizer implementation.
     *
     * @param sb the soundbank to check
     * @throws IllegalArgumentException if the soundbank is not supported
     */
    private void checkSoundbank(Soundbank sb) {
        if (!isSoundbankSupported(sb))
            throw new IllegalArgumentException("soundbank is not supported");
    }

    /**
     * Checks if the instrument belongs to a soundbank that is supported by this
     * synthesizer implementation.
     *
     * @param instr the instrument to check
     * @throws IllegalArgumentException if the instrument's soundbank
     *                                  is not supported
     */
    private void checkInstrument(Instrument instr) {
        checkSoundbank(instr.getSoundbank());
    }

    private class NewFluidMidiChannel extends TMidiChannel {

        public NewFluidMidiChannel(int nChannel) {
            super(nChannel);
        }

        @Override
        public void noteOn(int nNoteNumber, int nVelocity) {
            FluidSynthesizer.this.noteOn(getChannel(), nNoteNumber, nVelocity);
        }

        @Override
        public void noteOff(int nNoteNumber, int nVelocity) {
            FluidSynthesizer.this.noteOff(getChannel(), nNoteNumber, nVelocity);
        }

        @Override
        public void noteOff(int nNoteNumber) {
            noteOff(nNoteNumber, 0);
        }

        /**
         * Fluidsynth does not implement poly pressure (aftertouch). Therefore,
         * this method does nothing.
         */
        @Override
        public void setPolyPressure(int nNoteNumber, int nPressure) {
        }

        /**
         * Fluidsynth does not implement poly pressure (aftertouch). Therefore,
         * this method always return 0.
         */
        @Override
        public int getPolyPressure(int nNoteNumber) {
            return 0;
        }

        /**
         * Fluidsynth does not implement channel pressure. Therefore,
         * this method does nothing.
         */
        @Override
        public void setChannelPressure(int nPressure) {
        }

        /**
         * Fluidsynth does not implement channel pressure. Therefore,
         * this method always returns 0.
         */
        @Override
        public int getChannelPressure() {
            return 0;
        }

        @Override
        public void controlChange(int nController, int nValue) {
            FluidSynthesizer.this.controlChange(getChannel(), nController, nValue);
        }

        @Override
        public int getController(int nController) {
            return FluidSynthesizer.this.getController(getChannel(), nController);
        }

        @Override
        public void programChange(int nProgram) {
            FluidSynthesizer.this.programChange(getChannel(), nProgram);
        }

        @Override
        public int getProgram() {
            return FluidSynthesizer.this.getProgram(getChannel());
        }

        @Override
        public void setPitchBend(int nBend) {
            FluidSynthesizer.this.setPitchBend(getChannel(), nBend);
        }

        @Override
        public int getPitchBend() {
            return FluidSynthesizer.this.getPitchBend(getChannel());
        }

        // TODO emulate by manipulating volume
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
}
