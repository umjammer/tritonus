/*
 *  Copyright (c) 2000 by Florian Bomers
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

package org.tritonus.sampled.file;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import org.tritonus.share.sampled.file.TAudioOutputStream;
import org.tritonus.share.sampled.file.TDataOutputStream;

import static java.lang.System.getLogger;


/**
 * AudioOutputStream for Wave files.
 *
 * @author Florian Bomers
 */
public class WaveAudioOutputStream extends TAudioOutputStream {

    private static final Logger logger= getLogger("org.tritonus.TraceAudioOutputStream");

    // this constant is used for chunk lengths when the length is not known yet
    private static final int LENGTH_NOT_KNOWN = -1;

    public WaveAudioOutputStream(AudioFormat audioFormat,
                                 long length,
                                 TDataOutputStream dataOutputStream) {
        // always do back-patching if the stream supports seeking, in case the
        // reported stream length is longer than the actual data
        super(audioFormat,
                length,
                dataOutputStream,
                dataOutputStream.supportsSeek());
        // wave cannot store more than 4GB
        if (length != AudioSystem.NOT_SPECIFIED && (length + WaveTool.DATA_OFFSET) > 0xffff_ffffL) {
            logger.log(Level.TRACE, "WaveAudioOutputStream: Length exceeds 4GB: " +
                    length + "=0x" + Long.toHexString(length) +
                    " with header=" + (length + WaveTool.DATA_OFFSET) +
                    "=0x" + Long.toHexString(length + WaveTool.DATA_OFFSET));
            throw new IllegalArgumentException("Wave files cannot be larger than 4GB.");
        }
        // double-check that we can write this audio format
        if (WaveTool.getFormatCode(getFormat()) == WaveTool.WAVE_FORMAT_UNSPECIFIED) {
            throw new IllegalArgumentException("Unknown encoding/format for WAVE file: " + audioFormat);
        }
        // WAVE requires unsigned 8-bit data
        requireSign8bit(false);
        // WAVE requires little endian
        requireEndianness(false);

        logger.log(Level.TRACE, "Writing WAVE: " + audioFormat.getSampleSizeInBits() + " bits, " + audioFormat.getEncoding());
    }

    @Override
    protected void writeHeader() throws IOException {
        logger.log(Level.TRACE, "WaveAudioOutputStream.writeHeader()");

        int formatCode = WaveTool.getFormatCode(getFormat());
        AudioFormat format = getFormat();
        long length = getLength();
        int formatChunkAdd = 0;
        if (formatCode == WaveTool.WAVE_FORMAT_GSM610) {
            // space for extra fields
            formatChunkAdd += 2;
        }
        int dataOffset = WaveTool.DATA_OFFSET + formatChunkAdd;
        if (formatCode != WaveTool.WAVE_FORMAT_PCM) {
            // space for fact chunk
            dataOffset += 4 + WaveTool.CHUNK_HEADER_SIZE;
        }

        // if patching the header, and the length has not been known at first
        // writing of the header, just truncate the size fields, don't throw an exception
        if (length != AudioSystem.NOT_SPECIFIED && length + dataOffset > 0xFFFF_FFFFL) {
            length = 0xFFFF_FFFFL - dataOffset;
        }

        // chunks must be on word-boundaries
        long dataChunkSize = length + (length % 2);
        if (length == AudioSystem.NOT_SPECIFIED || dataChunkSize > 0xFFFF_FFFFL) {
            dataChunkSize = 0xFFFF_FFFFL;
        }

        long RIFF_Size = dataChunkSize + dataOffset - WaveTool.CHUNK_HEADER_SIZE;
        if (length == AudioSystem.NOT_SPECIFIED || RIFF_Size > 0xFFFF_FFFFL) {
            RIFF_Size = 0xFFFF_FFFFL;
        }

        TDataOutputStream dos = getDataOutputStream();

        // write RIFF container chunk
        dos.writeInt(WaveTool.WAVE_RIFF_MAGIC);
        dos.writeLittleEndian32((int) RIFF_Size);
        dos.writeInt(WaveTool.WAVE_WAVE_MAGIC);

        // write fmt_ chunk
        int formatChunkSize = WaveTool.FMT_CHUNK_SIZE + formatChunkAdd;
        short sampleSizeInBits = (short) format.getSampleSizeInBits();
        int decodedSamplesPerBlock = 1;

        if (formatCode == WaveTool.WAVE_FORMAT_GSM610) {
            if (format.getFrameSize() == 33) {
                decodedSamplesPerBlock = 160;
            } else if (format.getFrameSize() == 65) {
                decodedSamplesPerBlock = 320;
            } else {
                // how to retrieve this value here ?
                decodedSamplesPerBlock = (int) (format.getFrameSize() * (320.0f / 65.0f));
            }
            sampleSizeInBits = 0; // MS standard
        }

        int avgBytesPerSec = ((int) format.getSampleRate()) / decodedSamplesPerBlock * format.getFrameSize();
        dos.writeInt(WaveTool.WAVE_FMT_MAGIC);
        dos.writeLittleEndian32(formatChunkSize);
        dos.writeLittleEndian16((short) formatCode);             // wFormatTag
        dos.writeLittleEndian16((short) format.getChannels());   // nChannels
        dos.writeLittleEndian32((int) format.getSampleRate());   // nSamplesPerSec
        dos.writeLittleEndian32(avgBytesPerSec);                 // nAvgBytesPerSec
        dos.writeLittleEndian16((short) format.getFrameSize());  // nBlockalign
        dos.writeLittleEndian16(sampleSizeInBits);               // wBitsPerSample
        dos.writeLittleEndian16((short) formatChunkAdd);         // cbSize

        if (formatCode == WaveTool.WAVE_FORMAT_GSM610) {
            dos.writeLittleEndian16((short) decodedSamplesPerBlock); // wSamplesPerBlock
        }

        // write fact chunk

        if (formatCode != WaveTool.WAVE_FORMAT_PCM) {
            // write "fact" chunk: number of samples
            // TODO add this as an attribute or property
            //  in AudioOutputStream or AudioInputStream
            long samples = 0;
            if (length != AudioSystem.NOT_SPECIFIED) {
                samples = length / format.getFrameSize() * decodedSamplesPerBlock;
            }
            // saturate sample count
            if (samples > 0xFFFF_FFFFL) {
                samples = (0xFFFF_FFFFL / decodedSamplesPerBlock) * decodedSamplesPerBlock;
            }
            dos.writeInt(WaveTool.WAVE_FACT_MAGIC);
            dos.writeLittleEndian32(4);
            dos.writeLittleEndian32((int) samples);
        }

        // write header of data chunk
        dos.writeInt(WaveTool.WAVE_DATA_MAGIC);
        dos.writeLittleEndian32((length != AudioSystem.NOT_SPECIFIED) ? ((int) length) : LENGTH_NOT_KNOWN);
    }

    @Override
    protected void patchHeader() throws IOException {
        TDataOutputStream tdos = getDataOutputStream();
        tdos.seek(0);
        setLengthFromCalculatedLength();
        writeHeader();
    }

    @Override
    public void close() throws IOException {
        long nBytesWritten = getCalculatedLength();

        if ((nBytesWritten % 2) == 1) {
            logger.log(Level.TRACE, "WaveOutputStream.close(): adding padding byte");

            // extra byte for to align on word boundaries
            TDataOutputStream tdos = getDataOutputStream();
            tdos.writeByte(0);
            // DON'T adjust calculated length !
        }

        super.close();
    }
}
