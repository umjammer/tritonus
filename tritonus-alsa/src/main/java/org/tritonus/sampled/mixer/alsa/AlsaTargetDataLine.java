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

    private byte[] m_abSwapBuffer;

    public AlsaTargetDataLine(AlsaDataLineMixer mixer, AudioFormat format, int nBufferSize)
            throws LineUnavailableException {
        // TODO use an info object that represents the mixer's capabilities (all possible formats for the line)
        super(mixer,
                new DataLine.Info(TargetDataLine.class,
                        format,
                        nBufferSize) /*,
          // TODO has info object to change if format or buffer size are changed later?
          format, nBufferSize */);
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
//            logger.log(Level.TRACE, "AlsaTargetDataLine.start(): channel started.");
//        }
//    }

    @Override
    protected void stopImpl() {
        logger.log(Level.TRACE, "AlsaTargetDataLine.stopImpl(): called.");

        int nReturn = 0; //getAlsaPcm().flushChannel(AlsaPcm.SND_PCM_CHANNEL_CAPTURE);
        if (nReturn != 0) {
            logger.log(Level.TRACE, "flushChannel: " + Alsa.getStringError(nReturn));
        }
    }

    @Override
    public int available() {
        // TODO
        return -1;
    }

    // TODO check if should block
    @Override
    public int read(byte[] abData, int nOffset, int nLength) {
        logger.log(Level.TRACE, "AlsaTargetDataLine.read(): called.");
        logger.log(Level.TRACE, "AlsaTargetDataLine.read(): wanted length: " + nLength);
        int nOriginalOffset = nOffset;
        if (nLength > 0 && !isActive()) {
            start();
        }
        if (!isOpen()) {
            logger.log(Level.TRACE, "AlsaTargetDataLine.read(): stream closed");
        }
        int nBytesRead = readImpl(abData, nOffset, nLength);
        logger.log(Level.TRACE, "AlsaTargetDataLine.read(): read (bytes): " + nBytesRead);

        if (getSwapBytes() && nBytesRead > 0) {
            TConversionTool.swapOrder16(abData, nOriginalOffset, nBytesRead / 2);
        }
        return nBytesRead;
    }

    // TODO check if should block
    public int readImpl(byte[] abData, int nOffset, int nLength) {
        logger.log(Level.TRACE, "AlsaTargetDataLine.readImpl(): called.");
        logger.log(Level.TRACE, "AlsaTargetDataLine.readImpl(): wanted length: " + nLength);
        int nFrameSize = getFormat().getFrameSize();
        int nFramesToRead = nLength / nFrameSize;
        if (nLength > 0 && !isActive()) {
            start();
        }
        if (!isOpen()) {
            logger.log(Level.TRACE, "AlsaTargetDataLine.readImpl(): stream closed");
        }
        int nFramesRead = (int) getAlsaPcm().readi(abData, nOffset, nFramesToRead);
        if (nFramesRead < 0) {
            logger.log(Level.TRACE, "AlsaTargetDataLine.readImpl(): " + Alsa.getStringError(nFramesRead));
        }
        int nBytesRead = nFramesRead * nFrameSize;
        logger.log(Level.TRACE, "AlsaTargetDataLine.readImpl(): read (bytes): " + nBytesRead);

        return nBytesRead;
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
    protected void setGain(float fGain) {
    }

    public class AlsaTargetDataLineGainControl extends FloatControl {

        private static final float MAX_GAIN = 90.0F;
        private static final float MIN_GAIN = -96.0F;

        // TODO recheck this value
        private static final int GAIN_INCREMENTS = 1000;

//        private float  m_fGain;
//        private boolean  m_bMuted;

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
//            m_bMuted = false; // should be included in a compund control?
        }

        @Override
        public void setValue(float fGain) {
            fGain = Math.max(Math.min(fGain, getMaximum()), getMinimum());
            if (Math.abs(fGain - getValue()) > 1.0E9) {
                super.setValue(fGain);
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
//        public void fade(float fInitialGain, float fFinalGain, int nFrames) {
//            // TODO
//        }
//
//        public int getFadePrecision() {
//            //TODO
//            return -1;
//        }
//
//        public boolean getMute() {
//            return m_bMuted;
//        }
//
//        public void setMute(boolean bMuted) {
//            if (bMuted != getMute()) {
//                m_bMuted = bMuted;
//                if (getMute()) {
//                    AlsaTargetDataLine.this.setGain(getMinimum());
//                } else {
//                    AlsaTargetDataLine.this.setGain(getGain());
//                }
//            }
//        }
    }
}
