/*
 *  Copyright (c) 1999 by Matthias Pfisterer
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import static java.lang.System.getLogger;


public class TSoftClip extends TClip implements Runnable {

    private static final Logger logger= getLogger("org.tritonus.TraceClip");

    // $$fb the following field is never used
//    private static final Class[] CONTROL_CLASSES = { /* GainControl.class */ };
    private static final int BUFFER_SIZE = 16384;

    // $$fb the following field is never used
//    private Mixer mixer;
    private final SourceDataLine line;
    private byte[] clip;
    private int repeatCount;
    private Thread thread;

    public TSoftClip(Mixer mixer, AudioFormat format) throws LineUnavailableException {
        // TODO info object
//        DataLine.Info info = new DataLine.Info(Clip.class, audioFormat, -1);
        super(null);
//        this.mixer = mixer;
        // TODO should pass a real AudioFormat object that isn't too restrictive
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        line = (SourceDataLine) AudioSystem.getLine(info);
    }

    @Override
    public void open(AudioInputStream audioInputStream) throws LineUnavailableException, IOException {
        AudioFormat audioFormat = audioInputStream.getFormat();
        setFormat(audioFormat);
        int frameSize = audioFormat.getFrameSize();
        if (frameSize < 1) {
            throw new IllegalArgumentException("frame size must be positive");
        }
        logger.log(Level.TRACE, "format: " + audioFormat);
//logger.log(Level.TRACE, "sample rate: " + audioFormat.getSampleRate());
        byte[] data = new byte[BUFFER_SIZE];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead = 0;
        while (bytesRead != -1) {
            try {
                bytesRead = audioInputStream.read(data, 0, data.length);
            } catch (IOException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            if (bytesRead >= 0) {
                logger.log(Level.TRACE, "Trying to write: " + bytesRead);

                baos.write(data, 0, bytesRead);

                logger.log(Level.TRACE, "Written: " + bytesRead);
            }
        }
        clip = baos.toByteArray();
        setBufferSize(clip.length);
        // open the line
        line.open(getFormat());
        // to trigger the events
//        open();
    }

    @Override
    public int getFrameLength() {
        if (isOpen()) {
            return getBufferSize() / getFormat().getFrameSize();
        } else {
            return AudioSystem.NOT_SPECIFIED;
        }
    }

    @Override
    public long getMicrosecondLength() {
        if (isOpen()) {
            return (long) (getFrameLength() * getFormat().getFrameRate() * 1000000);
        } else {
            return AudioSystem.NOT_SPECIFIED;
        }
    }

    @Override
    public void setFramePosition(int position) {
        // TODO
    }

    @Override
    public void setMicrosecondPosition(long position) {
        // TODO
    }

    @Override
    public int getFramePosition() {
        // TODO
        return -1;
    }

    @Override
    public long getMicrosecondPosition() {
        // TODO
        return -1;
    }

    @Override
    public void setLoopPoints(int start, int end) {
        // TODO
    }

    @Override
    public void loop(int count) {
        logger.log(Level.TRACE, "called; count = " + count);

        if (false /* isStarted() */) {
            // only allow zero count to stop the looping
            // at the end of an iteration.
            if (count == 0) {
                logger.log(Level.TRACE, "stopping sample");

//                esdSample.stop();
            }
        } else {
            repeatCount = count;
            thread = new Thread(this);
            thread.start();
        }
        // TODO
    }

    @Override
    public void flush() {
        // TODO
    }

    @Override
    public void drain() {
        // TODO
    }

    @Override
    public void close() {
//        esdSample.free();
//        esdSample.close();
        // TODO
    }

    @Override
    public void open() {
        // TODO
    }

    @Override
    public void start() {
        logger.log(Level.TRACE, "called");

        // This is a hack. What start() really should do is
        // start playing at the position playback was stopped.
        logger.log(Level.TRACE, "calling 'loop(0)' [hack]");

        loop(0);
    }

    @Override
    public void stop() {
        // TODO
//        esdSample.kill();
    }

    /*
     * This method is enforced by DataLine, but doesn't make any
     * sense for Clips.
     */
    @Override
    public int available() {
        return -1;
    }

    @Override
    public void run() {
        while (repeatCount >= 0) {
            line.write(clip, 0, clip.length);
            repeatCount--;
        }
    }
}
