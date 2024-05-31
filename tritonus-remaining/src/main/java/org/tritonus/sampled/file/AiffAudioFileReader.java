/*
 *  Copyright (c) 2000 by Florian Bomers
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
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.share.sampled.file.TAudioFileFormat;
import org.tritonus.share.sampled.file.TAudioFileReader;

import static java.lang.System.getLogger;


/**
 * Class for reading AIFF and AIFF-C files.
 *
 * @author Florian Bomers
 * @author Matthias Pfisterer
 */
public class AiffAudioFileReader extends TAudioFileReader {

    private static final Logger logger= getLogger("org.tritonus.TraceAudioFileReader");

    private static final int READ_LIMIT = 1000;

    public AiffAudioFileReader() {
        super(READ_LIMIT);
    }

    private void skipChunk(DataInputStream dataInputStream, int chunkLength, int chunkRead) throws IOException {
        chunkLength -= chunkRead;
        if (chunkLength > 0) {
            dataInputStream.skip(chunkLength + (chunkLength % 2));
        }
    }

    private AudioFormat readCommChunk(DataInputStream dataInputStream, int chunkLength)
            throws IOException, UnsupportedAudioFileException {

        int numChannels = dataInputStream.readShort();
        if (numChannels <= 0) {
            throw new UnsupportedAudioFileException("not an AIFF file: number of channels must be positive");
        }
        logger.log(Level.TRACE, "Found " + numChannels + " channels.");

        // ignored: frame count
        dataInputStream.readInt();
        int sampleSize = dataInputStream.readShort();
        float sampleRate = (float) readIeeeExtended(dataInputStream);
        if (sampleRate <= 0.0) {
            throw new UnsupportedAudioFileException("not an AIFF file: sample rate must be positive");
        }
        logger.log(Level.TRACE, "Found framerate " + sampleRate);

        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        int read = 18;
        boolean isLittleEndian = false;
        if (chunkLength > read) {
            int _encoding = dataInputStream.readInt();
            read += 4;
            if (_encoding == AiffTool.AIFF_COMM_PCM
                    || _encoding == AiffTool.AIFF_COMM_TWOS
                    || _encoding == AiffTool.AIFF_COMM_SOWT) {
                // PCM
                // Check for little-endian
                isLittleEndian = _encoding == AiffTool.AIFF_COMM_SOWT;
            } else if (_encoding == AiffTool.AIFF_COMM_ULAW) {
                // ULAW
                encoding = AudioFormat.Encoding.ULAW;
                sampleSize = 8;
            } else if (_encoding == AiffTool.AIFF_COMM_IMA_ADPCM) {
                encoding = new AudioFormat.Encoding("IMA_ADPCM");
                sampleSize = 4;
            } else {
                throw new UnsupportedAudioFileException(
                        "Encoding 0x" + Integer.toHexString(_encoding) + " of AIFF file not supported");
            }
        }
        // In case of IMA ADPCM, frame size is 0.5 bytes (since it is
        // always mono). A value of 1 as frame size would be wrong.
        // Handling of frame size 0 in defined nowhere. So the best
        // solution is to set the frame size to unspecified (-1).
        int frameSize = (sampleSize == 4) ?
                AudioSystem.NOT_SPECIFIED :
                calculateFrameSize(sampleSize, numChannels);
        logger.log(Level.TRACE, "calculated frame size: " + frameSize);

        skipChunk(dataInputStream, chunkLength, read);
        AudioFormat format = new AudioFormat(encoding,
                sampleRate,
                sampleSize,
                numChannels,
                frameSize,
                sampleRate,
                sampleSize > 8 && !isLittleEndian);
        return format;
    }

    private void readVerChunk(DataInputStream dataInputStream, int chunkLength)
            throws IOException, UnsupportedAudioFileException {
        if (chunkLength < 4) {
            throw new UnsupportedAudioFileException("Corrput AIFF file: FVER chunk too small.");
        }
        int ver = dataInputStream.readInt();
        if (ver != AiffTool.AIFF_FVER_TIME_STAMP) {
            throw new UnsupportedAudioFileException("Unsupported AIFF file: version not known.");
        }
        skipChunk(dataInputStream, chunkLength, 4);
    }

    @Override
    protected AudioFileFormat getAudioFileFormat(InputStream inputStream, long fileLengthInBytes)
            throws UnsupportedAudioFileException, IOException {
        logger.log(Level.TRACE, "begin");

        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int magic = dataInputStream.readInt();
        if (magic != AiffTool.AIFF_FORM_MAGIC) {
            throw new UnsupportedAudioFileException("not an AIFF file: header magic is not FORM");
        }
        int totalLength = dataInputStream.readInt();
        magic = dataInputStream.readInt();
        boolean isAifc;
        if (magic == AiffTool.AIFF_AIFF_MAGIC) {
            isAifc = false;
        } else if (magic == AiffTool.AIFF_AIFC_MAGIC) {
            isAifc = true;
        } else {
            throw new UnsupportedAudioFileException("unsupported IFF file: header magic neither AIFF nor AIFC");
        }
        boolean fVerFound = !isAifc;
        boolean commFound = false;
        boolean sSndFound = false;
        AudioFormat format = null;
        int dataChunkLength = 0;

        // walk through the chunks.
        // chunks may be in any order. However, in this implementation, SSND must be last
        while (!fVerFound || !commFound || !sSndFound) {
            magic = dataInputStream.readInt();
            int chunkLength = dataInputStream.readInt();
            switch (magic) {
            case AiffTool.AIFF_COMM_MAGIC:
                format = readCommChunk(dataInputStream, chunkLength);
                logger.log(Level.TRACE, "Read COMM chunk with length " + chunkLength);

                commFound = true;
                break;
            case AiffTool.AIFF_FVER_MAGIC:
                if (!fVerFound) {
                    readVerChunk(dataInputStream, chunkLength);
                    logger.log(Level.TRACE, "Read FVER chunk with length " + chunkLength);

                    fVerFound = true;
                } else {
                    skipChunk(dataInputStream, chunkLength, 0);
                }
                break;
            case AiffTool.AIFF_SSND_MAGIC:
                if (!commFound || !fVerFound) {
                    throw new UnsupportedAudioFileException("cannot handle AIFF file: SSND not last chunk");
                }
                sSndFound = true;
                dataChunkLength = chunkLength - 8;
                // 8 information bytes of no interest
                dataInputStream.skipBytes(8);
                logger.log(Level.TRACE, "Found SSND chunk with length " + chunkLength);

                break;
            default:
                logger.log(Level.TRACE, "Skipping unknown chunk: " + Integer.toHexString(magic));
                skipChunk(dataInputStream, chunkLength, 0);
                break;
            }
        }

        // TODO length argument has to be in frames
        AudioFileFormat audioFileFormat = new TAudioFileFormat(
                isAifc ? AudioFileFormat.Type.AIFC : AudioFileFormat.Type.AIFF,
                format,
                dataChunkLength / format.getFrameSize(),
                totalLength + 8);
        logger.log(Level.TRACE, "end");

        return audioFileFormat;
    }
}
