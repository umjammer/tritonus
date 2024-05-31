/*
 *  Copyright (c) 1999,2000 by Florian Bomers
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
import java.util.List;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TAudioFileWriter;
import org.tritonus.share.sampled.file.TDataOutputStream;

import static javax.sound.sampled.AudioFormat.Encoding.ALAW;
import static javax.sound.sampled.AudioFormat.Encoding.ULAW;
import static org.tritonus.sampled.file.WaveTool.GSM0610;


/**
 * Class for writing Microsoft(tm) WAVE files
 *
 * @author Florian Bomers
 */
public class WaveAudioFileWriter extends TAudioFileWriter {

    private static final AudioFileFormat.Type[] FILE_TYPES = {
            AudioFileFormat.Type.WAVE
    };

    /**
     * IMPORTANT: this array depends on the AudioFormat.match() algorithm which takes
     *            AudioSystem.NOT_SPECIFIED into account !
     */
    private static final AudioFormat[] AUDIO_FORMATS = {
            // Encoding, SampleRate, sampleSizeInBits, channels, frameSize, frameRate, bigEndian
            new AudioFormat(PCM_UNSIGNED, ALL, 8, ALL, ALL, ALL, true),
            new AudioFormat(PCM_UNSIGNED, ALL, 8, ALL, ALL, ALL, false),
            new AudioFormat(ULAW, ALL, 8, ALL, ALL, ALL, false),
            new AudioFormat(ULAW, ALL, 8, ALL, ALL, ALL, true),
            new AudioFormat(ALAW, ALL, 8, ALL, ALL, ALL, false),
            new AudioFormat(ALAW, ALL, 8, ALL, ALL, ALL, true),
            new AudioFormat(PCM_SIGNED, ALL, 16, ALL, ALL, ALL, false),
            new AudioFormat(PCM_SIGNED, ALL, 24, ALL, ALL, ALL, false),
            new AudioFormat(PCM_SIGNED, ALL, 32, ALL, ALL, ALL, false),
            new AudioFormat(GSM0610, ALL, ALL, ALL, ALL, ALL, false),
            new AudioFormat(GSM0610, ALL, ALL, ALL, ALL, ALL, true),
    };

    public WaveAudioFileWriter() {
        super(List.of(FILE_TYPES), List.of(AUDIO_FORMATS));
    }

    // overwritten for quicker and more accurate check
    @Override
    protected boolean isAudioFormatSupportedImpl(AudioFormat format, AudioFileFormat.Type fileType) {
        return WaveTool.getFormatCode(format) != WaveTool.WAVE_FORMAT_UNSPECIFIED;
    }

    @Override
    protected AudioOutputStream getAudioOutputStream(AudioFormat audioFormat,
                                                     long lengthInBytes,
                                                     AudioFileFormat.Type fileType,
                                                     TDataOutputStream dataOutputStream) throws IOException {
        return new WaveAudioOutputStream(audioFormat, lengthInBytes, dataOutputStream);
    }
}
