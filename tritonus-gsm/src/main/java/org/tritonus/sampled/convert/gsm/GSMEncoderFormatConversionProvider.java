/*
 *  Copyright (c) 1999 - 2001 by Matthias Pfisterer
 *  Copyright (c) 2001 by Florian Bomers
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

package org.tritonus.sampled.convert.gsm;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.tritonus.lowlevel.gsm.Encoder;
import org.tritonus.lowlevel.gsm.GsmConstants;
import org.tritonus.lowlevel.gsm.GsmFrameFormat;
import org.tritonus.share.sampled.AudioFormats;
import org.tritonus.share.sampled.TConversionTool;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;
import org.tritonus.share.sampled.convert.TSimpleFormatConversionProvider;

import static java.lang.System.getLogger;
import static org.tritonus.sampled.convert.gsm.GsmEncodings.MS_GSM_ENCODING;
import static org.tritonus.sampled.convert.gsm.GsmEncodings.TOAST_GSM_ENCODING;


/**
 * FormatConversionProvider for GSM 06.10.
 *
 * @author Matthias Pfisterer
 */
public class GSMEncoderFormatConversionProvider extends TSimpleFormatConversionProvider
        /* extends TEncodingFormatConversionProvider */ implements GsmConstants {

    private static final Logger logger = getLogger("org.tritonus.TraceAudioConverter");

    private static final AudioFormat[] SOURCE_FORMATS = {
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, false),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, true),};

    private static final AudioFormat[] TARGET_FORMATS = {
            new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, -1, 1, 33, 50.0F, false),
            new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, -1, 1, 33, 50.0F, true),
            new AudioFormat(MS_GSM_ENCODING, 8000.0F, -1, 1, 65, 25.0F, false),
            new AudioFormat(MS_GSM_ENCODING, 8000.0F, -1, 1, 65, 25.0F, true),
    };

    private static final int DECODED_BYTES_PER_FRAME = 2;

    /**
     * Constructor.
     */
    public GSMEncoderFormatConversionProvider() {
        super(List.of(SOURCE_FORMATS), List.of(TARGET_FORMATS));
        logger.log(Level.TRACE, "GSMFormatConversionProvider.<init>(): begin");

        logger.log(Level.TRACE, "GSMFormatConversionProvider.<init>(): end");
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        logger.log(Level.TRACE, "GSMFormatConversionProvider.getAudioInputStream(): begin");
        logger.log(Level.TRACE, "GSMFormatConversionProvider.getAudioInputStream():");
        logger.log(Level.TRACE, "checking if conversion supported");
        logger.log(Level.TRACE, "from: " + audioInputStream.getFormat());
        logger.log(Level.TRACE, "to: " + targetFormat);

        targetFormat = getDefaultTargetFormat(targetFormat, audioInputStream.getFormat());
        if (isConversionSupported(targetFormat, audioInputStream.getFormat())) {
            logger.log(Level.TRACE, "GSMFormatConversionProvider.getAudioInputStream():");
            logger.log(Level.TRACE, "conversion supported; trying to create EncodedGSMAudioInputStream");
            return new EncodedGSMAudioInputStream(targetFormat, audioInputStream);
        } else {
            logger.log(Level.TRACE, "GSMFormatConversionProvider.getAudioInputStream():");
            logger.log(Level.TRACE, "conversion not supported; throwing IllegalArgumentException");
            throw new IllegalArgumentException("conversion not supported");
        }
        // TODO this is unreachable
//logger.log(Level.TRACE, "end");
    }

    protected AudioFormat getDefaultTargetFormat(AudioFormat targetFormat, AudioFormat sourceFormat) {
        // return first of the matching formats
        // pre-condition: the predefined target formats (FORMATS2) must be
        // well-defined !
        for (AudioFormat format : getCollectionTargetFormats()) {
            if (AudioFormats.matches(targetFormat, format)) {
                return format;
            }
        }
        throw new IllegalArgumentException("conversion not supported");
    }

    /**
     * AudioInputStream returned on encoding of GSM. An instance of this class
     * is returned if you call AudioSystem.getAudioInputStream(AudioFormat,
     * AudioInputStream) to encode data to GSM. This class contains the logic of
     * maintaining buffers and calling the encoder.
     */
    private static class EncodedGSMAudioInputStream extends TAsynchronousFilteredAudioInputStream {

        private final AudioInputStream decodedStream;
        private final GsmFrameFormat gsmFrameFormat;
        private final Encoder encoder;

        /**
         * Holds one block of decoded data.
         */
        private final byte[] buffer;

        /**
         * Holds one block of decoded data.
         */
        private final short[] bufferS;

        /**
         * Holds one encoded GSM frame.
         */
        private final byte[] frameBuffer;

        public EncodedGSMAudioInputStream(AudioFormat targetFormat, AudioInputStream inputStream) {
            super(targetFormat, getTargetFrameLength(inputStream.getFrameLength(), targetFormat));
            logger.log(Level.TRACE, "begin");

            decodedStream = inputStream;
            gsmFrameFormat = getGsmFrameFormat(targetFormat);
            encoder = new Encoder(gsmFrameFormat);
            buffer = new byte[gsmFrameFormat.getSamplesPerFrame() * DECODED_BYTES_PER_FRAME];
            bufferS = new short[gsmFrameFormat.getSamplesPerFrame()];
            frameBuffer = new byte[targetFormat.getFrameSize()];

            logger.log(Level.TRACE, "end");
        }

        private static long getTargetFrameLength(long sourceFrameLength, AudioFormat targetFormat) {
            // $$fb 2001-04-16: FrameLength gives the number of 33-byte blocks !
//            inputStream.getFrameLength() == AudioSystem.NOT_SPECIFIED
//                    ? AudioSystem.NOT_SPECIFIED
//                    : inputStream.getFrameLength() / 160 * 33);
            return sourceFrameLength == AudioSystem.NOT_SPECIFIED
                    ? AudioSystem.NOT_SPECIFIED
                    : sourceFrameLength / getGsmFrameFormat(targetFormat).getSamplesPerFrame();
        }

        private static GsmFrameFormat getGsmFrameFormat(AudioFormat targetFormat) {
            if (targetFormat.getEncoding().equals(MS_GSM_ENCODING)) {
                return GsmFrameFormat.MICROSOFT;
            } else {
                return GsmFrameFormat.TOAST;
            }
        }

        @Override
        public void execute() {
            logger.log(Level.TRACE, "begin");

            try {
                int read = decodedStream.read(buffer);
                // Currently, we take all kinds of errors as end of stream.
                if (read != buffer.length) {
                    logger.log(Level.TRACE, "not read whole 160 sample block (" + read + ")");
                    getCircularBuffer().close();
                    return;
                }
            } catch (IOException e) {
                logger.log(Level.ERROR, e.getMessage(), e);

                getCircularBuffer().close();
                logger.log(Level.TRACE, "<");

                return;
            }
            for (int i = 0; i < gsmFrameFormat.getSamplesPerFrame(); i++) {
                bufferS[i] = TConversionTool.bytesToShort16(buffer, i * DECODED_BYTES_PER_FRAME, isBigEndian());
            }
            encoder.encode(bufferS, frameBuffer);
            getCircularBuffer().write(frameBuffer);
            logger.log(Level.TRACE, "encoded GSM frame written");

            logger.log(Level.TRACE, "end");
        }

        private boolean isBigEndian() {
            return decodedStream.getFormat().isBigEndian();
        }

        @Override
        public void close() throws IOException {
            super.close();
            decodedStream.close();
        }
    }
}
