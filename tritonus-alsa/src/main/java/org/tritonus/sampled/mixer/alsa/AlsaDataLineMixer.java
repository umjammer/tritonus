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

package org.tritonus.sampled.mixer.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import org.tritonus.lowlevel.alsa.Alsa;
import org.tritonus.lowlevel.alsa.AlsaPcm;
import org.tritonus.lowlevel.alsa.AlsaPcmHWParams;
import org.tritonus.lowlevel.alsa.AlsaPcmHWParamsFormatMask;
import org.tritonus.share.GlobalInfo;
import org.tritonus.share.TSettings;
import org.tritonus.share.sampled.mixer.TMixer;
import org.tritonus.share.sampled.mixer.TMixerInfo;
import org.tritonus.share.sampled.mixer.TSoftClip;

import static java.lang.System.getLogger;


public class AlsaDataLineMixer extends TMixer {

    private static final Logger logger = getLogger("org.tritonus.TraceMixer");

    private static final AudioFormat[] EMPTY_AUDIOFORMAT_ARRAY = new AudioFormat[0];
    private static final int CHANNELS_LIMIT = 32;

    // default buffer size in bytes.
    private static final int DEFAULT_BUFFER_SIZE = 32768;

    /** The name of the sound card this mixer is representing. */
    private String pcmName;

    public static String getDeviceNamePrefix() {
        if (TSettings.AlsaUsePlughw) {
            return "plughw";
        } else {
            return "hw";
        }
    }

    public static String getPcmName(int card) {
        String pcmName = getDeviceNamePrefix() + ":" + card;
        if (TSettings.AlsaUsePlughw) {
//            pcmName += ",0";
        }
        return pcmName;
    }

    public AlsaDataLineMixer() {
        this(0);
    }

    public AlsaDataLineMixer(int card) {
        this(getPcmName(card));
    }

    public AlsaDataLineMixer(String pcmName) {
        super(new TMixerInfo(
                        "Alsa DataLine Mixer (" + pcmName + ")",
                        GlobalInfo.getVendor(),
                        "Mixer for the Advanced Linux Sound Architecture (card " + pcmName + ")",
                        GlobalInfo.getVersion()),
                new Line.Info(Mixer.class));
        logger.log(Level.TRACE, "AlsaDataLineMixer.<init>(String): begin.");

        this.pcmName = pcmName;
        List<AudioFormat> sourceFormats = getSupportedFormats(AlsaPcm.SND_PCM_STREAM_PLAYBACK);
        List<AudioFormat> targetFormats = getSupportedFormats(AlsaPcm.SND_PCM_STREAM_CAPTURE);
        List<Line.Info> sourceLineInfos = new ArrayList<>();
        Line.Info sourceLineInfo = new DataLine.Info(
                SourceDataLine.class,
                sourceFormats.toArray(EMPTY_AUDIOFORMAT_ARRAY),
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED);
        sourceLineInfos.add(sourceLineInfo);
        List<Line.Info> targetLineInfos = new ArrayList<>();
        Line.Info targetLineInfo = new DataLine.Info(
                TargetDataLine.class,
                targetFormats.toArray(EMPTY_AUDIOFORMAT_ARRAY),
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED);
        targetLineInfos.add(targetLineInfo);
        setSupportInformation(sourceFormats,
                targetFormats,
                sourceLineInfos,
                targetLineInfos);

        logger.log(Level.TRACE, "AlsaDataLineMixer.<init>(String): end.");
    }

