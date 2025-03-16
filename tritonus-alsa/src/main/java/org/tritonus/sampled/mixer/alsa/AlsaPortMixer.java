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

package org.tritonus.sampled.mixer.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;

import org.tritonus.lowlevel.alsa.AlsaMixer;
import org.tritonus.lowlevel.alsa.AlsaMixerElement;
import org.tritonus.share.GlobalInfo;
import org.tritonus.share.sampled.mixer.TCompoundControlType;
import org.tritonus.share.sampled.mixer.TMixer;
import org.tritonus.share.sampled.mixer.TMixerInfo;
import org.tritonus.share.sampled.mixer.TPort;

import static java.lang.System.getLogger;


/**
 * TODO
 */
public class AlsaPortMixer extends TMixer {

    private static final Logger logger = getLogger("org.tritonus.TraceMixer");

    /**
     * Used to signal an illegal value for direction.
     */
    public static final int DIRECTION_NONE = -1;

    /**
     * Used to signal common volume or common switch.
     */
    public static final int DIRECTION_COMMON = 0;

    /**
     * Used to signal playback volume or playback switch.
     */
    public static final int DIRECTION_PLAYBACK = 1;

    /**
     * Used to signal capture volume or capture switch.
     */
    public static final int DIRECTION_CAPTURE = 2;

    /**
     * For the first shot, we try to create one port line per mixer
     * element. For now, the following two lists should have the same size
     * and related elements at the same index position.
     */
    private final List<AlsaMixerElement> mixerElements;

    /**
     * Port.Infos are keys, Port instances are values.
     */
    private final Map<Port.Info, Port> portMap;

    /**
     * Port.Infos are keys, AlsaMixerElement instances are values.
     */
    private final Map<Port.Info, AlsaMixerElement> mixerElementMap;

    public AlsaPortMixer(int card) {
        this("hw:" + card);
    }

