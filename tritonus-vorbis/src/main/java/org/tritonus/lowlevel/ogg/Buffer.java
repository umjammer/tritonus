/*
 *  Copyright (c) 2000 - 2001 by Matthias Pfisterer
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

package org.tritonus.lowlevel.ogg;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import vavi.sound.sampled.jna.ogg.OggLibrary;
import vavi.sound.sampled.jna.ogg.oggpack_buffer;

import static java.lang.System.getLogger;


/**
 * Wrapper for oggpack_buffer.
 * <p>
 * This file is part of Tritonus: http://www.tritonus.org/
 */
public class Buffer {

    private static final Logger logger= getLogger("org.tritonus.TraceOggNative");

    /**
     * Holds the pointer to oggpack_buffer
     * for the code.
     * This must be long to be 64bit-clean.
     */
    private oggpack_buffer handle;

    public Buffer() {
        logger.log(Level.TRACE, "<init>: begin");

        int ret = malloc();
        if (ret < 0) {
            throw new RuntimeException("malloc of ogg_page failed");
        }

        logger.log(Level.TRACE, "<init>: end");
    }

    private int malloc() {
        logger.log(Level.TRACE, "malloc: begin");

        handle = new oggpack_buffer();
        logger.log(Level.TRACE, "malloc: handle: %s".formatted(handle));

        logger.log(Level.TRACE, "malloc: end");

        return 0;
    }

    public void free() {
        logger.log(Level.TRACE, "free: begin");

        handle = null;

        logger.log(Level.TRACE, "free: end");
    }

    /**
     * Calls oggpack_writeinit().
     */
    public void writeInit() {
        logger.log(Level.TRACE, "writeInit: begin");

        OggLibrary.INSTANCE.oggpack_writeinit(handle);

        logger.log(Level.TRACE, "writeInit: end");
    }

    /**
     * Calls oggpack_writetrunc().
     */
    public void writeTrunc(int bits) {
        logger.log(Level.TRACE, "begin");

        OggLibrary.INSTANCE.oggpack_writetrunc(handle, new NativeLong(bits));

        logger.log(Level.TRACE, "end");
    }

    /**
     * Calls oggpack_writealign().
     */
    public void writeAlign() {
        logger.log(Level.TRACE, "begin");

        OggLibrary.INSTANCE.oggpack_writealign(handle);

        logger.log(Level.TRACE, "end");
    }

    /**
     * Calls oggpack_writecopy().
     */
    public void writeCopy(byte[] _source, int bits) {
        Memory source = new Memory(_source.length);
        logger.log(Level.TRACE, "begin");

        source.write(0, _source, 0, _source.length);
        OggLibrary.INSTANCE.oggpack_writecopy(handle, source, new NativeLong(bits));
        source.close();

        logger.log(Level.TRACE, "end");
    }

    /**
     * Calls oggpack_reset().
     */
    public void reset() {
        logger.log(Level.TRACE, "begin");

        OggLibrary.INSTANCE.oggpack_reset(handle);

        logger.log(Level.TRACE, "end");
    }

    /**
     * Calls oggpack_writeclear().
     */
    public void writeClear() {
        logger.log(Level.TRACE, "begin");

        OggLibrary.INSTANCE.oggpack_writeclear(handle);

        logger.log(Level.TRACE, "end");
    }

    /**
     * Calls oggpack_readinit().
     */
    public void readInit(byte[] _buffer, int bytes) {
        logger.log(Level.TRACE, "begin");

        logger.log(Level.TRACE, "bytes: " + bytes);

        ByteBuffer buffer = ByteBuffer.allocate(_buffer.length);
        logger.log(Level.TRACE, "buffer[0]: " + buffer.get(0));
        logger.log(Level.TRACE, "buffer[1]: " + buffer.get(1));
        logger.log(Level.TRACE, "buffer[2]: " + buffer.get(2));

        OggLibrary.INSTANCE.oggpack_readinit(handle, buffer, bytes);
        buffer.get(_buffer);

        logger.log(Level.TRACE, "end");
    }

    /**
     * Calls oggpack_write().
     */
    public void write(int value, int bits) {
        logger.log(Level.TRACE, "begin");

        OggLibrary.INSTANCE.oggpack_write(handle, new NativeLong(value), bits);

        logger.log(Level.TRACE, "end");
    }

    /**
     * Calls oggpack_look().
     */
    public int look(int bits) {
        logger.log(Level.TRACE, "begin");

        NativeLong ret = OggLibrary.INSTANCE.oggpack_look(handle, bits);

        logger.log(Level.TRACE, "end");

        return ret.intValue();
    }

    /**
     * Calls oggpack_look1().
     */
    public int look1() {
        logger.log(Level.TRACE, "begin");

        NativeLong ret = OggLibrary.INSTANCE.oggpack_look1(handle);

        logger.log(Level.TRACE, "end");

        return ret.intValue();
    }

    /**
     * Calls oggpack_adv().
     */
    public void adv(int bits) {
        logger.log(Level.TRACE, "begin");

        OggLibrary.INSTANCE.oggpack_adv(handle, bits);

        logger.log(Level.TRACE, "end");
    }

    /**
     * Calls oggpack_adv1().
     */
    public void adv1() {
        logger.log(Level.TRACE, "adv1: begin");

        OggLibrary.INSTANCE.oggpack_adv1(handle);

        logger.log(Level.TRACE, "adv1: end");
    }

    /**
     * Calls oggpack_read().
     */
    public int read(int bits) {
        logger.log(Level.TRACE, "begin");

        NativeLong ret = OggLibrary.INSTANCE.oggpack_read(handle, bits);

        logger.log(Level.TRACE, "end");

        return ret.intValue();
    }

    /**
     * Calls oggpack_read1().
     */
    public int read1() {
        logger.log(Level.TRACE, "begin");

        NativeLong ret = OggLibrary.INSTANCE.oggpack_read1(handle);

        logger.log(Level.TRACE, "end");

        return ret.intValue();
    }

    /**
     * Calls oggpack_bytes().
     */
    public int bytes() {
        logger.log(Level.TRACE, "begin");

        NativeLong ret = OggLibrary.INSTANCE.oggpack_bytes(handle);

        logger.log(Level.TRACE, "end");

        return ret.intValue();
    }

    /**
     * Calls oggpack_bits().
     */
    public int bits() {
        logger.log(Level.TRACE, "begin");

        NativeLong ret = OggLibrary.INSTANCE.oggpack_bits(handle);

        logger.log(Level.TRACE, "end");

        return ret.intValue();
    }

    /**
     * Calls oggpack_get_buffer().
     */
    public byte[] getBuffer() {
        logger.log(Level.TRACE, "begin");

        Pointer buffer = OggLibrary.INSTANCE.oggpack_get_buffer(handle);
        byte[] _buffer = new byte[handle.storage.intValue()];
        buffer.read(0, _buffer, 0, handle.storage.intValue());

        logger.log(Level.TRACE, "end");

        return _buffer;
    }
}
