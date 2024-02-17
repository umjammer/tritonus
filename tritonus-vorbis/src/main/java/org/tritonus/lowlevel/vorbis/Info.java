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

/*
|<---            this code is formatted to fit into 80 columns             --.|
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
        logger.log(Level.TRACE, "Info.<init>(): begin");

        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of vorbis_info failed");
        }

        logger.log(Level.TRACE, "Info.<init>(): end");
    }

    private int malloc() {
        logger.log(Level.TRACE, "malloc(): begin");

        handle = new vorbis_info();
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
     * Calls vorbis_info_init().
     */
    public void init() {
        logger.log(Level.TRACE, "init(): begin");

        CodecLibrary.INSTANCE.vorbis_info_init(handle);

        logger.log(Level.TRACE, "init(): end");
    }

    /**
     * Calls vorbis_info_clear().
     */
    public void clear() {
        logger.log(Level.TRACE, "clear(): begin");

        CodecLibrary.INSTANCE.vorbis_info_clear(handle);

        logger.log(Level.TRACE, "clear(): end");
    }

    // blocksize?

    /**
     * Accesses channels.
     */
    public int getChannels() {
        logger.log(Level.TRACE, "getChannels(): begin");

        int nReturn = handle.channels;

        logger.log(Level.TRACE, "getChannels(): end");

        return nReturn;
    }

    /**
     * Accesses rate.
     */
    public int getRate() {
        logger.log(Level.TRACE, "getRate(): begin");

        NativeLong nReturn = handle.rate;

        logger.log(Level.TRACE, "getRate(): end");

        return nReturn.intValue();
    }

    /**
     * Calls vorbis_encode_init().
     */
    public int encodeInit(
            int nChannels,
            int nRate,
            int nMaxBitrate,
            int nNominalBitrate,
            int nMinBitrate) {
        logger.log(Level.TRACE, "encodeInit(): begin");

        int nReturn = VorbisencLibrary.INSTANCE.vorbis_encode_init(handle, new NativeLong(nChannels), new NativeLong(nRate),
                new NativeLong(nMaxBitrate), new NativeLong(nNominalBitrate), new NativeLong(nMinBitrate));

        logger.log(Level.TRACE, "encodeInit(): end");

        return nReturn;
    }

    /**
     * Calls vorbis_encode_init_vbr().
     */
    public int encodeInitVBR(
            int nChannels,
            int nRate,
            float fQuality) {
        logger.log(Level.TRACE, "encodeInitVBR(): begin");

        int nReturn = VorbisencLibrary.INSTANCE.vorbis_encode_init_vbr(handle, new NativeLong(nChannels), new NativeLong(nRate), fQuality);

        logger.log(Level.TRACE, "encodeInitVBR(): end");

        return nReturn;
    }

    /**
     * Calls vorbis_synthesis_headerin().
     */
    public int headerIn(Comment comment, Packet packet) {
        logger.log(Level.TRACE, "headerIn(): begin");

        vorbis_comment commentHandle = comment.getHandle();
        ogg_packet packetHandle = packet.getHandle();
        int nReturn = CodecLibrary.INSTANCE.vorbis_synthesis_headerin(handle, commentHandle, packetHandle);

        logger.log(Level.TRACE, "headerIn(): end");

        return nReturn;
    }
}