    public AlsaPortMixer(String deviceName) {
        super(new TMixerInfo("Alsa Port Mixer (" + deviceName + ")",
                        GlobalInfo.getVendor(),
                        "System Mixer for the Advanced Linux Sound System (card " + deviceName + ")",
                        GlobalInfo.getVersion()),
                new Line.Info(Mixer.class));
        logger.log(Level.TRACE, "begin");

        mixerElements = new ArrayList<>();
        mixerElementMap = new HashMap<>();
        portMap = new HashMap<>();
        AlsaMixer alsaMixer = null;
        try {
            alsaMixer = new AlsaMixer(deviceName);
            logger.log(Level.TRACE, "successfully created AlsaMixer instance");

        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        int arraySize = 150; // TODO original value: 128. With this value, a vm crash occurs
        int[] indices;
        String[] names;
        int controlCount;
        while (true) {
            indices = new int[arraySize];
            names = new String[arraySize];
            controlCount = alsaMixer.readControlList(indices, names);
            if (controlCount >= 0) {
                break;
            }
            logger.log(Level.TRACE, "increasing array size for AlsaMixer.readControlList(): now " + arraySize * 2);

            arraySize *= 2;
        }
        List<Line.Info> sourcePortInfos = new ArrayList<>();
        List<Line.Info> targetPortInfos = new ArrayList<>();
        for (int i = 0; i < controlCount; i++) {
            logger.log(Level.TRACE, "AlsaPortMixer.<init>(): control " + i + ": " + indices[i] + " " + names[i]);

            AlsaMixerElement element = new AlsaMixerElement(alsaMixer, indices[i], names[i]);
            if (element.isActive()) {
                mixerElements.add(element);
                if (hasPlaybackChannels(element)) {
                    Port.Info info = new Port.Info(Port.class, element.getName(), true);
                    sourcePortInfos.add(info);
                    mixerElementMap.put(info, element);
                }
                if (hasCaptureChannels(element)) {
                    Port.Info info = new Port.Info(Port.class, element.getName(), false);
                    targetPortInfos.add(info);
                    mixerElementMap.put(info, element);
                }
            }
        }
        setSupportInformation(new ArrayList<>(), new ArrayList<>(), sourcePortInfos, targetPortInfos);

        logger.log(Level.TRACE, "end");
    }

    private static boolean hasPlaybackChannels(AlsaMixerElement element) {
        boolean hasChannels = false;
        for (int channel = AlsaMixerElement.SND_MIXER_SCHN_FRONT_LEFT;
             channel <= AlsaMixerElement.SND_MIXER_SCHN_WOOFER;
             channel++) {
            hasChannels |= element.hasPlaybackChannel(channel);
        }
        return hasChannels;
    }

    private static boolean hasCaptureChannels(AlsaMixerElement element) {
        boolean hasChannels = false;
        for (int channel = AlsaMixerElement.SND_MIXER_SCHN_FRONT_LEFT;
             channel <= AlsaMixerElement.SND_MIXER_SCHN_WOOFER;
             channel++) {
            hasChannels |= element.hasCaptureChannel(channel);
        }
        return hasChannels;
    }

    // Line ----

    // TODO allow real close and reopen of mixer
    @Override
    public void open() {
        logger.log(Level.TRACE, "begin");

        // currently does nothing

        logger.log(Level.TRACE, "end");
    }

    @Override
    public void close() {
        logger.log(Level.TRACE, "begin");

        // currently does nothing

        logger.log(Level.TRACE, "end");
    }

    // Mixer ----

//    public Line.Info getLineInfo(Line.Info info) {
//        // TODO
//        return null;
//    }

    // TODO
    @Override
    public int getMaxLines(Line.Info info) {
        // TODO
        return 0;
    }

    @Override
    protected Port getPort(Port.Info info) throws LineUnavailableException {
        logger.log(Level.TRACE, "begin");

        Port port = portMap.get(info);
        if (port == null) {
            port = createPort(info);
            portMap.put(info, port);
        }

        logger.log(Level.TRACE, "end");

        return port;
    }

    private Port createPort(Port.Info info) {
        logger.log(Level.TRACE, "begin");

        AlsaMixerElement element = mixerElementMap.get(info);
        if (element == null) {
            throw new IllegalArgumentException("no port for this info");
        }
        List<Control> controls;
//        Control c;
//        int direction;
        if (info.isSource()) {
            controls = createSourcePortControls(element);
        } else {
//            direction = DIRECTION_CAPTURE;
//            controls = createTargetPortControls(element);
            controls = new ArrayList<>();
        }
        Port port = new TPort(this, info, controls);

        logger.log(Level.TRACE, "end");

        return port;
    }

    /**
     * TODO
     */
    private static List<Control> createSourcePortControls(AlsaMixerElement element) {
        int direction = DIRECTION_PLAYBACK;
        List<Control> controls = new ArrayList<>();
        Control c;
        if (element.hasPlaybackVolume() ||
                element.hasCommonVolume()) {
            if (element.isPlaybackMono() ||
                    element.hasPlaybackVolumeJoined()) {
                c = createVolumeControl(element, AlsaMixerElement.SND_MIXER_SCHN_FRONT_LEFT, direction);
                controls.add(c);
            } else {
                List<Control> volumeControls = new ArrayList<>();
                for (int channel = AlsaMixerElement.SND_MIXER_SCHN_FRONT_LEFT; channel < AlsaMixerElement.SND_MIXER_SCHN_LAST; channel++) {
                    if (element.hasPlaybackChannel(channel)) {
//logger.log(Level.TRACE, "adding channel " + channel);
                        c = createVolumeControl(element, channel, direction);
//logger.log(Level.DEBUG, "control to add: " + c);
                        volumeControls.add(c);
                    }
                }
                // list should not be empty
                CompoundControl.Type type = new TCompoundControlType("test");
                Control[] memberControls = volumeControls.toArray(new Control[0]);
//logger.log(Level.DEBUG, "member controls: " + memberControls);
//logger.log(Level.DEBUG, "# member controls: " + memberControls.length);
                c = new AlsaCompoundControl(type, memberControls);
                controls.add(c);
            }
        }
        if (element.hasPlaybackSwitch() || element.hasCommonSwitch()) {
            if (element.isPlaybackMono() || element.hasPlaybackSwitchJoined()) {
                c = createSwitchControl(element, AlsaMixerElement.SND_MIXER_SCHN_FRONT_LEFT, direction);
                controls.add(c);
            } else {
                List<Control> volumeControls = new ArrayList<>();
                for (int channel = AlsaMixerElement.SND_MIXER_SCHN_FRONT_LEFT; channel < AlsaMixerElement.SND_MIXER_SCHN_LAST; channel++) {
                    if (element.hasPlaybackChannel(channel)) {
//logger.log(Level.TRACE, "adding channel " + channel);
                        c = createSwitchControl(element, channel, direction);
//logger.log(Level.DEBUG, "control to add: " + c);
                        volumeControls.add(c);
                    }
                }
                // list should not be empty
                CompoundControl.Type type = new TCompoundControlType("test");
                Control[] memberControls = volumeControls.toArray(new Control[0]);
//logger.log(Level.DEBUG, "member controls: " + memberControls);
//logger.log(Level.DEBUG, "# member controls: " + memberControls.length);
                c = new AlsaCompoundControl(type, memberControls);
                controls.add(c);
            }
        }
        return controls;
    }

    /**
     * TODO
     */
    private static FloatControl createVolumeControl(AlsaMixerElement element, int channel, int direction) {
        int[] values = new int[2];
        switch (direction) {
        case DIRECTION_COMMON:
        case DIRECTION_PLAYBACK:
            element.getPlaybackVolumeRange(values);
            break;

        case DIRECTION_CAPTURE:
            element.getCaptureVolumeRange(values);
            break;
        }
        FloatControl control = new AlsaVolumeControl(
                FloatControl.Type.VOLUME,
                values[0],
                values[1],
                1.0F,
                -1,
                values[0],
                "", "", "", "",
                element,
                channel,
                direction);
        return control;
    }

    /**
     * TODO
     */
    private static BooleanControl createSwitchControl(AlsaMixerElement element, int channel, int direction) {
        BooleanControl control = new AlsaSwitchControl(
                BooleanControl.Type.MUTE,
                false,
                "", "",
                element,
                channel,
                direction);
        return control;
    }

    // inner classes ----

    private static class AlsaVolumeControl extends FloatControl {

        private final AlsaMixerElement element;
        private final int channel;

        /**
         * One of the constants DIRECTION_*.
         */
        private final int direction;

        /**
         * @param direction One of the constants DIRECTION_*.
         */
        public AlsaVolumeControl(Type type,
                                 float minimum,
                                 float maximum,
                                 float precision,
                                 int updatePeriod,
                                 float initialValue,
                                 String units,
                                 String minLabel,
                                 String midLabel,
                                 String maxLabel,
                                 AlsaMixerElement element,
                                 int channel,
                                 int direction) {
            super(type,
                    minimum,
                    maximum,
                    precision,
                    updatePeriod,
                    initialValue,
                    units,
                    minLabel,
                    midLabel,
                    maxLabel);
            logger.log(Level.TRACE, "begin");

            this.element = element;
            this.channel = channel;
            this.direction = direction;
            setValue(getValueImpl());

            logger.log(Level.TRACE, "end");
        }

        private AlsaMixerElement getElement() {
            return element;
        }

        private int getChannel() {
            return channel;
        }

        private int getDirection() {
            return direction;
        }

        // TODO respect channels
        @Override
        public void setValue(float fValue) {
            super.setValue(fValue);
            int value = (int) fValue;
            switch (getDirection()) {
            case DIRECTION_COMMON:
            case DIRECTION_PLAYBACK:
                getElement().setPlaybackVolumeAll(value);
                break;

            case DIRECTION_CAPTURE:
                getElement().setCaptureVolumeAll(value);
                break;
            }
        }

        private float getValueImpl() {
            int channel = getChannel();
            float value = switch (getDirection()) {
                case DIRECTION_COMMON, DIRECTION_PLAYBACK -> getElement().getPlaybackVolume(channel);
                case DIRECTION_CAPTURE -> getElement().getCaptureVolume(channel);
                default -> 0.0F;
            };
            return value;
        }
    }

    private static class AlsaSwitchControl extends BooleanControl {

        private final AlsaMixerElement element;
        private final int channel;

        /**
         * One of the constants DIRECTION_*.
         */
        private final int direction;

        /**
         * @param direction One of the constants DIRECTION_*.
         */
        public AlsaSwitchControl(Type type,
                                 boolean initialValue,
                                 String trueLabel,
                                 String falseLabel,
                                 AlsaMixerElement element,
                                 int channel,
                                 int direction) {
            super(type, initialValue, trueLabel, falseLabel);
            logger.log(Level.TRACE, "begin");

            this.element = element;
            this.channel = channel;
            this.direction = direction;

            logger.log(Level.TRACE, "end");
        }

        private AlsaMixerElement getElement() {
            return element;
        }

        private int getChannel() {
            return channel;
        }

        private int getDirection() {
            return direction;
        }

        // TODO respect channels
        @Override
        public void setValue(boolean value) {
            super.setValue(value);
            switch (getDirection()) {
            case DIRECTION_COMMON:
            case DIRECTION_PLAYBACK:
                getElement().setPlaybackSwitchAll(value);
                break;

            case DIRECTION_CAPTURE:
                getElement().setCaptureSwitchAll(value);
                break;
            }
        }
    }

    /**
     * CompoundControl class.
     * This class is only needed to provide a public
     * constructor.
     */
    public static class AlsaCompoundControl extends CompoundControl {

        public AlsaCompoundControl(CompoundControl.Type type, Control[] memberControls) {
            super(type, memberControls);
        }
    }
}
