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
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import org.tritonus.share.sampled.file.TAudioOutputStream;
import org.tritonus.share.sampled.file.TDataOutputStream;

import static java.lang.System.getLogger;


/**
 * AudioOutputStream for AIFF and AIFF-C files.
 *
 * @author Florian Bomers
 */
public class AiffAudioOutputStream extends TAudioOutputStream {

    private static final Logger logger= getLogger("org.tritonus.TraceAudioOutputStream");

    // this constant is used for chunk lengths when the length is not known yet
    private static final int LENGTH_NOT_KNOWN = -1;

    private AudioFileFormat.Type fileType;

    public AiffAudioOutputStream(AudioFormat audioFormat,
                                 AudioFileFormat.Type fileType,
                                 long length,
                                 TDataOutputStream dataOutputStream) {
        // always do backpatching if the stream supports seeking, in case the
        // reported stream length is longer than the actual data
        super(audioFormat, length, dataOutputStream, dataOutputStream.supportsSeek());
        // AIFF files cannot exceed 2GB
        if (length != AudioSystem.NOT_SPECIFIED && length > 0x7fff_ffffL) {
            throw new IllegalArgumentException("AIFF files cannot be larger than 2GB.");
        }
        // IDEA: write AIFF file instead of AIFC when encoding=PCM ?
        this.fileType = fileType;
        if (!audioFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED)
                && !audioFormat.getEncoding().equals(AudioFormat.Encoding.PCM_UNSIGNED)) {
            // only AIFC files can handle non-pcm data
            this.fileType = AudioFileFormat.Type.AIFC;
        }
        // double-check that we can write this audio format
        if (AiffTool.getFormatCode(audioFormat) == AiffTool.AIFF_COMM_UNSPECIFIED) {
            throw new IllegalArgumentException("Unknown encoding/format for AIFF file: " + audioFormat);
        }
        // AIFF requires signed 8-bit data
        requireSign8bit(true);
        // AIFF requires big endian
        requireEndianness(true);

        logger.log(Level.TRACE, "Writing " + this.fileType + ": " + audioFormat.getSampleSizeInBits() +
                    " bits, " + audioFormat.getEncoding());
    }

    @Override
    protected void writeHeader() throws IOException {
        logger.log(Level.TRACE, "called.");

        AudioFormat format = getFormat();
        boolean isAifc = fileType.equals(AudioFileFormat.Type.AIFC);
        long length = getLength();
        TDataOutputStream dos = getDataOutputStream();
        int commChunkSize = 18;
        int formatCode = AiffTool.getFormatCode(format);
        if (isAifc) {
            // encoding takes 4 bytes
            // encoding name takes at minimum 2 bytes
            commChunkSize += 6;
        }
        int headerSize = 4          // magic
                + 8 + commChunkSize // COMM chunk
                + 8;                 // header of SSND chunk
        if (isAifc) {
            // add length for FVER chunk
            headerSize += 12;
        }
        // if patching the header, and the length has not been known at first
        // writing of the header, just truncate the size fields, don't throw an exception
        if (length != AudioSystem.NOT_SPECIFIED && length + headerSize > 0x7fff_ffffL) {
            length = 0x7fff_ffffL - headerSize;
        }
        // chunks must be on word-boundaries
        long sSndChunkSize = (length != AudioSystem.NOT_SPECIFIED) ?
                (length + (length % 2) + 8) : AudioSystem.NOT_SPECIFIED;

        // write IFF container chunk
        dos.writeInt(AiffTool.AIFF_FORM_MAGIC);
        dos.writeInt((length != AudioSystem.NOT_SPECIFIED) ?
                ((int) (sSndChunkSize + headerSize)) : LENGTH_NOT_KNOWN);
        if (isAifc) {
            dos.writeInt(AiffTool.AIFF_AIFC_MAGIC);
            // write FVER chunk
            dos.writeInt(AiffTool.AIFF_FVER_MAGIC);
            dos.writeInt(4);
            dos.writeInt(AiffTool.AIFF_FVER_TIME_STAMP);
        } else {
            dos.writeInt(AiffTool.AIFF_AIFF_MAGIC);
        }

        // write COMM chunk
        dos.writeInt(AiffTool.AIFF_COMM_MAGIC);
        dos.writeInt(commChunkSize);
        dos.writeShort((short) format.getChannels());
        dos.writeInt((length != AudioSystem.NOT_SPECIFIED) ?
                ((int) (length / format.getFrameSize())) : LENGTH_NOT_KNOWN);
        if (formatCode == AiffTool.AIFF_COMM_ULAW) {
            // AIFF ulaw states 16 bits for ulaw data
            dos.writeShort(16);
        } else {
            dos.writeShort((short) format.getSampleSizeInBits());
        }
        writeIeeeExtended(dos, format.getSampleRate());
        if (isAifc) {
            dos.writeInt(formatCode);
            dos.writeShort(0); // no encoding name
            // TODO write encoding.toString() ??
        }

        // write header of SSND chunk
        dos.writeInt(AiffTool.AIFF_SSND_MAGIC);
        // don't use sSndChunkSize here !
        dos.writeInt((length != AudioSystem.NOT_SPECIFIED) ? (int) (length + 8) : LENGTH_NOT_KNOWN);
        // 8 information bytes of no interest
        dos.writeInt(0); // offset
        dos.writeInt(0); // blocksize
    }

    @Override
    protected void patchHeader() throws IOException {
        TDataOutputStream dos = getDataOutputStream();
        dos.seek(0);
        setLengthFromCalculatedLength();
        writeHeader();
    }

    @Override
    public void close() throws IOException {
        long bytesWritten = getCalculatedLength();

        if ((bytesWritten % 2) == 1) {
            logger.log(Level.TRACE, "adding padding byte");

            // extra byte for to align on word boundaries
            TDataOutputStream dos = getDataOutputStream();
            dos.writeByte(0);
            // DON'T adjust calculated length !
        }
        super.close();
    }

    public void writeIeeeExtended(TDataOutputStream dos, float sampleRate) throws IOException {
        // currently, only integer sample rates are written
        // TODO real conversion
        // I don't know exactly how much I have to shift left the mantissa for normalisation
        // now I do it so that there are any bits set in the first 5 bits
        int _sampleRate = (int) sampleRate;
        short ieeeExponent = 0;
        while ((_sampleRate != 0) && (_sampleRate & 0x80000000) == 0) {
            ieeeExponent++;
            _sampleRate <<= 1;
        }
        dos.writeShort(16414 - ieeeExponent); // exponent
        dos.writeInt(_sampleRate);               // mantissa high double word
        dos.writeInt(0);                      // mantissa low double word
    }
}
