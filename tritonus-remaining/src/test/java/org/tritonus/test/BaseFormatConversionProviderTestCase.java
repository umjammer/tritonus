/*
 *  Copyright (c) 2001 - 2003 by Matthias Pfisterer
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

package org.tritonus.test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.FormatConversionProvider;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tritonus.share.sampled.Encodings;

import static javax.sound.sampled.AudioFormat.Encoding.ALAW;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED;
import static javax.sound.sampled.AudioFormat.Encoding.ULAW;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * BaseAudioFileReaderTestCase.java
 */
@Disabled
public class BaseFormatConversionProviderTestCase extends BaseProviderTestCase {

    private static final Encoding[] EMPTY_ENCODING_ARRAY = new Encoding[0];

    private static final Encoding[] ALL_ENCODINGS = new Encoding[] {
            PCM_SIGNED, PCM_UNSIGNED, ULAW, ALAW,
            Encodings.getEncoding("GSM0610"),
            Encodings.getEncoding("MPEG1L1"),
            Encodings.getEncoding("MPEG1L2"),
            Encodings.getEncoding("MPEG1L3"),
            Encodings.getEncoding("MPEG2L1"),
            Encodings.getEncoding("MPEG2L2"),
            Encodings.getEncoding("MPEG2L3"),
            Encodings.getEncoding("MPEG2DOT5L1"),
            Encodings.getEncoding("MPEG2DOT5L2"),
            Encodings.getEncoding("MPEG2DOT5L3"),
            Encodings.getEncoding("VORBIS"),
            Encodings.getEncoding("IMA_ADPCM")
    };

    private static final boolean DEBUG = true;
    private static final String RESOURCE_BASENAME = "formatconversionprovider";

    public BaseFormatConversionProviderTestCase() {
        super(RESOURCE_BASENAME);
    }

    protected FormatConversionProvider getFormatConversionProvider() {
        return (FormatConversionProvider) getProvider();
    }

    @Test
    public void testGetSourceEncodings() {
        AudioFormat.Encoding[] encodings;
        if (getTestProvider()) {
            encodings = getFormatConversionProvider().getSourceEncodings();
            checkEncodings(encodings, true);
        }
    }

    @Test
    public void testGetTargetEncodings() {
        AudioFormat.Encoding[] encodings;
        if (getTestProvider()) {
            encodings = getFormatConversionProvider().getTargetEncodings();
            checkEncodings(encodings, false);
        }
    }

    private void checkEncodings(AudioFormat.Encoding[] encodings, boolean source) {
        AudioFormat.Encoding[] _expectedEncodings = getEncodings(source);
        Iterator<AudioFormat.Encoding> iter;
        List<AudioFormat.Encoding> encodingList = List.of(encodings);
        List<AudioFormat.Encoding> expectedEncodings = List.of(_expectedEncodings);
        iter = encodingList.iterator();
        while (iter.hasNext()) {
            Object encoding = iter.next();
            assertTrue(expectedEncodings.contains(encoding), "returned encoding in expected encodingList");
        }
        iter = expectedEncodings.iterator();
        while (iter.hasNext()) {
            Object encoding = iter.next();
            assertTrue(encodingList.contains(encoding), "expected encoding in returned encodingList");
        }
    }

    @Test
    public void testIsSourceEncodingsSupported() {
        implTestIsEncodingSupported(true);
    }

    @Test
    public void testIsTargetEncodingsSupported() {
        implTestIsEncodingSupported(false);
    }

    private void implTestIsEncodingSupported(boolean source) {
        if (getTestProvider()) {
            AudioFormat.Encoding[] expectedEncodings = getEncodings(source);
            for (AudioFormat.Encoding expectedEncoding : expectedEncodings) {
                boolean supported;
                if (source) {
                    supported = getFormatConversionProvider().isSourceEncodingSupported(expectedEncoding);
                } else {
                    supported = getFormatConversionProvider().isTargetEncodingSupported(expectedEncoding);
                }
                assertTrue(supported, "expected encoding supported");
            }
            AudioFormat.Encoding[] unexpectedEncodings = getUnexpectedEncodings(source);
            for (AudioFormat.Encoding unexpectedEncoding : unexpectedEncodings) {
                boolean supported;
                if (source) {
                    supported = getFormatConversionProvider().isSourceEncodingSupported(unexpectedEncoding);
                } else {
                    supported = getFormatConversionProvider().isTargetEncodingSupported(unexpectedEncoding);
                }
                assertFalse(supported, "unexpected encoding supported");
            }
        }
    }

