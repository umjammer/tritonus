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

package org.tritonus.share.sampled.convert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioFormat;

import org.tritonus.share.TCircularBuffer;

import static java.lang.System.getLogger;


/**
 * Base class for asynchronous converters.
 * This class serves as base class for
 * converters that do not have a fixed
 * ratio between the size of a block of input
 * data and the size of a block of output data.
 * These types of converters therefore need an
 * internal buffer, which is realized in this
 * class.
 *
 * @author Matthias Pfisterer
 */
public abstract class TAsynchronousFilteredAudioInputStream extends TAudioInputStream
        implements TCircularBuffer.Trigger {

    private static final Logger logger = getLogger("org.tritonus.TraceAudioConverter");

    private static final int DEFAULT_BUFFER_SIZE = 327670;
    private static final int DEFAULT_MIN_AVAILABLE = 4096;
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    // must be protected because it's accessed by the native CDDA lib
    protected TCircularBuffer circularBuffer;
    private final int minAvailable;
    private byte[] singleByte;

    /**
     * Constructor.
     * This constructor uses the default buffer size and the default
     * min available amount.
     *
     * @param length length of this stream in frames. May be
     *               AudioSystem.NOT_SPECIFIED.
     */
    public TAsynchronousFilteredAudioInputStream(AudioFormat outputFormat, long length) {
        this(outputFormat, length, DEFAULT_BUFFER_SIZE, DEFAULT_MIN_AVAILABLE);
    }

    /**
     * Constructor.
     * With this constructor, the buffer size and the minimum
     * available amount can be specified as parameters.
     *
     * @param length     length of this stream in frames. May be
     *                   AudioSystem.NOT_SPECIFIED.
     * @param bufferSize size of the circular buffer in bytes.
     */
    public TAsynchronousFilteredAudioInputStream(AudioFormat outputFormat, long length, int bufferSize, int minAvailable) {
        // The usage of a ByteArrayInputStream is a hack.
        // (the infamous "JavaOne hack", because I did it on June
        // 6th 2000 in San Francisco, only hours before a
        // JavaOne session where I wanted to show mp3 playback
        // with Java Sound.) It is necessary because in the FCS
        // version of the Sun jdk1.3, the constructor of
        // AudioInputStream throws an exception if its first
        // argument is null. So we have to pass a dummy non-null
        // value.

        super(new ByteArrayInputStream(EMPTY_BYTE_ARRAY), outputFormat, length);
        logger.log(Level.TRACE, "begin");

        circularBuffer = new TCircularBuffer(bufferSize, false, true, this);
        this.minAvailable = minAvailable;
        logger.log(Level.TRACE, "end");
    }

    /**
     * Returns the circular buffer.
     */
    protected TCircularBuffer getCircularBuffer() {
        return circularBuffer;
    }

    /**
     * Check if writing more data to the circular buffer is recommended.
     * This checks the available write space in the circular buffer
     * against the minimum available property. If the available write
     * space is greater than th minimum available property, more
     * writing is encouraged, so this method returns true.
     * Note that this is only a  hint to subclasses. However,
     * it is an important hint.
     *
     * @return true if more writing to the circular buffer is
     * recommended. Otherwise, false is returned.
     */
    protected boolean writeMore() {
        return getCircularBuffer().availableWrite() > minAvailable;
    }

    @Override
    public int read() throws IOException {
//logger.log(Level.TRACE, "begin");
        int _byte;
        if (singleByte == null) {
            singleByte = new byte[1];
        }
        int ret = read(singleByte);
        if (ret == -1) {
            _byte = -1;
        } else {
            // $$fb 2001-04-14 nobody really knows that...
            _byte = singleByte[0] & 0xFF;
        }
//logger.log(Level.TRACE, "end");
        return _byte;
    }

    @Override
    public int read(byte[] data) throws IOException {
        logger.log(Level.TRACE, "begin");

        int read = read(data, 0, data.length);

        logger.log(Level.TRACE, "end");

        return read;
    }

    @Override
    public int read(byte[] data, int offset, int length) throws IOException {
        logger.log(Level.TRACE, "begin");

        // $$fb 2001-04-22: this returns at maximum circular buffer
        // length. This is not very efficient...
        // $$fb 2001-04-25: we should check that we do not exceed getFrameLength() !
        int read = circularBuffer.read(data, offset, length);

        logger.log(Level.TRACE, "end");

        return read;
    }

    @Override
    public long skip(long skip) throws IOException {
        // TODO this is quite inefficient
        for (long skipped = 0; skipped < skip; skipped++) {
            int ret = read();
            if (ret == -1) {
                return skipped;
            }
        }
        return skip;
    }

    @Override
    public int available() throws IOException {
        return circularBuffer.availableRead();
    }

    @Override
    public void close() throws IOException {
        circularBuffer.close();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void mark(int readLimit) {
    }

    @Override
    public void reset() throws IOException {
        throw new IOException("mark not supported");
    }
}
