/*
 *  Copyright (c) 1999 - 2004 by Matthias Pfisterer
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

package org.tritonus.core;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Iterator;
import java.util.Set;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.spi.MidiDeviceProvider;
import javax.sound.midi.spi.MidiFileReader;
import javax.sound.midi.spi.MidiFileWriter;
import javax.sound.midi.spi.SoundbankReader;

import org.tritonus.core.TInit.ProviderRegistrationAction;
import org.tritonus.share.ArraySet;

import static java.lang.System.getLogger;


/**
 * TODO
 */
public class TMidiConfig {

    private static final Logger logger = getLogger("org.tritonus.TraceMidiConfig");

    private static Set<MidiDeviceProvider> midiDeviceProviders = null;
    private static Set<MidiFileReader> midiFileReaders = null;
    private static Set<MidiFileWriter> midiFileWriters = null;
    private static Set<SoundbankReader> soundbankReaders = null;

    private static MidiDevice.Info defaultMidiInDeviceInfo = null;
    private static MidiDevice.Info defaultMidiOutDeviceInfo = null;
    private static MidiDevice.Info defaultSequencerInfo = null;
    private static MidiDevice.Info defaultSynthesizerInfo = null;

    static {
        init();
    }

    /**
     * Constructor to prevent instantiation.
     */
    private TMidiConfig() {
    }

    /**
     * Initialize the collections of providers and the default devices.
     */
    private static void init() {
        // init providers from scanning the class path
        // note: this already sets the default devices
        getMidiDeviceProvidersImpl();
        getMidiFileReadersImpl();
        getMidiFileWritersImpl();
        getSoundbankReadersImpl();
        // now check properties for default devices
        // ... TODO
    }

    private static void registerMidiDeviceProviders() {
        ProviderRegistrationAction action = obj -> {
            MidiDeviceProvider midiDeviceProvider = (MidiDeviceProvider) obj;
            TMidiConfig.addMidiDeviceProvider(midiDeviceProvider);
        };
        TInit.registerClasses(MidiDeviceProvider.class, action);
    }

    private static void registerMidiFileReaders() {
        ProviderRegistrationAction action = obj -> {
            MidiFileReader provider = (MidiFileReader) obj;
            TMidiConfig.addMidiFileReader(provider);
        };
        TInit.registerClasses(MidiFileReader.class, action);
    }

    private static void registerMidiFileWriters() {
        ProviderRegistrationAction action = obj -> {
            MidiFileWriter provider = (MidiFileWriter) obj;
            TMidiConfig.addMidiFileWriter(provider);
        };
        TInit.registerClasses(MidiFileWriter.class, action);
    }

    private static void registerSoundbankReaders() {
        ProviderRegistrationAction action = obj -> {
            SoundbankReader provider = (SoundbankReader) obj;
            TMidiConfig.addSoundbankReader(provider);
        };
        TInit.registerClasses(SoundbankReader.class, action);
    }

    // ----

    public static synchronized void addMidiDeviceProvider(MidiDeviceProvider provider) {
//logger.log(Level.TRACE, "MidiDeviceProvider: " + provider);
        getMidiDeviceProvidersImpl().add(provider);
        if (getDefaultMidiInDeviceInfo() == null ||
                getDefaultMidiOutDeviceInfo() == null ||
                getDefaultSynthesizerInfo() == null ||
                getDefaultSequencerInfo() == null) {
            MidiDevice.Info[] infos = provider.getDeviceInfo();
//logger.log(Level.TRACE, "#infos: " + infos.length);
            for (MidiDevice.Info info : infos) {
                MidiDevice device = null;
                try {
                    device = provider.getDevice(info);
                } catch (IllegalArgumentException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
                if (device instanceof Synthesizer) {
                    if (getDefaultSynthesizerInfo() == null) {
                        defaultSynthesizerInfo = info;
                    }
                } else if (device instanceof Sequencer) {
                    if (getDefaultSequencerInfo() == null) {
                        defaultSequencerInfo = info;
                    }
                } else if (device.getMaxTransmitters() != 0) {
                    if (getDefaultMidiInDeviceInfo() == null) {
                        defaultMidiInDeviceInfo = info;
                    }
                } else if (device.getMaxReceivers() != 0) {
                    if (getDefaultMidiOutDeviceInfo() == null) {
                        defaultMidiOutDeviceInfo = info;
                    }
                }
            }
        }
    }

    public static synchronized void removeMidiDeviceProvider(MidiDeviceProvider provider) {
        getMidiDeviceProvidersImpl().remove(provider);
        // TODO change default infos
    }

    public static synchronized Iterator<MidiDeviceProvider> getMidiDeviceProviders() {
        return getMidiDeviceProvidersImpl().iterator();
    }

    private static synchronized Set<MidiDeviceProvider> getMidiDeviceProvidersImpl() {
        if (midiDeviceProviders == null) {
            midiDeviceProviders = new ArraySet<>();
            registerMidiDeviceProviders();
        }
        return midiDeviceProviders;
    }

    public static synchronized void addMidiFileReader(MidiFileReader reader) {
        logger.log(Level.TRACE, "adding " + reader);

        getMidiFileReadersImpl().add(reader);

        logger.log(Level.TRACE, "size " + midiFileReaders.size());
    }

    public static synchronized void removeMidiFileReader(MidiFileReader reader) {
        getMidiFileReadersImpl().remove(reader);
    }

    public static synchronized Iterator<MidiFileReader> getMidiFileReaders() {
        return getMidiFileReadersImpl().iterator();
    }

    private static synchronized Set<MidiFileReader> getMidiFileReadersImpl() {
        if (midiFileReaders == null) {
            midiFileReaders = new ArraySet<>();
            registerMidiFileReaders();
        }
        return midiFileReaders;
    }

    public static synchronized void addMidiFileWriter(MidiFileWriter reader) {
        getMidiFileWritersImpl().add(reader);
    }

    public static synchronized void removeMidiFileWriter(MidiFileWriter reader) {
        getMidiFileWritersImpl().remove(reader);
    }

    public static synchronized Iterator<MidiFileWriter> getMidiFileWriters() {
        return getMidiFileWritersImpl().iterator();
    }

    private static synchronized Set<MidiFileWriter> getMidiFileWritersImpl() {
        if (midiFileWriters == null) {
            midiFileWriters = new ArraySet<>();
            registerMidiFileWriters();
        }
        return midiFileWriters;
    }

    public static synchronized void addSoundbankReader(SoundbankReader reader) {
        getSoundbankReadersImpl().add(reader);
    }

    public static synchronized void removeSoundbankReader(SoundbankReader reader) {
        getSoundbankReadersImpl().remove(reader);
    }

    public static synchronized Iterator<SoundbankReader> getSoundbankReaders() {
        return getSoundbankReadersImpl().iterator();
    }

    private static synchronized Set<SoundbankReader> getSoundbankReadersImpl() {
        if (soundbankReaders == null) {
            soundbankReaders = new ArraySet<>();
            registerSoundbankReaders();
        }
        return soundbankReaders;
    }

    public static MidiDevice.Info getDefaultMidiInDeviceInfo() {
        return defaultMidiInDeviceInfo;
    }

    public static MidiDevice.Info getDefaultMidiOutDeviceInfo() {
        return defaultMidiOutDeviceInfo;
    }

    public static MidiDevice.Info getDefaultSynthesizerInfo() {
        return defaultSynthesizerInfo;
    }

    public static MidiDevice.Info getDefaultSequencerInfo() {
        return defaultSequencerInfo;
    }
}
