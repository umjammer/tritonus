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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import static java.lang.System.getLogger;


public class CddaStreamHandler extends URLStreamHandler {

    private static final Logger logger = getLogger("org.tritonus.TraceCdda");

    @Override
    public URLConnection openConnection(URL url) {
        logger.log(Level.TRACE, "CddaStreamHandler.openConnection():begin");

        URLConnection connection;
        if (url.getFile().isEmpty()) {
            connection = new CddaDriveListConnection(url);
        } else if (url.getRef() == null) {
            connection = new CddaTocConnection(url);
        } else {
            connection = new CddaDataConnection(url);
        }

        logger.log(Level.TRACE, "CddaStreamHandler.openConnection():end");

        return connection;
    }
}
