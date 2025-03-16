/*
 *  Copyright (c) 2003 by Matthias Pfisterer
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
 * ---
 *
 * The code doing the encoding and decoding is based on C code with the
 * following copyright:
 *
 * Copyright 1992 by Stichting Mathematisch Centrum, Amsterdam, The
 * Netherlands.
 *
 *                        All Rights Reserved
 *
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose and without fee is hereby granted,
 * provided that the above copyright notice appear in all copies and that
 * both that copyright notice and this permission notice appear in
 * supporting documentation, and that the names of Stichting Mathematisch
 * Centrum or CWI not be used in advertising or publicity pertaining to
 * distribution of the software without specific, written prior permission.
 *
 * STICHTING MATHEMATISCH CENTRUM DISCLAIMS ALL WARRANTIES WITH REGARD TO
 * THIS SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS, IN NO EVENT SHALL STICHTING MATHEMATISCH CENTRUM BE LIABLE
 * FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT
 * OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 */

package org.tritonus.sampled.convert;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.tritonus.share.sampled.AudioFormats;
import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider;
import org.tritonus.share.sampled.convert.TSynchronousFilteredAudioInputStream;

import static java.lang.System.getLogger;


/**
 * IMA ADPCM encoder and decoder.
 *
 * @author Matthias Pfisterer
 */
public class ImaAdpcmFormatConversionProvider extends TEncodingFormatConversionProvider {

    private static final Logger logger = getLogger("org.tritonus.TraceAudioConverter");

    // only used as abbreviation
    private static final AudioFormat.Encoding IMA_ADPCM = new AudioFormat.Encoding("IMA_ADPCM");
    private static final AudioFormat.Encoding PCM_SIGNED = new AudioFormat.Encoding("PCM_SIGNED");

    private static final AudioFormat[] INPUT_FORMATS = {
            // mono
            new AudioFormat(IMA_ADPCM, -1.0F, 4, 1, -1, -1.0F, false),
            new AudioFormat(IMA_ADPCM, -1.0F, 4, 1, -1, -1.0F, true),
            // mono, 16 bit signed
            new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, false),
            new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, true),
    };

