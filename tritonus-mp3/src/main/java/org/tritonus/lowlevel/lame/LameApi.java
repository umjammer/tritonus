/*
 *  Copyright (c) 2001 by Florian Bomers
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

package org.tritonus.lowlevel.lame;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.ByteBuffer;

import com.sun.jna.ptr.PointerByReference;
import vavi.sound.sampled.jna.lame.LameLibrary.vbr_mode_e;
import vavi.sound.sampled.jna.lame.lame_version_t;

import static java.lang.System.getLogger;
import static vavi.sound.sampled.jna.lame.LameLibrary.INSTANCE;


/**
 * functions that use the lame API
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2024-02-10 nsano initial version <br>
 */
public class LameApi {

    private static final Logger logger = getLogger(LameApi.class.getName());

    public int channels;
    public int sampleRate;
    public int bitrate;
    public int mode;
    public int quality;
    public boolean vbr;
    public int mpegVersion;
    public boolean swapBytes;
    public PointerByReference gf;

    /** @return -1 if something failed. */
    public int doInit() {

        this.gf = INSTANCE.lame_init();
        if (this.gf == null) {
            //throwRuntimeException(env, "out of memory");
            return org.tritonus.lowlevel.lame.Lame.OUT_OF_MEMORY;
        }

        INSTANCE.lame_set_num_channels(this.gf, this.channels);
        INSTANCE.lame_set_in_samplerate(this.gf, this.sampleRate);
        if (this.mode != org.tritonus.lowlevel.lame.Lame.CHANNEL_MODE_AUTO) {
            INSTANCE.lame_set_mode(this.gf, this.mode);
        }
        if (this.vbr) {
            INSTANCE.lame_set_VBR(this.gf, vbr_mode_e.vbr_default);
            INSTANCE.lame_set_VBR_q(this.gf, this.quality);
        } else {
            if (this.bitrate != org.tritonus.lowlevel.lame.Lame.BITRATE_AUTO) {
                INSTANCE.lame_set_brate(this.gf, this.bitrate);
            }
        }
        INSTANCE.lame_set_quality(this.gf, this.quality);
        int result = INSTANCE.lame_init_params(this.gf);

        // return effective values
        this.sampleRate = INSTANCE.lame_get_out_samplerate(this.gf);
logger.log(Level.DEBUG, "sampleRate: " + sampleRate);
        this.bitrate = INSTANCE.lame_get_brate(this.gf);
logger.log(Level.DEBUG, "bitrate: " + bitrate);
        this.mode = INSTANCE.lame_get_mode(this.gf);
logger.log(Level.DEBUG, "mode: " + mode);
        this.vbr = INSTANCE.lame_get_VBR(this.gf) != 0;
logger.log(Level.DEBUG, "vbr: " + vbr);
        this.quality = this.vbr ? INSTANCE.lame_get_VBR_q(this.gf) : INSTANCE.lame_get_quality(this.gf);
logger.log(Level.DEBUG, "quality: " + quality);
        this.mpegVersion = INSTANCE.lame_get_version(this.gf);
logger.log(Level.DEBUG, "mpegVersion: " + mpegVersion);

        return result;
    }

    public int doGetPCMBufferSize(int wishedBufferSize) {
        // lame supports all buffer sizes
        return wishedBufferSize;
    }

    public int doEncode(byte[] pcmSamples, int pcmLengthInFrames, byte[] encodedBytes, int encodedArrayByteSize) {
        if (this.gf == null) {
            //throwRuntimeException(env, "not initialized");
            return org.tritonus.lowlevel.lame.Lame.NOT_INITIALIZED;
        }
        if (this.channels == 1) {
            return INSTANCE.lame_encode_buffer(this.gf, pcmSamples, pcmSamples, pcmLengthInFrames,
                    encodedBytes, encodedArrayByteSize);
        } else {
            return INSTANCE.lame_encode_buffer_interleaved(this.gf, pcmSamples, pcmLengthInFrames,
                    encodedBytes, encodedArrayByteSize);
        }
    }

    public int doEncodeFinish(ByteBuffer encodedBytes, int encodedArrayByteSize) {
        if (this.gf == null) {
            //throwRuntimeException(env, "not initialized");
            return org.tritonus.lowlevel.lame.Lame.NOT_INITIALIZED;
        }
        return INSTANCE.lame_encode_flush(this.gf, encodedBytes, encodedArrayByteSize);
    }

    public void doClose() {
        if (this.gf != null) {
            INSTANCE.lame_close(this.gf);
            this.gf = null;
        }
    }

    int copyVersion(String[] s, int len, int major, int minor, boolean alpha, boolean beta) {
        int thislen;
        int result = 0;
        if (len < 8) {
            return -1;
        }
        s[0] = "%d.%d".formatted(major, minor);
        thislen = s[0].length();
        len -= thislen;
        result += thislen;
        // now put something like "alpha" or "beta"
        if (alpha && len >= 6) {
            s[0] += "alpha";
            len -= 5;
            result += 5;
        } else if (beta && len >= 5) {
            s[0] += "beta";
            len -= 4;
            result += 4;
        }
        return result;
    }

    /**
     * returns -1 if string is too short or returns one of the exception
     * constants if everything OK, returns the length of the string
     */
    public int doGetEncoderVersion(String[] charBuffer, int charBufferSize) {
        lame_version_t version;
        int size = charBufferSize;
        int len;
        int result = 0;

        version = new lame_version_t();
        INSTANCE.get_lame_version_numerical(version);
        // first put something like "LAME 333.333 alpha"
        if (size >= 13) {
            charBuffer[0] = "LAME ";
            size -= 5;
            result += 5;
        }
        len = copyVersion(charBuffer, size, version.major, version.minor, version.alpha != 0, version.beta != 0);
        if (len <= 0) {
            return result;
        }
        size -= len;
        result += len;

        // first put something like "(psy model 333.333beta)"
        if (size >= 25) {
            charBuffer[0] += "; psy model ";
            size -= 12;
            result += 12;
            len = copyVersion(charBuffer, size, version.major, version.minor, version.alpha != 0, version.beta != 0);
            if (len <= 0) {
                return result;
            }
            size -= len;
            result += len;
        }
        // at last, copy features string
        if (size > 5 && version.features != null && version.features.getByte(0) != 0) {
            charBuffer[0] += "; ";
            size -= 2;
            result += 2;
            charBuffer[0] += version.features;
            result = charBuffer[0].length();
        }
        return result;
    }
}
