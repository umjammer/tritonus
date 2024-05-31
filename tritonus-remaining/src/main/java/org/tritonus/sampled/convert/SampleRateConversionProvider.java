/*
 *  Copyright (c) 2001,2006,2008 by Florian Bomers
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

package org.tritonus.sampled.convert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.tritonus.share.ArraySet;
import org.tritonus.share.sampled.AudioFormats;
import org.tritonus.share.sampled.AudioUtils;
import org.tritonus.share.sampled.FloatSampleBuffer;
import org.tritonus.share.sampled.FloatSampleInput;
import org.tritonus.share.sampled.convert.TSimpleFormatConversionProvider;

import static java.lang.System.getLogger;


/**
 * This provider converts sample rate of 2 PCM streams. <br>
 * It does:
 * <ul>
 * <li>conversion of different sample rates
 * <li>conversion of unsigned/signed (only 8bit unsigned supported)
 * <li>conversion of big/small endian
 * <li>8,16,24,32 bit conversion
 * </ul>
 * It does NOT:
 * <ul>
 * <li>change channel count
 * <li>accept a stream where the sample rates are equal. This case should be
 * handled by the PCM2PCM converter
 * </ul>
 *
 * @author Florian Bomers
 */
public class SampleRateConversionProvider extends TSimpleFormatConversionProvider {

    private static final Logger logger = getLogger(SampleRateConversionProvider.class.getName());

    // only used as abbreviation
    public static AudioFormat.Encoding PCM_SIGNED = AudioFormat.Encoding.PCM_SIGNED;
    public static AudioFormat.Encoding PCM_UNSIGNED = AudioFormat.Encoding.PCM_UNSIGNED;

    private static final boolean DEBUG_STREAM = false;
    private static final boolean DEBUG_STREAM_PROBLEMS = false;

    private static final int ALL = AudioSystem.NOT_SPECIFIED;
    private static final AudioFormat[] OUTPUT_FORMATS = {
            // Encoding, SampleRate, sampleSizeInBits, channels, frameSize,
            // frameRate, bigEndian
            new AudioFormat(PCM_SIGNED, ALL, 8, ALL, ALL, ALL, false),
            new AudioFormat(PCM_SIGNED, ALL, 8, ALL, ALL, ALL, true),
            new AudioFormat(PCM_UNSIGNED, ALL, 8, ALL, ALL, ALL, false),
            new AudioFormat(PCM_UNSIGNED, ALL, 8, ALL, ALL, ALL, true),
            new AudioFormat(PCM_SIGNED, ALL, 16, ALL, ALL, ALL, false),
            new AudioFormat(PCM_SIGNED, ALL, 16, ALL, ALL, ALL, true),
            new AudioFormat(PCM_SIGNED, ALL, 24, ALL, ALL, ALL, false),
            new AudioFormat(PCM_SIGNED, ALL, 24, ALL, ALL, ALL, true),
            new AudioFormat(PCM_SIGNED, ALL, 32, ALL, ALL, ALL, false),
            new AudioFormat(PCM_SIGNED, ALL, 32, ALL, ALL, ALL, true),
    };

    /**
     * Constructor.
     */
    public SampleRateConversionProvider() {
        super(List.of(OUTPUT_FORMATS), List.of(OUTPUT_FORMATS));
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream) {
        AudioFormat sourceFormat = sourceStream.getFormat();

        // the non-conversion case
        if (AudioFormats.matches(sourceFormat, targetFormat)) {
            return sourceStream;
        }

        targetFormat = replaceNotSpecified(sourceFormat, targetFormat);
        // do not support NOT_SPECIFIED as sample rates
        if (targetFormat.getSampleRate() != AudioSystem.NOT_SPECIFIED
                && sourceFormat.getSampleRate() != AudioSystem.NOT_SPECIFIED
                && targetFormat.getChannels() != AudioSystem.NOT_SPECIFIED
                && sourceFormat.getChannels() != AudioSystem.NOT_SPECIFIED
                && targetFormat.getSampleSizeInBits() != AudioSystem.NOT_SPECIFIED
                && sourceFormat.getSampleSizeInBits() != AudioSystem.NOT_SPECIFIED
                && isConversionSupported(targetFormat, sourceFormat)) {
            return new SampleRateConverterStream(sourceStream, targetFormat);
        }
        throw new IllegalArgumentException("format conversion not supported");
    }

    // replaces the sample rate and frame rate.
    // Should only be used with PCM_SIGNED or PCM_UNSIGNED
    private static AudioFormat replaceSampleRate(AudioFormat format, float newSampleRate) {
        if (format.getSampleRate() == newSampleRate) {
            return format;
        }
        return new AudioFormat(format.getEncoding(), newSampleRate,
                format.getSampleSizeInBits(), format.getChannels(),
                format.getFrameSize(), newSampleRate, format.isBigEndian());
    }

    private static final float[] commonSampleRates = {
            8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000,
            56000, 64000, 88200, 96000, 192000
    };

