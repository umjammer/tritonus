/*
 *  Copyright (c) 2001 by Matthias Pfisterer
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

package org.tritonus.lowlevel.cdda.cooked_ioctl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Iterator;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.tritonus.lowlevel.cdda.CddaMidLevel;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;

import static java.lang.System.getLogger;


public class CookedIoctlMidLevel implements CddaMidLevel {

    private static final Logger logger = getLogger("org.tritonus.TraceCdda");

    private static final int PCM_FRAMES_PER_CDDA_FRAME = 588;
    private static final AudioFormat CDDA_FORMAT = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            44100.0F, 16, 2, 4, 44100.0F, false);

    public CookedIoctlMidLevel() {
        logger.log(Level.TRACE, "begin");

        logger.log(Level.TRACE, "end");
    }

    @Override
    public Iterator<String> getDevices() {
        // TODO hack!! should be replaced by a real search
        String[] devices = {"/dev/cdrom"};
        // TODO should make list immutable
        List<String> devicesList = List.of(devices);
        Iterator<String> iterator = devicesList.iterator();
        return iterator;
    }

    @Override
    public String getDefaultDevice() {
        return "/dev/cdrom";
    }

    @Override
    public InputStream getTocAsXml(String device) throws IOException {
        logger.log(Level.TRACE, "begin");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        int[] values = new int[2];
        int[] startFrame = new int[100];
        int[] length = new int[100];
        int[] type = new int[100];
        boolean[] copy = new boolean[100];
        boolean[] pre = new boolean[100];
        int[] channels = new int[100];
        CookedIoctl cookedIoctl = new CookedIoctl(device);
        cookedIoctl.readTOC(values,
                startFrame,
                length,
                type,
                copy,
                pre,
                channels);

        int tracks = values[1] - values[0] + 1;
        for (int i = 0; i <= tracks; i++) {
            out.print("<track");
            out.print(" id=\"" + (i + values[0]) + "\"");
            out.print(" start=\"" + startFrame[i] + "\"");
            out.print(" type=\"" + type[i] + "\" />\n");
        }
        byte[] data = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        cookedIoctl.close();

        logger.log(Level.TRACE, "end");

        return bais;
    }

    @Override
    public AudioInputStream getTrack(String device, int track) throws IOException {
        logger.log(Level.TRACE, "begin");

        AudioInputStream audioInputStream = new CddaAudioInputStream(device, track);

        logger.log(Level.TRACE, "end");

        return audioInputStream;
    }

    private static class CddaAudioInputStream extends TAsynchronousFilteredAudioInputStream {

        private static final int BUFFER_SIZE = CddaMidLevel.FRAME_SIZE;

        /** */
        private final CookedIoctl cookedIoctl;

        /**
         * This variable gets initialized to the total number of cdda
         * frames for the respective track. On reading of a frame, it
         * decremented untill zero.
         */
        private int cddaFrameCount;

        /**
         * This variable contains the number of the cdda
         * frame where the current track begins.
         */
        private final int startFrame;

        /**
         * This variable contains the number of the cdda
         * frame where the current track begins.
         */
        private int endFrame;

        /**
         * Buffer for reading cdda frames.
         */
        private final byte[] data;

        /**
         * Track number.
         */
        private final int track;

        public CddaAudioInputStream(String device, int track) {
            super(CDDA_FORMAT, AudioSystem.NOT_SPECIFIED /* getTrackLengthInPcmFrames() */);
            logger.log(Level.TRACE, "begin");

            this.track = track;
            int[] values = new int[2];
            int[] startFrame = new int[100];
            int[] length = new int[100];
            int[] type = new int[100];
            boolean[] copy = new boolean[100];
            boolean[] pre = new boolean[100];
            int[] channels = new int[100];
            cookedIoctl = new CookedIoctl(device);
            cookedIoctl.readTOC(values,
                    startFrame,
                    length,
                    type,
                    copy,
                    pre,
                    channels);

            cddaFrameCount = 0;
            this.startFrame = startFrame[getTrack()];
            // !!! writing to protected superclass variable !!!
            frameLength = getTrackLengthInPcmFrames();
            data = new byte[BUFFER_SIZE];

            logger.log(Level.TRACE, "end");
        }

        private long getTrackLengthInPcmFrames() {
            int cddaFrames = getTrackLengthInCddaFrames();
            long length = (long) cddaFrames * PCM_FRAMES_PER_CDDA_FRAME;
            return length;
        }

        private int getTrackLengthInCddaFrames() {
            int length = getEndFrame() - getStartFrame() + 1;
            return length;
        }

        private int getStartFrame() {
            return startFrame;
        }

        private int getEndFrame() {
            return endFrame;
        }

        private int getTrack() {
            return track;
        }

        private int getCurrentFrameNumber() {
            return cddaFrameCount + startFrame;
        }

        private void increaseCurrentFrameNumber() {
            cddaFrameCount++;
        }

        private boolean isEndOfTrackReached() {
            return cddaFrameCount >= getTrackLengthInCddaFrames();
        }

        @Override
        public void execute() {
            logger.log(Level.TRACE, "begin");

            if (!isEndOfTrackReached()) {
                logger.log(Level.TRACE, "begin");

                while (getCircularBuffer().availableWrite() >= BUFFER_SIZE && !isEndOfTrackReached()) {
                    logger.log(Level.TRACE, "before readFrame()");

                    cookedIoctl.readFrame(getCurrentFrameNumber(), 1, data);
                    logger.log(Level.TRACE, "after readFrame(), before cb.write()");

                    getCircularBuffer().write(data, 0, BUFFER_SIZE);
                    logger.log(Level.TRACE, "after cb.write()");

                    increaseCurrentFrameNumber();
                }
            } else {
                logger.log(Level.TRACE, "end of cdda track");

                getCircularBuffer().close();
            }

            logger.log(Level.TRACE, "end");
        }

        @Override
        public void close() throws IOException {
            cookedIoctl.close();
            super.close();
            // TODO close cdda?
//            encodedStream.close();
        }
    }
}
