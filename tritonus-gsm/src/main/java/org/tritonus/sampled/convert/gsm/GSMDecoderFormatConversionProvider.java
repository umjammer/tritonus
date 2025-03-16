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

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.tritonus.lowlevel.gsm.GSMDecoder;
import org.tritonus.lowlevel.gsm.GsmConstants;
import org.tritonus.lowlevel.gsm.GsmFrameFormat;
import org.tritonus.lowlevel.gsm.InvalidGSMFrameException;
import org.tritonus.share.sampled.AudioFormats;
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
public class GSMDecoderFormatConversionProvider extends TSimpleFormatConversionProvider
        /* TEncodingFormatConversionProvider */ implements GsmConstants {

    private static final Logger logger = getLogger("org.tritonus.TraceAudioConverter");

    /**
     * Debugging (profiling) hack.
     */
    private static final boolean MEASURE_DECODING_TIME = false;

    private static final AudioFormat[] SOURCE_FORMATS = {
            new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, -1, 1, 33, 50.0F, false),
            new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, -1, 1, 33, 50.0F, true),
            new AudioFormat(MS_GSM_ENCODING, 8000.0F, -1, 1, 65, 25.0F, false),
            new AudioFormat(MS_GSM_ENCODING, 8000.0F, -1, 1, 65, 25.0F, true),
    };

    private static final AudioFormat[] TARGET_FORMATS = {
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000.0F, 16, 1, 2,
                    8000.0F, false),
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 8000.0F, 16, 1, 2,
                    8000.0F, true)
    };

    /**
     * Constructor.
     */
    public GSMDecoderFormatConversionProvider() {
        super(List.of(SOURCE_FORMATS), List.of(TARGET_FORMATS));
        logger.log(Level.TRACE, "begin");

        logger.log(Level.TRACE, "end");
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        logger.log(Level.TRACE, "begin");
        logger.log(Level.TRACE, "checking if conversion supported");
        logger.log(Level.TRACE, "from: " + audioInputStream.getFormat());
        logger.log(Level.TRACE, "to: " + targetFormat);

        targetFormat = getDefaultTargetFormat(targetFormat, audioInputStream.getFormat());
        if (isConversionSupported(targetFormat, audioInputStream.getFormat())) {
            logger.log(Level.TRACE, "conversion supported; trying to create DecodedGSMAudioInputStream");
            return new DecodedGSMAudioInputStream(targetFormat, audioInputStream);
        } else {
            logger.log(Level.TRACE, "conversion not supported; throwing IllegalArgumentException");
            throw new IllegalArgumentException("conversion not supported");
        }
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
     * AudioInputStream returned on decoding of GSM. An instance of this class
     * is returned if you call AudioSystem.getAudioInputStream(AudioFormat,
     * AudioInputStream) to decode a GSM stream. This class contains the logic
     * of maintaining buffers and calling the decoder.
     */
    private static class DecodedGSMAudioInputStream extends TAsynchronousFilteredAudioInputStream {

        /*
         * Seems like DataInputStream (opposite to InputStream) is only needed
         * for readFully(). readFully-behaviour should perhaps be implemented in
         * AudioInputStream anyway (so this construct may become obsolete).
         */
        private final DataInputStream encodedStream;
        private final GSMDecoder decoder;

        /**
         * Holds one encoded GSM frame.
         */
        private final byte[] frameBuffer;
        private final byte[] sampleBuffer;

        public DecodedGSMAudioInputStream(AudioFormat outputFormat, AudioInputStream inputStream) {
            super(outputFormat, inputStream.getFrameLength() == AudioSystem.NOT_SPECIFIED
                            ? AudioSystem.NOT_SPECIFIED
                            : inputStream.getFrameLength() * getSamplesPerFrame(inputStream.getFormat()));
            logger.log(Level.TRACE, "begin");

            encodedStream = new DataInputStream(inputStream);
            GsmFrameFormat gsmFrameFormat = getGsmFrameFormat(inputStream.getFormat());
            int sampleBufferSize = gsmFrameFormat.getSamplesPerFrame() * 2;
            decoder = new GSMDecoder(gsmFrameFormat);
            frameBuffer = new byte[inputStream.getFormat().getFrameSize()];
            sampleBuffer = new byte[sampleBufferSize];

            logger.log(Level.TRACE, "end");
        }

        @Override
        public void execute() {
            logger.log(Level.TRACE, "begin");

            try {
                encodedStream.readFully(frameBuffer);
            } catch (IOException e) {
                // Not only errors, but also EOF is caught here.
                logger.log(Level.ERROR, e.getMessage(), e);

                getCircularBuffer().close();
                return;
            }

            try {
                long timestamp1;
                long timestamp2;
                if (MEASURE_DECODING_TIME) {
                    timestamp1 = System.currentTimeMillis();
                }
                decoder.decode(frameBuffer, 0, sampleBuffer, 0, isBigEndian());
                // testing test hack
//                m_abBuffer[0] = 0;
                if (MEASURE_DECODING_TIME) {
                    timestamp2 = System.currentTimeMillis();
                    logger.log(Level.DEBUG, "GSM decode (ms): " + (timestamp2 - timestamp1));
                }
            } catch (InvalidGSMFrameException e) {
                logger.log(Level.ERROR, e.getMessage(), e);

                getCircularBuffer().close();
                return;
            }

            getCircularBuffer().write(sampleBuffer);
            logger.log(Level.TRACE, "decoded GSM frame written");

            logger.log(Level.TRACE, "end");
        }

        private static int getSamplesPerFrame(AudioFormat audioFormat) {
            return getGsmFrameFormat(audioFormat).getSamplesPerFrame();
        }

        private static GsmFrameFormat getGsmFrameFormat(AudioFormat audioFormat) {
            if (audioFormat.getEncoding().equals(MS_GSM_ENCODING)) {
                return GsmFrameFormat.MICROSOFT;
            } else if (audioFormat.getEncoding().equals(TOAST_GSM_ENCODING)) {
                return GsmFrameFormat.TOAST;
            } else {
                throw new RuntimeException("Unknown GSM frame format");
            }
        }

        private boolean isBigEndian() {
            return getFormat().isBigEndian();
        }

        @Override
        public void close() throws IOException {
            super.close();
            encodedStream.close();
        }
    }
}
