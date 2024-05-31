/*
 *  Copyright (c) 1999,2000 by Florian Bomers
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

package org.tritonus.share.sampled.convert;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.tritonus.share.sampled.AudioUtils;
import org.tritonus.share.sampled.FloatSampleBuffer;
import org.tritonus.share.sampled.FloatSampleInput;

import static java.lang.System.getLogger;


/**
 * Base class for types of audio filter/converter that translate one frame to another frame.<br>
 * It provides all the transformation of frame sizes.<br>
 * It does NOT handle different sample rates of original stream and this stream !
 *
 * @author Florian Bomers
 */
public abstract class TSynchronousFilteredAudioInputStream extends TAudioInputStream implements FloatSampleInput {

    private static final Logger logger = getLogger("org.tritonus.TraceAudioConverter");
    
    private final AudioInputStream originalStream;

    /** the same originalStream cast to FloatSampleInput, if it is one */
    private FloatSampleInput originalStreamFloat;

    private final AudioFormat originalFormat;
    /** 1 if original format's frame size is NOT_SPECIFIED */
    private final int originalFrameSize;
    /** 1 if original format's frame size is NOT_SPECIFIED */
    private final int newFrameSize;

    private boolean EOF = false;

    /**
     * The intermediate buffer used during convert actions
     * (if not convertInPlace is used).
     * It remains until this audioStream is closed or destroyed
     * and grows with the time - it always has the size of the
     * largest intermediate buffer ever needed.
     */
    protected byte[] buffer = null;

    /**
     * For use of the more efficient method convertInPlace.
     * it will be set to true when (frameSizeFactor==1)
     */
    private boolean convertInPlace;

    /** if this flag is set, convert(FloatSampleBuffer) is implemented by overriding classes */
    private boolean m_enableFloatConversion;

    public TSynchronousFilteredAudioInputStream(AudioInputStream audioInputStream, AudioFormat newFormat) {
        // the super class will do nothing... we override everything
        super(audioInputStream, newFormat, audioInputStream.getFrameLength());
        originalStream = audioInputStream;
        originalFormat = audioInputStream.getFormat();
        originalFrameSize = (originalFormat.getFrameSize() <= 0) ? 1 : originalFormat.getFrameSize();
        newFrameSize = (getFormat().getFrameSize() <= 0) ? 1 : getFormat().getFrameSize();
        if (originalStream instanceof FloatSampleInput) {
            originalStreamFloat = (FloatSampleInput) originalStream;
        }
        logger.log(Level.TRACE, "original format =" + AudioUtils.format2ShortStr(originalFormat));
        logger.log(Level.TRACE, "converted format=" + AudioUtils.format2ShortStr(getFormat()));
        convertInPlace = false;
        m_enableFloatConversion = false;
    }

    /**
     * descendant classes should call this method if they have implemented
     * convertInPlace(). ConvertInPlace will only be used if the converted frame
     * size is larger than the original frame size.
     */
    protected boolean enableConvertInPlace() {
        if (newFrameSize >= originalFrameSize) {
            convertInPlace = true;
        }
        return convertInPlace;
    }

    /**
     * Descendant classes should call this method if they have implemented
     * convert(FloatSampleBuffer). That convert method will only be called
     * if this class' FloatSampleInput.read() is used.
     */
    protected void enableFloatConversion() {
        m_enableFloatConversion = true;
    }

    /**
     * Override this method to do the actual conversion.
     * inBuffer starts always at index 0 (it is an internal buffer)
     * You should always override this.
     * inFrameCount is the number of frames in inBuffer. These
     * frames are of the format originalFormat.
     *
     * @return the resulting number of <B>frames</B> converted and put into
     * outBuffer. The return value is in the format of this stream.
     */
    protected abstract int convert(byte[] inBuffer, byte[] outBuffer, int outByteOffset, int inFrameCount);

