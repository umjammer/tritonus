/*
 * WaveAudioOutputStreamTestCase.java
 */
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

import javax.sound.sampled.AudioFormat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tritonus.sampled.file.WaveAudioOutputStream;
import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TDataOutputStream;


class WaveAudioOutputStreamTestCase extends BaseAudioOutputStreamTestCase {

    private static final int EXPECTED_ADDITIONAL_HEADER_LENGTH = 0;

    @Override
    protected AudioOutputStream createAudioOutputStreamImpl(
            AudioFormat audioFormat,
            long length,
            TDataOutputStream dataOutputStream)
            throws Exception {
        return new WaveAudioOutputStream(audioFormat,
                length,
                dataOutputStream);
    }

    /**
     * nLength has to be < 255, or the implementation of this method
     * has to be changed
     */
    @Override
    protected byte[] getExpectedHeaderData(AudioFormat audioFormat,
                                           int length,
                                           boolean seekable,
                                           boolean lengthGiven) {
        int totalLength = 38 + length;
        int sampleRate = (int) audioFormat.getSampleRate();
        int bytesPerSecond = sampleRate * audioFormat.getFrameSize();
        byte[] expectedHeaderData = new byte[] {
                0x52, 0x49, 0x46, 0x46,
                (byte) totalLength, 0, 0, 0,
                0x57, 0x41, 0x56, 0x45,
                0x66, 0x6d, 0x74, 0x20,
                18, 0, 0, 0,
                1, 0, (byte) audioFormat.getChannels(), 0,
                (byte) sampleRate, (byte) (sampleRate / 256), (byte) (sampleRate / 65536), 0,
                (byte) bytesPerSecond, (byte) (bytesPerSecond / 256), (byte) (bytesPerSecond / 65536), 0,
                (byte) audioFormat.getFrameSize(), 0,
                (byte) audioFormat.getSampleSizeInBits(), 0,
                0, 0,
                0x64, 0x61, 0x74, 0x61,
                (byte) length, (byte) (length / 256), (byte) (length / 65536), 0,
        };
//        if (lengthGiven || seekable) {
//            expectedHeaderData[11] = (byte) length;
//        } else {
//            expectedHeaderData[8] = (byte) 0xff;
//            expectedHeaderData[9] = (byte) 0xff;
//            expectedHeaderData[10] = (byte) 0xff;
//            expectedHeaderData[11] = (byte) 0xff;
//        }
        return expectedHeaderData;
    }

    private static byte getEncoding(AudioFormat format) {
        // works only for simple cases
        return (byte) (format.getSampleSizeInBits() / 8 + 1);
    }

    @Override
    protected int getExpectedAdditionalHeaderLength() {
        return EXPECTED_ADDITIONAL_HEADER_LENGTH;
    }

    @Override
    protected boolean getBigEndian() {
        return false;
    }

    @Override
    protected boolean is8bitUnsigned() {
        return true;
    }

    // non-seekable, unknown length
    @Override
    @Test
    @Disabled // TODO ???
    public void testAOS2() throws Exception {
    }
}
