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

package org.tritonus.lowlevel.vorbis;

import org.tritonus.lowlevel.ogg.Packet;
import org.tritonus.share.TDebug;
import vavi.sound.sampled.jna.codec.CodecLibrary;
import vavi.sound.sampled.jna.codec.vorbis_block;
import vavi.sound.sampled.jna.codec.vorbis_dsp_state;
import vavi.sound.sampled.jna.ogg.ogg_packet;


/**
 * Wrapper for vorbis_block.
 */
public class Block {

    /**
     * Holds the pointer to vorbis_block
     * for the code.
     * This must be long to be 64bit-clean.
     */
    private vorbis_block handle;

    public vorbis_block getHandle() {
        return handle;
    }

    public Block() {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("Block.<init>(): begin");
        }
        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of vorbis_block failed");
        }
        if (TDebug.TraceVorbisNative) {
            TDebug.out("Block.<init>(): end");
        }
    }

    private int malloc() {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("malloc(): begin");
        }
        handle = new vorbis_block();
        if (TDebug.TraceVorbisNative) {
            TDebug.out(String.format("malloc(): handle: %s", handle));
        }
        if (TDebug.TraceVorbisNative) {
            TDebug.out("malloc(): end");
        }
        return 0;
    }

    public void free() {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("free(): begin");
        }
        handle = null;
        if (TDebug.TraceVorbisNative) {
            TDebug.out("free(): end");
        }
    }

    /**
     * Calls vorbis_block_init().
     */
    public int init(DspState dspState) {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("init(): begin");
        }
        vorbis_dsp_state dspStateHandle = dspState.getHandle();
        int nReturn = CodecLibrary.INSTANCE.vorbis_block_init(dspStateHandle, handle);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("init(): end");
        }
        return nReturn;
    }

    /**
     * Calls vorbis_bitrate_addblock().
     */
    public int addBlock() {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("addBlock(): begin");
        }
        int nReturn = CodecLibrary.INSTANCE.vorbis_bitrate_addblock(handle);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("addBlock(): end");
        }
        return nReturn;
    }

    /**
     * Calls vorbis_analysis().
     */
    public int analysis(Packet packet) {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("analysis(): begin");
        }
        ogg_packet packetHandle = null;
        if (packet != null) {
            packetHandle = packet.getHandle();
        }
        int nReturn = CodecLibrary.INSTANCE.vorbis_analysis(handle, packetHandle);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("analysis(): end");
        }
        return nReturn;
    }

    /**
     * Calls vorbis_synthesis().
     */
    public int synthesis(Packet packet) {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("synthesis(): begin");
        }
        ogg_packet packetHandle = null;
        if (packet != null) {
            packetHandle = packet.getHandle();
        }
        if (TDebug.TraceVorbisNative) {
            TDebug.out(String.format("synthesis(): packet handle: %s", packetHandle));
        }
        int nReturn = CodecLibrary.INSTANCE.vorbis_synthesis(handle, packetHandle);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("synthesis(): end");
        }
        return nReturn;
    }

    /**
     * Calls vorbis_block_clear().
     */
    public int clear() {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("clear(): begin");
        }
        int nReturn = CodecLibrary.INSTANCE.vorbis_block_clear(handle);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("clear(): end");
        }
        return nReturn;
    }
}