    @Override
    public AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat) {
        logger.log(Level.TRACE, "begin");
        logger.log(Level.TRACE, "checking out possible target formats");
        logger.log(Level.TRACE, "from: " + sourceFormat);
        logger.log(Level.TRACE, "to  : " + targetEncoding);
        float sourceSampleRate = sourceFormat.getSampleRate();
        // a trick: set sourceFormat's sample rate to -1 so that
        // replaceNotSpecified does not replace the sample rate.
        // we want to convert that !
        sourceFormat = replaceSampleRate(sourceFormat, AudioSystem.NOT_SPECIFIED);
        if (isConversionSupported(targetEncoding, sourceFormat)) {
            ArraySet<AudioFormat> result = new ArraySet<>();
            for (AudioFormat targetFormat : getCollectionTargetFormats()) {
                targetFormat = replaceNotSpecified(sourceFormat, targetFormat);
                if (isConversionSupported(targetFormat, sourceFormat)) {
                    result.add(targetFormat);
                }
            }
            // for convenience, add some often used sample rates as output
            // this may help applications that do not handle NOT_SPECIFIED
            if (!result.isEmpty() && sourceSampleRate != AudioSystem.NOT_SPECIFIED) {
                int count = result.size();
                for (int i = 0; i < count; i++) {
                    AudioFormat format = result.get(i);
                    for (float commonSampleRate : commonSampleRates) {
                        if (!doMatch(sourceSampleRate, commonSampleRate)) {
                            result.add(replaceSampleRate(format, commonSampleRate));
                        }
                    }
                }
            }
            logger.log(Level.TRACE, "<found " + result.size() + " matching formats.");

            return result.toArray(EMPTY_FORMAT_ARRAY);
        } else {
            logger.log(Level.TRACE, "<returning empty array.");

            return EMPTY_FORMAT_ARRAY;
        }
    }

    @Override
    public boolean isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat) {
        // do not match when targetSampleRate set and sourceSamplerate set and
        // NOT both the same
        boolean result = (targetFormat.getSampleRate() == AudioSystem.NOT_SPECIFIED
                || targetFormat.getSampleRate() == AudioSystem.NOT_SPECIFIED
                || !doMatch(targetFormat.getSampleRate(), sourceFormat.getSampleRate())
                && doMatch(targetFormat.getChannels(), sourceFormat.getChannels()))
                && AudioUtils.containsFormat(sourceFormat, getCollectionSourceFormats().iterator())
                && AudioUtils.containsFormat(targetFormat, getCollectionTargetFormats().iterator());
        logger.log(Level.TRACE, "begin");
        logger.log(Level.TRACE, "checking if conversion possible");
        logger.log(Level.TRACE, "from: " + sourceFormat);
        logger.log(Level.TRACE, "to  : " + targetFormat);
        logger.log(Level.TRACE, "< result : " + result);

        return result;
    }

    protected static long convertLength(AudioFormat sourceFormat, AudioFormat targetFormat, long sourceLength) {
        if (sourceLength == AudioSystem.NOT_SPECIFIED) {
            return sourceLength;
        }
        return (long) (targetFormat.getSampleRate() / sourceFormat.getSampleRate() * sourceLength);
    }

    protected static long convertLength(float sourceSR, float targetSR, long sourceLength) {
        if (sourceLength == AudioSystem.NOT_SPECIFIED) {
            return sourceLength;
        }
        return (long) (targetSR / sourceSR * sourceLength);
    }

    /**
     * SampleRateConverterStream
     * <p>
     * at the moment, there are so many special things to care
     * about, and new things in an AIS, that I derive directly from
     * AudioInputStream.
     * I cannot use TAsynchronousFilteredAudioInputStream because
     * - it doesn't allow convenient use of a history. The history will be
     * needed especially when performing filtering
     * - it doesn't work on FloatSampleBuffer (yet)
     * - each sample must be calculated one-by-one. The asynchronous
     * difficulty isn't overcome by using a TCircularBuffer
     * I cannot use TSynchronousFilteredAudioInputStream because
     * - it doesn't handle different sample rates
     * Later we can make a base class for this, e.g. THistoryAudioInputStream
     * TODO when target sample rate is < source sample rate (or only slightly
     * above),
     * this stream calculates ONE sample too much.
     */
    public static class SampleRateConverterStream extends AudioInputStream implements FloatSampleInput {

        /** the current working buffer with samples of the sourceStream */
        private FloatSampleBuffer thisBuffer = null;
        /** used when read(byte[],int,int) is called */
        private FloatSampleBuffer writeBuffer = null;
        private byte[] byteBuffer; // used for reading samples of sourceStream
        private final AudioInputStream sourceStream;
        private final FloatSampleInput sourceInput;
        private final float sourceSampleRate;
        private float targetSampleRate;
        private final long sourceFrameLength;
        /** index in thisBuffer */
        private double pos;
        /** Conversion algorithm */
        public static final int SAMPLE_AND_HOLD = 1;
        /** Conversion algorithm */
        public static final int LINEAR_INTERPOLATION = 2;
        /** Conversion algorithm */
        public static final int RESAMPLE = 3;

        private boolean eofReached = false;

        /** source stream is read in buffers of this size - in milliseconds */
        private final int sourceBufferTime;

        /** source stream is read in buffers of this size - in samples */
        private int sourceBufferSizeSamples;

        /** the current conversion algorithm */
        private int conversionAlgorithm = LINEAR_INTERPOLATION;
//        private int conversionAlgorithm = SAMPLE_AND_HOLD;

        // History support

        /** the buffer with history samples */
        private FloatSampleBuffer historyBuffer = null;
        /**
         * the minimum number of samples that must be present in the history
         * buffer
         */
        private static final int minimumSamplesInHistory = 1;

        /** force to discard current contents in thisBuffer if true */
        private boolean thisBufferValid = false;

        public SampleRateConverterStream(AudioInputStream sourceStream, AudioFormat targetFormat) {
            // clean up targetFormat:
            // - ignore frame rate totally
            // - recalculate frame size
            super(sourceStream, new SRCAudioFormat(targetFormat),
                    convertLength(sourceStream.getFormat(), targetFormat, sourceStream.getFrameLength()));
            logger.log(Level.TRACE, "SampleRateConverterStream: <init>");

            this.sourceStream = sourceStream;
            if (sourceStream instanceof FloatSampleInput) {
                sourceInput = (FloatSampleInput) sourceStream;
            } else {
                this.sourceInput = null;
            }
            sourceSampleRate = sourceStream.getFormat().getSampleRate();
            targetSampleRate = targetFormat.getSampleRate();
            sourceFrameLength = sourceStream.getFrameLength();
            pos = 0;
            // use a buffer size of 100ms
            sourceBufferTime = 100;
            resizeBuffers();
            flush(); // force read of source stream next time read is called
        }

        public SampleRateConverterStream(FloatSampleInput sourceInput, AudioFormat targetFormat, long frameLength) {
            // clean up targetFormat:
            // - ignore frame rate totally
            // - recalculate frame size
            super(new ByteArrayInputStream(new byte[0]), new SRCAudioFormat(targetFormat),
                    convertLength(sourceInput.getSampleRate(), targetFormat.getSampleRate(), frameLength));
            logger.log(Level.TRACE, "begin");

            this.sourceStream = null;
            this.sourceInput = sourceInput;
            sourceSampleRate = sourceInput.getSampleRate();
            targetSampleRate = targetFormat.getSampleRate();
            sourceFrameLength = frameLength;
            pos = 0;
            // use a buffer size of 100ms
            sourceBufferTime = 100;
            resizeBuffers();
            flush(); // force read of source stream next time read is called
        }

        /**
         * Assures that both historyBuffer and working buffer
         * <ul>
         * <li>exist
         * <li>have about <code>sourceBufferTime</code> ms samples
         * <li>that both have at least <code>minimumSamplesInHistory</code>
         * samples
         * </ul>
         * This method must be called when anything is changed that may change
         * the size of the buffers.
         */
        private synchronized void resizeBuffers() {
            sourceBufferSizeSamples = (int) AudioUtils.millis2Frames((long) sourceBufferTime, sourceSampleRate);
            if (sourceBufferSizeSamples < minimumSamplesInHistory) {
                sourceBufferSizeSamples = minimumSamplesInHistory;
            }
            // we must be able to calculate at least one output sample from
            // one input buffer block
            if (sourceBufferSizeSamples < outSamples2inSamples(1)) {
                sourceBufferSizeSamples = ((int) outSamples2inSamples(1)) + 1;
            }
            if (historyBuffer == null) {
                historyBuffer = new FloatSampleBuffer(getFormat().getChannels(), sourceBufferSizeSamples, sourceSampleRate);
                historyBuffer.makeSilence();
            }
            // TODO retain last samples !
            historyBuffer.changeSampleCount(sourceBufferSizeSamples, true);
            if (thisBuffer == null) {
                thisBuffer = new FloatSampleBuffer(getFormat().getChannels(), sourceBufferSizeSamples, sourceSampleRate);
            }
            // TODO retain last samples and adjust pos
            thisBuffer.changeSampleCount(sourceBufferSizeSamples, true);
            if (DEBUG_STREAM) {
                logger.log(Level.TRACE, "Initialized thisBuffer and historyBuffer with " +
                        sourceBufferSizeSamples + " samples");
            }
        }

        /**
         * Maintenance work before reading from the source stream. In
         * particular, it is ensured that the temporary buffer for reading from
         * the source buffer is large enough.
         */
        private void beforeReadFromSourceStream() {
            FloatSampleBuffer lBuffer = thisBuffer;
            if (lBuffer != null && lBuffer.getSampleCount() != sourceBufferSizeSamples) {
                lBuffer.changeSampleCount(sourceBufferSizeSamples, false);
            }
        }

        /**
         * Reads from a source stream that cannot handle float buffers. After
         * this method has been called, it is to be checked whether we are
         * closed ! Precondition: sourceStream!=null
         */
        private void readFromByteSourceStream() {
            int byteCount = thisBuffer.getByteArrayBufferSize(sourceStream.getFormat());
            if (byteBuffer == null || byteBuffer.length < byteCount) {
                byteBuffer = new byte[byteCount];
            }
            if (DEBUG_STREAM) {
                logger.log(Level.TRACE, "in readFromByteSourceStream: trying to read " +
                        byteCount + " bytes = " + (byteCount / sourceStream.getFormat().getFrameSize()) +
                        " samples from source stream");
            }
            // finally read it
            int bytesRead = 0;
            int thisRead;
            do {
                try {
                    thisRead = sourceStream.read(byteBuffer, bytesRead, byteCount - bytesRead);
                } catch (IOException ioe) {
                    thisRead = -1;
                }
                if (thisRead > 0) {
                    bytesRead += thisRead;
                }
            } while (bytesRead < byteCount && thisRead > 0);
            if (bytesRead == 0) {
                // sourceStream is closed. We don't accept 0 bytes read from
                // source stream
                close();
            } else {
                thisBuffer.initFromByteArray(byteBuffer, 0, bytesRead, sourceStream.getFormat());
                if (DEBUG_STREAM) {
                    logger.log(Level.TRACE, "in readFromByteSourceStream: initialized thisBuffer with " +
                            thisBuffer.getSampleCount() + " samples");
                }
            }
        }

        /** pre-condition: sourceInput != null, thisBuffer.getSampleCount()>0 */
        private void readFromSourceInput() {
            if (sourceInput.isDone()) {
                close();
            } else {
                sourceInput.read(thisBuffer);
            }
        }

        private long testInFramesRead = 0;
        private long testOutFramesReturned = 0;

        /**
         * fills thisBuffer with new samples. It sets the history buffer to the
         * last buffer. thisBuffer's sampleCount will be the number of samples
         * read. Calling methods MUST check whether this stream is closed upon
         * completion of this method. If the stream is closed, the contents of
         * <code>thisBuffer</code> are not valid.
         */
        private void readFromSourceStream() {
            if (isClosed()) {
                return;
            }
            // reuse history buffer
            FloatSampleBuffer buffer = historyBuffer;
            historyBuffer = thisBuffer;
            thisBuffer = buffer;
            beforeReadFromSourceStream();
            int oldSampleCount = thisBuffer.getSampleCount();

            // ensure that we don't read more than the source stream claimed to
            // have
            if (sourceFrameLength != AudioSystem.NOT_SPECIFIED
                    && buffer.getSampleCount() + testInFramesRead > sourceFrameLength) {
                long remaining = sourceFrameLength - testInFramesRead;
                if (remaining <= 0) {
                    if (DEBUG_STREAM) {
                        logger.log(Level.TRACE, "Read more than allowed from source stream:"
                                + " sourceFrameLength=" + sourceFrameLength
                                + " samples, inFramesRead=" + testInFramesRead + " samples.");
                    }
                    close();
                    return;
                }
                if (DEBUG_STREAM) {
                    logger.log(Level.TRACE, "Reading from source stream: change from "
                            + buffer.getSampleCount() + " samples to" + remaining + " samples");
                }
                buffer.changeSampleCount((int) remaining, false);
            }

            if (sourceInput != null) {
                readFromSourceInput();
            } else {
                readFromByteSourceStream();
            }

            int sampleCount = (buffer == null) ? 0 : buffer.getSampleCount();
            testInFramesRead += sampleCount;

            if (DEBUG_STREAM) {
                String src = (sourceInput != null) ? "source input" : "source byte stream";
                logger.log(Level.TRACE, "Read " + sampleCount + " frames from " + src
                        + " (requested=" + oldSampleCount + "). Total=" + testInFramesRead);
            }
            double inc = outSamples2inSamples(1.0);
            if (!thisBufferValid) {
                thisBufferValid = true;
                pos = 0.0;
            } else {
                double temp = pos;
                pos -= oldSampleCount;
                if (DEBUG_STREAM) {
                    logger.log(Level.TRACE, "new pos: " + temp + " - " + oldSampleCount + " = " + pos);
                }
                if ((pos > inc || pos < -inc) && ((int) pos) != 0) {
                    // hard-reset pos if - why ever - it got out of bounds
                    if (DEBUG_STREAM_PROBLEMS) {
                        logger.log(Level.TRACE, "Need to hard reset pos=" + pos + " !");
                    }
                    pos = 0.0;
                }
            }
        }

        protected void convertSampleAndHold1(float[] inSamples,
                                             double inSampleOffset, int inSampleCount, double increment,
                                             float[] outSamples, int outSampleOffset, int outSampleCount,
                                             float[] history, int historyLength) {
            if (DEBUG_STREAM) {
                logger.log(Level.TRACE, "convertSampleAndHold1(inSamples["
                        + inSamples.length
                        + "], "
                        + ((int) inSampleOffset)
                        + " to "
                        + ((int) (inSampleOffset + increment
                        * (outSampleCount - 1))) + ", " + "outSamples["
                        + outSamples.length + "], " + outSampleOffset + " to "
                        + (outSampleOffset + outSampleCount - 1) + ")");
                System.out.flush();
            }
            for (int i = 0; i < outSampleCount; i++) {
                int inIndex = (int) (inSampleOffset + increment * i);
                if (inIndex < 0) {
                    outSamples[i + outSampleOffset] = history[inIndex
                            + historyLength];
                    if (DEBUG_STREAM) {
                        logger.log(Level.TRACE, "convertSampleAndHold: using history["
                                + (inIndex + historyLength)
                                + " because inIndex=" + inIndex);
                    }
                } else if (inIndex >= inSampleCount) {
                    if (DEBUG_STREAM_PROBLEMS) {
                        logger.log(Level.TRACE, "convertSampleAndHold: INDEX OUT OF BOUNDS outSamples["
                                + i
                                + "]=inSamples[roundDown("
                                + inSampleOffset
                                + ")=" + inIndex + "];");
                    }
                } else {
                    outSamples[i + outSampleOffset] = inSamples[inIndex];
//                    outSamples[i] = inSamples[roundDown(inSampleOffset)];
                }
//                inSampleOffset+=increment; <- this produces too much rounding
                // errors...
            }
        }

        /**
         * optimized version
         */
        private static void convertSampleAndHold2(float[] inSamples,
                                                  double inSampleOffset, int inSampleCount, double increment,
                                                  float[] outSamples, int outSampleOffset, int outSampleCount,
                                                  float[] history, int historyLength) {
            if (DEBUG_STREAM) {
                logger.log(Level.TRACE, "convertSampleAndHold2(inSamples["
                        + inSamples.length
                        + "], "
                        + ((int) inSampleOffset)
                        + " to "
                        + ((int) (inSampleOffset + increment
                        * (outSampleCount - 1))) + ", " + "outSamples["
                        + outSamples.length + "], " + outSampleOffset + " to "
                        + (outSampleOffset + outSampleCount - 1) + ")");
                System.out.flush();
            }
            int endSampleOffset = outSampleOffset + outSampleCount;
            // first go through the history
            double dHistoryLength = historyLength;
            while (inSampleOffset < 0.0d && outSampleOffset < endSampleOffset) {
                double dInIndex = (inSampleOffset + dHistoryLength);
                outSamples[outSampleOffset] = history[(int) dInIndex];
                inSampleOffset += increment;
                outSampleOffset++;
            }

            // then go through the remaining new samples
            while (outSampleOffset < endSampleOffset) {
                outSamples[outSampleOffset] = inSamples[(int) inSampleOffset];

                inSampleOffset += increment;
                outSampleOffset++;
            }
        }

        protected void convertLinearInterpolation1(float[] inSamples,
                                                   double inSampleOffset, int inSampleCount, double increment,
                                                   float[] outSamples, int outSampleOffset, int outSampleCount,
                                                   float[] history, int historyLength) {
            if (DEBUG_STREAM) {
                logger.log(Level.TRACE, "convertLinearInterpolate1(inSamples["
                        + inSamples.length
                        + "], "
                        + ((int) inSampleOffset)
                        + " to "
                        + ((int) (inSampleOffset + increment
                        * (outSampleCount - 1))) + ", " + "outSamples["
                        + outSamples.length + "], " + outSampleOffset + " to "
                        + (outSampleOffset + outSampleCount - 1) + ")");
                System.out.flush();
            }
            for (int i = 0; i < outSampleCount; i++) {
                try {
                    double _inIndex = inSampleOffset + increment * i - 1;
                    int inIndex = (int) Math.floor(_inIndex);
                    double factor = 1.0d - (_inIndex - inIndex);
                    float value = 0;
                    for (int x = 0; x < 2; x++) {
                        if (inIndex >= inSampleCount) {
                            // we clearly need more samples !
                            if (DEBUG_STREAM_PROBLEMS) {
                                logger.log(Level.TRACE, "linear interpolation: INDEX OUT OF BOUNDS inIndex="
                                        + inIndex + " inSampleCount=" + inSampleCount);
                            }
                        } else if (inIndex < 0) {
                            int histIndex = inIndex + historyLength;
                            if (histIndex >= 0) {
                                value += (float) (history[histIndex] * factor);
                                if (DEBUG_STREAM) {
                                    logger.log(Level.TRACE, "linear interpolation: using history[" + inIndex + "]");
                                }
                            } else if (DEBUG_STREAM_PROBLEMS) {
                                logger.log(Level.TRACE, "linear interpolation: history INDEX OUT OF BOUNDS inIndex="
                                        + inIndex
                                        + " histIndex="
                                        + histIndex
                                        + " history length=" + historyLength);
                            }
                        } else {
                            value += (float) (inSamples[inIndex] * factor);
                        }
                        factor = 1 - factor;
                        inIndex++;
                    }
                    outSamples[i + outSampleOffset] = value;
                    // outSamples[i]=inSamples[roundDown(inSampleOffset)];
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                    if (DEBUG_STREAM_PROBLEMS) {
                        logger.log(Level.TRACE, "**** REAL INDEX OUT OF BOUNDS ****** outSamples["
                                + i
                                + "]=inSamples[roundDown("
                                + inSampleOffset
                                + ")=" + ((int) inSampleOffset) + "];");
                    }
                    // throw aioobe;
                }
                // inSampleOffset+=increment; <- this produces too much rounding
                // errors...
            }
        }

        /**
         * optimized version of the linear interpolator
         */
        private static void convertLinearInterpolation2(float[] inSamples,
                                                        double inSampleOffset, int inSampleCount, double increment,
                                                        float[] outSamples, int outSampleOffset, int outSampleCount,
                                                        float[] history, int historyLength) {
            // cast results:
            // (int) -1.7d=-1 (int) -1.5d=-1 (int) -1.2d=-1 (int) -1.0d=-1 (int)
            // -0.7d=0 (int) -0.5d=0 (int) -0.2d=0
            if (DEBUG_STREAM) {
                logger.log(Level.TRACE, "convertLinearInterpolate2(inSamples["
                        + inSamples.length
                        + "], "
                        + ((int) inSampleOffset)
                        + " to "
                        + ((int) (inSampleOffset + increment
                        * (outSampleCount - 1))) + ", " + "outSamples["
                        + outSamples.length + "], " + outSampleOffset + " to "
                        + (outSampleOffset + outSampleCount - 1) + ")");
                System.out.flush();
            }

            try {
                int endSampleOffset = outSampleOffset + outSampleCount;

                // first go through the history
                double _historyLength = historyLength;
                while (inSampleOffset < 0.0d && outSampleOffset < endSampleOffset) {
                    double inIndex = (inSampleOffset + _historyLength);
                    int histIndex = (int) inIndex;
                    float factor = (float) (inIndex - histIndex);

                    outSamples[outSampleOffset] = (history[histIndex - 1] * (1.0f - factor))
                            + (history[histIndex] * factor);

                    inSampleOffset += increment;
                    outSampleOffset++;
                }

                // then the transition area: last sample is in history, new
                // sample in inSamples
                while (inSampleOffset < 1.0d && outSampleOffset < endSampleOffset) {
                    float factor = (float) inSampleOffset;
                    outSamples[outSampleOffset] = (history[historyLength - 1] * (1.0f - factor))
                            + (inSamples[0] * factor);

                    inSampleOffset += increment;
                    outSampleOffset++;
                }

                // then go through the remaining new samples
                while (outSampleOffset < endSampleOffset) {
                    int inIndex = (int) inSampleOffset;
                    float factor = (float) (inSampleOffset - inIndex);

                    outSamples[outSampleOffset] = (inSamples[inIndex - 1] * (1.0f - factor))
                            + (inSamples[inIndex] * factor);

                    inSampleOffset += increment;
                    outSampleOffset++;
                }

            } catch (ArrayIndexOutOfBoundsException e) {
                if (DEBUG_STREAM_PROBLEMS) {
                    logger.log(Level.TRACE, "**** INDEX OUT OF BOUNDS ****** inSampleOffset="
                            + inSampleOffset
                            + "  inSamples.length="
                            + inSamples.length
                            + "  outSampleOffset="
                            + outSampleOffset
                            + "  outSamples.length="
                            + outSamples.length);
                }
                logger.log(Level.ERROR, e.getMessage(), e);
//                throw aioobe;
            }
        }

        private double inSamples2outSamples(double inSamples) {
            return inSamples * targetSampleRate / sourceSampleRate;
        }

        private double outSamples2inSamples(double outSamples) {
            return outSamples * sourceSampleRate / targetSampleRate;
        }

        // interface FloatSampleInput

        @Override
        public int getChannels() {
            return getFormat().getChannels();
        }

        @Override
        public float getSampleRate() {
            return getFormat().getSampleRate();
        }

        @Override
        public boolean isDone() {
            return isClosed();
        }

        @Override
        public void read(FloatSampleBuffer outBuffer) {
            read(outBuffer, 0, outBuffer.getSampleCount());
        }

        /**
         * Main read method. It blocks until all samples are converted or the
         * source stream is at its end or closed.<br>
         * The sourceStream's sample rate is converted following the current
         * setting of <code>conversionAlgorithm</code>. At most
         * outBuffer.getSampleCount() are converted. In general, if
         * outBuffer.getSampleCount()) is less after processing this function,
         * then it is an indicator that it was the last block to be processed.
         *
         * @param outBuffer the buffer that the converted samples will be written to.
         * @throws IllegalArgumentException when outBuffer's channel count does not match
         * @see #setConversionAlgorithm(int)
         */
        @Override
        @SuppressWarnings("cast")
        public void read(FloatSampleBuffer outBuffer, int offset, int count) {
            if (isClosed() || count == 0) {
                outBuffer.setSampleCount(offset, true);
                return;
            }
            if (outBuffer.getChannelCount() != thisBuffer.getChannelCount()) {
                throw new IllegalArgumentException("passed buffer has different channel count");
            }
            if (DEBUG_STREAM) {
                logger.log(Level.TRACE, ">SamplerateConverterStream.read(" + count + " samples)");
            }
            FloatSampleBuffer lSourceBuffer = thisBuffer;
            float[] outSamples;
            float[] inSamples;
            float[] history;
            double increment = outSamples2inSamples(1.0);
            int writtenSamples = 0;
            do {
                // check thisBuffer with samples of source stream
                int inSampleCount = lSourceBuffer.getSampleCount();
                if (((int) pos) >= inSampleCount || !thisBufferValid) {
                    // need to load new data of sourceStream
                    readFromSourceStream();
                    if (isClosed()) {
                        break;
                    }
                    lSourceBuffer = thisBuffer;
                    inSampleCount = thisBuffer.getSampleCount();
                    if (inSampleCount == 0) {
                        // cannot read anything right now
                        break;
                    }
                }
                // calculate number of samples to write
                int writeCount = count - writtenSamples;
                // check whether this exceeds the current in-buffer
                if (((int) (outSamples2inSamples(writeCount) + pos)) >= inSampleCount) {
                    int lastOutIndex = ((int) (inSamples2outSamples(((double) inSampleCount) - pos))) + 1;
                    // normally, the above formula gives the exact writeCount.
                    // but due to rounding issues, sometimes it has to be
                    // decremented once.
                    // so we need to iterate to get the last index and then
                    // increment it once to make
                    // it the writeCount (=the number of samples to write)
                    while ((int) (outSamples2inSamples(lastOutIndex) + pos) >= inSampleCount) {
                        lastOutIndex--;
                        if (DEBUG_STREAM) {
                            logger.log(Level.TRACE, "--------- Decremented lastOutIndex=" + lastOutIndex);
                        }
                    }
                    if (DEBUG_STREAM_PROBLEMS) {
                        int testLastOutIndex = writeCount - 1;
                        if (DEBUG_STREAM_PROBLEMS) {
                            while ((int) (outSamples2inSamples(testLastOutIndex) + pos) >= inSampleCount) {
                                testLastOutIndex--;
                            }
                        }
                        if (testLastOutIndex != lastOutIndex) {
                            logger.log(Level.TRACE, "lastOutIndex wrong: lastOutIndex="
                                    + lastOutIndex
                                    + " testLastOutIndex="
                                    + testLastOutIndex
                                    + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        }
                    }
                    writeCount = lastOutIndex + 1;
                }
                // finally do the actual conversion - separated per channel
                for (int channel = 0; channel < outBuffer.getChannelCount(); channel++) {
                    inSamples = lSourceBuffer.getChannel(channel);
                    outSamples = outBuffer.getChannel(channel);
                    history = historyBuffer.getChannel(channel);
                    switch (conversionAlgorithm) {
                    case SAMPLE_AND_HOLD:
                        convertSampleAndHold2(inSamples, pos, inSampleCount,
                                increment, outSamples, writtenSamples + offset,
                                writeCount, history,
                                historyBuffer.getSampleCount());
                        break;
                    case LINEAR_INTERPOLATION:
                        convertLinearInterpolation2(inSamples, pos,
                                inSampleCount, increment, outSamples,
                                writtenSamples + offset, writeCount, history,
                                historyBuffer.getSampleCount());
                        break;
                    }
                }
                writtenSamples += writeCount;
                // adjust new position
                pos += outSamples2inSamples(writeCount);
            } while (!isClosed() && writtenSamples < outBuffer.getSampleCount());

            if (writtenSamples < count) {
                outBuffer.changeSampleCount(writtenSamples + offset, true);
            }
            if (DEBUG_STREAM) {
                testOutFramesReturned += outBuffer.getSampleCount();
                logger.log(Level.TRACE, "< return " + outBuffer.getSampleCount()
                        + "frames. Total=" + testOutFramesReturned + " frames. Read total " + testInFramesRead
                        + " frames from source stream");
            }
        }

        // utility methods ----

        protected double sourceFrames2targetFrames(double sourceFrames) {
            return targetSampleRate / sourceSampleRate * sourceFrames;
        }

        protected double targetFrames2sourceFrames(double targetFrames) {
            return sourceSampleRate / targetSampleRate * targetFrames;
        }

        protected long sourceBytes2targetBytes(long sourceBytes) {
            long sourceFrames = sourceBytes / getSourceFrameSize();
            long targetFrames = (long) sourceFrames2targetFrames(sourceFrames);
            return targetFrames * getFrameSize();
        }

        protected long targetBytes2sourceBytes(long targetBytes) {
            long targetFrames = targetBytes / getFrameSize();
            long sourceFrames = (long) targetFrames2sourceFrames(targetFrames);
            return sourceFrames * getSourceFrameSize();
        }

        public int getFrameSize() {
            return getFormat().getFrameSize();
        }

        public int getSourceFrameSize() {
            return sourceStream != null ? sourceStream.getFormat().getFrameSize() : 1;
        }

        // methods overwritten of AudioInputStream ----

        @Override
        public int read() throws IOException {
            if (getFormat().getFrameSize() != 1) {
                throw new IOException("frame size must be 1 to read a single byte");
            }
            // very ugly, but efficient. Who uses this method anyway ?
            byte[] temp = new byte[1];
            int result = read(temp);
            if (result <= 0) {
                return -1;
            }
            return temp[0] & 0xff;
        }

        /**
         * @see #read(byte[], int, int)
         */
        @Override
        public int read(byte[] data) throws IOException {
            return read(data, 0, data.length);
        }

        /**
         * Read length bytes that will be the converted samples of the original
         * inputStream. When length is not an integral number of frames, this
         * method may read less than length bytes.
         */
        @Override
        public int read(byte[] data, int offset, int length) throws IOException {
            if (isClosed()) {
                return -1;
            }
            int frameCount = length / getFrameSize();
            if (writeBuffer == null) {
                writeBuffer = new FloatSampleBuffer(getFormat().getChannels(), frameCount, getFormat().getSampleRate());
            } else {
                writeBuffer.changeSampleCount(frameCount, false);
            }
            read(writeBuffer);

            if (writeBuffer.getSampleCount() == 0 && eofReached) {
                return -1;
            }

            int written = writeBuffer.convertToByteArray(data, offset, getFormat());
            return written;
        }

        @Override
        public synchronized long skip(long nSkip) throws IOException {
            // only returns integral frames
            long sourceSkip = targetBytes2sourceBytes(nSkip);
            long sourceSkipped = sourceStream != null ? sourceStream.skip(sourceSkip) : 0;
            flush();
            return sourceBytes2targetBytes(sourceSkipped);
        }

        @Override
        public int available() throws IOException {
            if (sourceStream == null) {
                return -1;
            }
            return (int) sourceBytes2targetBytes(sourceStream.available());
        }

        @Override
        public void mark(int readlimit) {
            if (sourceStream != null) {
                sourceStream.mark((int) targetBytes2sourceBytes(readlimit));
            }
        }

        @Override
        public synchronized void reset() throws IOException {
            if (sourceStream != null) {
                sourceStream.reset();
                flush();
            }
        }

        @Override
        public boolean markSupported() {
            if (sourceStream != null) {
                return sourceStream.markSupported();
            }
            return false;
        }

        @Override
        public void close() {
            if (isClosed()) {
                return;
            }
            if (sourceStream != null) {
                try {
                    sourceStream.close();
                } catch (IOException ignored) {
                }
            }
            eofReached = true;
            // clean memory, this will also be an indicator that
            // the stream is closed
            thisBuffer = null;
            historyBuffer = null;
            byteBuffer = null;
        }

        // additional methods ----

        public boolean isClosed() {
            return eofReached || (thisBuffer == null);
        }

        /**
         * Flushes the internal buffers
         */
        public synchronized void flush() {
            if (!isClosed()) {
                thisBufferValid = false;
                historyBuffer.makeSilence();
            }
        }

        // Properties ----

        public synchronized void setTargetSampleRate(float sr) {
            if (sr > 0) {
                targetSampleRate = sr;
//                ((SRCAudioFormat) getFormat()).setSampleRate(sr);
                resizeBuffers();
            }
        }

        public synchronized void setConversionAlgorithm(int algo) {
            if ((algo == SAMPLE_AND_HOLD || algo == LINEAR_INTERPOLATION) && (algo != conversionAlgorithm)) {
                conversionAlgorithm = algo;
                resizeBuffers();
            }
        }

        public synchronized float getTargetSampleRate() {
            return targetSampleRate;
        }

        public synchronized int getConversionAlgorithm() {
            return conversionAlgorithm;
        }
    }

    /**
     * Obviously, this class is used to be able to set the frame rate/sample
     * rate after the AudioFormat object has been created. It assumes the PCM
     * case where the frame rate is always in sync with the sample rate. (MP)
     */
    public static class SRCAudioFormat extends AudioFormat {

        private float sampleRate;

        public SRCAudioFormat(AudioFormat targetFormat) {
            super(targetFormat.getEncoding(),
                    targetFormat.getSampleRate(),
                    targetFormat.getSampleSizeInBits(),
                    targetFormat.getChannels(),
                    AudioUtils.getFrameSize(targetFormat.getChannels(), targetFormat.getSampleSizeInBits()),
                    targetFormat.getSampleRate(),
                    targetFormat.isBigEndian(),
                    targetFormat.properties());
            this.sampleRate = targetFormat.getSampleRate();
        }

        public void setSampleRate(float sr) {
            if (sr > 0) {
                this.sampleRate = sr;
            }
        }

        @Override
        public float getSampleRate() {
            return this.sampleRate;
        }

        @Override
        public float getFrameRate() {
            return this.sampleRate;
        }
    }
}
