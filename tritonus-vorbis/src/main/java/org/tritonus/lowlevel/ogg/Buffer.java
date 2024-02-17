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

        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of ogg_page failed");
        }

        logger.log(Level.TRACE, "<init>: end");
    }

    private int malloc() {
        logger.log(Level.TRACE, "malloc: begin");

        handle = new oggpack_buffer();
        logger.log(Level.TRACE, String.format("malloc: handle: %s", handle));

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
    public void writeTrunc(int nBits) {
        logger.log(Level.TRACE, "writeTrunc: begin");

        OggLibrary.INSTANCE.oggpack_writetrunc(handle, new NativeLong(nBits));

        logger.log(Level.TRACE, "writeTrunc: end");
    }

    /**
     * Calls oggpack_writealign().
     */
    public void writeAlign() {
        logger.log(Level.TRACE, "writeAlign: begin");

        OggLibrary.INSTANCE.oggpack_writealign(handle);

        logger.log(Level.TRACE, "writeAlign: end");
    }

    /**
     * Calls oggpack_writecopy().
     */
    public void writeCopy(byte[] abSource, int nBits) {
        Memory source = new Memory(abSource.length);
        logger.log(Level.TRACE, "writeCopy: begin");

        source.write(0, abSource, 0, abSource.length);
        OggLibrary.INSTANCE.oggpack_writecopy(handle, source, new NativeLong(nBits));
        source.close();

        logger.log(Level.TRACE, "writeCopy: end");
    }

    /**
     * Calls oggpack_reset().
     */
    public void reset() {
        logger.log(Level.TRACE, "reset: begin");

        OggLibrary.INSTANCE.oggpack_reset(handle);

        logger.log(Level.TRACE, "reset: end");
    }

    /**
     * Calls oggpack_writeclear().
     */
    public void writeClear() {
        logger.log(Level.TRACE, "writeClear: begin");

        OggLibrary.INSTANCE.oggpack_writeclear(handle);

        logger.log(Level.TRACE, "writeClear: end");
    }

    /**
     * Calls oggpack_readinit().
     */
    public void readInit(byte[] abBuffer, int nBytes) {
        logger.log(Level.TRACE, "readInit: begin");

        logger.log(Level.TRACE, "readInit: nBytes: " + nBytes);

        ByteBuffer buffer = ByteBuffer.allocate(abBuffer.length);
        logger.log(Level.TRACE, "readInit: buffer[0]: " + buffer.get(0));
        logger.log(Level.TRACE, "readInit: buffer[1]: " + buffer.get(1));
        logger.log(Level.TRACE, "readInit: buffer[2]: " + buffer.get(2));

        OggLibrary.INSTANCE.oggpack_readinit(handle, buffer, nBytes);
        buffer.get(abBuffer);

        logger.log(Level.TRACE, "readInit: end");
    }

    /**
     * Calls oggpack_write().
     */
    public void write(int nValue, int nBits) {
        logger.log(Level.TRACE, "write: begin");

        OggLibrary.INSTANCE.oggpack_write(handle, new NativeLong(nValue), nBits);

        logger.log(Level.TRACE, "write: end");
    }

    /**
     * Calls oggpack_look().
     */
    public int look(int nBits) {
        logger.log(Level.TRACE, "look: begin");

        NativeLong nReturn = OggLibrary.INSTANCE.oggpack_look(handle, nBits);

        logger.log(Level.TRACE, "look: end");

        return nReturn.intValue();
    }

    /**
     * Calls oggpack_look1().
     */
    public int look1() {
        logger.log(Level.TRACE, "look1: begin");

        NativeLong nReturn = OggLibrary.INSTANCE.oggpack_look1(handle);

        logger.log(Level.TRACE, "look1: end");

        return nReturn.intValue();
    }

    /**
     * Calls oggpack_adv().
     */
    public void adv(int nBits) {
        logger.log(Level.TRACE, "adv: begin");

        OggLibrary.INSTANCE.oggpack_adv(handle, nBits);

        logger.log(Level.TRACE, "adv: end");
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
    public int read(int nBits) {
        logger.log(Level.TRACE, "read: begin");

        NativeLong nReturn = OggLibrary.INSTANCE.oggpack_read(handle, nBits);

        logger.log(Level.TRACE, "read: end");

        return nReturn.intValue();
    }

    /**
     * Calls oggpack_read1().
     */
    public int read1() {
        logger.log(Level.TRACE, "read1: begin");

        NativeLong nReturn = OggLibrary.INSTANCE.oggpack_read1(handle);

        logger.log(Level.TRACE, "read1: end");

        return nReturn.intValue();
    }

    /**
     * Calls oggpack_bytes().
     */
    public int bytes() {
        logger.log(Level.TRACE, "bytes: begin");

        NativeLong nReturn = OggLibrary.INSTANCE.oggpack_bytes(handle);

        logger.log(Level.TRACE, "bytes: end");

        return nReturn.intValue();
    }

    /**
     * Calls oggpack_bits().
     */
    public int bits() {
        logger.log(Level.TRACE, "bits: begin");

        NativeLong nReturn = OggLibrary.INSTANCE.oggpack_bits(handle);

        logger.log(Level.TRACE, "bits: end");

        return nReturn.intValue();
    }

    /**
     * Calls oggpack_get_buffer().
     */
    public byte[] getBuffer() {
        logger.log(Level.TRACE, "getBuffer: begin");

        Pointer buffer = OggLibrary.INSTANCE.oggpack_get_buffer(handle);
        byte[] abBuffer = new byte[handle.storage.intValue()];
        buffer.read(0, abBuffer, 0, handle.storage.intValue());

        logger.log(Level.TRACE, "getBuffer: end");

        return abBuffer;
    }
}
