/*
 *  Copyright (c) 2002 by Matthias Pfisterer
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

package org.tritonus.saol.engine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


public class SaslParser implements Runnable {

    private static final Logger logger = getLogger(SaslParser.class.getName());

    private final RTSystem m_rtSystem;
    private boolean m_bRunning;
    private final BufferedReader m_bufferedReader;

    protected SaslParser(RTSystem rtSystem, InputStream inputStream) {
        m_rtSystem = rtSystem;
        m_bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    }

    @Override
    public void run() {
        try {
            runImpl();
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    private void runImpl() throws IOException {
        m_bRunning = true;
        while (m_bRunning) {
            String strLine = m_bufferedReader.readLine();
            if (strLine == null) {
                // EOF signaled.
                break;
            }
            strLine = strLine.trim();
            if (strLine.isEmpty()) {
                continue;
            }
            logger.log(Level.TRACE, "line: " + strLine);
            String[] astrParts = strLine.split("\\s");
            boolean bHighPriority = false;
            int nIndex = 0;
            if (astrParts[nIndex].equals("*")) {
                bHighPriority = true;
                nIndex++;
            }
            float fTime = Float.parseFloat(astrParts[nIndex]);
            nIndex++;
            String strCommandName = astrParts[nIndex];
            nIndex++;
            if (strCommandName.equals("end")) {
                m_rtSystem.scheduleEnd(fTime);
            } else {
                float fDuration = Float.parseFloat(astrParts[nIndex]);
                m_rtSystem.scheduleInstrument(strCommandName, fTime, fDuration);
            }
        }
    }
}


