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

import org.tritonus.lowlevel.cdda.CddaMidLevel;
import org.tritonus.lowlevel.cdda.CddaUtils;

import static java.lang.System.getLogger;


public class CddaTocConnection extends URLConnection {

    private static final Logger logger = getLogger("org.tritonus.TraceCdda");

    /**
     * The cdda device name to read from.
     */
    private String device;

    private CddaMidLevel cddaMidLevel;

    // TODO m_cdda.close();
    public CddaTocConnection(URL url) {
        super(url);
        logger.log(Level.TRACE, "begin");

        device = url.getPath();

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
        InputStream inputStream = cddaMidLevel.getTocAsXml(device);

        logger.log(Level.TRACE, "end");

        return inputStream;
    }

    private String getDevice() {
        return device;
    }
}
