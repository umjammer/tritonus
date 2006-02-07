/*
 * FluidSynthesizer.java
 *
 * This file is part of Tritonus: http://www.tritonus.org/
 */

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

/*
|<---            this code is formatted to fit into 80 columns             --->|
*/

package org.tritonus.midi.device.fluidsynth;

import javax.sound.midi.*;
import org.tritonus.share.midi.TMidiDevice;
import org.tritonus.midi.sb.fluidsynth.FluidSoundbank;

/*
TODO: implement openImpl() and closeImpl()
*/
public class FluidSynthesizer extends TMidiDevice implements Synthesizer
{
    static {
        System.loadLibrary("tritonusfluid");
    }


    private MidiChannel channels[];
    private FluidSoundbank defaultSoundbank;
    private int defaultbankSfontID;



	/**
	 * Kept temporarily for compatibility.
	 */
    public FluidSynthesizer(MidiDevice.Info info, boolean bIn, boolean bOut) throws Exception
    {
		this(info);
	}


	/**
	 * should become the new main constructor.
	 */
    public FluidSynthesizer(MidiDevice.Info info) throws Exception
    {
        super(info, false, true);
        if (newSynth() < 0) {
            throw new Exception("Low-level initialization of the synthesizer failed");
        }
        try
        {
            Receiver r = getReceiver();
            channels = new FluidMidiChannel[16];
            for (int i = 0; i < 16; i++)
                channels[i] = new FluidMidiChannel(r, i);
        }
        catch (Exception e)
        {
        }
    }


    public void setDefaultSoundBank(int sfontID)
    {
        defaultSoundbank = new FluidSoundbank(sfontID);
    }


    protected void finalize(){
        deleteSynth();
    }


    public void close()
    {
        deleteSynth();
        super.close();
    }


    protected void receive(MidiMessage message, long lTimeStamp)
    {
//        System.out.print("FluidSynth.receive: ");
//        Juke.printHex(message.getMessage(), 0, message.getLength());
//        System.out.println();
        if (message instanceof ShortMessage)
        {
            ShortMessage sm = (ShortMessage) message;
            nReceive(sm.getCommand(), sm.getChannel(), sm.getData1(), sm.getData2());
        }
    }


    public native void nReceive(int command, int channel, int data1, int data2);
    public native int loadSoundFont(String filename);
    public native void setBankOffset(int sfontID, int offset);
    public native void setGain(float gain);
/* $$mp: currently not functional because fluid_synth_set_reverb_preset() is not
         present in fluidsynth 1.0.6
*/
    public native void setReverbPreset(int reverbPreset);

    public native int getMaxPolyphony();

    protected native int newSynth();
    protected native void deleteSynth();

    public boolean isSoundbankSupported(Soundbank soundbank)
    {
        return (soundbank instanceof FluidSoundbank);
    }

    public boolean loadAllInstruments(Soundbank soundbank)
    {
        return true;
    }

    public void unloadAllInstruments(Soundbank soundbank)
    {
    }

    public void unloadInstruments(Soundbank soundbank, Patch[] patchList)
    {
    }

    public boolean loadInstruments(Soundbank soundbank, Patch[] patchList)
    {
        return true;
    }

    public void unloadInstrument(Instrument instrument)
    {
    }

    public boolean loadInstrument(Instrument instrument)
    {
        return true;
    }

    public Instrument[] getAvailableInstruments()
    {
        return null;
    }

    public MidiChannel[] getChannels()
    {
        return channels;
    }

    public Soundbank getDefaultSoundbank()
    {
        return defaultSoundbank;
    }

    public long getLatency()
    {
        return 0L;
    }

    public Instrument[] getLoadedInstruments()
    {
        return null;
    }

    public VoiceStatus[] getVoiceStatus()
    {
        return null;
    }

    public boolean remapInstrument(Instrument from, Instrument to)
    {
        return true;
    }
}

/* FluidSynthesizer.java */