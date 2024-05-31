/*
 *  Copyright (c) 1999 - 2004 by Matthias Pfisterer
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

package org.tritonus.share.sampled.mixer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collection;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;

import static java.lang.System.getLogger;


/**
 * Base class for classes implementing DataLine.
 */
public abstract class TDataLine extends TLine implements DataLine {

    private static final Logger logger= getLogger("org.tritonus.TraceSourceDataLine");

    private static final int DEFAULT_BUFFER_SIZE = 128000;

    private AudioFormat format;
    private int bufferSize;
    private boolean running;
//    private boolean active;

    public TDataLine(TMixer mixer, DataLine.Info info) {
        super(mixer, info);
        init(info);
    }

    public TDataLine(TMixer mixer, DataLine.Info info, Collection<Control> controls) {
        super(mixer, info, controls);
        init(info);
    }

    // IDEA: extract format and bufSize from info?
    private void init(DataLine.Info info) {
        format = null;
        bufferSize = AudioSystem.NOT_SPECIFIED;
        setRunning(false);
//        setActive(false);
    }

    // not defined here:
    // public void drain()
    // public void flush()

    @Override
    public void start() {
        logger.log(Level.TRACE, "called");

        setRunning(true);
    }

    @Override
    public void stop() {
        logger.log(Level.TRACE, "called");

        setRunning(false);
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    // TODO recheck
    protected void setRunning(boolean running) {
        boolean oldValue = isRunning();
        this.running = running;
        if (oldValue != isRunning()) {
            if (isRunning()) {
                startImpl();
                notifyLineEvent(LineEvent.Type.START);
            } else {
                stopImpl();
                notifyLineEvent(LineEvent.Type.STOP);
            }
        }
    }

    protected void startImpl() {
    }

    protected void stopImpl() {
    }

    /**
     * This implementation returns the status of isRunning().
     * Subclasses should overwrite this method if there is more
     * precise information about the status of the line available.
     */
    @Override
    public boolean isActive() {
        return isRunning();
    }

//    public boolean isStarted() {
//        return started;
//    }

    // TODO should only ALLOW engaging in data I/O.
    // actual START event should only be sent when line really becomes active
//    protected void setStarted(boolean started) {
//        this.started = started;
//        if (!isRunning()) {
//            setActive(false);
//        }
//    }

    @Override
    public AudioFormat getFormat() {
        return format;
    }

    protected void setFormat(AudioFormat format) {
        logger.log(Level.TRACE, "setting: " + format);

        this.format = format;
    }

    @Override
    public int getBufferSize() {
        return bufferSize;
    }

    protected void setBufferSize(int bufferSize) {
        logger.log(Level.TRACE, "setting: " + bufferSize);

        this.bufferSize = bufferSize;
    }

    // not defined here:
    // public int available()

    @Override
    public int getFramePosition() {
        // TODO
        return -1;
    }

    @Override
    public long getLongFramePosition() {
        // TODO
        return -1;
    }

    @Override
    public long getMicrosecondPosition() {
        return (long) (getFramePosition() * getFormat().getFrameRate() * 1_000_000);
    }

    /*
     * Has to be overridden to be useful.
     */
    @Override
    public float getLevel() {
        return AudioSystem.NOT_SPECIFIED;
    }

    protected void checkOpen() {
        if (getFormat() == null) {
            throw new IllegalStateException("format must be specified");
        }
        if (getBufferSize() == AudioSystem.NOT_SPECIFIED) {
            setBufferSize(getDefaultBufferSize());
        }
    }

    protected int getDefaultBufferSize() {
        return DEFAULT_BUFFER_SIZE;
    }

    @Override
    protected void notifyLineEvent(LineEvent.Type type) {
        notifyLineEvent(new LineEvent(this, type, getFramePosition()));
    }
}
