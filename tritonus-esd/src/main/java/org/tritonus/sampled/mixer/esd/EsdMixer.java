/*
 *  Copyright (c) 1999, 2000 by Matthias Pfisterer
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

package org.tritonus.sampled.mixer.esd;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import org.tritonus.share.GlobalInfo;
import org.tritonus.share.sampled.mixer.TMixer;
import org.tritonus.share.sampled.mixer.TMixerInfo;
import org.tritonus.share.sampled.mixer.TSoftClip;

import static java.lang.System.getLogger;


public class EsdMixer extends TMixer {

    private static final Logger logger = getLogger("org.tritonus.TraceMixer");

    // default buffer size in bytes.
    private static final int DEFAULT_BUFFER_SIZE = 32768;

    private static final AudioFormat[] FORMATS = {
            // hack for testing.
            // new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 11025/*AudioSystem.NOT_SPECIFIED*/, 16, 1, 2, 11025/*AudioSystem.NOT_SPECIFIED*/, false),
            // Formats supported directely by esd.
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 8, 1, 1, AudioSystem.NOT_SPECIFIED, true),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 8, 1, 1, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 8, 2, 2, AudioSystem.NOT_SPECIFIED, true),
            new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, AudioSystem.NOT_SPECIFIED, 8, 2, 2, AudioSystem.NOT_SPECIFIED, false),

            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 1, 2, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 2, 4, AudioSystem.NOT_SPECIFIED, false),

            // Format supported through "simple" conversions.
            // "Simple" conversions are changes in the byte order
            // and changing signed/unsigned for 8 bit.

            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 8, 1, 1, AudioSystem.NOT_SPECIFIED, true),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 8, 1, 1, AudioSystem.NOT_SPECIFIED, false),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 8, 2, 2, AudioSystem.NOT_SPECIFIED, true),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 8, 2, 2, AudioSystem.NOT_SPECIFIED, false),

            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 1, 2, AudioSystem.NOT_SPECIFIED, true),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.NOT_SPECIFIED, 16, 2, 4, AudioSystem.NOT_SPECIFIED, true),
    };

    private static final Line.Info[] SOURCE_LINE_INFOS = {
            new DataLine.Info(SourceDataLine.class, FORMATS, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED),
    };

    private static final Line.Info[] TARGET_LINE_INFOS = {
            new DataLine.Info(TargetDataLine.class, FORMATS, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED),
    };

    public EsdMixer() {
        super(new TMixerInfo(
                        "Esd Mixer",
                        GlobalInfo.getVendor(),
                        "Mixer for the Enlightened Sound Daemon (esd) running on the local machine",
                        GlobalInfo.getVersion()),
                new Line.Info(Mixer.class),
                Arrays.asList(FORMATS),
                Arrays.asList(FORMATS),
                Arrays.asList(SOURCE_LINE_INFOS),
                Arrays.asList(TARGET_LINE_INFOS));
        logger.log(Level.TRACE, "EsdMixer.<init>: begin");

        logger.log(Level.TRACE, "EsdMixer.<init>: end");
    }

    // Line ----

    // TODO allow real close and reopen of mixer
    @Override
    public void open() {
        logger.log(Level.TRACE, "EsdMixer.open(): begin");

        // currently does nothing

        logger.log(Level.TRACE, "EsdMixer.open(): end");
    }

    @Override
    public void close() {
        logger.log(Level.TRACE, "EsdMixer.close(): begin");

        // currently does nothing

        logger.log(Level.TRACE, "EsdMixer.close(): end");
    }

    // Mixer ----

    @Override
    public int getMaxLines(Line.Info info) {
        logger.log(Level.TRACE, "EsdMixer.getMaxLines(): begin");


        int nMaxLines = 0;
        if (info instanceof DataLine.Info) {
            Class<?> lineClass = info.getLineClass();
            if (lineClass == SourceDataLine.class) {
                nMaxLines = 32;
            } else if (lineClass == TargetDataLine.class) {
                nMaxLines = 1;
            } else {
                // DO NOTHING; only source and target lines are supported.
            }
        } else {
            // DO NOTHING; only data lines are supported.
        }

        logger.log(Level.TRACE, "EsdMixer.getMaxLines(): end");

        return nMaxLines;
    }

    // private ----

    /** @param nBufferSize is in bytes! */
    @Override
    protected SourceDataLine getSourceDataLine(AudioFormat format, int nBufferSize) throws LineUnavailableException {
        logger.log(Level.TRACE, "EsdMixer.getSourceDataLine(): begin");

        logger.log(Level.TRACE, "EsdMixer.getSourceDataLine(): format: " + format);
        logger.log(Level.TRACE, "EsdMixer.getSourceDataLine(): buffer size: " + nBufferSize);
        if (nBufferSize < 1) {
            nBufferSize = DEFAULT_BUFFER_SIZE;
        }
//        int nBufferSizeInBytes = nBufferSize * format.getFrameSize();
        EsdSourceDataLine sourceDataLine = new EsdSourceDataLine(this, format, nBufferSize);
        sourceDataLine.start();
        logger.log(Level.TRACE, "EsdMixer.getSourceDataLine(): returning: " + sourceDataLine);

        logger.log(Level.TRACE, "EsdMixer.getSourceDataLine(): end");

        return sourceDataLine;
    }

    /** @param nBufferSize is in bytes! */
    @Override
    protected TargetDataLine getTargetDataLine(AudioFormat format, int nBufferSize) throws LineUnavailableException {
        logger.log(Level.TRACE, "EsdMixer.getTargetDataLine(): begin");

        int nBufferSizeInBytes = nBufferSize * format.getFrameSize();
        EsdTargetDataLine targetDataLine = new EsdTargetDataLine(this, format, nBufferSizeInBytes);
//        registerChannel(sourceDataLine);
        targetDataLine.start();
        logger.log(Level.TRACE, "EsdMixer.getTargetDataLine(): returning: " + targetDataLine);

        logger.log(Level.TRACE, "EsdMixer.getTargetDataLine(): end");

        return targetDataLine;
    }

    @Override
    protected Clip getClip(AudioFormat format) throws LineUnavailableException {
        logger.log(Level.TRACE, "EsdMixer.getClip(): begin");

        Clip clip = new TSoftClip(this, format);

        logger.log(Level.TRACE, "EsdMixer.getClip(): end");

        return clip;
    }
}
