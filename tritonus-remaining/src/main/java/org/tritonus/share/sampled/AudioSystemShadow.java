/*
 *  Copyright (c) 1999, 2000 by Matthias Pfisterer
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

package org.tritonus.share.sampled;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

import org.tritonus.sampled.file.AiffAudioOutputStream;
import org.tritonus.sampled.file.AuAudioOutputStream;
import org.tritonus.sampled.file.WaveAudioOutputStream;
import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TDataOutputStream;
import org.tritonus.share.sampled.file.TNonSeekableDataOutputStream;
import org.tritonus.share.sampled.file.TSeekableDataOutputStream;


/**
 * Experminatal area for AudioSystem.
 * This class is used to host features that may become part of the
 * Java Sound API (In which case they will be moved to AudioSystem).
 */
public class AudioSystemShadow {

    public static TDataOutputStream getDataOutputStream(File file)
            throws IOException {
        return new TSeekableDataOutputStream(file);
    }

    public static TDataOutputStream getDataOutputStream(OutputStream stream)
            throws IOException {
        return new TNonSeekableDataOutputStream(stream);
    }

    // TODO lengthInBytes actually should be lLengthInFrames (design problem of A.O.S.)
    public static AudioOutputStream getAudioOutputStream(AudioFileFormat.Type type, AudioFormat audioFormat, long lengthInBytes, TDataOutputStream dataOutputStream) {
        AudioOutputStream audioOutputStream = null;

        if (type.equals(AudioFileFormat.Type.AIFF) ||
                type.equals(AudioFileFormat.Type.AIFF)) {
            audioOutputStream = new AiffAudioOutputStream(audioFormat, type, lengthInBytes, dataOutputStream);
        } else if (type.equals(AudioFileFormat.Type.AU)) {
            audioOutputStream = new AuAudioOutputStream(audioFormat, lengthInBytes, dataOutputStream);
        } else if (type.equals(AudioFileFormat.Type.WAVE)) {
            audioOutputStream = new WaveAudioOutputStream(audioFormat, lengthInBytes, dataOutputStream);
        }
        return audioOutputStream;
    }

    public static AudioOutputStream getAudioOutputStream(AudioFileFormat.Type type, AudioFormat audioFormat, long lengthInBytes, File file)
            throws IOException {
        TDataOutputStream dataOutputStream = getDataOutputStream(file);
        AudioOutputStream audioOutputStream = getAudioOutputStream(type, audioFormat, lengthInBytes, dataOutputStream);
        return audioOutputStream;
    }

    public static AudioOutputStream getAudioOutputStream(AudioFileFormat.Type type, AudioFormat audioFormat, long lengthInBytes, OutputStream outputStream)
            throws IOException {
        TDataOutputStream dataOutputStream = getDataOutputStream(outputStream);
        AudioOutputStream audioOutputStream = getAudioOutputStream(type, audioFormat, lengthInBytes, dataOutputStream);
        return audioOutputStream;
    }
}
