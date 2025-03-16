/*
 *  Copyright (c) 2001 - 2002 by Matthias Pfisterer
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

package org.tritonus.lowlevel.alsa;

/**
 * TODO
 */
public class AlsaMixerElement {

    // Channel type constants.
    // They mirror the values of snd_mixer_selem_channel_id_t.

    /** Unknown */
    public static final int SND_MIXER_SCHN_UNKNOWN = -1;
    /** Front left */
    public static final int SND_MIXER_SCHN_FRONT_LEFT = 0;
    /** Front right */
    public static final int SND_MIXER_SCHN_FRONT_RIGHT = 1;
    /** Front center */
    public static final int SND_MIXER_SCHN_FRONT_CENTER = 2;
    /** Rear left */
    public static final int SND_MIXER_SCHN_REAR_LEFT = 3;
    /** Rear right */
    public static final int SND_MIXER_SCHN_REAR_RIGHT = 4;
    /** Woofer */
    public static final int SND_MIXER_SCHN_WOOFER = 5;
    public static final int SND_MIXER_SCHN_LAST = 31;
    /** Mono (Front left alias) */
    public static final int SND_MIXER_SCHN_MONO = SND_MIXER_SCHN_FRONT_LEFT;

    private final AlsaMixer mixer;

    @SuppressWarnings("unused")
    private long nativeHandle;

    static {
        Alsa.loadNativeLibrary();
    }

    public AlsaMixerElement(AlsaMixer mixer, int index, String name) {
        this.mixer = mixer;
        int ret;
        ret = open(getMixer(), index, name);
        if (ret < 0) {
            throw new RuntimeException("cannot open");
        }
    }

    /**
     * Calls snd_mixer_find_selem().
     */
    private native int open(AlsaMixer mixer, int index, String name);

    private AlsaMixer getMixer() {
        return mixer;
    }

    // TODO getId()

    /**
     * Calls snd_mixer_selem_get_name().
     */
    public native String getName();

    /**
     * Calls snd_mixer_selem_get_index().
     */
    public native int getIndex();

    /**
     * Calls snd_mixer_selem_is_active().
     */
    public native boolean isActive();

    /**
     * Calls snd_mixer_selem_is_playback_mono().
     */
    public native boolean isPlaybackMono();

    /**
     * Calls snd_mixer_selem_has_playback_channel().
     */
    public native boolean hasPlaybackChannel(int channelType);

    /**
     * Calls snd_mixer_selem_is_capture_mono().
     */
    public native boolean isCaptureMono();

    /**
     * Calls snd_mixer_selem_has_capture_channel().
     */
    public native boolean hasCaptureChannel(int channelType);

    /**
     * Calls snd_mixer_selem_get_capture_group().
     */
    public native int getCaptureGroup();

    /**
     * Calls snd_mixer_selem_has_common_volume().
     */
    public native boolean hasCommonVolume();

    /**
     * Calls snd_mixer_selem_has_playback_volume().
     */
    public native boolean hasPlaybackVolume();

    /**
     * Calls snd_mixer_selem_has_playback_volume_joined().
     */
    public native boolean hasPlaybackVolumeJoined();

    /**
     * Calls snd_mixer_selem_has_capture_volume().
     */
    public native boolean hasCaptureVolume();

    /**
     * Calls snd_mixer_selem_has_capture_volume_joined().
     */
    public native boolean hasCaptureVolumeJoined();

    /**
     * Calls snd_mixer_selem_has_common_switch().
     */
    public native boolean hasCommonSwitch();

    /**
     * Calls snd_mixer_selem_has_playback_switch().
     */
    public native boolean hasPlaybackSwitch();

    /**
     * Calls snd_mixer_selem_has_playback_switch_joined().
     */
    public native boolean hasPlaybackSwitchJoined();

    /**
     * Calls snd_mixer_selem_has_capture_switch().
     */
    public native boolean hasCaptureSwitch();

    /**
     * Calls snd_mixer_selem_has_capture_switch_joined().
     */
    public native boolean hasCaptureSwitchJoinded();

    /**
     * Calls snd_mixer_selem_has_capture_switch_exclusive().
     */
    public native boolean hasCaptureSwitchExclusive();

    /**
     * Calls snd_mixer_selem_get_playback_volume().
     */
    public native int getPlaybackVolume(int channelType);

    /**
     * Calls snd_mixer_selem_get_capture_volume().
     */
    public native int getCaptureVolume(int channelType);

    /**
     * Calls snd_mixer_selem_get_playback_switch().
     */
    public native boolean getPlaybackSwitch(int channelType);

    /**
     * Calls snd_mixer_selem_get_capture_switch().
     */
    public native boolean getCaptureSwitch(int channelType);

    /**
     * Calls snd_mixer_selem_set_playback_volume().
     */
    public native void setPlaybackVolume(int channelType, int value);

    /**
     * Calls snd_mixer_selem_set_capture_volume().
     */
    public native void setCaptureVolume(int channelType, int value);

    /**
     * Calls snd_mixer_selem_set_playback_volume_all().
     */
    public native void setPlaybackVolumeAll(int value);

    /**
     * Calls snd_mixer_selem_set_capture_volume_all().
     */
    public native void setCaptureVolumeAll(int value);

    /**
     * Calls snd_mixer_selem_set_playback_switch().
     */
    public native void setPlaybackSwitch(int channelType, boolean value);

    /**
     * Calls snd_mixer_selem_set_capture_switch().
     */
    public native void setCaptureSwitch(int channelType, boolean value);

    /**
     * Calls snd_mixer_selem_set_playback_switch_all().
     */
    public native void setPlaybackSwitchAll(boolean value);

    /**
     * Calls snd_mixer_selem_set_capture_switch_all().
     */
    public native void setCaptureSwitchAll(boolean value);

    /**
     * Calls snd_mixer_selem_get_playback_volume_range().
     * values[0]: minimum
     * values[1]: maximum
     */
    public native void getPlaybackVolumeRange(int[] values);

    /**
     * Calls snd_mixer_selem_get_capture_volume_range().
     * values[0]: minimum
     * values[1]: maximum
     */
    public native void getCaptureVolumeRange(int[] values);

    /**
     * Calls snd_mixer_selem_set_playback_volume_range().
     */
    public native void setPlaybackVolumeRange(int min, int max);

    /**
     * Calls snd_mixer_selem_set_capture_volume_range().
     */
    public native void setCaptureVolumeRange(int min, int max);

    /**
     * Calls snd_mixer_selem_channel_name().
     */
    public static native String getChannelName(int channelType);

    /**
     * TODO
     */
    private static native void setTrace(boolean trace);
}
