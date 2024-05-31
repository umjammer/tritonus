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

    private final Mixer mixer;
    private final EsdSample esdSample;

    public EsdClip(TMixer mixer) {
        super(mixer, null);
        this.mixer = mixer;
        esdSample = new EsdSample();
    }

    // interface Clip

    @Override
    public void open(AudioFormat audioFormat, byte[] data, int offset, int frames) throws LineUnavailableException {
        int bufferLength = frames * audioFormat.getFrameSize();
        // TODO check if offset + bufferLength <= data.length
        // perhaps truncate automatically
        ByteArrayInputStream bais = new ByteArrayInputStream(data, offset, bufferLength);
        try {
            AudioInputStream audioInputStream = new AudioInputStream(bais, audioFormat, frames);
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
        int frameSize = audioFormat.getFrameSize();
        long totalLength = audioInputStream.getFrameLength() * frameSize;
        int format = Esd.ESD_STREAM | Esd.ESD_PLAY | EsdUtils.getEsdFormat(audioFormat);
        logger.log(Level.TRACE, "format: " + format);
        logger.log(Level.TRACE, "sample rate: " + audioFormat.getSampleRate());
        esdSample.open(format, (int) audioFormat.getSampleRate(), (int) totalLength);
        logger.log(Level.TRACE, "size in esd: " + audioInputStream.getFrameLength() * frameSize);

        int bufferLength = BUFFER_FRAMES * frameSize;
        byte[] data = new byte[bufferLength];
        int bytesRead = 0;
        int totalBytes = 0;
        while (bytesRead != -1) {
            try {
                bytesRead = audioInputStream.read(data, 0, data.length);
            } catch (IOException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
            if (bytesRead >= 0) {
                totalBytes += bytesRead;
                logger.log(Level.TRACE, "EsdClip.open(): total bytes: " + totalBytes);
                logger.log(Level.TRACE, "EsdClip.open(): Trying to write: " + bytesRead);
                int bytesWritten = esdSample.write(data, 0, bytesRead);
                logger.log(Level.TRACE, "EsdClip.open(): Written: " + bytesWritten);
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

                esdSample.stop();
            }
        } else {
            if (count == 0) {
                logger.log(Level.TRACE, "starting sample (once)");

                esdSample.play();
            } else {
                // we're ignoring the count, because esd
                // cannot loop for a fixed number of
                // times.
//logger.log(Level.TRACE, "hallo");
                logger.log(Level.TRACE, "starting sample (forever)");

                esdSample.loop();
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
        esdSample.free();
        esdSample.close();
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
        esdSample.kill();
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
