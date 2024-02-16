/*
 * DspState.java
 *
 * This file is part of Tritonus: http://www.tritonus.org/
 */

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

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;
import org.tritonus.lowlevel.ogg.Packet;
import org.tritonus.share.TDebug;
import vavi.sound.sampled.jna.codec.CodecLibrary;
import vavi.sound.sampled.jna.codec.vorbis_block;
import vavi.sound.sampled.jna.codec.vorbis_comment;
import vavi.sound.sampled.jna.codec.vorbis_dsp_state;
import vavi.sound.sampled.jna.codec.vorbis_info;
import vavi.sound.sampled.jna.ogg.ogg_packet;


/**
 * Wrapper for vorbis_dsp_state.
 */
public class DspState {

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
        if (TDebug.TraceVorbisNative) {
            TDebug.out("DspState.<init>(): begin");
        }
        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of vorbis_dsp_state failed");
        }
        if (TDebug.TraceVorbisNative) {
            TDebug.out("DspState.<init>(): end");
        }
    }

    private int malloc() {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("malloc(): begin");
        }
        handle = new vorbis_dsp_state();
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
     * Initialize for encoding.
     * Calls vorbis_analysis_init().
     */
    public int initAnalysis(Info info) {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("initAnalysis(): begin");
        }
        vorbis_info infoHandle = info.getHandle();
        int nReturn = CodecLibrary.INSTANCE.vorbis_analysis_init(handle, infoHandle);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("initAnalysis(): end");
        }
        return nReturn;
    }

    /**
     * Calls vorbis_analysis_headerout().
     */
    public int headerOut(
            Comment comment,
            Packet packet,
            Packet commentPacket,
            Packet codePacket) {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("headerOut(): begin");
        }
        vorbis_comment commentHandle = comment.getHandle();
        ogg_packet packetHandle = packet.getHandle();
        ogg_packet commentPacketHandle = commentPacket.getHandle();
        ogg_packet codePacketHandle = codePacket.getHandle();
        int nReturn = CodecLibrary.INSTANCE.vorbis_analysis_headerout(handle,
                commentHandle,
                packetHandle,
                commentPacketHandle,
                codePacketHandle);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("headerOut(): end");
        }
        return nReturn;
    }

    /**
     * Calls vorbis_analysis_buffer() and
     * vorbis_analysis_wrote().
     */
    public int write(float[][] afValues, int nValues) {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("write(): begin");
        }
        Pointer bufferPointer = CodecLibrary.INSTANCE.vorbis_analysis_buffer(handle, nValues).getValue();
        if (TDebug.TraceVorbisNative) {
            TDebug.out(String.format("write(): bufferPointer: %s, %d", bufferPointer, nValues));
        }
        if (afValues != null) {
            int nObjectArrayLength = afValues.length;
            if (TDebug.TraceVorbisNative) {
                TDebug.out(String.format("write(): objectArray length: %d", nObjectArrayLength));
            }
            long bufferPointerP = 0;
            for (float[] floatArray : afValues) {
                if (TDebug.TraceVorbisNative) {
                    TDebug.out(String.format("write(): floatArray: %d", floatArray.length));
                }
                bufferPointer.write(bufferPointerP, floatArray, 0, nValues);
                bufferPointerP += ((long) nValues * Float.BYTES);
            }
        }
        int nReturn = CodecLibrary.INSTANCE.vorbis_analysis_wrote(handle, nValues);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("write(): end");
        }
        return nReturn;
    }

    /**
     * Calls vorbis_analysis_blockout().
     */
    public int blockOut(Block block) {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("blockOut(): begin");
        }
        vorbis_block blockHandle = block.getHandle();
        int nReturn = CodecLibrary.INSTANCE.vorbis_analysis_blockout(handle, blockHandle);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("blockOut(): end");
        }
        return nReturn;
    }

    /**
     * Calls vorbis_bitrate_flushpacket().
     */
    public int flushPacket(Packet packet) {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("flushPacket(): begin");
        }
        ogg_packet packetHandle = packet.getHandle();
        int nReturn = CodecLibrary.INSTANCE.vorbis_bitrate_flushpacket(handle, packetHandle);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("flushPacket(): end");
        }
        return nReturn;
    }

    /**
     * Initialize for decoding.
     * Calls vorbis_synthesis_init().
     */
    public int initSynthesis(Info info) {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("initSynthesis(): begin");
        }
        vorbis_info infoHandle = info.getHandle();
        int nReturn = CodecLibrary.INSTANCE.vorbis_synthesis_init(handle, infoHandle);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("initSynthesis(): end");
        }
        return nReturn;
    }

    /**
     * Calls vorbis_synthesis_blockin().
     */
    public int blockIn(Block block) {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("blockIn(): begin");
        }
        vorbis_block blockHandle = block.getHandle();
        int nReturn = CodecLibrary.INSTANCE.vorbis_synthesis_blockin(handle, blockHandle);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("blockIn(): end");
        }
        return nReturn;
    }

    /**
     * Calls vorbis_synthesis_pcmout().
     */
    public int pcmOut(float[][] afPcm) {
        PointerByReference pcm = new PointerByReference();
        if (TDebug.TraceVorbisNative) {
            TDebug.out("pcmOut(): begin");
        }
        int nSamples = CodecLibrary.INSTANCE.vorbis_synthesis_pcmout(handle, pcm);
        if (TDebug.TraceVorbisNative) {
            TDebug.out(String.format("pcmOut(): samples: %d", nSamples));
        }
        if (nSamples > 0) {
            int nChannels = new vorbis_info(handle.vi).channels;
            if (TDebug.TraceVorbisNative) {
                TDebug.out(String.format("pcmOut(): channels: %d", nChannels));
            }
            Pointer[] pp = pcm.getValue().getPointerArray(0, nChannels);
            for (int nChannel = 0; nChannel < nChannels; nChannel++) {
                afPcm[nChannel] = pp[nChannel].getFloatArray(0, nSamples);
                if (TDebug.TraceVorbisNative) {
                    TDebug.out(String.format("pcmOut(): float array: %d", afPcm[nChannel].length));
                }
            }
            if (TDebug.TraceVorbisNative) {
                TDebug.out("pcmOut(): end");
            }
        }
        return nSamples;
    }

    /**
     * Calls vorbis_synthesis_read().
     */
    public int read(int nSamples) {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("read(): begin");
        }
        int nReturn = CodecLibrary.INSTANCE.vorbis_synthesis_read(handle, nSamples);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("read(): end");
        }
        return nReturn;
    }

    /**
     * Accesses sequence.
     */
    public long getSequence() {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("getSequence(): begin");
        }
        long lReturn = handle.sequence;
        if (TDebug.TraceVorbisNative) {
            TDebug.out("getSequence(): end");
        }
        return lReturn;
    }

    /**
     * Calls vorbis_dsp_clear().
     */
    public void clear() {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("clear(): begin");
        }
        CodecLibrary.INSTANCE.vorbis_dsp_clear(handle);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("clear(): end");
        }
    }
}

/* DspState.java */