    private void checkAudioInputStream(AudioInputStream audioInputStream, boolean realLengthExpected) throws Exception {
        checkAudioFormat(audioInputStream.getFormat());
        long expectedFrameLength = AudioSystem.NOT_SPECIFIED;
        if (/*getCheckRealLengths() ||*/ realLengthExpected) {
            expectedFrameLength = getFrameLength();
        }
        assertEquals(expectedFrameLength, audioInputStream.getFrameLength(), "frame length");
        if (/*getCheckRealLengths() ||*/ realLengthExpected) {
            int expectedDataLength = (int) (expectedFrameLength * getFrameSize());
            byte[] retrievedData = new byte[expectedDataLength];
            int read = audioInputStream.read(retrievedData);
            assertEquals(expectedDataLength, read, "reading data");
// 			for (int i = 0; i < expectedDataLength; i++) {
// 				assertEquals("data content", 0, retrievedData[i]);
// 			}
        } else {
            // TODO try to at least read some bytes?
        }
    }

    private void checkAudioFormat(AudioFormat audioFormat) throws Exception {
        assertEquals(getEncoding(), audioFormat.getEncoding(), "encoding");
        assertEquals(getSampleRate(), audioFormat.getSampleRate(), DELTA, "sample rate");
        assertEquals(getSampleSizeInBits(), audioFormat.getSampleSizeInBits(), "sample size (bits)");
        assertEquals(getChannels(), audioFormat.getChannels(), "channels");
        assertEquals(getFrameSize(), audioFormat.getFrameSize(), "frame size");
        assertEquals(getFrameRate(), audioFormat.getFrameRate(), DELTA, "frame rate");
        assertEquals(getBigEndian(), audioFormat.isBigEndian(), "big endian");
    }

    private String getFilename() {
        String fileName = getResourceString(getResourcePrefix() + ".filename");
        return fileName;
    }

    private long getByteLength() {
        String _byteLength = getResourceString(getResourcePrefix() + ".byteLength");
        long byteLength = Long.parseLong(_byteLength);
        return byteLength;
    }

    private Encoding getEncoding() {
        String encodingName = getResourceString(getResourcePrefix() + ".format.encoding");
        Encoding encoding = Encodings.getEncoding(encodingName);
        return encoding;
    }

    private float getSampleRate() {
        String _sampleRate = getResourceString(getResourcePrefix() + ".format.sampleRate");
        float sampleRate = Float.parseFloat(_sampleRate);
        return sampleRate;
    }

    private int getSampleSizeInBits() {
        String _sampleSizeInBits = getResourceString(getResourcePrefix() + ".format.sampleSizeInBits");
        int sampleSizeInBits = Integer.parseInt(_sampleSizeInBits);
        return sampleSizeInBits;
    }

    private int getChannels() {
        String _channels = getResourceString(getResourcePrefix() + ".format.channels");
        int channels = Integer.parseInt(_channels);
        return channels;
    }

    private int getFrameSize() {
        String _frameSize = getResourceString(getResourcePrefix() + ".format.frameSize");
        int frameSize = Integer.parseInt(_frameSize);
        return frameSize;
    }

    private float getFrameRate() {
        String _frameRate = getResourceString(getResourcePrefix() + ".format.frameRate");
        float frameRate = Float.parseFloat(_frameRate);
        return frameRate;
    }

    private boolean getBigEndian() {
        String _bigEndian = getResourceString(getResourcePrefix() + ".format.bigEndian");
        boolean bigEndian = _bigEndian.equals("true");
        return bigEndian;
    }

    private long getFrameLength() {
        String _frameLength = getResourceString(getResourcePrefix() + ".frameLength");
        long frameLength = Long.parseLong(_frameLength);
        return frameLength;
    }

    private Encoding[] getEncodings(boolean source) {
        if (source) {
            return getEncodings("sourceEncodings");
        } else {
            return getEncodings("targetEncodings");
        }
    }

    private Encoding[] getUnexpectedEncodings(boolean source) {
        Encoding[] _expectedEncodings;
        if (source) {
            _expectedEncodings = getEncodings("sourceEncodings");
        } else {
            _expectedEncodings = getEncodings("targetEncodings");
        }
        List<Encoding> expectedEncodings = List.of(_expectedEncodings);
        Encoding[] allEncodings = ALL_ENCODINGS;
        Encoding[] unexpectedEncodings = new Encoding[allEncodings.length - _expectedEncodings.length];
        int index = 0;
        for (Encoding allEncoding : allEncodings) {
            if (!expectedEncodings.contains(allEncoding)) {
                unexpectedEncodings[index] = allEncoding;
                index++;
            }
        }
        return unexpectedEncodings;
    }

    private Encoding[] getEncodings(String key) {
        String _encodings = getResourceString(getResourcePrefix() + "." + key);
        List<Encoding> encodingsList = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(_encodings);
        while (tokenizer.hasMoreTokens()) {
            String encodingName = tokenizer.nextToken();
            Encoding encoding = Encodings.getEncoding(encodingName);
            encodingsList.add(encoding);
        }
        return encodingsList.toArray(EMPTY_ENCODING_ARRAY);
    }
}
