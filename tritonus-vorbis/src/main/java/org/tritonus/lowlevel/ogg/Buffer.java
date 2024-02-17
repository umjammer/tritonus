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

import java.nio.ByteBuffer;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.tritonus.share.TDebug;
import vavi.sound.sampled.jna.ogg.OggLibrary;
import vavi.sound.sampled.jna.ogg.oggpack_buffer;


/**
 * Wrapper for oggpack_buffer.
 * <p>
 * This file is part of Tritonus: http://www.tritonus.org/
 */
public class Buffer {

    /**
     * Holds the pointer to oggpack_buffer
     * for the code.
     * This must be long to be 64bit-clean.
     */
    private oggpack_buffer handle;

    public Buffer() {
        if (TDebug.TraceOggNative) {
            TDebug.out("<init>: begin");
        }
        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of ogg_page failed");
        }
        if (TDebug.TraceOggNative) {
            TDebug.out("<init>: end");
        }
    }

    private int malloc() {
        if (TDebug.TraceOggNative) {
            TDebug.out("malloc: begin");
        }
        handle = new oggpack_buffer();
        if (TDebug.TraceOggNative) {
            TDebug.out(String.format("malloc: handle: %s", handle));
        }
        if (TDebug.TraceOggNative) {
            TDebug.out("malloc: end");
        }
        return 0;
    }

    public void free() {
        if (TDebug.TraceOggNative) {
            TDebug.out("free: begin");
        }
        handle = null;
        if (TDebug.TraceOggNative) {
            TDebug.out("free: end");
        }
    }

    /**
     * Calls oggpack_writeinit().
     */
    public void writeInit() {
        if (TDebug.TraceOggNative) {
            TDebug.out("writeInit: begin");
        }
        OggLibrary.INSTANCE.oggpack_writeinit(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("writeInit: end");
        }
    }

    /**
     * Calls oggpack_writetrunc().
     */
    public void writeTrunc(int nBits) {
        if (TDebug.TraceOggNative) {
            TDebug.out("writeTrunc: begin");
        }
        OggLibrary.INSTANCE.oggpack_writetrunc(handle, new NativeLong(nBits));
        if (TDebug.TraceOggNative) {
            TDebug.out("writeTrunc: end");
        }
    }

    /**
     * Calls oggpack_writealign().
     */
    public void writeAlign() {
        if (TDebug.TraceOggNative) {
            TDebug.out("writeAlign: begin");
        }
        OggLibrary.INSTANCE.oggpack_writealign(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("writeAlign: end");
        }
    }

    /**
     * Calls oggpack_writecopy().
     */
    public void writeCopy(byte[] abSource, int nBits) {
        Memory source = new Memory(abSource.length);
        if (TDebug.TraceOggNative) {
            TDebug.out("writeCopy: begin");
        }
        source.write(0, abSource, 0, abSource.length);
        OggLibrary.INSTANCE.oggpack_writecopy(handle, source, new NativeLong(nBits));
        source.close();
        if (TDebug.TraceOggNative) {
            TDebug.out("writeCopy: end");
        }
    }

    /**
     * Calls oggpack_reset().
     */
    public void reset() {
        if (TDebug.TraceOggNative) {
            TDebug.out("reset: begin");
        }
        OggLibrary.INSTANCE.oggpack_reset(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("reset: end");
        }
    }

    /**
     * Calls oggpack_writeclear().
     */
    public void writeClear() {
        if (TDebug.TraceOggNative) {
            TDebug.out("writeClear: begin");
        }
        OggLibrary.INSTANCE.oggpack_writeclear(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("writeClear: end");
        }
    }

    /**
     * Calls oggpack_readinit().
     */
    public void readInit(byte[] abBuffer, int nBytes) {
        if (TDebug.TraceOggNative) {
            TDebug.out("readInit: begin");
        }
        if (TDebug.TraceOggNative) {
            TDebug.out(String.format("readInit: nBytes: %d", nBytes));
        }
        ByteBuffer buffer = ByteBuffer.allocate(abBuffer.length);
        if (TDebug.TraceOggNative) {
            TDebug.out(String.format("readInit: buffer[0]: %d", buffer.get(0)));
        }
        if (TDebug.TraceOggNative) {
            TDebug.out(String.format("readInit: buffer[1]: %d", buffer.get(1)));
        }
        if (TDebug.TraceOggNative) {
            TDebug.out(String.format("readInit: buffer[2]: %d", buffer.get(2)));
        }
        OggLibrary.INSTANCE.oggpack_readinit(handle, buffer, nBytes);
        buffer.get(abBuffer);
        if (TDebug.TraceOggNative) {
            TDebug.out("readInit: end");
        }
    }

    /**
     * Calls oggpack_write().
     */
    public void write(int nValue, int nBits) {
        if (TDebug.TraceOggNative) {
            TDebug.out("write: begin");
        }
        OggLibrary.INSTANCE.oggpack_write(handle, new NativeLong(nValue), nBits);
        if (TDebug.TraceOggNative) {
            TDebug.out("write: end");
        }
    }

    /**
     * Calls oggpack_look().
     */
    public int look(int nBits) {
        if (TDebug.TraceOggNative) {
            TDebug.out("look: begin");
        }
        NativeLong nReturn = OggLibrary.INSTANCE.oggpack_look(handle, nBits);
        if (TDebug.TraceOggNative) {
            TDebug.out("look: end");
        }
        return nReturn.intValue();
    }

    /**
     * Calls oggpack_look1().
     */
    public int look1() {
        if (TDebug.TraceOggNative) {
            TDebug.out("look1: begin");
        }
        NativeLong nReturn = OggLibrary.INSTANCE.oggpack_look1(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("look1: end");
        }
        return nReturn.intValue();
    }

    /**
     * Calls oggpack_adv().
     */
    public void adv(int nBits) {
        if (TDebug.TraceOggNative) {
            TDebug.out("adv: begin");
        }
        OggLibrary.INSTANCE.oggpack_adv(handle, nBits);
        if (TDebug.TraceOggNative) {
            TDebug.out("adv: end");
        }
    }

    /**
     * Calls oggpack_adv1().
     */
    public void adv1() {
        if (TDebug.TraceOggNative) {
            TDebug.out("adv1: begin");
        }
        OggLibrary.INSTANCE.oggpack_adv1(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("adv1: end");
        }
    }

    /**
     * Calls oggpack_read().
     */
    public int read(int nBits) {
        if (TDebug.TraceOggNative) {
            TDebug.out("read: begin");
        }
        NativeLong nReturn = OggLibrary.INSTANCE.oggpack_read(handle, nBits);
        if (TDebug.TraceOggNative) {
            TDebug.out("read: end");
        }
        return nReturn.intValue();
    }

    /**
     * Calls oggpack_read1().
     */
    public int read1() {
        if (TDebug.TraceOggNative) {
            TDebug.out("read1: begin");
        }
        NativeLong nReturn = OggLibrary.INSTANCE.oggpack_read1(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("read1: end");
        }
        return nReturn.intValue();
    }

    /**
     * Calls oggpack_bytes().
     */
    public int bytes() {
        if (TDebug.TraceOggNative) {
            TDebug.out("bytes: begin");
        }
        NativeLong nReturn = OggLibrary.INSTANCE.oggpack_bytes(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("bytes: end");
        }
        return nReturn.intValue();
    }

    /**
     * Calls oggpack_bits().
     */
    public int bits() {
        if (TDebug.TraceOggNative) {
            TDebug.out("bits: begin");
        }
        NativeLong nReturn = OggLibrary.INSTANCE.oggpack_bits(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("bits: end");
        }
        return nReturn.intValue();
    }

    /**
     * Calls oggpack_get_buffer().
     */
    public byte[] getBuffer() {
        if (TDebug.TraceOggNative) {
            TDebug.out("getBuffer: begin");
        }
        Pointer buffer = OggLibrary.INSTANCE.oggpack_get_buffer(handle);
        byte[] abBuffer = new byte[handle.storage.intValue()];
        buffer.read(0, abBuffer, 0, handle.storage.intValue());
        if (TDebug.TraceOggNative) {
            TDebug.out("getBuffer: end");
        }
        return abBuffer;
    }
}


