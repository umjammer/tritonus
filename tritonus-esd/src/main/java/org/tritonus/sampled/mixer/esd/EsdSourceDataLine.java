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
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.tritonus.lowlevel.esd.Esd;
import org.tritonus.lowlevel.esd.EsdStream;
import org.tritonus.share.sampled.TConversionTool;
import org.tritonus.share.sampled.mixer.TBaseDataLine;
import org.tritonus.share.sampled.mixer.TMixer;

import static java.lang.System.getLogger;


public class EsdSourceDataLine extends TBaseDataLine implements SourceDataLine {

    private static final Logger logger = getLogger("org.tritonus.TraceSourceDataLine");

    private EsdStream esdStream;
    private boolean swapBytes;
    private byte[] swapBuffer;

    /*
     * Only used if swapBytes is true.
     */
    private int bytesPerSample;

    /*
     * Used to store the muted state.
     */
    private boolean muted;

    /*
     * Used to store the gain while the channel is muted.
     */
    private float gain;

    /*
     * Used to store the pan while the channel is muted.
     */
    private float pan;

    // TODO has info object to change if format or buffer size are changed later?
    //  no, but it has to represent the mixer's capabilities. So a fixed info per mixer.
    public EsdSourceDataLine(TMixer mixer, AudioFormat format, int bufferSize) throws LineUnavailableException {
        super(mixer, new DataLine.Info(SourceDataLine.class, format, bufferSize));
        addControl(new EsdSourceDataLineGainControl());
        addControl(new EsdSourceDataLinePanControl());
        addControl(new EsdSourceDataLineMuteControl());

//logger.log(Level.TRACE, "EsdSourceDataLine.<init>(): buffer size: " + bufferSize);
    }

    @Override
    protected void openImpl() {
        logger.log(Level.TRACE, "EsdSourceDataLine.openImpl(): called.");

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
        // Ugly hack, should fade as soon as possible.
        // IDEA: if swapping IS necessary here, isolate the "detection" of
        // big-endian architectures into a seperate class. Perhaps have a
        // property with a list of big-endian architecture names, so that
        // support can be extended to other architectures without changes
        // in the source code.
        // TODO does 8 bit work? (perhaps problem inside esd?)
        if (System.getProperty("os.arch").equals("ppc") && format.getSampleSizeInBits() == 16) {
            swapBytes ^= true;
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

        esdStream = new EsdStream();
        esdStream.open(outFormat, (int) format.getSampleRate());
    }

    @Override
    public int available() {
        // TODO
        return -1;
    }

    // TODO check if should block
    @Override
    public int write(byte[] data, int offset, int length) {
        logger.log(Level.TRACE, "called.");

        if (swapBytes) {
            if (swapBuffer == null || swapBuffer.length < offset + length) {
                swapBuffer = new byte[offset + length];
            }
            TConversionTool.changeOrderOrSign(data, offset, swapBuffer, offset, length, bytesPerSample);
            data = swapBuffer;
        }
        if (length > 0 && !isActive()) {
            start();
        }
        int remaining = length;
        while (remaining > 0 && isOpen()) {
            synchronized (this) {
//        while ((availableWrite() == 0 || isPaused()) && isOpen()) {
//            try {
//                wait();
//            } catch (InterruptedException e) {
//logger.log(Level.ERROR, e.getMessage(), e);
//            }
//        }
                if (!isOpen()) {
                    return length - remaining;
                }
                // TODO check return
                int written = esdStream.write(data, offset, remaining);
                offset += written;
                remaining -= written;
            }
        }
        return length;
    }

    @Override
    protected void closeImpl() {
        logger.log(Level.TRACE, "called.");

        esdStream.close();
    }

    @Override
    public void drain() {
        logger.log(Level.TRACE, "called.");

        // TODO
    }

    @Override
    public void flush() {
        logger.log(Level.TRACE, "called.");

        // TODO
    }

    /**
     * gain is logarithmic!!
     */
    protected void setGain(float gain) {
        logger.log(Level.TRACE, "gain: " + gain);

        this.gain = gain;
        if (!muted) {
            setGainImpl();
        }
    }

    /**
     *
     */
    protected void setPan(float pan) {
        logger.log(Level.TRACE, "pan: " + pan);

        this.pan = pan;
        if (!muted) {
            setGainImpl();
        }
    }

    /**
     *
     */
    protected void setMuted(boolean muted) {
        logger.log(Level.TRACE, "muted: " + muted);

        this.muted = muted;
        if (this.muted) {
            // esdStream.setVolume(0, 0);
        } else {
            setGainImpl();
        }
    }

    /**
     *
     */
    private void setGainImpl() {
        logger.log(Level.TRACE, "called: ");

//        float leftDb = gain + pan * 15.0F;
//        float rightDb = gain - pan * 15.0F;
//        float leftLinear = (float) TVolumeUtils.log2lin(leftDb);
//        float rightLinear = (float) TVolumeUtils.log2lin(rightDb);

// 		  esdStream.setVolume((int) (leftLinear * 256), (int) (rightLinear * 256));
    }

    // IDEA: move inner classes to TBaseDataLine
    public class EsdSourceDataLineGainControl extends FloatControl {

        private static final float MAX_GAIN = 24.0F;
        private static final float MIN_GAIN = -96.0F;

        /* package */ EsdSourceDataLineGainControl() {
            super(FloatControl.Type.MASTER_GAIN, // or VOLUME  ?
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
            logger.log(Level.TRACE, "gain: " + gain);

            float oldGain = getValue();
            super.setValue(gain);
            if (Math.abs(oldGain - getValue()) > 1.0E-9) {
                logger.log(Level.TRACE, "really changing gain");

                EsdSourceDataLine.this.setGain(getValue());
            }
        }
    }

    // IDEA: move inner classes to TBaseDataLine
    public class EsdSourceDataLinePanControl extends FloatControl {

        /* package */ EsdSourceDataLinePanControl() {
            super(FloatControl.Type.PAN,
                    -1.0F,    // MIN_GAIN,
                    1.0F,    // MAX_GAIN,
                    0.01F,    // precision
                    0,    // update period?
                    0.0F,    // initial value
                    "??",
                    "left",
                    "center",
                    "right");
        }

        @Override
        public void setValue(float pan) {
            logger.log(Level.TRACE, "pan: " + pan);

            float oldPan = getValue();
            super.setValue(pan);
            if (Math.abs(oldPan - getValue()) > 1.0E-9) {
                logger.log(Level.TRACE, "really changing pan");

                EsdSourceDataLine.this.setPan(getValue());
            }
        }
    }

    public class EsdSourceDataLineMuteControl extends BooleanControl {

        /* package */ EsdSourceDataLineMuteControl() {
            super(BooleanControl.Type.MUTE,
                    false,
                    "muted",
                    "unmuted");
        }

        @Override
        public void setValue(boolean muted) {
            logger.log(Level.TRACE, "muted: " + muted);

            if (muted != getValue()) {
                logger.log(Level.TRACE, "really changing mute status");

                super.setValue(muted);
                EsdSourceDataLine.this.setMuted(getValue());
            }
        }

//        public boolean getMute() {
//            return muted;
//        }
//
//        public void setMute(boolean mutes) {
//            if (mutes != getMute()) {
//                this.muted = mutes;
//                if (getMute()) {
//                    EsdSourceDataLine.this.setGain(getMinimum());
//                } else {
//                    EsdSourceDataLine.this.setGain(getGain());
//                }
//            }
//        }
    }
}
