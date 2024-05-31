/*
 *  Copyright (c) 1999 - 2004 by Matthias Pfisterer
 *  Copyright (c) 2001 by Florian Bomers
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

package org.tritonus.sampled.file.gsm;

import java.io.EOFException;
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
 * AudioFileReader class for GSM 06.10 data.
 *
 * @author Matthias Pfisterer
 */
public class GSMAudioFileReader extends TAudioFileReader {

    private static final Logger logger = getLogger("org.tritonus.TraceAudioFileReader");

    private static final int GSM_MAGIC = 0xD0;
    private static final int GSM_MAGIC_MASK = 0xF0;

    private static final int MARK_LIMIT = 1;

    public GSMAudioFileReader() {
        super(MARK_LIMIT, true);
    }

    @Override
    protected AudioFileFormat getAudioFileFormat(InputStream inputStream, long fileLengthInBytes)
            throws UnsupportedAudioFileException, IOException {
        logger.log(Level.TRACE, "begin");

        int b0 = inputStream.read();
        if (b0 < 0) {
            throw new EOFException();
        }

        // Check for magic number.
        if ((b0 & GSM_MAGIC_MASK) != GSM_MAGIC) {
            throw new UnsupportedAudioFileException("not a GSM stream: wrong magic number");
        }

        // If the file size is known, we derive the number of frames
        // ('frame size') from it.
        // If the values don't fit into integers, we leave them at
        // NOT_SPECIFIED. 'Unknown' is considered less incorrect than
        // a wrong value.
        // [fb] not specifying it causes Sun's Wave file writer to write rubbish
        int byteSize = AudioSystem.NOT_SPECIFIED;
        int frameSize = AudioSystem.NOT_SPECIFIED;
        Map<String, Object> properties = new HashMap<>();
        if (fileLengthInBytes != AudioSystem.NOT_SPECIFIED) {
            // the number of GSM frames
            long _frameSize = fileLengthInBytes / 33;
            // duration in microseconds
            long duration = _frameSize * 20000;
            properties.put("duration", duration);
            if (fileLengthInBytes <= Integer.MAX_VALUE) {
                byteSize = (int) fileLengthInBytes;
                frameSize = (int) (fileLengthInBytes / 33);
            }
        }

        Map<String, Object> _properties = new HashMap<>();
        _properties.put("bitrate", 13200L);
        AudioFormat format = new AudioFormat(
                new AudioFormat.Encoding("GSM0610"),
                8000.0F,
                AudioSystem.NOT_SPECIFIED, // ??? [sample size in bits]
                1,
                33,
                50.0F,
                true, // this value is chosen arbitrarily
                _properties);
        AudioFileFormat audioFileFormat = new TAudioFileFormat(
                new AudioFileFormat.Type("GSM", "gsm"),
                format,
                frameSize,
                byteSize,
                properties);

        logger.log(Level.TRACE, "end");

        return audioFileFormat;
    }
}
