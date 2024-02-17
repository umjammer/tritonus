
/*
 *  Copyright (c) 1999 - 2004 by Matthias Pfisterer
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

import java.util.Iterator;
import java.util.Set;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.spi.AudioFileReader;
import javax.sound.sampled.spi.AudioFileWriter;
import javax.sound.sampled.spi.FormatConversionProvider;
import javax.sound.sampled.spi.MixerProvider;

import org.tritonus.core.TInit.ProviderRegistrationAction;
import org.tritonus.share.ArraySet;


/**
 * TODO:
 */
public class TAudioConfig {

    private static Set<AudioFileWriter> sm_audioFileWriters = null;
    private static Set<AudioFileReader> sm_audioFileReaders = null;
    private static Set<FormatConversionProvider> sm_formatConversionProviders = null;
    private static Set<MixerProvider> sm_mixerProviders = null;

    private static Mixer.Info sm_defaultMixerInfo;

    /**
     * Constructor to prevent instantiation.
     */
    private TAudioConfig() {
    }

    private static void registerAudioFileReaders() {
        ProviderRegistrationAction action;
        action = obj -> {
            AudioFileReader provider = (AudioFileReader) obj;
            TAudioConfig.addAudioFileReader(provider);
        };
        TInit.registerClasses(AudioFileReader.class, action);
    }

    private static void registerAudioFileWriters() {
        ProviderRegistrationAction action;
        action = obj -> {
            AudioFileWriter provider = (AudioFileWriter) obj;
            TAudioConfig.addAudioFileWriter(provider);
        };
        TInit.registerClasses(AudioFileWriter.class, action);
    }

    private static void registerFormatConversionProviders() {
        ProviderRegistrationAction action;
        action = obj -> {
            FormatConversionProvider provider = (FormatConversionProvider) obj;
            TAudioConfig.addFormatConversionProvider(provider);
        };
        TInit.registerClasses(FormatConversionProvider.class, action);
    }

    private static void registerMixerProviders() {
        ProviderRegistrationAction action;
        action = obj -> {
            MixerProvider provider = (MixerProvider) obj;
            TAudioConfig.addMixerProvider(provider);
        };
        TInit.registerClasses(MixerProvider.class, action);
    }

    ////////////////////////////////////////////////////////////////

    public static synchronized void addAudioFileReader(AudioFileReader provider) {
        getAudioFileReadersImpl().add(provider);
    }

    public static synchronized void removeAudioFileReader(AudioFileReader provider) {
        getAudioFileReadersImpl().remove(provider);
    }

    public static synchronized Iterator<AudioFileReader> getAudioFileReaders() {
        return getAudioFileReadersImpl().iterator();
    }

    private static synchronized Set<AudioFileReader> getAudioFileReadersImpl() {
        if (sm_audioFileReaders == null) {
            sm_audioFileReaders = new ArraySet<>();
            registerAudioFileReaders();
        }
        return sm_audioFileReaders;
    }

    public static synchronized void addAudioFileWriter(AudioFileWriter provider) {
        getAudioFileWritersImpl().add(provider);
    }

    public static synchronized void removeAudioFileWriter(AudioFileWriter provider) {
        getAudioFileWritersImpl().remove(provider);
    }

    public static synchronized Iterator<AudioFileWriter> getAudioFileWriters() {
        return getAudioFileWritersImpl().iterator();
    }

    private static synchronized Set<AudioFileWriter> getAudioFileWritersImpl() {
        if (sm_audioFileWriters == null) {
            sm_audioFileWriters = new ArraySet<>();
            registerAudioFileWriters();
        }
        return sm_audioFileWriters;
    }

    public static synchronized void addFormatConversionProvider(FormatConversionProvider provider) {
        getFormatConversionProvidersImpl().add(provider);
    }

    public static synchronized void removeFormatConversionProvider(FormatConversionProvider provider) {
        getFormatConversionProvidersImpl().remove(provider);
    }

    public static synchronized Iterator<FormatConversionProvider> getFormatConversionProviders() {
        return getFormatConversionProvidersImpl().iterator();
    }

    private static synchronized Set<FormatConversionProvider> getFormatConversionProvidersImpl() {
        if (sm_formatConversionProviders == null) {
            sm_formatConversionProviders = new ArraySet<>();
            registerFormatConversionProviders();
        }
        return sm_formatConversionProviders;
    }

    public static synchronized void addMixerProvider(MixerProvider provider) {
        getMixerProvidersImpl().add(provider);
    }

    public static synchronized void removeMixerProvider(MixerProvider provider) {
        getMixerProvidersImpl().remove(provider);
    }

    public static synchronized Iterator<MixerProvider> getMixerProviders() {
        return getMixerProvidersImpl().iterator();
    }

    private static synchronized Set<MixerProvider> getMixerProvidersImpl() {
        if (sm_mixerProviders == null) {
            sm_mixerProviders = new ArraySet<>();
            registerMixerProviders();
        }
        return sm_mixerProviders;
    }

    // TODO: a way to set the default mixer
    public static Mixer.Info getDefaultMixerInfo() {
        return sm_defaultMixerInfo;
    }

}