    public String getPcmName() {
        return pcmName;
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

    @Override
    public int getMaxLines(Line.Info info) {
        logger.log(Level.TRACE, "begin");

        // TODO

        logger.log(Level.TRACE, "end");

        return 0;
    }

    // private ----

    // bufferSize is in bytes!
    @Override
    protected SourceDataLine getSourceDataLine(AudioFormat format, int bufferSize) throws LineUnavailableException {
        logger.log(Level.TRACE, "begin");

        logger.log(Level.TRACE, "format: " + format);
        logger.log(Level.TRACE, "buffer size: " + bufferSize);
        if (bufferSize < 1) {
            bufferSize = DEFAULT_BUFFER_SIZE;
        }
        AlsaSourceDataLine sourceDataLine = new AlsaSourceDataLine(this, format, bufferSize);
//        sourceDataLine.start();
        logger.log(Level.TRACE, "returning: " + sourceDataLine);

        logger.log(Level.TRACE, "end");

        return sourceDataLine;
    }

    // bufferSize is in bytes!
    @Override
    protected TargetDataLine getTargetDataLine(AudioFormat format, int bufferSize) throws LineUnavailableException {
        logger.log(Level.TRACE, "begin");

        int bufferSizeInBytes = bufferSize * format.getFrameSize();
        AlsaTargetDataLine targetDataLine = new AlsaTargetDataLine(this, format, bufferSizeInBytes);
//        targetDataLine.start();
        logger.log(Level.TRACE, "returning: " + targetDataLine);

        logger.log(Level.TRACE, "end");

        return targetDataLine;
    }

    @Override
    protected Clip getClip(AudioFormat format) throws LineUnavailableException {
        logger.log(Level.TRACE, "begin");

        Clip clip = new TSoftClip(this, format);

        logger.log(Level.TRACE, "end");

        return clip;
    }

    /**
     * direction: should be AlsaPcm.SND_PCM_STREAM_PLAYBACK or
     * AlsaPcm.SND_PCM_STREAM_CAPTURE.
     */
    private List<AudioFormat> getSupportedFormats(int direction) {
        logger.log(Level.TRACE, "begin");

        logger.log(Level.TRACE, "direction: " + direction);

        List<AudioFormat> supportedFormats = new ArrayList<>();
        AlsaPcm alsaPcm;
        try {
            alsaPcm = new AlsaPcm(getPcmName(), direction, 0); // no special mode
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);

            throw new RuntimeException("cannot open pcm");
        }
        AlsaPcmHWParams hwParams = new AlsaPcmHWParams();
        int ret = alsaPcm.getAnyHWParams(hwParams);
        if (ret != 0) {
            logger.log(Level.TRACE, "getAnyHWParams(): " + Alsa.getStringError(ret));
            throw new RuntimeException(Alsa.getStringError(ret));
        }
        AlsaPcmHWParamsFormatMask formatMask = new AlsaPcmHWParamsFormatMask();
        int minChannels = hwParams.getChannelsMin();
        logger.log(Level.TRACE, "min channels: " + minChannels);

        int maxChannels = hwParams.getChannelsMax();
        maxChannels = Math.min(maxChannels, CHANNELS_LIMIT);
        logger.log(Level.TRACE, "max channels: " + maxChannels);

        hwParams.getFormatMask(formatMask);
        for (int i = 0; i < 32; i++) {
            logger.log(Level.TRACE, "checking ALSA format index: " + i);

            if (formatMask.test(i)) {
                logger.log(Level.TRACE, "...supported");

                AudioFormat audioFormat = AlsaUtils.getAlsaFormat(i);

                logger.log(Level.TRACE, "adding AudioFormat: " + audioFormat);

                addChanneledAudioFormats(supportedFormats, audioFormat, minChannels, maxChannels);
//                supportedFormats.add(audioFormat);
            } else {
                logger.log(Level.TRACE, "...not supported");
            }
        }
        // TODO close/free mask & hwParams?
        alsaPcm.close();

        logger.log(Level.TRACE, "end");

        return supportedFormats;
    }

    private static void addChanneledAudioFormats(
            Collection<AudioFormat> collection,
            AudioFormat protoAudioFormat,
            int minChannels,
            int maxChannels) {
        logger.log(Level.TRACE, "begin");

        for (int channels = minChannels; channels <= maxChannels; channels++) {
            AudioFormat channeledAudioFormat = getChanneledAudioFormat(protoAudioFormat, channels);
            logger.log(Level.TRACE, "adding AudioFormat: " + channeledAudioFormat);

            collection.add(channeledAudioFormat);
        }

        logger.log(Level.TRACE, "end");
    }

    // TODO better name
    // TODO calculation of frame size is not perfect
    private static AudioFormat getChanneledAudioFormat(AudioFormat audioFormat, int channels) {
        AudioFormat channeledAudioFormat = new AudioFormat(
                audioFormat.getEncoding(),
                audioFormat.getSampleRate(),
                audioFormat.getSampleSizeInBits(),
                channels,
                (audioFormat.getSampleSizeInBits() / 8) * channels,
                audioFormat.getFrameRate(),
                audioFormat.isBigEndian());
        return channeledAudioFormat;
    }
}
