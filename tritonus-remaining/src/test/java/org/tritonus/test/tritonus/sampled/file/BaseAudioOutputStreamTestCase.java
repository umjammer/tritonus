/*
 *  Copyright (c) 2001 - 2002 by Matthias Pfisterer
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

package org.tritonus.test.tritonus.sampled.file;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import org.junit.jupiter.api.Test;
import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TDataOutputStream;
import org.tritonus.share.sampled.file.TNonSeekableDataOutputStream;
import org.tritonus.share.sampled.file.TSeekableDataOutputStream;
import org.tritonus.test.Util;

import static java.lang.System.getLogger;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * TEST:
 * - length (not) given/ (not) seekable
 * - illegal cases should throw exception
 * - formats:
 *
 * signed 16 bit
 * signed 24 bit
 * signed 32 bit
 * unsigned 8 bit
 * signed 8 bit? check for exception?
 * always: stereo and mono
 *
 * either big or little, depending on file format
 * - illegal endianess should throw exception
 */
public abstract class BaseAudioOutputStreamTestCase {

    private static final Logger logger = getLogger(BaseAudioOutputStreamTestCase.class.getName());

    /**
     * List of sample rates that are used for testing.
     */
    private static final int[] SAMPLE_RATES = {
            8000, 11025, 12000,
            16000, 22050, 24000,
            32000, 44100, 48000,
            96000, 192000
    };

    /**
     * List of sample sizes that are used for testing.
     */
    private static final int[] SAMPLE_SIZES = {
            8, 16, 24, 32
    };

    /**
     * List of (number of) channels that are used for testing.
     */
    private static final int[] CHANNEL_COUNTS = {1, 2};

    private File file;
    private ByteArrayOutputStream baos;

    // non-seekable, given length
    @Test
    public void testAOS1() throws Exception {
        doTest(false, true);
    }

    // non-seekable, unknown length
    @Test
    public void testAOS2() throws Exception {
        doTest(false, false);
    }

    // seekable, given length
    @Test
    public void testAOS3() throws Exception {
        doTest(true, true);
    }

    // seekable, unknown length
    @Test
    public void testAOS4() throws Exception {
        doTest(true, false);
    }

    private void doTest(boolean seekable, boolean lengthGiven) throws Exception {
        for (int sampleRate : SAMPLE_RATES) {
            logger.log(Level.DEBUG, "sample rate: " + sampleRate);
            for (int sampleSize : SAMPLE_SIZES) {
                logger.log(Level.DEBUG, "sample size: " + sampleSize);
                for (int channelCount : CHANNEL_COUNTS) {
                    logger.log(Level.DEBUG, "sample size: " + channelCount);
                    boolean signed = !(sampleSize == 8 && is8bitUnsigned());
                    var audioFormat = new AudioFormat(sampleRate, sampleSize, channelCount, signed, getBigEndian());
                    logger.log(Level.DEBUG, "AudioFormat: " + audioFormat);
                    doTest(audioFormat, seekable, lengthGiven);
                }
            }
        }
    }

    private void doTest(AudioFormat audioFormat, boolean seekable, boolean lengthGiven) throws Exception {
        byte[] data = createAudioData(audioFormat.getFrameSize());
        int statedLength;
        if (lengthGiven) {
            statedLength = data.length;
        } else {
            statedLength = AudioSystem.NOT_SPECIFIED;
        }
        AudioOutputStream aos = createAudioOutputStream(audioFormat, statedLength, seekable);
        aos.write(data, 0, data.length);
        aos.close();
        byte[] expectedHeaderData = getExpectedHeaderData(audioFormat, data.length, seekable, lengthGiven);
        byte[] resultingData = getWrittenData(seekable);
        if (logger.isLoggable(Level.DEBUG)) {
            logger.log(Level.DEBUG, "expected:");
            Util.dumpByteArray(expectedHeaderData);
            logger.log(Level.DEBUG, "actual:");
            Util.dumpByteArray(resultingData);
        }
        boolean headerDataOk = Util.compareByteArrays(expectedHeaderData, 0, resultingData, 0, expectedHeaderData.length);
        logger.log(Level.DEBUG, "headerok: " + headerDataOk);
        assertTrue(headerDataOk, "header data");
        assertTrue(Util.compareByteArrays(data, 0, resultingData, expectedHeaderData.length + getExpectedAdditionalHeaderLength(), data.length), "audio data");
        if (file != null) {
            file.delete();
            file = null;
        }
    }

    private static byte[] createAudioData(int frameSize) {
        byte[] data = new byte[8 * frameSize];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        return data;
    }

    private TDataOutputStream createDataOutputStream(boolean seekable) throws Exception {
        TDataOutputStream dataOutputStream;
        if (seekable) {
            file = File.createTempFile("aos", "au");
            dataOutputStream = new TSeekableDataOutputStream(file);
        } else {
            baos = new ByteArrayOutputStream();
            dataOutputStream = new TNonSeekableDataOutputStream(baos);
        }
        return dataOutputStream;
    }

    private AudioOutputStream createAudioOutputStream(
            AudioFormat audioFormat, long length, boolean seekable) throws Exception {
        TDataOutputStream dataOutputStream = createDataOutputStream(seekable);
        return createAudioOutputStreamImpl(audioFormat, length, dataOutputStream);
    }

    protected abstract AudioOutputStream createAudioOutputStreamImpl(
            AudioFormat audioFormat, long length, TDataOutputStream dataOutputStream) throws Exception;

    private byte[] getWrittenData(boolean seekable) throws Exception {
        byte[] resultingData;
        if (seekable) {
            resultingData = Util.getByteArrayFromFile(file);
        } else {
            resultingData = baos.toByteArray();
        }
        return resultingData;
    }

    protected abstract byte[] getExpectedHeaderData(AudioFormat audioFormat,
                                                    int length,
                                                    boolean seekable,
                                                    boolean lengthGiven);

    protected abstract int getExpectedAdditionalHeaderLength();

    protected abstract boolean getBigEndian();

    protected abstract boolean is8bitUnsigned();
}
