/*
 * Copyright (c) 2006 by Henri Manson
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

package org.tritonus.midi.sb.fluidsynth;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;

import com.sun.jna.ptr.PointerByReference;
import org.tritonus.midi.device.fluidsynth.FluidSynthesizer;
import vavi.sound.midi.fluidsynth.jna.sfont.SfontLibrary;
import vavi.sound.midi.fluidsynth.jna.synth.SynthLibrary;

import static java.lang.System.getLogger;


/**
 * FluidSoundbank.java
 * <p>
 * This file is part of Tritonus: http://www.tritonus.org/
 *
 * @author Manson
 */
public class FluidSoundbank implements Soundbank {

    private static final Logger logger = getLogger("org.tritonus.TraceFluidNative");

    private FluidSynthesizer synth;
    private int sfontID;
    private FluidInstrument[] instruments;

    // $$mp: needs to be public for native code now
    public class FluidInstrument extends Instrument {

        public FluidInstrument(int bank, int program, String name) {
            super(FluidSoundbank.this, new Patch(bank, program), name, null);
        }

        public String toString() {
            return "Instrument " + getName() + " (bank " + getPatch().getBank() + " program " + getPatch().getProgram() + ")";
        }

        @Override
        public Object getData() {
            return null;
        }
    }

    public FluidSoundbank(FluidSynthesizer synth, int sfontID) {
        this.synth = synth;
        this.sfontID = sfontID;
        instruments = nGetInstruments(sfontID);
    }

    public FluidInstrument[] nGetInstruments(int sfontID) {

        PointerByReference /* fluid_synth_t */ synth = this.synth.getSynthesizer();

        logger.log(Level.TRACE, String.format("nGetInstruments: synth: %s\n", synth));

        if (synth != null) {

            PointerByReference /* fluid_sfont_t */ sfont = SynthLibrary.INSTANCE.fluid_synth_get_sfont_by_id(synth, sfontID);
            PointerByReference /* fluid_preset_t */ preset;

            int count = 0;
            if (sfont != null) {
                SfontLibrary.INSTANCE.fluid_sfont_iteration_start(sfont);

                while ((preset = SfontLibrary.INSTANCE.fluid_sfont_iteration_next(sfont)) != null) {
                    count++;
                }
            }

            FluidInstrument[] instruments = new FluidInstrument[count];

            sfont = SynthLibrary.INSTANCE.fluid_synth_get_sfont_by_id(synth, sfontID);
            int offset = SynthLibrary.INSTANCE.fluid_synth_get_bank_offset(synth, sfontID);

            if (sfont == null)
                return null;

            SfontLibrary.INSTANCE.fluid_sfont_iteration_start(sfont);

            int i = 0;
            while ((preset = SfontLibrary.INSTANCE.fluid_sfont_iteration_next(sfont)) != null) {
                String instrname = SfontLibrary.INSTANCE.fluid_preset_get_name(preset);
                FluidInstrument instrument = new FluidInstrument(
                        SfontLibrary.INSTANCE.fluid_preset_get_banknum(preset) + offset,
                        SfontLibrary.INSTANCE.fluid_preset_get_num(preset),
                        instrname);
                instruments[i++] = instrument;
            }
            return instruments;
        } else
            return null;
    }

    @Override
    public Instrument getInstrument(Patch patch) {
        return null;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getVendor() {
        return "Mansoft";
    }

    @Override
    public SoundbankResource[] getResources() {
        return null;
    }

    @Override
    public String getName() {
        return "Mansoft";
    }

    @Override
    public Instrument[] getInstruments() {
        return instruments;
    }

    @Override
    public String getDescription() {
        return "Mansoft";
    }
}
