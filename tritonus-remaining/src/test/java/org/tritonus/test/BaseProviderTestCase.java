/*
 * BaseProviderTestCase.java
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

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;

import org.junit.jupiter.api.BeforeEach;
import org.tritonus.share.sampled.AudioFileTypes;
import org.tritonus.share.sampled.Encodings;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class BaseProviderTestCase {

    /**
     * Precision for float comparisons.
     */
    protected static final float DELTA = 0.1F;

    private final ResourceBundle resourceBundle;
    private String resourcePrefix;
    private Object provider;
    private boolean checkRealLengths;

    public BaseProviderTestCase(String resourceBasename) {
        resourceBundle = loadResourceBundle(resourceBasename);
    }

    protected void setResourcePrefix(String resourcePrefix) {
        this.resourcePrefix = resourcePrefix;
    }

    protected String getResourcePrefix() {
        return resourcePrefix;
    }

    @BeforeEach
    protected void setUp() throws Exception {
        if (getTestProvider()) {
            String className = getClassName();
            Class<?> cls = Class.forName(className);
            provider = cls.getDeclaredConstructor().newInstance();
        }
    }

    protected Object getProvider() {
        return provider;
    }

    protected boolean getTestProvider() {
        return true;
    }

    protected boolean getTestAudioSystem() {
        return true;
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

    private ResourceBundle loadResourceBundle(String resourceBasename) {
        ResourceBundle resourceBundle = null;
        try {
            resourceBundle = ResourceBundle.getBundle(resourceBasename);
        } catch (MissingResourceException e) {
            e.printStackTrace(System.err);
        }
        return resourceBundle;
    }

    protected String getResourceString(String key) {
        return resourceBundle.getString(key);
    }

    private String getClassName() {
        String className = getResourceString(getResourcePrefix() + ".class");
        return className;
    }

    private String getFilename() {
        String fileName = getResourceString(getResourcePrefix() + ".filename");
        return fileName;
    }

    private AudioFileFormat.Type getType() {
        String typeName = getResourceString(getResourcePrefix() + ".type");
        AudioFileFormat.Type type = AudioFileTypes.getType(typeName);
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
