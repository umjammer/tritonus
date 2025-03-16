/*
 *  Copyright (c) 1999,2000,2001 by Florian Bomers
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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.share.sampled.file.TAudioFileFormat;
import org.tritonus.share.sampled.file.TAudioFileReader;

import static java.lang.System.getLogger;


/**
 * Class for reading Sun/Next AU files.
 *
 * @author Florian Bomers
 * @author Matthias Pfisterer
 */
public class AuAudioFileReader extends TAudioFileReader {

    private static final Logger logger = getLogger(AuAudioFileReader.class.getName());

    private static final int READ_LIMIT = 1000;

    public AuAudioFileReader() {
        super(READ_LIMIT);
    }

    private static String readDescription(DataInputStream dis, int len) throws IOException {
        byte c = -1;
        StringBuilder ret = new StringBuilder();
        while (len > 0 && (c = dis.readByte()) != 0) {
            ret.append((char) c);
            len--;
        }
        if (len > 1 && c == 0) {
            dis.skip(len - 1);
        }
        return ret.toString();
    }

    @Override
    protected AudioFileFormat getAudioFileFormat(InputStream inputStream, long fileLengthInBytes)
            throws UnsupportedAudioFileException, IOException {
        logger.log(Level.TRACE, "begin");

        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int magic = dataInputStream.readInt();
        if (magic != AuTool.AU_HEADER_MAGIC) {
            throw new UnsupportedAudioFileException("not an AU file: wrong header magic");
        }
        int dataOffset = dataInputStream.readInt();
        logger.log(Level.TRACE, "data offset: " + dataOffset);

        if (dataOffset < AuTool.DATA_OFFSET) {
            throw new UnsupportedAudioFileException("not an AU file: data offset must be 24 or greater");
        }
        int dataLength = dataInputStream.readInt();
        logger.log(Level.TRACE, "data length: " + dataLength);

        if (dataLength < 0 && dataLength != AuTool.AUDIO_UNKNOWN_SIZE) {
            throw new UnsupportedAudioFileException("not an AU file: data length must be positive, 0 or -1 for unknown");
        }
        AudioFormat.Encoding encoding = null;
        int sampleSize = 0;
        int _encoding = dataInputStream.readInt();
        sampleSize = switch (_encoding) {
            case AuTool.SND_FORMAT_MULAW_8 -> {
                encoding = AudioFormat.Encoding.ULAW;
                yield 8; // 8-bit uLaw G.711
            }
            case AuTool.SND_FORMAT_LINEAR_8 -> {
                encoding = AudioFormat.Encoding.PCM_SIGNED;
                yield 8;
            }
            case AuTool.SND_FORMAT_LINEAR_16 -> {
                encoding = AudioFormat.Encoding.PCM_SIGNED;
                yield 16;
            }
            case AuTool.SND_FORMAT_LINEAR_24 -> {
                encoding = AudioFormat.Encoding.PCM_SIGNED;
                yield 24;
            }
            case AuTool.SND_FORMAT_LINEAR_32 -> {
                encoding = AudioFormat.Encoding.PCM_SIGNED;
                yield 32;
            }
            case AuTool.SND_FORMAT_ALAW_8 -> {
                encoding = AudioFormat.Encoding.ALAW;
                yield 8; // 8-bit aLaw G.711
            }
            default -> sampleSize;
        };
        if (sampleSize == 0) {
            throw new UnsupportedAudioFileException("unsupported AU file: unknown encoding " + _encoding);
        }
        int sampleRate = dataInputStream.readInt();
        if (sampleRate <= 0) {
            throw new UnsupportedAudioFileException("corrupt AU file: sample rate must be positive");
        }
        int channels = dataInputStream.readInt();
        if (channels <= 0) {
            throw new UnsupportedAudioFileException("corrupt AU file: number of channels must be positive");
        }
        // skip header information field
//        inputStream.skip(dataOffset - AuTool.DATA_OFFSET);
        // read header info field
        String desc = readDescription(dataInputStream, dataOffset - AuTool.DATA_OFFSET);
        // add the description to the file format's properties
        Map<String, Object> properties = new HashMap<>();
        if (!desc.isEmpty()) {
            properties.put("title", desc);
        }

        AudioFormat format = new AudioFormat(encoding,
                sampleRate,
                sampleSize,
                channels,
                calculateFrameSize(sampleSize, channels),
                sampleRate,
                sampleSize > 8);
        AudioFileFormat audioFileFormat = new TAudioFileFormat(
                AudioFileFormat.Type.AU,
                format,
                (dataLength == AuTool.AUDIO_UNKNOWN_SIZE) ? AudioSystem.NOT_SPECIFIED : (dataLength / format.getFrameSize()),
                (dataLength == AuTool.AUDIO_UNKNOWN_SIZE) ? AudioSystem.NOT_SPECIFIED : (dataLength + dataOffset),
                properties);

        logger.log(Level.TRACE, "begin");

        return audioFileFormat;
    }
}