//    private static final AudioFormat[] OUTPUT_FORMATS = {
//            // mono, 16 bit signed
//            new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, false),
//            new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, true),
//    };

    static final int[] indexTable = {
            -1, -1, -1, -1, 2, 4, 6, 8,
            -1, -1, -1, -1, 2, 4, 6, 8,
    };

    static final int[] stepSizeTable = {
            7, 8, 9, 10, 11, 12, 13, 14, 16, 17,
            19, 21, 23, 25, 28, 31, 34, 37, 41, 45,
            50, 55, 60, 66, 73, 80, 88, 97, 107, 118,
            130, 143, 157, 173, 190, 209, 230, 253, 279, 307,
            337, 371, 408, 449, 494, 544, 598, 658, 724, 796,
            876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066,
            2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358,
            5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
            15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, 32767
    };

    /**
     * Constructor.
     */
    public ImaAdpcmFormatConversionProvider() {
        super(List.of(INPUT_FORMATS),
                List.of(INPUT_FORMATS)
                // true, // new behaviour
                // false // bidirectional .. constants UNIDIR../BIDIR..?
        );
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        // The AudioInputStream to return.
        AudioInputStream convertedAudioInputStream;

        logger.log(Level.TRACE, ">ImaAdpcmFormatConversionProvider.getAudioInputStream(): begin");
        logger.log(Level.TRACE, "checking if conversion supported");
        logger.log(Level.TRACE, "from: " + audioInputStream.getFormat());
        logger.log(Level.TRACE, "to: " + targetFormat);

        // what is this ???
        targetFormat = getDefaultTargetFormat(targetFormat, audioInputStream.getFormat());
        if (isConversionSupported(targetFormat, audioInputStream.getFormat())) {
            if (targetFormat.getEncoding().equals(IMA_ADPCM)) {
                logger.log(Level.TRACE, "conversion supported; trying to create EncodedImaAdpcmAudioInputStream");

                convertedAudioInputStream = new EncodedImaAdpcmAudioInputStream(audioInputStream, targetFormat);
            } else {
                logger.log(Level.TRACE, "conversion supported; trying to create DecodedImaAdpcmAudioInputStream");

                convertedAudioInputStream = new DecodedImaAdpcmAudioInputStream(audioInputStream, targetFormat);
            }
        } else {
            logger.log(Level.TRACE, "<conversion not supported; throwing IllegalArgumentException");

            throw new IllegalArgumentException("conversion not supported");
        }

        logger.log(Level.TRACE, "<ImaAdpcmFormatConversionProvider.getAudioInputStream(): end");

        return convertedAudioInputStream;
    }

    // TODO recheck !!
    protected AudioFormat getDefaultTargetFormat(AudioFormat targetFormat, AudioFormat sourceFormat) {
        logger.log(Level.TRACE, "target format: " + targetFormat);

        logger.log(Level.TRACE, "source format: " + sourceFormat);

        AudioFormat newTargetFormat = null;
        // return first of the matching formats
        // pre-condition: the predefined target formats (FORMATS2) must be well-defined !
        for (AudioFormat format : getCollectionTargetFormats()) {
            if (AudioFormats.matches(targetFormat, format)) {
                newTargetFormat = format;
            }
        }
        if (newTargetFormat == null) {
            throw new IllegalArgumentException("conversion not supported");
        }

        logger.log(Level.TRACE, "new target format: " + newTargetFormat);

        // hacked together...
        // ... only works for PCM target encoding ...
        newTargetFormat = new AudioFormat(targetFormat.getEncoding(),
                sourceFormat.getSampleRate(),
                newTargetFormat.getSampleSizeInBits(),
                newTargetFormat.getChannels(),
                newTargetFormat.getFrameSize(),
                sourceFormat.getSampleRate(),
                newTargetFormat.isBigEndian());

        logger.log(Level.TRACE, "really new target format: " + newTargetFormat);

        return newTargetFormat;
    }

    /**
     * AudioInputStream returned on decoding of IMA ADPCM.
     * An instance of this class is returned if you call
     * AudioSystem.getAudioInputStream(AudioFormat, AudioInputStream)
     * to decode an IMA ADPCM stream.
     * <p>
     * Class should be private, but is public due to a bug (?) in the
     * aspectj compiler.
     */
    /* private */ public static class DecodedImaAdpcmAudioInputStream extends TSynchronousFilteredAudioInputStream {

        private final ImaAdpcmState state;

        /**
         * Constructor.
         */
        public DecodedImaAdpcmAudioInputStream(AudioInputStream encodedStream, AudioFormat outputFormat) {
            super(encodedStream, outputFormat);
            logger.log(Level.TRACE, "begin");

            state = new ImaAdpcmState();

            logger.log(Level.TRACE, "end");
        }

        @Override
        protected int convert(byte[] inBuffer, byte[] outBuffer, int outByteOffset, int inFrameCount) {
            logger.log(Level.TRACE, "begin");

            int inp; // Input buffer pointer
            int outP; // output buffer pointer
            int sign; // Current adpcm sign bit
            int delta; // Current adpcm output value
            int step; // Step size
            int pred; // Predicted value
            int vpDiff; // Current change to pred
            int index; // Current step change index
            int inputBuffer = 0; // place to keep next 4-bit value
            boolean bufferStep; // toggle between inputBuffer/input
            int len = inFrameCount;

            inp = 0;
            outP = outByteOffset;

            pred = state.prev;
            index = state.index;
            step = stepSizeTable[index];

            bufferStep = false;

            for (; len > 0; len--) {
                // Step 1 - get the delta value
                if (bufferStep) {
                    delta = inputBuffer & 0xf;
                } else {
                    inputBuffer = inBuffer[inp];
                    inp++;
                    delta = (inputBuffer >> 4) & 0xf;
                }
                bufferStep = !bufferStep;

                // Step 2 - Find new index value (for later)
                index += indexTable[delta];
                if (index < 0) index = 0;
                if (index > 88) index = 88;

                // Step 3 - Separate sign and magnitude
                sign = delta & 8;
                delta = delta & 7;

                // Step 4 - Compute difference and new predicted value
                //
                // Computes 'vpDiff = (delta+0.5)*step/4', but see comment
                // in adpcm_coder.
                vpDiff = step >> 3;
                if ((delta & 4) != 0)
                    vpDiff += step;
                if ((delta & 2) != 0)
                    vpDiff += step >> 1;
                if ((delta & 1) != 0)
                    vpDiff += step >> 2;

                if (sign != 0)
                    pred -= vpDiff;
                else
                    pred += vpDiff;

                // Step 5 - clamp output value
                if (pred > 32767)
                    pred = 32767;
                else if (pred < -32768)
                    pred = -32768;

                // Step 6 - Update step value
                step = stepSizeTable[index];

                // Step 7 - Output value
                // *outP++ = pred;
                if (isBigEndian()) {
                    outBuffer[outP++] = (byte) (pred >> 8);
                    outBuffer[outP++] = (byte) (pred & 0xFF);
                } else {
                    outBuffer[outP++] = (byte) (pred & 0xFF);
                    outBuffer[outP++] = (byte) (pred >> 8);
                }
            }

            state.prev = pred;
            state.index = index;

            logger.log(Level.TRACE, "end");

            return inFrameCount;
        }

        /**  */
        protected int getSampleSizeInBytes() {
            return getFormat().getFrameSize() / getFormat().getChannels();
        }

        /**  */
        protected int getFrameSize() {
            return getFormat().getFrameSize();
        }

        /**
         * Returns if this stream (the decoded one) is big endian.
         *
         * @return true if this stream is big endian.
         */
        private boolean isBigEndian() {
            return getFormat().isBigEndian();
        }
    }

    /**
     * AudioInputStream returned on encoding to IMA ADPCM.
     * An instance of this class is returned if you call
     * AudioSystem.getAudioInputStream(AudioFormat, AudioInputStream)
     * to encode to a IMA ADPCM stream.
     * <p>
     * TODO Class should be private, but is public due to a bug (?) in the
     *  aspectj compiler.
     */
    /* private */ public static class EncodedImaAdpcmAudioInputStream extends TSynchronousFilteredAudioInputStream {

        private final ImaAdpcmState state;

        /**
         * Constructor.
         */
        public EncodedImaAdpcmAudioInputStream(AudioInputStream decodedStream, AudioFormat outputFormat) {
            super(decodedStream, outputFormat);
            logger.log(Level.TRACE, "begin");

            state = new ImaAdpcmState();
            logger.log(Level.TRACE, "end");

        }

        @Override
        protected int convert(byte[] inBuffer, byte[] outBuffer, int outByteOffset, int inFrameCount) {
            logger.log(Level.TRACE, "begin");

            int inp; // Input buffer pointer
            int outP; // output buffer pointer
            int val; // Current input sample value
            int sign; // Current adpcm sign bit
            int delta; // Current adpcm output value
            int diff; // Difference between val and prev
            int step; // Step size
            int pred; // Predicted output value
            int vpDiff; // Current change to pred
            int index; // Current step change index
            int outputBuffer = 0; // place to keep previous 4-bit value
            boolean bufferStep; // toggle between outputBuffer/output
            int len = inFrameCount;

            inp = 0;
            outP = outByteOffset;

            pred = state.prev;
            index = state.index;
            step = stepSizeTable[index];

            bufferStep = true;

            for (; len > 0; len--) {
                // val = *inp++;
                val = isBigEndian() ?
                        ((inBuffer[inp] << 8) | (inBuffer[inp + 1] & 0xFF)) :
                        ((inBuffer[inp + 1] << 8) | (inBuffer[inp] & 0xFF));
                inp += 2;

                // Step 1 - compute difference with previous value
                diff = val - pred;
                sign = (diff < 0) ? 8 : 0;
                if (sign != 0)
                    diff = (-diff);

                // Step 2 - Divide and clamp
                // Note:
                // This code *approximately* computes:
                //    delta = diff*4/step;
                //    vpDiff = (delta+0.5)*step/4;
                // but in shift step bits are dropped. The net result of this is
                // that even if you have fast mul/div hardware you cannot put it to
                // good use since the fixup would be too expensive.
                delta = 0;
                vpDiff = (step >> 3);

                if (diff >= step) {
                    delta = 4;
                    diff -= step;
                    vpDiff += step;
                }
                step >>= 1;
                if (diff >= step) {
                    delta |= 2;
                    diff -= step;
                    vpDiff += step;
                }
                step >>= 1;
                if (diff >= step) {
                    delta |= 1;
                    vpDiff += step;
                }

                // Step 3 - Update previous value
                if (sign != 0)
                    pred -= vpDiff;
                else
                    pred += vpDiff;

                // Step 4 - Clamp previous value to 16 bits
                if (pred > 32767)
                    pred = 32767;
                else if (pred < -32768)
                    pred = -32768;

                // Step 5 - Assemble value, update index and step values
                delta |= sign;

                index += indexTable[delta];
                if (index < 0)
                    index = 0;
                if (index > 88)
                    index = 88;
                step = stepSizeTable[index];

                // Step 6 - Output value
                if (bufferStep) {
                    outputBuffer = (delta << 4) & 0xf0;
                } else {
                    outBuffer[outP++] = (byte) ((delta & 0x0f) | outputBuffer);
                }
                bufferStep = !bufferStep;
            }

            // Output last step, if needed
            if (!bufferStep)
                outBuffer[outP++] = (byte) outputBuffer;

            state.prev = pred;
            state.index = index;

            logger.log(Level.TRACE, "end");

            return inFrameCount;
        }

        /**  */
        protected int getSampleSizeInBytes() {
            return getFormat().getFrameSize() / getFormat().getChannels();
        }

        /**  */
        protected int getFrameSize() {
            return getFormat().getFrameSize();
        }

        /**
         * Returns if this stream (the decoded one) is big endian.
         *
         * @return true if this stream is big endian.
         */
        private boolean isBigEndian() {
            return getFormat().isBigEndian();
        }
    }

    /**
     * persistent state of a IMA ADPCM decoder.
     * This state class contains the information that
     * has to be passed between two blocks that are encoded or
     * decoded.
     */
    private static class ImaAdpcmState {

        public int prev;
        public int index;
    }
}
