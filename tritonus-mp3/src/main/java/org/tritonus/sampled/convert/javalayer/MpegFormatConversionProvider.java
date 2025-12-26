/*
 *  Copyright (c) 1999 - 2004 by Matthias Pfisterer
 *  Copyright (c) 2008 by Florian Bomers
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

package org.tritonus.sampled.convert.javalayer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.DecoderException;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.Obuffer;
import org.tritonus.share.sampled.AudioUtils;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;
import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider;

import static java.lang.System.getLogger;
import static org.tritonus.share.sampled.TConversionTool.shortToBytes16;


/**
 * ConversionProvider for decoding mp3 files.
 *
 * @author Matthias Pfisterer
 * @author Florian Bomers
 */
public class MpegFormatConversionProvider extends TEncodingFormatConversionProvider {

    private static final Logger logger= getLogger("org.tritonus.TraceAudioConverter");

    public static final AudioFormat.Encoding MPEG1L1 = new AudioFormat.Encoding("MPEG1L1");
    public static final AudioFormat.Encoding MPEG1L2 = new AudioFormat.Encoding("MPEG1L2");
    public static final AudioFormat.Encoding MPEG1L3 = new AudioFormat.Encoding("MPEG1L3");
    public static final AudioFormat.Encoding MP3 = new AudioFormat.Encoding("MP3"); // alias for MPEG1L3
    public static final AudioFormat.Encoding MPEG2L1 = new AudioFormat.Encoding("MPEG2L1");
    public static final AudioFormat.Encoding MPEG2L2 = new AudioFormat.Encoding("MPEG2L2");
    public static final AudioFormat.Encoding MPEG2L3 = new AudioFormat.Encoding("MPEG2L3");
    public static final AudioFormat.Encoding MPEG2DOT5L1 = new AudioFormat.Encoding("MPEG2DOT5L1");
    public static final AudioFormat.Encoding MPEG2DOT5L2 = new AudioFormat.Encoding("MPEG2DOT5L2");
    public static final AudioFormat.Encoding MPEG2DOT5L3 = new AudioFormat.Encoding("MPEG2DOT5L3");

    private static final AudioFormat.Encoding PCM_SIGNED = AudioFormat.Encoding.PCM_SIGNED;

