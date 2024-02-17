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
    private String m_strPcmName;

    public static String getDeviceNamePrefix() {
        if (TSettings.AlsaUsePlughw) {
            return "plughw";
        } else {
            return "hw";
        }
    }

    public static String getPcmName(int nCard) {
        String strPcmName = getDeviceNamePrefix() + ":" + nCard;
        if (TSettings.AlsaUsePlughw) {
//            strPcmName += ",0";
        }
        return strPcmName;
    }

    public AlsaDataLineMixer() {
        this(0);
    }

    public AlsaDataLineMixer(int nCard) {
        this(getPcmName(nCard));
    }

    public AlsaDataLineMixer(String strPcmName) {
        super(new TMixerInfo(
                        "Alsa DataLine Mixer (" + strPcmName + ")",
                        GlobalInfo.getVendor(),
                        "Mixer for the Advanced Linux Sound Architecture (card " + strPcmName + ")",
                        GlobalInfo.getVersion()),
                new Line.Info(Mixer.class));
        logger.log(Level.TRACE, "AlsaDataLineMixer.<init>(String): begin.");

        m_strPcmName = strPcmName;
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
        return m_strPcmName;
    }

    // Line ----

    // TODO allow real close and reopen of mixer
    @Override
    public void open() {
        logger.log(Level.TRACE, "AlsaDataLineMixer.open(): begin");

        // currently does nothing

        logger.log(Level.TRACE, "AlsaDataLineMixer.open(): end");
    }

    @Override
    public void close() {
        logger.log(Level.TRACE, "AlsaDataLineMixer.close(): begin");

        // currently does nothing

        logger.log(Level.TRACE, "AlsaDataLineMixer.close(): end");
    }

    // Mixer ----

    @Override
    public int getMaxLines(Line.Info info) {
        logger.log(Level.TRACE, "AlsaDataLineMixer.getMaxLines(): begin");

        // TODO

        logger.log(Level.TRACE, "AlsaDataLineMixer.getMaxLines(): end");

        return 0;
    }

    // private ----

    // nBufferSize is in bytes!
    @Override
    protected SourceDataLine getSourceDataLine(AudioFormat format, int nBufferSize) throws LineUnavailableException {
        logger.log(Level.TRACE, "AlsaDataLineMixer.getSourceDataLine(): begin");

        logger.log(Level.TRACE, "AlsaDataLineMixer.getSourceDataLine(): format: " + format);
        logger.log(Level.TRACE, "AlsaDataLineMixer.getSourceDataLine(): buffer size: " + nBufferSize);
        if (nBufferSize < 1) {
            nBufferSize = DEFAULT_BUFFER_SIZE;
        }
        AlsaSourceDataLine sourceDataLine = new AlsaSourceDataLine(this, format, nBufferSize);
//        sourceDataLine.start();
        logger.log(Level.TRACE, "AlsaDataLineMixer.getSourceDataLine(): returning: " + sourceDataLine);

        logger.log(Level.TRACE, "AlsaDataLineMixer.getSourceDataLine(): end");

        return sourceDataLine;
    }

    // nBufferSize is in bytes!
    @Override
    protected TargetDataLine getTargetDataLine(AudioFormat format, int nBufferSize) throws LineUnavailableException {
        logger.log(Level.TRACE, "AlsaDataLineMixer.getTargetDataLine(): begin");

        int nBufferSizeInBytes = nBufferSize * format.getFrameSize();
        AlsaTargetDataLine targetDataLine = new AlsaTargetDataLine(this, format, nBufferSizeInBytes);
//        targetDataLine.start();
        logger.log(Level.TRACE, "AlsaDataLineMixer.getTargetDataLine(): returning: " + targetDataLine);

        logger.log(Level.TRACE, "AlsaDataLineMixer.getTargetDataLine(): end");

        return targetDataLine;
    }

    @Override
    protected Clip getClip(AudioFormat format) throws LineUnavailableException {
        logger.log(Level.TRACE, "AlsaDataLineMixer.getClip(): begin");

        Clip clip = new TSoftClip(this, format);

        logger.log(Level.TRACE, "AlsaDataLineMixer.getClip(): end");

        return clip;
    }

    /**
     * nDirection: should be AlsaPcm.SND_PCM_STREAM_PLAYBACK or
     * AlsaPcm.SND_PCM_STREAM_CAPTURE.
     */
    private List<AudioFormat> getSupportedFormats(int nDirection) {
        logger.log(Level.TRACE, "AlsaDataLineMixer.getSupportedFormats(): begin");

        logger.log(Level.TRACE, "AlsaDataLineMixer.getSupportedFormats(): direction: " + nDirection);

        List<AudioFormat> supportedFormats = new ArrayList<>();
        AlsaPcm alsaPcm;
        try {
            alsaPcm = new AlsaPcm(
                    getPcmName(),
                    nDirection,
                    0); // no special mode
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);

            throw new RuntimeException("cannot open pcm");
        }
        int nReturn;
        AlsaPcmHWParams hwParams = new AlsaPcmHWParams();
        nReturn = alsaPcm.getAnyHWParams(hwParams);
        if (nReturn != 0) {
            logger.log(Level.TRACE, "AlsaDataLineMixer.getSupportedFormats(): getAnyHWParams(): " + Alsa.getStringError(nReturn));
            throw new RuntimeException(Alsa.getStringError(nReturn));
        }
        AlsaPcmHWParamsFormatMask formatMask = new AlsaPcmHWParamsFormatMask();
        int nMinChannels = hwParams.getChannelsMin();
        logger.log(Level.TRACE, "AlsaDataLineMixer.getSupportedFormats(): min channels: " + nMinChannels);

        int nMaxChannels = hwParams.getChannelsMax();
        nMaxChannels = Math.min(nMaxChannels, CHANNELS_LIMIT);
        logger.log(Level.TRACE, "AlsaDataLineMixer.getSupportedFormats(): max channels: " + nMaxChannels);

        hwParams.getFormatMask(formatMask);
        for (int i = 0; i < 32; i++) {
            logger.log(Level.TRACE, "AlsaDataLineMixer.getSupportedFormats(): checking ALSA format index: " + i);

            if (formatMask.test(i)) {
                logger.log(Level.TRACE, "AlsaDataLineMixer.getSupportedFormats(): ...supported");

                AudioFormat audioFormat = AlsaUtils.getAlsaFormat(i);

                logger.log(Level.TRACE, "AlsaDataLineMixer.getSupportedFormats(): adding AudioFormat: " + audioFormat);

                addChanneledAudioFormats(supportedFormats, audioFormat, nMinChannels, nMaxChannels);
//                supportedFormats.add(audioFormat);
            } else {
                logger.log(Level.TRACE, "AlsaDataLineMixer.getSupportedFormats(): ...not supported");
            }
        }
        // TODO close/free mask & hwParams?
        alsaPcm.close();

        logger.log(Level.TRACE, "AlsaDataLineMixer.getSupportedFormats(): end");

        return supportedFormats;
    }

    private static void addChanneledAudioFormats(
            Collection<AudioFormat> collection,
            AudioFormat protoAudioFormat,
            int nMinChannels,
            int nMaxChannels) {
        logger.log(Level.TRACE, "AlsaDataLineMixer.addChanneledAudioFormats(): begin");

        for (int nChannels = nMinChannels; nChannels <= nMaxChannels; nChannels++) {
            AudioFormat channeledAudioFormat = getChanneledAudioFormat(protoAudioFormat, nChannels);
            logger.log(Level.TRACE, "AlsaDataLineMixer.addChanneledAudioFormats(): adding AudioFormat: " + channeledAudioFormat);

            collection.add(channeledAudioFormat);
        }

        logger.log(Level.TRACE, "AlsaDataLineMixer.addChanneledAudioFormats(): end");
    }

    // TODO better name
    // TODO calculation of frame size is not perfect
    private static AudioFormat getChanneledAudioFormat(AudioFormat audioFormat, int nChannels) {
        AudioFormat channeledAudioFormat = new AudioFormat(
                audioFormat.getEncoding(),
                audioFormat.getSampleRate(),
                audioFormat.getSampleSizeInBits(),
                nChannels,
                (audioFormat.getSampleSizeInBits() / 8) * nChannels,
                audioFormat.getFrameRate(),
                audioFormat.isBigEndian());
        return channeledAudioFormat;
    }
}
