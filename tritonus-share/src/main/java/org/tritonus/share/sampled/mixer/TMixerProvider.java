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
 */

package org.tritonus.share.sampled.mixer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.spi.MixerProvider;

import static java.lang.System.getLogger;


public abstract class TMixerProvider extends MixerProvider {

    private static final Logger logger= getLogger("org.tritonus.TraceMixerProvider");

    private static final Mixer.Info[] EMPTY_MIXER_INFO_ARRAY = new Mixer.Info[0];

    private static final Map<Class<?>, MixerProviderStruct> mixerProviderStructs = new HashMap<>();

    private boolean disabled = false;

    public TMixerProvider() {
        logger.log(Level.TRACE, "begin");

        // currently does nothing

        logger.log(Level.TRACE, "end");
    }

    /**
     * Override this method if you want a thread-safe static initializaiton.
     */
    protected abstract void staticInit();

    private MixerProviderStruct getMixerProviderStruct() {
        logger.log(Level.TRACE, "begin");

        Class<?> clazz = this.getClass();
        logger.log(Level.TRACE, "called from " + clazz);

        // Thread.dumpStack();
        synchronized (TMixerProvider.class) {
            MixerProviderStruct struct = mixerProviderStructs.get(clazz);
            if (struct == null) {
                logger.log(Level.TRACE, "creating new MixerProviderStruct for " + clazz);

                struct = new MixerProviderStruct();
                mixerProviderStructs.put(clazz, struct);
            }

            logger.log(Level.TRACE, "end");

            return struct;
        }
    }

    protected void disable() {
        logger.log(Level.TRACE, "disabling " + getClass().getName());

        disabled = true;
    }

    protected boolean isDisabled() {
        return disabled;
    }

    protected void addMixer(Mixer mixer) {
        logger.log(Level.TRACE, "begin");

        MixerProviderStruct struct = getMixerProviderStruct();
        synchronized (struct) {
            struct.mixers.add(mixer);
            if (struct.defaultMixer == null) {
                struct.defaultMixer = mixer;
            }
        }

        logger.log(Level.TRACE, "end");
    }

    protected void removeMixer(Mixer mixer) {
        logger.log(Level.TRACE, "begin");

        MixerProviderStruct struct = getMixerProviderStruct();
        synchronized (struct) {
            struct.mixers.remove(mixer);
            // TODO should search for another mixer
            if (struct.defaultMixer == mixer) {
                struct.defaultMixer = null;
            }
        }

        logger.log(Level.TRACE, "end");
    }

    // TODO $$mp 2003/01/11:this implementation may become obsolete once the overridden method
    //  in spi.MixerProvider is implemented in a way documented officially.
    @Override
    public boolean isMixerSupported(Mixer.Info info) {
        logger.log(Level.TRACE, "begin");

        boolean isSupported = false;
        Mixer.Info[] infos = getMixerInfo();
        for (Mixer.Info value : infos) {
            if (value.equals(info)) {
                isSupported = true;
                break;
            }
        }

        logger.log(Level.TRACE, "end");

        return isSupported;
    }

    @Override
    public Mixer getMixer(Mixer.Info info) {
        logger.log(Level.TRACE, "begin");

        MixerProviderStruct struct = getMixerProviderStruct();
        Mixer mixerResult = null;
        synchronized (struct) {
            if (info == null) {
                mixerResult = struct.defaultMixer;
            } else {
                for (Mixer mixer : struct.mixers) {
                    if (mixer.getMixerInfo().equals(info)) {
                        mixerResult = mixer;
                        break;
                    }
                }
            }
        }
        if (mixerResult == null) {
            throw new IllegalArgumentException("no mixer available for " + info);
        }

        logger.log(Level.TRACE, "end");

        return mixerResult;
    }

    @Override
    public Mixer.Info[] getMixerInfo() {
        logger.log(Level.TRACE, "begin");

        Set<Mixer.Info> mixerInfos = new HashSet<>();
        MixerProviderStruct struct = getMixerProviderStruct();
        synchronized (struct) {
            for (Mixer mixer : struct.mixers) {
                mixerInfos.add(mixer.getMixerInfo());
            }
        }

        logger.log(Level.TRACE, "end");

        return mixerInfos.toArray(EMPTY_MIXER_INFO_ARRAY);
    }

    private static class MixerProviderStruct {

        public List<Mixer> mixers;
        public Mixer defaultMixer;

        public MixerProviderStruct() {
            mixers = new ArrayList<>();
            defaultMixer = null;
        }
    }
}
