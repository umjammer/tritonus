/*
 *  Copyright (c) 1999 - 2001 by Matthias Pfisterer
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

package org.tritonus.sampled.mixer.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collection;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;

import org.tritonus.lowlevel.alsa.Alsa;
import org.tritonus.lowlevel.alsa.AlsaPcm;
import org.tritonus.lowlevel.alsa.AlsaPcmHWParams;
import org.tritonus.lowlevel.alsa.AlsaPcmSWParams;
import org.tritonus.share.sampled.mixer.TBaseDataLine;

import static java.lang.System.getLogger;


public abstract class AlsaBaseDataLine extends TBaseDataLine {

    private static final Logger logger = getLogger("org.tritonus.TraceSourceDataLine");

//    private static final Class[] CONTROL_CLASSES = {GainControl.class};

    private AlsaPcm alsaPcm;
    private boolean swapBytes;

    /**
     * Only used if swapBytes is true.
     */
    private int bytesPerSample;

    public AlsaBaseDataLine(AlsaDataLineMixer mixer, DataLine.Info info) throws LineUnavailableException {
        super(mixer, info);
        logger.log(Level.TRACE, "begin");

        logger.log(Level.TRACE, "end");
    }

    public AlsaBaseDataLine(AlsaDataLineMixer mixer, DataLine.Info info, Collection<?> controls)
            throws LineUnavailableException {
        super(mixer, info);
        logger.log(Level.TRACE, "begin");

        logger.log(Level.TRACE, "end");
    }

    protected AlsaDataLineMixer getAlsaDataLineMixer() {
        return (AlsaDataLineMixer) getMixer();
    }

    protected AlsaPcm getAlsaPcm() {
        return alsaPcm;
    }

    /**
     * Returns the ALSA stream type of this line.
     * Subclasses must implement this method to return either
     * AlsaPcm.SND_PCM_STREAM_PLAYBACK or
     * AlsaPcm.SND_PCM_STREAM_CAPTURE.
     * The return value is used by this class to decide if it has
     * to deal with a source (PLAYBACK) or target (CAPTURE)
     * data line.
     */
    protected abstract int getAlsaStreamType();

    protected boolean getSwapBytes() {
        return swapBytes;
    }

    protected int getBytesPerSample() {
        return bytesPerSample;
    }

    @Override
    protected void openImpl() throws LineUnavailableException {
        logger.log(Level.TRACE, "begin");

        // Checks that a format is set.
        // Sets the buffer size to a default value if not
        // already set.
        checkOpen();
        AudioFormat format = getFormat();
        logger.log(Level.TRACE, "input format: " + format);

        // hack, only true for pmac
        boolean hwBigEndian = false;

        AudioFormat.Encoding encoding = format.getEncoding();
        boolean bigEndian = format.isBigEndian();
        swapBytes = false;
        if (format.getSampleSizeInBits() == 16 && bigEndian != hwBigEndian) {
            swapBytes = true;
            bigEndian = hwBigEndian;
        } else if (format.getSampleSizeInBits() == 8 && encoding.equals(AudioFormat.Encoding.PCM_SIGNED)) {
            swapBytes = true;
            encoding = AudioFormat.Encoding.PCM_UNSIGNED;
        }
        if (getSwapBytes()) {
            format = new AudioFormat(encoding,
                    format.getSampleRate(),
                    format.getSampleSizeInBits(),
                    format.getChannels(),
                    format.getFrameSize(),
                    format.getFrameRate(),
                    bigEndian);
            logger.log(Level.TRACE, "output format: " + format);

            bytesPerSample = format.getFrameSize() / format.getChannels();
        }
        int alsaOutFormat = AlsaUtils.getAlsaFormat(format);
        logger.log(Level.TRACE, "ALSA output format: " + alsaOutFormat);

        if (alsaOutFormat == AlsaPcm.SND_PCM_FORMAT_UNKNOWN) {
            throw new IllegalArgumentException("unsupported format");
        }

        try {
            alsaPcm = new AlsaPcm(getAlsaDataLineMixer().getPcmName(), getAlsaStreamType(), 0); // no special mode
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);

            throw new LineUnavailableException();
        }
        AlsaPcmHWParams hwParams = new AlsaPcmHWParams();
        int ret = alsaPcm.getAnyHWParams(hwParams);
        if (ret != 0) {
            logger.log(Level.TRACE, "getAnyHWParams(): " + Alsa.getStringError(ret));
            throw new LineUnavailableException(Alsa.getStringError(ret));
        }
        ret = alsaPcm.setHWParamsAccess(hwParams, AlsaPcm.SND_PCM_ACCESS_RW_INTERLEAVED);
        if (ret != 0) {
            logger.log(Level.TRACE, "setHWParamsFormat(): " + Alsa.getStringError(ret));
            throw new LineUnavailableException(Alsa.getStringError(ret));
        }
        ret = alsaPcm.setHWParamsFormat(hwParams, alsaOutFormat);
        if (ret != 0) {
            logger.log(Level.TRACE, "setHWParamsFormat(): " + Alsa.getStringError(ret));
            throw new LineUnavailableException(Alsa.getStringError(ret));
        }
        ret = alsaPcm.setHWParamsChannels(hwParams, format.getChannels());
        if (ret != 0) {
            logger.log(Level.TRACE, "setHWParamsChannels(): " + Alsa.getStringError(ret));
            throw new LineUnavailableException(Alsa.getStringError(ret));
        }
        ret = alsaPcm.setHWParamsRateNear(hwParams, (int) format.getSampleRate());
