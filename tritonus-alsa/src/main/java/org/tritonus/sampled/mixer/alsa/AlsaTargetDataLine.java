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
import javax.sound.sampled.TargetDataLine;

import org.tritonus.lowlevel.alsa.Alsa;
import org.tritonus.lowlevel.alsa.AlsaPcm;
import org.tritonus.share.sampled.TConversionTool;

import static java.lang.System.getLogger;


public class AlsaTargetDataLine extends AlsaBaseDataLine implements TargetDataLine {

    private static final Logger logger = getLogger("org.tritonus.TraceTargetDataLine");

    private byte[] swapBuffer;

    public AlsaTargetDataLine(AlsaDataLineMixer mixer, AudioFormat format, int bufferSize)
            throws LineUnavailableException {
        // TODO use an info object that represents the mixer's capabilities (all possible formats for the line)
        super(mixer, new DataLine.Info(TargetDataLine.class, format, bufferSize) /*,
          // TODO has info object to change if format or buffer size are changed later?
          format, bufferSize */);
    }

    @Override
    protected int getAlsaStreamType() {
        return AlsaPcm.SND_PCM_STREAM_CAPTURE;
    }

//    public void start() {
//        // getAlsaPcm().goCapture();
//        setStarted(true);
//        setActive(true);
//        if (TDebug.TraceSourceDataLine) {
//logger.log(Level.TRACE, "channel started.");
//        }
//    }

    @Override
    protected void stopImpl() {
        logger.log(Level.TRACE, "called.");

        int ret = 0; //getAlsaPcm().flushChannel(AlsaPcm.SND_PCM_CHANNEL_CAPTURE);
        if (ret != 0) {
            logger.log(Level.TRACE, "flushChannel: " + Alsa.getStringError(ret));
        }
    }

    @Override
    public int available() {
        // TODO
        return -1;
    }

    // TODO check if should block
    @Override
    public int read(byte[] data, int offset, int length) {
        logger.log(Level.TRACE, "called.");
        logger.log(Level.TRACE, "wanted length: " + length);
        int originalOffset = offset;
        if (length > 0 && !isActive()) {
            start();
        }
        if (!isOpen()) {
            logger.log(Level.TRACE, "stream closed");
        }
        int bytesRead = readImpl(data, offset, length);
        logger.log(Level.TRACE, "read (bytes): " + bytesRead);

        if (getSwapBytes() && bytesRead > 0) {
            TConversionTool.swapOrder16(data, originalOffset, bytesRead / 2);
        }
        return bytesRead;
    }

    // TODO check if should block
    public int readImpl(byte[] data, int offset, int length) {
        logger.log(Level.TRACE, "called.");
        logger.log(Level.TRACE, "wanted length: " + length);
        int frameSize = getFormat().getFrameSize();
        int framesToRead = length / frameSize;
        if (length > 0 && !isActive()) {
            start();
        }
        if (!isOpen()) {
            logger.log(Level.TRACE, "stream closed");
        }
        int framesRead = (int) getAlsaPcm().readi(data, offset, framesToRead);
        if (framesRead < 0) {
            logger.log(Level.TRACE, Alsa.getStringError(framesRead));
        }
        int bytesRead = framesRead * frameSize;
        logger.log(Level.TRACE, "read (bytes): " + bytesRead);

        return bytesRead;
    }

    @Override
    public void drain() {
        // TODO
    }

    @Override
    public void flush() {
        // TODO
    }

    public long getPosition() {
        // TODO
        return 0;
    }

    /**
     * fGain is logarithmic!!
     */
    @Override
    protected void setGain(float gain) {
    }

    public class AlsaTargetDataLineGainControl extends FloatControl {

        private static final float MAX_GAIN = 90.0F;
        private static final float MIN_GAIN = -96.0F;

        // TODO recheck this value
        private static final int GAIN_INCREMENTS = 1000;

//        private float gain;
//        private boolean muted;

        /* package */ AlsaTargetDataLineGainControl() {
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
                AlsaTargetDataLine.this.setGain(getValue());
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
//        public void setMute(boolean muted) {
//            if (muted != getMute()) {
//                this.muted = muted;
//                if (getMute()) {
//                    AlsaTargetDataLine.this.setGain(getMinimum());
//                } else {
//                    AlsaTargetDataLine.this.setGain(getGain());
//                }
//            }
//        }
    }
}