    // TODO mechanism to make the double specification with
    //  different endianess obsolete.
    private static final AudioFormat[] INPUT_FORMATS = {
            // mono
            new AudioFormat(MPEG1L1, -1.0F, -1, 1, -1, -1.0F, false),
            new AudioFormat(MPEG1L1, -1.0F, -1, 1, -1, -1.0F, true),
            // stereo
            new AudioFormat(MPEG1L1, -1.0F, -1, 2, -1, -1.0F, false),
            new AudioFormat(MPEG1L1, -1.0F, -1, 2, -1, -1.0F, true),

            // mono
            new AudioFormat(MPEG1L2, -1.0F, -1, 1, -1, -1.0F, false),
            new AudioFormat(MPEG1L2, -1.0F, -1, 1, -1, -1.0F, true),
            // stereo
            new AudioFormat(MPEG1L2, -1.0F, -1, 2, -1, -1.0F, false),
            new AudioFormat(MPEG1L2, -1.0F, -1, 2, -1, -1.0F, true),

            // mono
            new AudioFormat(MPEG1L3, -1.0F, -1, 1, -1, -1.0F, false),
            new AudioFormat(MPEG1L3, -1.0F, -1, 1, -1, -1.0F, true),
            // stereo
            new AudioFormat(MPEG1L3, -1.0F, -1, 2, -1, -1.0F, false),
            new AudioFormat(MPEG1L3, -1.0F, -1, 2, -1, -1.0F, true),

            // mono
            new AudioFormat(MP3, -1.0F, -1, 1, -1, -1.0F, false),
            new AudioFormat(MP3, -1.0F, -1, 1, -1, -1.0F, true),
            // stereo
            new AudioFormat(MP3, -1.0F, -1, 2, -1, -1.0F, false),
            new AudioFormat(MP3, -1.0F, -1, 2, -1, -1.0F, true),

            // mono
            new AudioFormat(MPEG2L1, -1.0F, -1, 1, -1, -1.0F, false),
            new AudioFormat(MPEG2L1, -1.0F, -1, 1, -1, -1.0F, true),
            // stereo
            new AudioFormat(MPEG2L1, -1.0F, -1, 2, -1, -1.0F, false),
            new AudioFormat(MPEG2L1, -1.0F, -1, 2, -1, -1.0F, true),

            // mono
            new AudioFormat(MPEG2L2, -1.0F, -1, 1, -1, -1.0F, false),
            new AudioFormat(MPEG2L2, -1.0F, -1, 1, -1, -1.0F, true),
            // stereo
            new AudioFormat(MPEG2L2, -1.0F, -1, 2, -1, -1.0F, false),
            new AudioFormat(MPEG2L2, -1.0F, -1, 2, -1, -1.0F, true),

            // mono
            new AudioFormat(MPEG2L3, -1.0F, -1, 1, -1, -1.0F, false),
            new AudioFormat(MPEG2L3, -1.0F, -1, 1, -1, -1.0F, true),
            // stereo
            new AudioFormat(MPEG2L3, -1.0F, -1, 2, -1, -1.0F, false),
            new AudioFormat(MPEG2L3, -1.0F, -1, 2, -1, -1.0F, true),

            // mono
            new AudioFormat(MPEG2DOT5L1, -1.0F, -1, 1, -1, -1.0F, false),
            new AudioFormat(MPEG2DOT5L1, -1.0F, -1, 1, -1, -1.0F, true),
            // stereo
            new AudioFormat(MPEG2DOT5L1, -1.0F, -1, 2, -1, -1.0F, false),
            new AudioFormat(MPEG2DOT5L1, -1.0F, -1, 2, -1, -1.0F, true),

            // mono
            new AudioFormat(MPEG2DOT5L2, -1.0F, -1, 1, -1, -1.0F, false),
            new AudioFormat(MPEG2DOT5L2, -1.0F, -1, 1, -1, -1.0F, true),
            // stereo
            new AudioFormat(MPEG2DOT5L2, -1.0F, -1, 2, -1, -1.0F, false),
            new AudioFormat(MPEG2DOT5L2, -1.0F, -1, 2, -1, -1.0F, true),

            // mono
            new AudioFormat(MPEG2DOT5L3, -1.0F, -1, 1, -1, -1.0F, false),
            new AudioFormat(MPEG2DOT5L3, -1.0F, -1, 1, -1, -1.0F, true),
            // stereo
            new AudioFormat(MPEG2DOT5L3, -1.0F, -1, 2, -1, -1.0F, false),
            new AudioFormat(MPEG2DOT5L3, -1.0F, -1, 2, -1, -1.0F, true),
    };

    private static final AudioFormat[] OUTPUT_FORMATS = {
            // mono, 16 bit signed
            new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, false),
            new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, true),

            // stereo, 16 bit signed
            new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, false),
            new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, true),

