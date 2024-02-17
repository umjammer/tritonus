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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.tritonus.lowlevel.ogg.Packet;
import vavi.sound.sampled.jna.codec.CodecLibrary;
import vavi.sound.sampled.jna.codec.vorbis_block;
import vavi.sound.sampled.jna.codec.vorbis_dsp_state;
import vavi.sound.sampled.jna.ogg.ogg_packet;

import static java.lang.System.getLogger;


/**
 * Wrapper for vorbis_block.
 */
public class Block {

    private static final Logger logger= getLogger("org.tritonus.TraceVorbisNative");

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
        logger.log(Level.TRACE, "Block.<init>(): begin");

        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of vorbis_block failed");
        }

        logger.log(Level.TRACE, "Block.<init>(): end");
    }

    private int malloc() {
        logger.log(Level.TRACE, "malloc(): begin");

        handle = new vorbis_block();
        logger.log(Level.TRACE, String.format("malloc(): handle: %s", handle));

        logger.log(Level.TRACE, "malloc(): end");

        return 0;
    }

    public void free() {
        logger.log(Level.TRACE, "free(): begin");

        handle = null;

        logger.log(Level.TRACE, "free(): end");
    }

    /**
     * Calls vorbis_block_init().
     */
    public int init(DspState dspState) {
        logger.log(Level.TRACE, "init(): begin");

        vorbis_dsp_state dspStateHandle = dspState.getHandle();
        int nReturn = CodecLibrary.INSTANCE.vorbis_block_init(dspStateHandle, handle);

        logger.log(Level.TRACE, "init(): end");

        return nReturn;
    }

    /**
     * Calls vorbis_bitrate_addblock().
     */
    public int addBlock() {
        logger.log(Level.TRACE, "addBlock(): begin");

        int nReturn = CodecLibrary.INSTANCE.vorbis_bitrate_addblock(handle);

        logger.log(Level.TRACE, "addBlock(): end");

        return nReturn;
    }

    /**
     * Calls vorbis_analysis().
     */
    public int analysis(Packet packet) {
        logger.log(Level.TRACE, "analysis(): begin");

        ogg_packet packetHandle = null;
        if (packet != null) {
            packetHandle = packet.getHandle();
        }
        int nReturn = CodecLibrary.INSTANCE.vorbis_analysis(handle, packetHandle);

        logger.log(Level.TRACE, "analysis(): end");

        return nReturn;
    }

    /**
     * Calls vorbis_synthesis().
     */
    public int synthesis(Packet packet) {
        logger.log(Level.TRACE, "synthesis(): begin");

        ogg_packet packetHandle = null;
        if (packet != null) {
            packetHandle = packet.getHandle();
        }
        logger.log(Level.TRACE, String.format("synthesis(): packet handle: %s", packetHandle));

        int nReturn = CodecLibrary.INSTANCE.vorbis_synthesis(handle, packetHandle);

        logger.log(Level.TRACE, "synthesis(): end");

        return nReturn;
    }

    /**
     * Calls vorbis_block_clear().
     */
    public int clear() {
        logger.log(Level.TRACE, "clear(): begin");

        int nReturn = CodecLibrary.INSTANCE.vorbis_block_clear(handle);

        logger.log(Level.TRACE, "clear(): end");

        return nReturn;
    }
}