    /**
     * Override this method to provide in-place conversion of samples.
     * To use it, call "enableConvertInPlace()". It will only be used when
     * input bytes per frame >= output bytes per frame.
     * This method must always convert frameCount frames, so no return value is necessary.
     */
    protected void convertInPlace(byte[] buffer, int byteOffset, int frameCount) {
        throw new RuntimeException("illegal call to convertInPlace");
    }

    /**
     * Override this method to do the actual conversion in the
     * FloatSampleBuffer. Use buffer's methods to shrink the number of samples,
     * if necessary. This method will only be called if this stream is accessed
     * by way of FloatSampleInput methods.
     *
     * @param buffer the buffer to convert
     * @param offset the offset in buffer in samples
     * @param count  the number of samples in buffer to convert
     */
    protected void convert(FloatSampleBuffer buffer, int offset, int count) {
        throw new RuntimeException("illegal call to convert(FloatSampleBuffer)");
    }

    @Override
    public int read() throws IOException {
        if (newFrameSize != 1) {
            throw new IOException("frame size must be 1 to read a single byte");
        }
        // very ugly, but efficient. Who uses this method anyway ?
        byte[] temp = new byte[1];
        int result = read(temp);
        if (result == -1) {
            return -1;
        }
        if (result == 0) {
            // what in this case ??? Let's hope it never occurs.
            return -1;
        }
        return temp[0] & 0xFF;
    }

    /** remove the temporary read buffer to save heap */
    private void clearBuffer() {
        buffer = null;
        floatByteBuffer = null;
    }

    public AudioInputStream getOriginalStream() {
        return originalStream;
    }

    public AudioFormat getOriginalFormat() {
        return originalFormat;
    }

    /**
     * Read length bytes that will be the converted samples
     * of the original InputStream.
     * When length is not an integral number of frames,
     * this method may read less than length bytes.
     */
    @Override
    public final int read(byte[] data, int offset, int length) throws IOException {
        // number of frames that we have to read from the underlying stream.
        int frameLength = length / newFrameSize;

        // number of bytes that we need to read from underlying stream.
        int originalBytes = frameLength * originalFrameSize;

        logger.log(Level.TRACE, "> TSynchronousFilteredAIS.read(buffer[" + data.length + "], " +
                offset + " ," + length + " bytes ^=" + frameLength + " frames)");
        int framesConverted;

        // set up buffer to read
        byte[] readBuffer;
        int readOffset;
        if (convertInPlace) {
            readBuffer = data;
            readOffset = offset;
        } else {
            // assert that the buffer fits
            if (buffer == null || buffer.length < originalBytes) {
                buffer = new byte[originalBytes];
            }
            readBuffer = buffer;
            readOffset = 0;
        }
        int bytesRead = originalStream.read(readBuffer, readOffset, originalBytes);
        if (bytesRead == -1) {
            // end of stream
            clearBuffer();
            EOF = true;
            return -1;
        }
        int framesRead = bytesRead / originalFrameSize;
        logger.log(Level.TRACE, "original.read returned " + bytesRead + " bytes ^=" + framesRead + " frames");
        if (convertInPlace) {
            convertInPlace(data, offset, framesRead);
            framesConverted = framesRead;
        } else {
            framesConverted = convert(buffer, data, offset, framesRead);
        }
        logger.log(Level.TRACE, "< converted " + framesConverted + " frames");

        return framesConverted * newFrameSize;
    }

    @Override
    public long skip(long skip) throws IOException {
        // only returns integral frames
        long skipFrames = skip / newFrameSize;
        long originalSkippedBytes = originalStream.skip(skipFrames * originalFrameSize);
        long skippedFrames = originalSkippedBytes / originalFrameSize;
        return skippedFrames * newFrameSize;
    }

    @Override
    public int available() throws IOException {
        int origAvailFrames = originalStream.available() / originalFrameSize;
        return origAvailFrames * newFrameSize;
    }

