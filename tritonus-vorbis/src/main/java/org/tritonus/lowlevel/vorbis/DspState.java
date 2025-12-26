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

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.tritonus.lowlevel.ogg.Packet;
import vavi.sound.sampled.jna.codec.CodecLibrary;
import vavi.sound.sampled.jna.codec.vorbis_block;
import vavi.sound.sampled.jna.codec.vorbis_comment;
import vavi.sound.sampled.jna.codec.vorbis_dsp_state;
import vavi.sound.sampled.jna.codec.vorbis_info;
import vavi.sound.sampled.jna.ogg.ogg_packet;

import static java.lang.System.getLogger;


/**
 * Wrapper for vorbis_dsp_state.
 */
public class DspState {

    private static final Logger logger= getLogger("org.tritonus.TraceVorbisNative");

    /**
     * Holds the pointer to vorbis_dsp_state
     * for the code.
     * This must be long to be 64bit-clean.
     */
    private vorbis_dsp_state handle;

    public vorbis_dsp_state getHandle() {
        return handle;
    }

    public DspState() {
        logger.log(Level.TRACE, "begin");

        int ret = malloc();
        if (ret < 0) {
            throw new RuntimeException("malloc of vorbis_dsp_state failed");
        }

        logger.log(Level.TRACE, "end");
    }

    private int malloc() {
        logger.log(Level.TRACE, "begin");

        handle = new vorbis_dsp_state();
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
     * Initialize for encoding.
     * Calls vorbis_analysis_init().
     */
    public int initAnalysis(Info info) {
        logger.log(Level.TRACE, "begin");

        vorbis_info infoHandle = info.getHandle();
        int ret = CodecLibrary.INSTANCE.vorbis_analysis_init(handle, infoHandle);

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Calls vorbis_analysis_headerout().
     */
    public int headerOut(Comment comment, Packet packet, Packet commentPacket, Packet codePacket) {
        logger.log(Level.TRACE, "begin");

        vorbis_comment commentHandle = comment.getHandle();
        ogg_packet packetHandle = packet.getHandle();
        ogg_packet commentPacketHandle = commentPacket.getHandle();
        ogg_packet codePacketHandle = codePacket.getHandle();
        int ret = CodecLibrary.INSTANCE.vorbis_analysis_headerout(handle,
                commentHandle,
                packetHandle,
                commentPacketHandle,
                codePacketHandle);

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Calls vorbis_analysis_buffer() and
     * vorbis_analysis_wrote().
     */
    public int write(float[][] values, int count) {
        logger.log(Level.TRACE, "begin");

        // vorbis_analysis_buffer returns float**, which is an array of pointers to float arrays
        Pointer floatStarStar = CodecLibrary.INSTANCE.vorbis_analysis_buffer(handle, count).getPointer();
        logger.log(Level.TRACE, "floatStarStar: %s, %d".formatted(floatStarStar, count));

        if (values != null) {
            int channels = values.length;
            logger.log(Level.TRACE, "channels: %d".formatted(channels));

            Pointer[] pointers = floatStarStar.getPointerArray(0, channels);
            for (int i = 0; i < channels; i++) {
                float[] floatArray = values[i];
                logger.log(Level.TRACE, "floatArray: %d".formatted(floatArray.length));
                pointers[i].write(0, floatArray, 0, count);
            }
        }
        int ret = CodecLibrary.INSTANCE.vorbis_analysis_wrote(handle, count);

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Calls vorbis_analysis_blockout().
     */
    public int blockOut(Block block) {
        logger.log(Level.TRACE, "begin");

        vorbis_block blockHandle = block.getHandle();
        int ret = CodecLibrary.INSTANCE.vorbis_analysis_blockout(handle, blockHandle);

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Calls vorbis_bitrate_flushpacket().
     */
    public int flushPacket(Packet packet) {
        logger.log(Level.TRACE, "begin");

        ogg_packet packetHandle = packet.getHandle();
        int ret = CodecLibrary.INSTANCE.vorbis_bitrate_flushpacket(handle, packetHandle);

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Initialize for decoding.
     * Calls vorbis_synthesis_init().
     */
    public int initSynthesis(Info info) {
        logger.log(Level.TRACE, "begin");

        vorbis_info infoHandle = info.getHandle();
        int ret = CodecLibrary.INSTANCE.vorbis_synthesis_init(handle, infoHandle);

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Calls vorbis_synthesis_blockin().
     */
    public int blockIn(Block block) {
        logger.log(Level.TRACE, "begin");

        vorbis_block blockHandle = block.getHandle();
        int ret = CodecLibrary.INSTANCE.vorbis_synthesis_blockin(handle, blockHandle);

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Calls vorbis_synthesis_pcmout().
     */
    public int pcmOut(float[][] pcm) {
        PointerByReference _pcm = new PointerByReference();

        logger.log(Level.TRACE, "begin");

        int samples = CodecLibrary.INSTANCE.vorbis_synthesis_pcmout(handle, _pcm);
        logger.log(Level.TRACE, "samples: %d".formatted(samples));

        if (samples > 0) {
            int channels = new vorbis_info(handle.vi).channels;
            logger.log(Level.TRACE, "channels: %d".formatted(channels));

            Pointer[] pp = _pcm.getValue().getPointerArray(0, channels);
            for (int channel = 0; channel < channels; channel++) {
                pcm[channel] = pp[channel].getFloatArray(0, samples);
                logger.log(Level.TRACE, "float array: %d".formatted(pcm[channel].length));
            }
            logger.log(Level.TRACE, "end");
        }
        return samples;
    }

    /**
     * Calls vorbis_synthesis_read().
     */
    public int read(int samples) {
        logger.log(Level.TRACE, "begin");

        int ret = CodecLibrary.INSTANCE.vorbis_synthesis_read(handle, samples);

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Accesses sequence.
     */
    public long getSequence() {
        logger.log(Level.TRACE, "begin");

        long ret = handle.sequence;

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Calls vorbis_dsp_clear().
     */
    public void clear() {
        logger.log(Level.TRACE, "begin");

        CodecLibrary.INSTANCE.vorbis_dsp_clear(handle);

        logger.log(Level.TRACE, "end");
    }
}
