/*
 *  Copyright (c) 1999 -2001 by Matthias Pfisterer
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

package org.tritonus.sampled.file.mpeg;

import java.io.EOFException;
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
 * TODO not supported mp3 tags, use mp3spi
 *
 * @author Matthias Pfisterer
 */
public class MpegAudioFileReader extends TAudioFileReader {

     private static final Logger logger= getLogger("org.tritonus.TraceAudioFileReader");

    private static final int SYNC = 0xffe0_0000;

    private static final AudioFormat.Encoding[][] encodings = {{
            new AudioFormat.Encoding("MPEG2DOT5L3"),
            new AudioFormat.Encoding("MPEG2DOT5L2"),
            new AudioFormat.Encoding("MPEG2DOT5L1")
        }, { // reserved
            null,
            null,
            null
        }, {
            new AudioFormat.Encoding("MPEG2L3"),
            new AudioFormat.Encoding("MPEG2L2"),
            new AudioFormat.Encoding("MPEG2L1")
        }, {
            new AudioFormat.Encoding("MPEG1L3"),
            new AudioFormat.Encoding("MPEG1L2"),
            new AudioFormat.Encoding("MPEG1L1")
        },
    };

    private static final float[][] samplingRates = {
            {11025.0F, 12000.0F, 8000.0F},
            {0.0F, 0.0F, 0.0F},            // reserved
            {22050.0F, 24000.0F, 16000.0F},
            {44100.0F, 48000.0F, 32000.0F},
    };

    private static final int MARK_LIMIT = 4;

    public MpegAudioFileReader() {
        super(MARK_LIMIT, true);
    }

    @Override
    protected AudioFileFormat getAudioFileFormat(InputStream inputStream, long fileLengthInBytes)
            throws UnsupportedAudioFileException, IOException {

        logger.log(Level.TRACE, "begin");

        int b0 = inputStream.read();
        int b1 = inputStream.read();
        int b2 = inputStream.read();
        int b3 = inputStream.read();
        if ((b0 | b1 | b2 | b3) < 0) {
            throw new EOFException();
        }
        int header = (b0 << 24) + (b1 << 16) + (b2 << 8) + (b3 << 0);
        // We check for the sync bits. If they are present, we
        // assume that we have an MPEG bitstream.
        if ((header & SYNC) != SYNC) {
            throw new UnsupportedAudioFileException("not a MPEG stream: no sync bits");
        }
        int version = (header >> 19) & 0x3;
        if (version == 1) {
            throw new UnsupportedAudioFileException("not a MPEG stream: wrong version");
        }
        int layer = (header >> 17) & 0x3;
        if (layer == 0) {
            throw new UnsupportedAudioFileException("not a MPEG stream: wrong layer");
        }
        AudioFormat.Encoding encoding = encodings[version][layer - 1];
        // TODO bit rate, protection
        int sfIndex = (header >> 10) & 0x3;
        if (sfIndex == 3) {
            throw new UnsupportedAudioFileException("not a MPEG stream: wrong sampling rate");
        }
        float samplingRate = samplingRates[version][sfIndex];
        int mode = (header >> 6) & 0x3;
        int channels = mode == 3 ? 1 : 2;

        AudioFormat format = new AudioFormat(
                encoding,
                samplingRate,
                AudioSystem.NOT_SPECIFIED, // ???
                channels,
                AudioSystem.NOT_SPECIFIED, // ????
                AudioSystem.NOT_SPECIFIED, // ????
                true);
        // $$fb 2000-08-15: workaround for the fixed extension problem in AudioFileFormat.Type
        // see org.tritonus.share.sampled.AudioFileTypes.java
        AudioFileFormat.Type type = new AudioFileFormat.Type("MPEG", "mpeg");
        if (encoding.equals(new AudioFormat.Encoding("MPEG1L3"))) {
            type = new AudioFileFormat.Type("MP3", "mp3");
        }

        // If the file size is known, we derive the number of frames
        // ('frame size') from it.
        // If the values don't fit into integers, we leave them at
        // NOT_SPECIFIED. 'Unknown' is considered less incorrect than
        // a wrong value.
        // [fb] not specifying it causes Sun's Wave file writer to write rubbish
        int byteSize = AudioSystem.NOT_SPECIFIED;
        int frameSize = AudioSystem.NOT_SPECIFIED;
        if (fileLengthInBytes != AudioSystem.NOT_SPECIFIED && fileLengthInBytes <= Integer.MAX_VALUE) {
            byteSize = (int) fileLengthInBytes;
            // TODO check if we can calculate a useful value here
//            frameSize = (int) (fileSizeInBytes / 33);
        }

        AudioFileFormat audioFileFormat = new TAudioFileFormat(type, format, frameSize, byteSize);

        logger.log(Level.TRACE, "end");

        return audioFileFormat;
    }
}