    @Override
    public void close() throws IOException {
        EOF = true;
        originalStream.close();
        clearBuffer();
    }

    @Override
    public void mark(int readLimit) {
        int readLimitFrames = readLimit / newFrameSize;
        originalStream.mark(readLimitFrames * originalFrameSize);
    }

    @Override
    public void reset() throws IOException {
        originalStream.reset();
    }

    @Override
    public boolean markSupported() {
        return originalStream.markSupported();
    }

    // interface FloatSampleInput

    @Override
    public int getChannels() {
        return format.getChannels();
    }

    @Override
    public float getSampleRate() {
        return format.getSampleRate();
    }

    @Override
    public boolean isDone() {
        // if this class was closed, never return open again
        if (EOF) return true;
        if (originalStreamFloat != null) {
            return originalStreamFloat.isDone();
        }
        return false;
    }

    /** temporary byte buffer for conversion from/to byte/float arrays */
    private byte[] floatByteBuffer = null;

    /**
     * read sampleCount converted samples at the specified offset. The current
     * implementation requires that offset is 0 and sampleCount ==
     * buffer.getSampleCount().
     */
    @Override
    public void read(FloatSampleBuffer buffer, int offset, int sampleCount) {
        try {
            // Case 1: reading cannot, but processing can be done in float layer,
            // so read unconverted bytes, then convert to float and process
            if (originalStreamFloat == null && m_enableFloatConversion) {
                // currently cannot convert in the middle of the buffer
                if (offset > 0 || sampleCount != buffer.getSampleCount()) {
                    throw new IllegalArgumentException("float reading with offset not supported");
                }
                // allocate a byte array large enough to hold the byte data
                int reqSize = sampleCount * originalFrameSize;
                if (floatByteBuffer == null || floatByteBuffer.length < reqSize) {
                    floatByteBuffer = new byte[reqSize];
                }
                // read into byte array -- is already processed
                int bytesRead = originalStream.read(floatByteBuffer, 0, reqSize);
                // convert the byte array to float
                if (bytesRead <= 0) {
                    // EOF or nothing read
                    buffer.setSampleCount(0, false);
                    return;
                }
                // convert to float
                buffer.initFromByteArray(floatByteBuffer, 0, bytesRead, originalFormat);
                // do the processing
                convert(buffer, 0, buffer.getSampleCount());
            } else
                // Case 2: reading or processing cannot be done in float layer,
                // do the conversion with byte array and convert afterwards
                if (originalStreamFloat == null || !m_enableFloatConversion) {
                    // currently cannot convert in the middle of the buffer
                    if (offset > 0 || sampleCount != buffer.getSampleCount()) {
                        throw new IllegalArgumentException("float reading with offset not supported");
                    }
                    // allocate a byte array large enough to hold the converted data
                    int reqSize = sampleCount * format.getFrameSize();
                    if (floatByteBuffer == null || floatByteBuffer.length < reqSize) {
                        floatByteBuffer = new byte[reqSize];
                    }
                    // read into byte array -- is already processed
                    int bytesRead = read(floatByteBuffer, 0, reqSize);
                    // convert the byte array to float
                    if (bytesRead <= 0) {
                        // EOF or nothing read
                        buffer.setSampleCount(0, false);
                        return;
                    }
                    // convert to float
                    buffer.initFromByteArray(floatByteBuffer, 0, bytesRead, format);
                } else {
                    // read from the source stream
                    originalStreamFloat.read(buffer, offset, sampleCount);
                    if (offset + sampleCount > buffer.getSampleCount()) {
                        sampleCount = buffer.getSampleCount() - offset;
                        if (sampleCount < 0) {
                            sampleCount = 0;
                        }
                    }
                    // do the actual processing
                    convert(buffer, offset, sampleCount);
                }

        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            buffer.setSampleCount(0, false);
        }
    }

    @Override
    public void read(FloatSampleBuffer buffer) {
        read(buffer, 0, buffer.getSampleCount());
    }
}
