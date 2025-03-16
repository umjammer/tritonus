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

package org.tritonus.sampled.cdda;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.net.URLConnection;
import javax.sound.sampled.AudioFormat;

import org.tritonus.lowlevel.cdda.CddaMidLevel;
import org.tritonus.lowlevel.cdda.CddaUtils;

import static java.lang.System.getLogger;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;


public class CddaDataConnection extends URLConnection {

    private static final Logger logger = getLogger("org.tritonus.TraceCdda");

    private static final int PCM_FRAMES_PER_CDDA_FRAME = 588;
    private static final AudioFormat CDDA_FORMAT = new AudioFormat(
            PCM_SIGNED, 44100.0F, 16, 2, 4, 44100.0F, false);

    /**
     * The cdda device name to read from.
     */
    private String device;

    /**
     * Track to read from the CD.
     */
    private final int track;

    private CddaMidLevel cddaMidLevel;

    public CddaDataConnection(URL url) {
        super(url);
        logger.log(Level.TRACE, "begin");

        device = url.getFile();
        String _track = url.getRef();
        track = Integer.parseInt(_track);

        logger.log(Level.TRACE, "end");
    }

    @Override
    public void connect() {
        logger.log(Level.TRACE, "begin");

        if (!connected) {
            cddaMidLevel = CddaUtils.getCddaMidLevel();
            if (device.isEmpty()) {
                device = cddaMidLevel.getDefaultDevice();
            }
            connected = true;
        }

        logger.log(Level.TRACE, "end");
    }

    @Override
    public InputStream getInputStream() throws IOException {
        logger.log(Level.TRACE, "begin");

        connect();
        String device = getDevice();
        int track = getTrack();
        InputStream inputStream = cddaMidLevel.getTrack(device, track);

        logger.log(Level.TRACE, "end");

        return inputStream;
    }

    private String getDevice() {
        return device;
    }

    private int getTrack() {
        return track;
    }
}
