/*
 *  Copyright (c) 1999 - 2001 by Matthias Pfisterer
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

package org.tritonus.sampled.mixer.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.tritonus.lowlevel.alsa.Alsa;
import org.tritonus.lowlevel.alsa.AlsaPcm;
import org.tritonus.share.sampled.TConversionTool;

import static java.lang.System.getLogger;


public class AlsaSourceDataLine extends AlsaBaseDataLine implements SourceDataLine {

    private static final Logger logger = getLogger("org.tritonus.TraceSourceDataLine");

//    private static final Class[] CONTROL_CLASSES = {GainControl.class};

    private byte[] swapBuffer;

    // TODO has info object to change if format or buffer size are changed later?
    // no, but it has to represent the mixer's capabilities. So a fixed info per mixer.
    public AlsaSourceDataLine(AlsaDataLineMixer mixer, AudioFormat format, int bufferSize)
            throws LineUnavailableException {
        super(mixer, new DataLine.Info(SourceDataLine.class, format, bufferSize));
        logger.log(Level.TRACE, "begin");

        logger.log(Level.TRACE, "buffer size: " + bufferSize);

        logger.log(Level.TRACE, "end");
    }

    @Override
    protected int getAlsaStreamType() {
        return AlsaPcm.SND_PCM_STREAM_PLAYBACK;
    }

//    public void start() {
//        setStarted(true);
//        setActive(true);
//        if (TDebug.TraceSourceDataLine) {
//logger.log(Level.TRACE, "channel started.");
//        }
//    }

    @Override
    protected void stopImpl() {
        logger.log(Level.TRACE, "called");

        int ret = 0;
//        int ret = getAlsaPcm().flushChannel(AlsaPcm.SND_PCM_CHANNEL_PLAYBACK);
        if (ret != 0) {
            logger.log(Level.TRACE, "flushChannel: " + Alsa.getStringError(ret));
        }
//        setStarted(false);
    }

    @Override
    public int available() {
        // TODO
        throw new UnsupportedOperationException("sorry, this feature is not yet implemented");
    }

    // TODO check if should block
    @Override
    public int write(byte[] data, int offset, int length) {
        logger.log(Level.TRACE, "begin");

        if (getSwapBytes()) {
            if (swapBuffer == null || swapBuffer.length < offset + length) {
                swapBuffer = new byte[offset + length];
            }
            TConversionTool.changeOrderOrSign(
                    data, offset,
                    swapBuffer, offset,
                    length, getBytesPerSample());
            data = swapBuffer;
        }
        int ret = writeImpl(data, offset, length);
        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Write data to the line.
     *
     * @param data  The buffer to use.
     * @param offset
     * @param length The length of the data that should be written,
     *                in bytes. Can be less that the length of data.
     * @return The number of bytes written. May be less than length.
     */

    // TODO check if should block
    private int writeImpl(byte[] data, int offset, int length) {
        logger.log(Level.TRACE, "begin");

        if (length > 0 && !isActive()) {
            start();
        }
        int frameSize = getFormat().getFrameSize();
        int remaining = length;
        while (remaining > 0 && isOpen()) {
            synchronized (this) {
                if (!isOpen()) {
                    return length - remaining;
                }
                logger.log(Level.TRACE, "trying to write (bytes): " + remaining);

                int remainingFrames = remaining / frameSize;
                logger.log(Level.TRACE, "trying to write (frames): " + remainingFrames);

                int writtenFrames = (int) getAlsaPcm().writei(data, offset, remainingFrames);
                if (writtenFrames < 0) {
                    logger.log(Level.TRACE, Alsa.getStringError(writtenFrames));
                    return length - remaining;
                }
                logger.log(Level.TRACE, "written (frames): " + writtenFrames);

                int writtenBytes = writtenFrames * frameSize;
                logger.log(Level.TRACE, "written (bytes): " + writtenBytes);

                offset += writtenBytes;
                remaining -= writtenBytes;
            }
        }
        return length;
    }

    @Override
    public void drain() {
        // TODO
    }

    @Override
    public void flush() {
        // TODO
    }

    /**
     * dGain is logarithmic!!
     */
    @Override
    protected void setGain(float gain) {
    }

    // IDEA: move inner classes to TBaseDataLine
    public class AlsaSourceDataLineGainControl extends FloatControl {

        private static final float MAX_GAIN = 90.0F;
        private static final float MIN_GAIN = -96.0F;

        // TODO recheck this value
        private static final int GAIN_INCREMENTS = 1000;

//        private float gain;
//        private boolean muted;

        /* package */ AlsaSourceDataLineGainControl() {
            super(FloatControl.Type.VOLUME, // or MASTER_GAIN ?
                    -96.0F, // MIN_GAIN,
                    24.0F, // MAX_GAIN,
                    0.01F, // precision
                    0, // update period?
                    0.0F, // initial value
                    "dB",
                    "-96.0",
                    "",
                    "+24.0");
//            muted = false; // should be included in a compound control?
        }

        @Override
        public void setValue(float gain) {
            gain = Math.max(Math.min(gain, getMaximum()), getMinimum());
            if (Math.abs(gain - getValue()) > 1.0E9) {
                super.setValue(gain);
//                if (!getMute()) {
                AlsaSourceDataLine.this.setGain(getValue());
//                }
            }
        }

//        public float getMaximum() {
//            return MAX_GAIN;
//        }
//
//        public float getMinimum() {
//            return MIN_GAIN;
//        }
//
//        public int getIncrements() {
//            // TODO check this value
//            return GAIN_INCREMENTS;
//        }
//
//        public void fade(float initialGain, float finalGain, int frames) {
//            // TODO
//        }
//
//        public int getFadePrecision() {
//            //TODO
//            return -1;
//        }
//
//        public boolean getMute() {
//            return muted;
//        }
//
//        public void setMute(boolean mutes) {
//            if (mutes != getMute()) {
//                this.muted = mutes;
//                if (getMute()) {
//                    AlsaSourceDataLine.this.setGain(getMinimum());
//                } else {
//                    AlsaSourceDataLine.this.setGain(getGain());
//                }
//            }
//        }
    }
}
