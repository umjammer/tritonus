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

    private final RTSystem rtSystem;
    private boolean running;
    private final BufferedReader bufferedReader;

    protected SaslParser(RTSystem rtSystem, InputStream inputStream) {
        this.rtSystem = rtSystem;
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
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
        running = true;
        while (running) {
            String line = bufferedReader.readLine();
            if (line == null) {
                // EOF signaled.
                break;
            }
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            logger.log(Level.TRACE, "line: " + line);
            String[] parts = line.split("\\s");
            boolean highPriority = false;
            int index = 0;
            if (parts[index].equals("*")) {
                highPriority = true;
                index++;
            }
            float time = Float.parseFloat(parts[index]);
            index++;
            String commandName = parts[index];
            index++;
            if (commandName.equals("end")) {
                rtSystem.scheduleEnd(time);
            } else {
                float duration = Float.parseFloat(parts[index]);
                rtSystem.scheduleInstrument(commandName, time, duration);
            }
        }
    }
}
