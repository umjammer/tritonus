/*
 * AuAudioOutputStreamTestCase.java
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

import org.tritonus.sampled.file.AuAudioOutputStream;
import org.tritonus.share.sampled.file.AudioOutputStream;
import org.tritonus.share.sampled.file.TDataOutputStream;


public class AuAudioOutputStreamTestCase
        extends BaseAudioOutputStreamTestCase {

    private static final int EXPECTED_ADDITIONAL_HEADER_LENGTH = 20;

    @Override
    protected AudioOutputStream createAudioOutputStreamImpl(
            AudioFormat audioFormat,
            long length,
            TDataOutputStream dataOutputStream)
            throws Exception {
        return new AuAudioOutputStream(audioFormat,
                length,
                dataOutputStream);
    }

    /*
      nLength has to be < 255, or the implementation of this method
      has to be changed
     */
    @Override
    protected byte[] getExpectedHeaderData(AudioFormat audioFormat,
                                           int length,
                                           boolean seekable,
                                           boolean lengthGiven) {
        int sampleRate = (int) audioFormat.getSampleRate();
        byte[] expectedHeaderData = new byte[] {
                0x2e, 0x73, 0x6e, 0x64,
                0, 0, 0, (byte) (24 + getExpectedAdditionalHeaderLength()),
                0, 0, 0, 0, // <-- not yet populated
                0, 0, 0, getEncoding(audioFormat),
                0, (byte) (sampleRate / 65536), (byte) (sampleRate / 256), (byte) sampleRate,
                0, 0, 0, (byte) audioFormat.getChannels()
        };
        if (lengthGiven || seekable) {
            expectedHeaderData[11] = (byte) length;
        } else {
            expectedHeaderData[8] = (byte) 0xff;
            expectedHeaderData[9] = (byte) 0xff;
            expectedHeaderData[10] = (byte) 0xff;
            expectedHeaderData[11] = (byte) 0xff;
        }
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
        return true;
    }

    @Override
    protected boolean is8bitUnsigned() {
        return false;
    }
}