//            // 24 and 32 bit not yet possible
//            // mono, 24 bit signed
//            new AudioFormat(PCM_SIGNED, -1.0F, 24, 1, 3, -1.0F, false),
//            new AudioFormat(PCM_SIGNED, -1.0F, 24, 1, 3, -1.0F, true),
//
//            // stereo, 24 bit signed
//            new AudioFormat(PCM_SIGNED, -1.0F, 24, 2, 6, -1.0F, false),
//            new AudioFormat(PCM_SIGNED, -1.0F, 24, 2, 6, -1.0F, true),
//
//            // mono, 32 bit signed
//            new AudioFormat(PCM_SIGNED, -1.0F, 32, 1, 4, -1.0F, false),
//            new AudioFormat(PCM_SIGNED, -1.0F, 32, 1, 4, -1.0F, true),
//
//            // stereo, 32 bit signed
//            new AudioFormat(PCM_SIGNED, -1.0F, 32, 2, 8, -1.0F, false),
//            new AudioFormat(PCM_SIGNED, -1.0F, 32, 2, 8, -1.0F, true),
    };

    /**
     * Constructor.
     */
    public MpegFormatConversionProvider() {
        super(List.of(INPUT_FORMATS), List.of(OUTPUT_FORMATS));
        logger.log(Level.TRACE, "MpegFormatConversionProvider()");
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        AudioFormat sourceFormat = audioInputStream.getFormat();

        logger.log(Level.TRACE, "begin");
        logger.log(Level.TRACE, "trying to convert");
        logger.log(Level.TRACE, "\tfrom: " + sourceFormat);
        logger.log(Level.TRACE, "\tto: " + targetFormat);

        targetFormat = getFullyQualifiedTargetFormat(targetFormat, sourceFormat, false);
        if (targetFormat != null) {
            logger.log(Level.TRACE, "< OK");

            return new DecodedMpegAudioInputStream(targetFormat, audioInputStream);
        }
        logger.log(Level.TRACE, "< not supported");

        throw new IllegalArgumentException("conversion not supported");
    }

    private AudioFormat getFullyQualifiedTargetFormat(AudioFormat targetFormat, AudioFormat sourceFormat, boolean allowUnspecified) {
        // check that sourceFormat and targetFormat are in list of supported formats
        if (!super.isConversionSupported(targetFormat.getEncoding(), sourceFormat)) {
            logger.log(Level.TRACE, "cannot convert: super.isConversionSupported()==false");

            return null;
        }

        // make it simple: we can only convert to PCM_SIGNED,
        // therefore, just fill in the missing fields
        if (!targetFormat.getEncoding().equals(PCM_SIGNED)) {
            logger.log(Level.TRACE, "cannot convert: target is not PCM_SIGNED");

            return null;
        }

        // some values are never allowed
        if (sourceFormat.getChannels() > 2
                || targetFormat.getChannels() > 2
                || sourceFormat.getChannels() == 0
                || targetFormat.getChannels() == 0
                || sourceFormat.getSampleRate() == 0
                || targetFormat.getSampleRate() == 0) {
            logger.log(Level.TRACE, "cannot convert: channels or sample rate out of bounds");

            return null;
        }

        // check channels
        if (sourceFormat.getChannels() < 0) {
            if (allowUnspecified) {
                // both channel fields must be -1
                if (targetFormat.getChannels() >= 0) {
                    // cannot convert a non-specified channel number to a different specified channel
                    logger.log(Level.TRACE, "cannot convert: cannot any to specific channels");

                    return null;
                }
            } else {
                // do not allow source channels = -1

                logger.log(Level.TRACE, "cannot convert: channels cannot be AudioSystem.NOT_SPECIFIED");

                return null;
            }
        } else {
            // if target channels are given, they must equal source channels
            if (targetFormat.getChannels() > 0 && targetFormat.getChannels() != sourceFormat.getChannels()) {
                // cannot convert a specified channel number to a different specified channel
                logger.log(Level.TRACE, "cannot convert: specified channel number must be the same");

                return null;
            }
        }

        // check sample rate
        if (sourceFormat.getSampleRate() < 0) {
            if (allowUnspecified) {
                // both SampleRate fields must be -1
                if (targetFormat.getSampleRate() >= 0) {
                    // cannot convert a non-specified SampleRate to a different specified SampleRate
                    logger.log(Level.TRACE, "cannot convert any to specific sample rate");

                    return null;
                }
            } else {
                // do not allow SampleRate = -1
                logger.log(Level.TRACE, "cannot convert: source sample rate is NOT_SPECIFIED");

                return null;
            }
        } else {
            // if target SampleRate is given, must equal source SampleRate
            if (targetFormat.getSampleRate() > 0 && targetFormat.getSampleRate() != sourceFormat.getSampleRate()) {
                // cannot convert a specified SampleRate to a different specified SampleRate
                logger.log(Level.TRACE, "cannot convert sample rate");

                return null;
            }
        }

        // check sample size
        if (targetFormat.getSampleSizeInBits() != 16) {
            logger.log(Level.TRACE, "cannot convert: source sample width is not 16");

            return null;
        }

        return new AudioFormat(
                PCM_SIGNED,
                sourceFormat.getSampleRate(),
                targetFormat.getSampleSizeInBits(),
                sourceFormat.getChannels(),
                AudioUtils.getFrameSize(sourceFormat.getChannels(), targetFormat.getSampleSizeInBits()),
                sourceFormat.getSampleRate(),
                targetFormat.isBigEndian(),
                targetFormat.properties());
    }

    @Override
    public boolean isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat) {
        logger.log(Level.TRACE, "begin");
        logger.log(Level.TRACE, "checking if conversion possible");
        logger.log(Level.TRACE, "from: " + sourceFormat);
        logger.log(Level.TRACE, "to: " + targetFormat);
        AudioFormat format = getFullyQualifiedTargetFormat(targetFormat, sourceFormat, true);
        boolean supported = (format != null);

        logger.log(Level.TRACE, "result=" + supported);

        return supported;
    }

    public static class DecodedMpegAudioInputStream extends TAsynchronousFilteredAudioInputStream {

        private final InputStream encodedStream;
        private final Bitstream bitstream;
        private final Decoder decoder;
        private final DMAISObuffer oBuffer;

        public DecodedMpegAudioInputStream(AudioFormat outputFormat, AudioInputStream inputStream) {
            // TODO try to find out length (possible?)
            super(outputFormat, AudioSystem.NOT_SPECIFIED);
            encodedStream = inputStream;
            bitstream = new Bitstream(inputStream);
            decoder = new Decoder(null);
            oBuffer = new DMAISObuffer(outputFormat.getChannels());
            decoder.setOutputBuffer(oBuffer);
        }

        @Override
        public void execute() {
            try {
                Header header = bitstream.readFrame();
                if (header == null) {
                    logger.log(Level.TRACE, "header is null (end of mpeg stream)");

                    getCircularBuffer().close();
                    return;
                }
                decoder.decodeFrame(header, bitstream);
                bitstream.closeFrame();
                getCircularBuffer().write(oBuffer.getBuffer(), 0, oBuffer.getCurrentBufferSize());
                oBuffer.reset();
            } catch (BitstreamException | DecoderException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }

        protected boolean isBigEndian() {
            return getFormat().isBigEndian();
        }

        @Override
        public void close() throws IOException {
            super.close();
            encodedStream.close();
        }

        private class DMAISObuffer extends Obuffer {

            private final int channels;
            private final byte[] buffer;
            private final int[] bufferPointers;
            private final boolean isBigEndian;

            public DMAISObuffer(int channels) {
                this.channels = channels;
                buffer = new byte[OBUFFERSIZE * channels];
                bufferPointers = new int[channels];
                reset();
                isBigEndian = DecodedMpegAudioInputStream.this.isBigEndian();
            }

            @Override
            public void append(int channel, short value) {
                shortToBytes16(value, buffer, bufferPointers[channel], isBigEndian);
                bufferPointers[channel] += channels * 2;
            }

            @Override
            public void setStopFlag() {
            }

            @Override
            public void close() {
            }

            @Override
            public void writeBuffer(int value) {
            }

            @Override
            public void clearBuffer() {
            }

            public byte[] getBuffer() {
                return buffer;
            }

            public int getCurrentBufferSize() {
                return bufferPointers[0];
            }

            public void reset() {
                for (int i = 0; i < channels; i++) {
                    // Points to byte location,
                    // implicitly assuming 16 bit samples.
                    bufferPointers[i] = i * 2;
                }
            }
        }
    }
}
