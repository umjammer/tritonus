
/*
 *  Copyright (c) 2000 by Florian Bomers
 *  Copyright (c) 2000 by Matthias Pfisterer
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

package org.tritonus.sampled.file.gsm;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

import org.tritonus.share.sampled.file.THeaderlessAudioFileWriter;

import static java.lang.System.getLogger;


/**
 * Class for writing GSM streams
 *
 * @author Florian Bomers
 * @author Matthias Pfisterer
 */
public class GSMAudioFileWriter extends THeaderlessAudioFileWriter {

    private static final Logger logger = getLogger("org.tritonus.TraceAudioFileWriter");

    private static final AudioFileFormat.Type[] FILE_TYPES = {
            new AudioFileFormat.Type("GSM", "gsm")
    };

    private static final AudioFormat[] AUDIO_FORMATS = {
            new AudioFormat(new AudioFormat.Encoding("GSM0610"), 8000.0F, ALL, 1, 33, 50.0F, false),
            new AudioFormat(new AudioFormat.Encoding("GSM0610"), 8000.0F, ALL, 1, 33, 50.0F, true),
    };

    public GSMAudioFileWriter() {
        super(Arrays.asList(FILE_TYPES), Arrays.asList(AUDIO_FORMATS));
        logger.log(Level.TRACE, "GSMAudioFileWriter.<init>(): begin");

        logger.log(Level.TRACE, "GSMAudioFileWriter.<init>(): end");
    }
}
