/*
 *  Copyright (c) 1999 - 2004 by Matthias Pfisterer
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

package org.tritonus.share.sampled.mixer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collection;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;

import static java.lang.System.getLogger;


/**
 * Base class for implementing SourceDataLine or TargetDataLine.
 */
public abstract class TBaseDataLine extends TDataLine {

    private static final Logger logger= getLogger("org.tritonus.TraceDataLine");

    public TBaseDataLine(TMixer mixer, DataLine.Info info) {
        super(mixer, info);
    }

    public TBaseDataLine(TMixer mixer, DataLine.Info info, Collection<Control> controls) {
        super(mixer, info, controls);
    }

    public void open(AudioFormat format, int nBufferSize) throws LineUnavailableException {
        logger.log(Level.TRACE, "TBaseDataLine.open(AudioFormat, int): called with buffer size: " + nBufferSize);

        setBufferSize(nBufferSize);
        open(format);
    }

    public void open(AudioFormat format) throws LineUnavailableException {
        logger.log(Level.TRACE, "TBaseDataLine.open(AudioFormat): called");

        setFormat(format);
        open();
    }
}
