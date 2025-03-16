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

import com.sun.jna.NativeLong;
import org.tritonus.lowlevel.ogg.Packet;
import vavi.sound.sampled.jna.codec.CodecLibrary;
import vavi.sound.sampled.jna.codec.vorbis_comment;
import vavi.sound.sampled.jna.codec.vorbis_info;
import vavi.sound.sampled.jna.ogg.ogg_packet;
import vavi.sound.sampled.jna.vorbisenc.VorbisencLibrary;

import static java.lang.System.getLogger;


/**
 * Wrapper for vorbis_info.
 */
public class Info {

    private static final Logger logger= getLogger("org.tritonus.TraceVorbisNative");

    /**
     * Holds the pointer to vorbis_info
     * for the code.
     * This must be long to be 64bit-clean.
     */
    private vorbis_info handle;

    public vorbis_info getHandle() {
        return handle;
    }

    public Info() {
        logger.log(Level.TRACE, "begin");

        int ret = malloc();
        if (ret < 0) {
            throw new RuntimeException("malloc of vorbis_info failed");
        }

        logger.log(Level.TRACE, "end");
    }

    private int malloc() {
        logger.log(Level.TRACE, "begin");

        handle = new vorbis_info();
        logger.log(Level.TRACE, "handle: %s".formatted(handle));

        logger.log(Level.TRACE, "end");

        return 0;
    }

    public void free() {
        logger.log(Level.TRACE, "begin");

        handle = null;

        logger.log(Level.TRACE, "end");
    }

    /**
     * Calls vorbis_info_init().
     */
    public void init() {
        logger.log(Level.TRACE, "begin");

        CodecLibrary.INSTANCE.vorbis_info_init(handle);

        logger.log(Level.TRACE, "end");
    }

    /**
     * Calls vorbis_info_clear().
     */
    public void clear() {
        logger.log(Level.TRACE, "begin");

        CodecLibrary.INSTANCE.vorbis_info_clear(handle);

        logger.log(Level.TRACE, "end");
    }

    // blocksize?

    /**
     * Accesses channels.
     */
    public int getChannels() {
        logger.log(Level.TRACE, "begin");

        int ret = handle.channels;

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Accesses rate.
     */
    public int getRate() {
        logger.log(Level.TRACE, "begin");

        NativeLong nReturn = handle.rate;

        logger.log(Level.TRACE, "end");

        return nReturn.intValue();
    }

    /**
     * Calls vorbis_encode_init().
     */
    public int encodeInit(
            int channels,
            int rate,
            int maxBitrate,
            int nominalBitrate,
            int minBitrate) {
        logger.log(Level.TRACE, "begin");

        int ret = VorbisencLibrary.INSTANCE.vorbis_encode_init(handle, new NativeLong(channels), new NativeLong(rate),
                new NativeLong(maxBitrate), new NativeLong(nominalBitrate), new NativeLong(minBitrate));

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Calls vorbis_encode_init_vbr().
     */
    public int encodeInitVBR(
            int channels,
            int rate,
            float quality) {
        logger.log(Level.TRACE, "begin");

        int ret = VorbisencLibrary.INSTANCE.vorbis_encode_init_vbr(handle, new NativeLong(channels), new NativeLong(rate), quality);

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Calls vorbis_synthesis_headerin().
     */
    public int headerIn(Comment comment, Packet packet) {
        logger.log(Level.TRACE, "begin");

        vorbis_comment commentHandle = comment.getHandle();
        ogg_packet packetHandle = packet.getHandle();
        int ret = CodecLibrary.INSTANCE.vorbis_synthesis_headerin(handle, commentHandle, packetHandle);

        logger.log(Level.TRACE, "end");

        return ret;
    }
}
