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

package org.tritonus.sampled.mixer.esd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import org.tritonus.lowlevel.esd.Esd;
import org.tritonus.lowlevel.esd.EsdSample;
import org.tritonus.share.sampled.mixer.TDataLine;
import org.tritonus.share.sampled.mixer.TMixer;

import static java.lang.System.getLogger;


public class EsdClip extends TDataLine implements Clip {

    private static final Logger logger = getLogger("org.tritonus.TraceClip");
    
    private static final Class<?>[] CONTROL_CLASSES = { /* GainControl.class */ };
    private static final int BUFFER_FRAMES = 16384;

    private Mixer m_mixer;
    private EsdSample m_esdSample;

    public EsdClip(TMixer mixer) {
        super(mixer, null);
        m_mixer = mixer;
        m_esdSample = new EsdSample();
    }

    // interface Clip

    @Override
    public void open(AudioFormat audioFormat, byte[] abData, int nOffset, int nNumFrames) throws LineUnavailableException {
        int nBufferLength = nNumFrames * audioFormat.getFrameSize();
        // TODO check if nOffset + nBufferLength <= abData.length
        // perhaps truncate automatically
        ByteArrayInputStream bais = new ByteArrayInputStream(abData, nOffset, nBufferLength);
        try {
            AudioInputStream audioInputStream = new AudioInputStream(bais, audioFormat, nNumFrames);
            open(audioInputStream);
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);

            throw new LineUnavailableException();
        }
    }

    @Override
    public void open(AudioInputStream audioInputStream) throws LineUnavailableException, IOException {
        AudioFormat audioFormat = audioInputStream.getFormat();
        // TODO
        DataLine.Info info = new DataLine.Info(Clip.class, audioFormat, -1 /* nBufferSize */);
        setLineInfo(info);
        int nFrameSize = audioFormat.getFrameSize();
        long lTotalLength = audioInputStream.getFrameLength() * nFrameSize;
        int nFormat = Esd.ESD_STREAM | Esd.ESD_PLAY | EsdUtils.getEsdFormat(audioFormat);
        logger.log(Level.TRACE, "format: " + nFormat);
        logger.log(Level.TRACE, "sample rate: " + audioFormat.getSampleRate());
        m_esdSample.open(nFormat, (int) audioFormat.getSampleRate(), (int) lTotalLength);
        logger.log(Level.TRACE, "size in esd: " + audioInputStream.getFrameLength() * nFrameSize);

        int nBufferLength = BUFFER_FRAMES * nFrameSize;
        byte[] abData = new byte[nBufferLength];
        int nBytesRead = 0;
        int nTotalBytes = 0;
        while (nBytesRead != -1) {
            try {
                nBytesRead = audioInputStream.read(abData, 0, abData.length);
            } catch (IOException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            if (nBytesRead >= 0) {
                nTotalBytes += nBytesRead;
                logger.log(Level.TRACE, "EsdClip.open(): total bytes: " + nTotalBytes);
                logger.log(Level.TRACE, "EsdClip.open(): Trying to write: " + nBytesRead);
                int nBytesWritten = m_esdSample.write(abData, 0, nBytesRead);
                logger.log(Level.TRACE, "EsdClip.open(): Written: " + nBytesWritten);
            }
        }
        // to trigger the events
        open();
    }

    @Override
    public int getFrameLength() {
        // TODO
        return -1;
    }

    @Override
    public long getMicrosecondLength() {
        // TODO
        return -1;
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
        logger.log(Level.TRACE, "EsdClip.loop(int): called; count = " + nCount);

        if (false /* isStarted() */) {
            // only allow zero count to stop the looping
            // at the end of an iteration.
            if (nCount == 0) {
                logger.log(Level.TRACE, "EsdClip.loop(int): stopping sample");

                m_esdSample.stop();
            }
        } else {
            if (nCount == 0) {
                logger.log(Level.TRACE, "EsdClip.loop(int): starting sample (once)");

                m_esdSample.play();
            } else {
                // we're ignoring the count, because esd
                // cannot loop for a fixed number of
                // times.
//                logger.log(Level.TRACE, "hallo");
                logger.log(Level.TRACE, "EsdClip.loop(int): starting sample (forever)");

                m_esdSample.loop();
            }
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
        m_esdSample.free();
        m_esdSample.close();
        // TODO
    }

    @Override
    public void open() {
        // TODO
    }

    @Override
    public void start() {
        logger.log(Level.TRACE, "EsdClip.start(): called");

        // This is a hack. What start() really should do is
        // start playing at the position playback was stopped.
        logger.log(Level.TRACE, "EsdClip.start(): calling 'loop(0)' [hack]");

        loop(0);
    }

    @Override
    public void stop() {
        // TODO
        m_esdSample.kill();
    }

    /*
     *	This method is enforced by DataLine, but doesn't make any
     *	sense for Clips.
     */
    @Override
    public int available() {
        return -1;
    }
}
