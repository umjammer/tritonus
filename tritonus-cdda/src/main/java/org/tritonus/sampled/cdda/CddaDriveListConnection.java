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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import org.tritonus.lowlevel.cdda.CddaMidLevel;
import org.tritonus.lowlevel.cdda.CddaUtils;

import static java.lang.System.getLogger;


public class CddaDriveListConnection extends URLConnection {

    private static final Logger logger = getLogger("org.tritonus.TraceCdda");

    private CddaMidLevel cddaMidLevel;

    // TODO m_cdda.close();
    public CddaDriveListConnection(URL url) {
        super(url);
        logger.log(Level.TRACE, "begin");

        logger.log(Level.TRACE, "end");
    }

    @Override
    public void connect() {
        logger.log(Level.TRACE, "begin");

        if (!connected) {
            cddaMidLevel = CddaUtils.getCddaMidLevel();
            connected = true;
        }

        logger.log(Level.TRACE, "end");
    }

    @Override
    public InputStream getInputStream() throws IOException {
        logger.log(Level.TRACE, "begin");

        connect();
        Iterator<String> drivesIterator = cddaMidLevel.getDevices();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        while (drivesIterator.hasNext()) {
            String drive = drivesIterator.next();
            out.print(drive + "\n");
        }
        byte[] data = baos.toByteArray();
        baos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);

        logger.log(Level.TRACE, "end");

        return bais;
    }
}