//        int rate = ret;
        if (ret < 0) {
            logger.log(Level.TRACE, "setHWParamsRateNear(): " + Alsa.getStringError(ret));
            throw new LineUnavailableException(Alsa.getStringError(ret));
        }
        ret = alsaPcm.setHWParamsBufferTimeNear(hwParams, 500000);
        int bufferTime = ret;
        if (ret < 0) {
            logger.log(Level.TRACE, "setHWParamsBufferTimeNear(): " + Alsa.getStringError(ret));
            throw new LineUnavailableException(Alsa.getStringError(ret));
        }
        ret = alsaPcm.setHWParamsPeriodTimeNear(hwParams, bufferTime / 4);
//        int periodTime = ret;
        if (ret < 0) {
            logger.log(Level.TRACE, "setHWParamsPeriodTimeNear(): " + Alsa.getStringError(ret));
            throw new LineUnavailableException(Alsa.getStringError(ret));
        }
        ret = alsaPcm.setHWParams(hwParams);
        if (ret < 0) {
            logger.log(Level.TRACE, "setHWParams(): " + Alsa.getStringError(ret));
            throw new LineUnavailableException(Alsa.getStringError(ret));
        }
        int chunkSize = hwParams.getPeriodSize(null);
        int bufferSize = hwParams.getBufferSize();
        if (chunkSize == bufferSize) {
            throw new LineUnavailableException("period size is equal to buffer size");
        }

        AlsaPcmSWParams swParams = new AlsaPcmSWParams();
        ret = alsaPcm.getSWParams(swParams);
        if (ret != 0) {
            logger.log(Level.TRACE, "getSWParams(): " + Alsa.getStringError(ret));
            throw new LineUnavailableException(Alsa.getStringError(ret));
        }
        ret = alsaPcm.setSWParamsSleepMin(swParams, 0);
        if (ret != 0) {
            logger.log(Level.TRACE, "setSWParamsSleepMin(): " + Alsa.getStringError(ret));
            throw new LineUnavailableException(Alsa.getStringError(ret));
        }
        ret = alsaPcm.setSWParamsXrunMode(swParams, AlsaPcm.SND_PCM_XRUN_NONE);
        if (ret != 0) {
            logger.log(Level.TRACE, "setSWParamsXrunMode(): " + Alsa.getStringError(ret));
            throw new LineUnavailableException(Alsa.getStringError(ret));
        }
        ret = alsaPcm.setSWParamsAvailMin(swParams, chunkSize);
        if (ret != 0) {
            logger.log(Level.TRACE, "setSWParamsAvailMin(): " + Alsa.getStringError(ret));
            throw new LineUnavailableException(Alsa.getStringError(ret));
        }
        long startThreshold = (long) ((double) format.getFrameRate() * 1 / 1000000);
        ret = alsaPcm.setSWParamsStartThreshold(swParams, (int) startThreshold);
        if (ret != 0) {
            logger.log(Level.TRACE, "setSWParamsStartThreshold(): " + Alsa.getStringError(ret));
            throw new LineUnavailableException(Alsa.getStringError(ret));
        }
        long stopThreshold = (long) (bufferSize + (double) format.getFrameRate() * 0 / 1000000);
        ret = alsaPcm.setSWParamsStopThreshold(swParams, (int) stopThreshold);
        if (ret != 0) {
            logger.log(Level.TRACE, "setSWParamsStopThreshold(): " + Alsa.getStringError(ret));
            throw new LineUnavailableException(Alsa.getStringError(ret));
        }
        // omitted: xfer_align
        ret = alsaPcm.setSWParams(swParams);
        if (ret != 0) {
            logger.log(Level.TRACE, "setSWParams(): " + Alsa.getStringError(ret));
            throw new LineUnavailableException(Alsa.getStringError(ret));
        }
        logger.log(Level.TRACE, "end");
    }

    @Override
    protected void closeImpl() {
        logger.log(Level.TRACE, "begin");

        alsaPcm.close();

        logger.log(Level.TRACE, "end");
    }

//    public void start() {
//        setStarted(true);
//        setActive(true);
//        if (TDebug.TraceSourceDataLine) {
//logger.log(Level.TRACE, "AlsaBaseDataLine.start(): channel started.");
//        }
//    }

    @Override
    protected void stopImpl() {
        logger.log(Level.TRACE, "called");

        int ret = 0;
//        int ret = alsaPcm.flushChannel(AlsaPcm.SND_PCM_CHANNEL_PLAYBACK);
        if (ret != 0) {
            logger.log(Level.TRACE, "flushChannel: " + Alsa.getStringError(ret));
        }
//        setStarted(false);
    }

    @Override
    public int available() {
        // TODO
        return -1;
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
     * gain is logarithmic!!
     */
    protected void setGain(float gain) {
    }

    // IDEA: move inner classes to TBaseDataLine
    public class AlsaBaseDataLineGainControl extends FloatControl {

        private static final float MAX_GAIN = 90.0F;
        private static final float MIN_GAIN = -96.0F;

        // TODO recheck this value
        private static final int GAIN_INCREMENTS = 1000;

//        private float gain;
//        private boolean muted;

        /* package */ AlsaBaseDataLineGainControl() {
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
//            muted = false; // should be included in a compund control?
        }

        @Override
        public void setValue(float gain) {
            gain = Math.max(Math.min(gain, getMaximum()), getMinimum());
            if (Math.abs(gain - getValue()) > 1.0E9) {
                super.setValue(gain);
//                if (!getMute()) {
                AlsaBaseDataLine.this.setGain(getValue());
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
//                    AlsaBaseDataLine.this.setGain(getMinimum());
//                } else {
//                    AlsaBaseDataLine.this.setGain(getGain());
//                }
//            }
//        }
    }
}
