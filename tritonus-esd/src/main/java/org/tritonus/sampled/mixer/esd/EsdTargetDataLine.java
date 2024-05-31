/*
 *  Copyright (c) 1999, 2000 by Matthias Pfisterer
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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.tritonus.lowlevel.esd.Esd;
import org.tritonus.lowlevel.esd.EsdRecordingStream;
import org.tritonus.share.sampled.TConversionTool;
import org.tritonus.share.sampled.mixer.TBaseDataLine;
import org.tritonus.share.sampled.mixer.TMixer;

import static java.lang.System.getLogger;


public class EsdTargetDataLine extends TBaseDataLine implements TargetDataLine {

    private static final Logger logger = getLogger("org.tritonus.TraceTargetDataLine");

//    private static final Class[] CONTROL_CLASSES = {GainControl.class};

    private EsdRecordingStream esdStream;
    private boolean swapBytes;
    private byte[] swapBuffer;

    /*
     * Only used if swapBytes is true.
     */
    private int bytesPerSample;

    public EsdTargetDataLine(TMixer mixer, AudioFormat format, int bufferSize) throws LineUnavailableException {
        // TODO use an info object that represents the mixer's capabilities (all possible formats for the line)
        super(mixer, new DataLine.Info(TargetDataLine.class, format, bufferSize)
            // TODO has info object to change if format or buffer size are changed later?
            /* format, bufferSize */);
    }

    @Override
    protected void openImpl() {
        logger.log(Level.TRACE, "EsdTargetDataLine.openImpl(): called.");

        // Checks that a format is set.
        // Sets the buffer size to a default value if not
        // already set.
        checkOpen();
        AudioFormat format = getFormat();
        AudioFormat.Encoding encoding = format.getEncoding();
        boolean bigEndian = format.isBigEndian();
        swapBytes = false;
        if (format.getSampleSizeInBits() == 16 && bigEndian) {
            swapBytes = true;
            bigEndian = false;
        } else if (format.getSampleSizeInBits() == 8 && encoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
            swapBytes = true;
            encoding = AudioFormat.Encoding.PCM_UNSIGNED;
        }
        if (swapBytes) {
            format = new AudioFormat(encoding,
                    format.getSampleRate(),
                    format.getSampleSizeInBits(),
                    format.getChannels(),
                    format.getFrameSize(),
                    format.getFrameRate(),
                    bigEndian);
            bytesPerSample = format.getFrameSize() / format.getChannels();
        }
        int outFormat = Esd.ESD_STREAM | Esd.ESD_PLAY | EsdUtils.getEsdFormat(format);
        esdStream = new EsdRecordingStream();
        esdStream.open(outFormat, (int) format.getSampleRate());
    }

//    public void start() {
//        setStarted(true);
//        setActive(true);
//        if (TDebug.TraceSourceDataLine) {
//logger.log(Level.TRACE, "channel started.");
//        }
//    }
//
//    public void stop() {
//        setStarted(false);
//    }

    @Override
    public int available() {
        // TODO
        return -1;
//        return m_nAvailable;
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
        int bytesRead = esdStream.read(data, offset, length);
        logger.log(Level.TRACE, "read (bytes): " + bytesRead);

        if (swapBytes && bytesRead > 0) {
            TConversionTool.swapOrder16(data, originalOffset, bytesRead / 2);
        }
        return bytesRead;
    }

    @Override
    public void closeImpl() {
        esdStream.close();
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
     * gain is logarithmic!!
     */
    protected void setGain(float gain) {
    }

    public class EsdTargetDataLineGainControl extends FloatControl {

        private static final float MAX_GAIN = 90.0F;
        private static final float MIN_GAIN = -96.0F;

        // TODO recheck this value
        private static final int GAIN_INCREMENTS = 1000;

//        private float	gain;
//        private boolean muted;

        /* package */ EsdTargetDataLineGainControl() {
            super(FloatControl.Type.VOLUME, // or MASTER_GAIN ?
                    -96.0F, // MIN_GAIN,
                    24.0F,  // MAX_GAIN,
                    0.01F,  // precision
                    0,      // update period?
                    0.0F,   // initial value
                    "dB",
                    "-96.0",
                    "",
                    "+24.0");
//            muted = false; // should be included in a compund control?
        }

        @Override
        public void setValue(float gain) {
            gain = Math.max(Math.min(gain, getMaximum()), getMinimum());
            if (Math.abs(gain - getValue()) > 1.0E9) {
                super.setValue(gain);
//                if (!getMute()) {
                EsdTargetDataLine.this.setGain(getValue());
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
//                    EsdTargetDataLine.this.setGain(getMinimum());
//                } else {
//                    EsdTargetDataLine.this.setGain(getGain());
//                }
//            }
//        }
    }
}
