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
//    private Mixer m_mixer;
    private SourceDataLine m_line;
    private byte[] m_abClip;
    private int m_nRepeatCount;
    private Thread m_thread;

    public TSoftClip(Mixer mixer, AudioFormat format) throws LineUnavailableException {
        // TODO info object
//        DataLine.Info info = new DataLine.Info(Clip.class, audioFormat, -1);
        super(null);
//        m_mixer = mixer;
        // TODO should pass a real AudioFormat object that isn't too restrictive
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        m_line = (SourceDataLine) AudioSystem.getLine(info);
    }

    @Override
    public void open(AudioInputStream audioInputStream) throws LineUnavailableException, IOException {
        AudioFormat audioFormat = audioInputStream.getFormat();
        setFormat(audioFormat);
        int nFrameSize = audioFormat.getFrameSize();
        if (nFrameSize < 1) {
            throw new IllegalArgumentException("frame size must be positive");
        }
        logger.log(Level.TRACE, "TSoftClip.open(): format: " + audioFormat);
//        logger.log(Level.TRACE, "sample rate: " + audioFormat.getSampleRate());
        byte[] abData = new byte[BUFFER_SIZE];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int nBytesRead = 0;
        while (nBytesRead != -1) {
            try {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
            } catch (IOException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            if (nBytesRead >= 0) {
                logger.log(Level.TRACE, "TSoftClip.open(): Trying to write: " + nBytesRead);

                baos.write(abData, 0, nBytesRead);

                logger.log(Level.TRACE, "TSoftClip.open(): Written: " + nBytesRead);
            }
        }
        m_abClip = baos.toByteArray();
        setBufferSize(m_abClip.length);
        // open the line
        m_line.open(getFormat());
        // to trigger the events
        // open();
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
    public void setFramePosition(int nPosition) {
        // TODO
    }

    @Override
    public void setMicrosecondPosition(long lPosition) {
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
    public void setLoopPoints(int nStart, int nEnd) {
        // TODO
    }

    @Override
    public void loop(int nCount) {
        logger.log(Level.TRACE, "TSoftClip.loop(int): called; count = " + nCount);

        if (false /* isStarted() */) {
            // only allow zero count to stop the looping
            // at the end of an iteration.
            if (nCount == 0) {
                logger.log(Level.TRACE, "TSoftClip.loop(int): stopping sample");

//                m_esdSample.stop();
            }
        } else {
            m_nRepeatCount = nCount;
            m_thread = new Thread(this);
            m_thread.start();
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
//        m_esdSample.free();
//        m_esdSample.close();
        // TODO
    }

    @Override
    public void open() {
        // TODO
    }

    @Override
    public void start() {
        logger.log(Level.TRACE, "TSoftClip.start(): called");

        // This is a hack. What start() really should do is
        // start playing at the position playback was stopped.
        logger.log(Level.TRACE, "TSoftClip.start(): calling 'loop(0)' [hack]");

        loop(0);
    }

    @Override
    public void stop() {
        // TODO
//        m_esdSample.kill();
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
        while (m_nRepeatCount >= 0) {
            m_line.write(m_abClip, 0, m_abClip.length);
            m_nRepeatCount--;
        }
    }
}
