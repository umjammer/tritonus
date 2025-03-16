/*
 * BaseAudioFileReaderTestCase.java
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

package org.tritonus.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.AudioFileReader;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.tritonus.share.sampled.AudioFileTypes;
import org.tritonus.share.sampled.Encodings;

import static org.junit.jupiter.api.Assertions.assertEquals;


@Disabled
public class BaseAudioFileReaderTestCase extends BaseProviderTestCase {

    private static final String RESOURCE_BASENAME = "audiofilereader";
    private static final String PROVIDER_PREFIX = "(Provider:) ";
    private static final String AUDIOSYSTEM_PREFIX = "(AudioSystem:) ";

    private boolean checkRealLengths;

    public BaseAudioFileReaderTestCase() {
        super(RESOURCE_BASENAME);
        setCheckRealLengths(true);
    }

    protected void setCheckRealLengths(boolean checkRealLengths) {
        this.checkRealLengths = checkRealLengths;
    }

    private boolean getCheckRealLengths() {
        return checkRealLengths;
    }

    protected AudioFileReader getAudioFileReader() {
        return (AudioFileReader) getProvider();
    }

    @Test
    public void testAudioFileFormatFile() throws Exception {
        File file = new File("src/test/resources/" + getFilename());
        AudioFileFormat audioFileFormat;
        if (getTestProvider()) {
            audioFileFormat = getAudioFileReader().getAudioFileFormat(file);
            checkAudioFileFormat(audioFileFormat, true, true);
        }
        if (getTestAudioSystem()) {
            audioFileFormat = AudioSystem.getAudioFileFormat(file);
            checkAudioFileFormat(audioFileFormat, true, false);
        }
    }

    @Test
    public void testAudioFileFormatURL() throws Exception {
        URL url = new URL("file:" + "src/test/resources/" + getFilename());
        AudioFileFormat audioFileFormat;
        if (getTestProvider()) {
            audioFileFormat = getAudioFileReader().getAudioFileFormat(url);
            checkAudioFileFormat(audioFileFormat, false, true);
        }
        if (getTestAudioSystem()) {
            audioFileFormat = AudioSystem.getAudioFileFormat(url);
            checkAudioFileFormat(audioFileFormat, false, false);
        }
    }

    @Test
    public void testAudioFileFormatInputStream() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/" + getFilename());
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        AudioFileFormat audioFileFormat;
        if (getTestProvider()) {
            audioFileFormat = getAudioFileReader().getAudioFileFormat(bufferedInputStream);
            checkAudioFileFormat(audioFileFormat, false, true);
        }
        inputStream = Files.newInputStream(Paths.get("src/test/resources/" + getFilename()));
        bufferedInputStream = new BufferedInputStream(inputStream);
        if (getTestAudioSystem()) {
            audioFileFormat = AudioSystem.getAudioFileFormat(bufferedInputStream);
            checkAudioFileFormat(audioFileFormat, false, false);
        }
    }

    @Test
    public void testAudioInputStreamFile() throws Exception {
        File file = new File("src/test/resources/" + getFilename());
        AudioInputStream audioInputStream;
        if (getTestProvider()) {
            audioInputStream = getAudioFileReader().getAudioInputStream(file);
            checkAudioInputStream(audioInputStream, true, true);
        }
        if (getTestAudioSystem()) {
            audioInputStream = AudioSystem.getAudioInputStream(file);
            checkAudioInputStream(audioInputStream, true, false);
        }
    }

    @Test
    public void testAudioInputStreamURL() throws Exception {
        URL url = new URL("file:" + "src/test/resources/" + getFilename());
        AudioInputStream audioInputStream;
        if (getTestProvider()) {
            audioInputStream = getAudioFileReader().getAudioInputStream(url);
            checkAudioInputStream(audioInputStream, false, true);
        }
        if (getTestAudioSystem()) {
            audioInputStream = AudioSystem.getAudioInputStream(url);
            checkAudioInputStream(audioInputStream, false, false);
        }
    }

    @Test
    public void testAudioInputStreamInputStream() throws Exception {
        InputStream inputStream = getClass().getResourceAsStream("/" + getFilename());
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        AudioInputStream audioInputStream;
        if (getTestProvider()) {
            audioInputStream = getAudioFileReader().getAudioInputStream(bufferedInputStream);
            checkAudioInputStream(audioInputStream, false, true);
        }
        inputStream = Files.newInputStream(Paths.get("src/test/resources/" + getFilename()));
        bufferedInputStream = new BufferedInputStream(inputStream);
        if (getTestAudioSystem()) {
            audioInputStream = AudioSystem.getAudioInputStream(bufferedInputStream);
            checkAudioInputStream(audioInputStream, false, false);
        }
    }

    private void checkAudioFileFormat(AudioFileFormat audioFileFormat,
                                      boolean realLengthExpected,
                                      boolean providerDirect) throws Exception {
        if (providerDirect) {
            checkAudioFileFormat(audioFileFormat, realLengthExpected, PROVIDER_PREFIX);
        } else {
            checkAudioFileFormat(audioFileFormat, realLengthExpected, AUDIOSYSTEM_PREFIX);
        }
    }

    private void checkAudioFileFormat(AudioFileFormat audioFileFormat,
                                      boolean realLengthExpected,
                                      String messagePrefix) throws Exception {
        assertEquals(getType(), audioFileFormat.getType(), messagePrefix + "type");
        checkAudioFormat(audioFileFormat.getFormat(), messagePrefix);
        if (getCheckRealLengths() || realLengthExpected) {
            long expectedByteLength = getByteLength();
            long expectedFrameLength = getFrameLength();
            assertEquals(expectedByteLength, audioFileFormat.getByteLength(), messagePrefix + "byte length");
            assertEquals(expectedFrameLength, audioFileFormat.getFrameLength(), messagePrefix + "frame length");
        }
    }

    private void checkAudioInputStream(AudioInputStream audioInputStream,
                                       boolean realLengthExpected,
                                       boolean providerDirect) throws Exception {
        if (providerDirect) {
            checkAudioInputStream(audioInputStream, realLengthExpected, PROVIDER_PREFIX);
        } else {
            checkAudioInputStream(audioInputStream, realLengthExpected, AUDIOSYSTEM_PREFIX);
        }
    }

    private void checkAudioInputStream(AudioInputStream audioInputStream,
                                       boolean realLengthExpected,
                                       String messagePrefix) throws Exception {
        checkAudioFormat(audioInputStream.getFormat(), messagePrefix);
        long expectedFrameLength = AudioSystem.NOT_SPECIFIED;
        if (getCheckRealLengths() || realLengthExpected) {
            expectedFrameLength = getFrameLength();
            assertEquals(expectedFrameLength, audioInputStream.getFrameLength(), messagePrefix + "frame length");
        }
        if (getCheckRealLengths() || realLengthExpected) {
            int expectedDataLength = (int) (expectedFrameLength * getFrameSize());
            byte[] retrievedData = new byte[expectedDataLength];
            int read = audioInputStream.read(retrievedData);
            if (read == -1) {
                read = 0;
            } // EOF
            assertEquals(expectedDataLength, read, messagePrefix + "reading data");
// 			  for (int i = 0; i < expectedDataLength; i++) {
// 			       assertEquals(messagePrefix + "data content", 0, retrievedData[i]);
// 			  }
        } else {
            // TODO try to at least read some bytes?
        }
    }

    private void checkAudioFormat(AudioFormat audioFormat, String messagePrefix) throws Exception {
        assertEquals(getEncoding(), audioFormat.getEncoding(), messagePrefix + "encoding");
        assertEquals(getSampleRate(), audioFormat.getSampleRate(), DELTA, messagePrefix + "sample rate");
        assertEquals(getSampleSizeInBits(), audioFormat.getSampleSizeInBits(), messagePrefix + "sample size (bits)");
        assertEquals(getChannels(), audioFormat.getChannels(), messagePrefix + "channels");
        assertEquals(getFrameSize(), audioFormat.getFrameSize(), messagePrefix + "frame size");
        assertEquals(getFrameRate(), audioFormat.getFrameRate(), DELTA, messagePrefix + "frame rate");
        assertEquals(getBigEndian(), audioFormat.isBigEndian(), messagePrefix + "big endian");
    }

    private String getFilename() {
        String fileName = getResourceString(getResourcePrefix() + ".filename");
        return fileName;
    }

    private AudioFileFormat.Type getType() {
        String typeName = getResourceString(getResourcePrefix() + ".type");
        AudioFileFormat.Type type = AudioFileTypes.getType(typeName);
        if (type == null) {
            type = new AudioFileFormat.Type(typeName, getResourcePrefix());
        }
        return type;
    }

    private long getByteLength() {
        String _byteLength = getResourceString(getResourcePrefix() + ".byteLength");
        long byteLength = Long.parseLong(_byteLength);
        return byteLength;
    }

    private AudioFormat.Encoding getEncoding() {
        String encodingName = getResourceString(getResourcePrefix() + ".format.encoding");
        AudioFormat.Encoding encoding = Encodings.getEncoding(encodingName);
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
}
