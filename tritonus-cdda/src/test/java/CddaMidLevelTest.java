/*
 *  Copyright (c) 2001 - 2002 by Matthias Pfisterer
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

import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.SourceDataLine;

import org.tritonus.lowlevel.cdda.CddaMidLevel;
import org.tritonus.lowlevel.cdda.CddaUtils;


/**
 * CddaMidLevelTest
 *
 * programs related to cdda:
 * command-line extractor
 * cd player
 * extractor/mp3 encoder
 */
public class CddaMidLevelTest {

    public static void main(String[] args) throws Exception {
        boolean tocOnly = true;
        int _track = 0;
        if (args.length < 1) {
            tocOnly = true;
        } else if (args.length == 1) {
            _track = Integer.parseInt(args[0]);
            tocOnly = false;
        }
        CddaMidLevel cddaMidLevel = CddaUtils.getCddaMidLevel();
        InputStream tocInputStream = cddaMidLevel.getTocAsXml("TODO");
        byte[] data = new byte[4096];
        int read;
        while ((tocInputStream.read(data)) >= 0) {
            System.out.print(new String(data));
        }
        System.out.print("\n");
        if (!tocOnly) {
            AudioInputStream track = null;
            SourceDataLine line = null;
            AudioFormat audioFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    44100.0F, 16, 2, 4, 44100.0F, false);
            Line.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            data = new byte[2352 * 8];
            track = cddaMidLevel.getTrack("TODO", _track);
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open();
            line.start();
            while ((read = track.read(data)) >= 0) {
                line.write(data, 0, read);
            }
        }
//        cdda.close();
    }
}
