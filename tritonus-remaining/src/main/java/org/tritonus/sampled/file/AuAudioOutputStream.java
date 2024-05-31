/*
 *  Copyright (c) 2000,2001 by Florian Bomers
 *  Copyright (c) 1999 by Matthias Pfisterer
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
 * AudioOutputStream for AU files.
 *
 * @author Florian Bomers
 * @author Matthias Pfisterer
 */

public class AuAudioOutputStream extends TAudioOutputStream {

    private static final Logger logger= getLogger("org.tritonus.TraceAudioOutputStream");

    private static final String description = "Created by Tritonus";

    /**
     * Writes a null-terminated ascii string s to f.
     * The total number of bytes written is aligned on a 2byte boundary.
     *
     * @throws IOException Write error.
     */
    protected static void writeText(TDataOutputStream dos, String s) throws IOException {
        if (!s.isEmpty()) {
            dos.writeBytes(s);
            dos.writeByte(0);  // pour terminer le texte
            if ((s.length() % 2) == 0) {
                // ajout d'un zero pour faire la longeur pair
                dos.writeByte(0);
            }
        }
    }

    /**
     * Returns number of bytes that have to written for string s (with alignment)
     */
    protected static int getTextLength(String s) {
        if (s.isEmpty()) {
            return 0;
        } else {
            return (s.length() + 2) & 0xFFFFFFFE;
        }
    }

    public AuAudioOutputStream(AudioFormat audioFormat,
                               long length,
                               TDataOutputStream dataOutputStream) {
        // always do backpatching if the stream supports seeking, in case the
        // reported stream length is longer than the actual data
        // if length exceeds 2GB, set the length field to NOT_SPECIFIED
        super(audioFormat,
                length > 0x7FFFFFFFL ? AudioSystem.NOT_SPECIFIED : length,
                dataOutputStream,
                dataOutputStream.supportsSeek());
        // double-check that we can write this audio format
        if (AuTool.getFormatCode(audioFormat) == AuTool.SND_FORMAT_UNSPECIFIED) {
            throw new IllegalArgumentException("Unknown encoding/format for AU file: " + audioFormat);
        }
        // AU requires signed 8-bit data
        requireSign8bit(true);
        // AU requires big endian
        requireEndianness(true);

        logger.log(Level.TRACE, "Writing AU: " + audioFormat.getSampleSizeInBits() +
                    " bits, " + audioFormat.getEncoding());
    }

    @Override
    protected void writeHeader() throws IOException {
        logger.log(Level.TRACE, "AuAudioOutputStream.writeHeader(): called.");

        AudioFormat format = getFormat();
        long length = getLength();
        TDataOutputStream dos = getDataOutputStream();
        logger.log(Level.TRACE, "AuAudioOutputStream.writeHeader(): AudioFormat: " + format);
        logger.log(Level.TRACE, "AuAudioOutputStream.writeHeader(): length: " + length);

        dos.writeInt(AuTool.AU_HEADER_MAGIC);
        dos.writeInt(AuTool.DATA_OFFSET + getTextLength(description));
        dos.writeInt((length != AudioSystem.NOT_SPECIFIED) ? ((int) length) : AuTool.AUDIO_UNKNOWN_SIZE);
        dos.writeInt(AuTool.getFormatCode(format));
        dos.writeInt((int) format.getSampleRate());
        dos.writeInt(format.getChannels());
        writeText(dos, description);
    }

    @Override
    protected void patchHeader() throws IOException {
        TDataOutputStream tdos = getDataOutputStream();
        tdos.seek(0);
        setLengthFromCalculatedLength();
        writeHeader();
    }
}
