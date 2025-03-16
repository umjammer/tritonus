/*
 *  Copyright (c) 2000 by Matthias Pfisterer
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

package org.tritonus.share.sampled.file;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

import static java.lang.System.getLogger;
import static org.tritonus.share.sampled.AudioUtils.isPCM;
import static org.tritonus.share.sampled.TConversionTool.convertSign8;
import static org.tritonus.share.sampled.TConversionTool.swapOrder16;
import static org.tritonus.share.sampled.TConversionTool.swapOrder24;
import static org.tritonus.share.sampled.TConversionTool.swapOrder32;


/**
 * Base class for classes implementing AudioOutputStream.
 *
 * @author Matthias Pfisterer
 */
public abstract class TAudioOutputStream implements AudioOutputStream {

    private static final Logger logger = getLogger(TAudioOutputStream.class.getName());

    private final AudioFormat audioFormat;
    private long length; // in bytes
    private long calculatedLength;
    private final TDataOutputStream dataOutputStream;
    private final boolean doBackPatching;
    private boolean headerWritten;
    /** if this flag is set, do sign conversion for 8-bit PCM data */
    private boolean doSignConversion;

    /** if this flag is set, do endian conversion for 16-bit PCM data */
    private boolean doEndianConversion;

    protected TAudioOutputStream(AudioFormat audioFormat,
                                 long length,
                                 TDataOutputStream dataOutputStream,
                                 boolean doBackPatching) {
        this.audioFormat = audioFormat;
        this.length = length;
        calculatedLength = 0;
        this.dataOutputStream = dataOutputStream;
        this.doBackPatching = doBackPatching;
        headerWritten = false;
    }

    /**
     * descendants should call this method if implicit sign conversion for 8-bit
     * data should be done
     */
    protected void requireSign8bit(boolean signed) {
        if (audioFormat.getSampleSizeInBits() == 8 && isPCM(audioFormat)) {
            boolean si = audioFormat.getEncoding().equals(AudioFormat.Encoding.PCM_SIGNED);
            doSignConversion = signed != si;
        }
    }

    /**
     * descendants should call this method if implicit endian conversion should
     * be done. Currently supported for 16, 24, and 32 bits per sample.
     */
    protected void requireEndianness(boolean bigEndian) {
        int ssib = audioFormat.getSampleSizeInBits();
        if ((ssib == 16 || ssib == 24 || ssib == 32) && isPCM(audioFormat)) {
            doEndianConversion = bigEndian != audioFormat.isBigEndian();
        }
    }

    @Override
    public AudioFormat getFormat() {
        return audioFormat;
    }

    /**
     * Gives length of the stream.
     * This value is in bytes. It may be AudioSystem.NOT_SPECIFIED
     * to express that the length is unknown.
     */
    @Override
    public long getLength() {
        return length;
    }

    /**
     * Gives number of bytes already written.
     * <p>
     * IDEA: rename this to BytesWritten or something like that ?
     */
    public long getCalculatedLength() {
        return calculatedLength;
    }

    protected TDataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    /** do sign or endianness conversion */
    private void handleImplicitConversions(byte[] data, int offset, int length) {
        if (doSignConversion) {
            convertSign8(data, offset, length);
        }
        if (doEndianConversion) {
            switch (audioFormat.getSampleSizeInBits()) {
            case 16:
                swapOrder16(data, offset, length / 2);
                break;
            case 24:
                swapOrder24(data, offset, length / 3);
                break;
            case 32:
                swapOrder32(data, offset, length / 4);
                break;
            }
        }
    }

    /**
     * Writes audio data to the destination (file or output stream).
     * <p>
     * IDEA: use long?
     */
    @Override
    public int write(byte[] data, int offset, int length) throws IOException {
        logger.log(Level.TRACE, "wanted length: " + length);

        if (!headerWritten) {
            writeHeader();
            headerWritten = true;
        }
        // $$fb added
        // check that total writes do not exceed specified length
        long totalLength = getLength();
        if (totalLength != AudioSystem.NOT_SPECIFIED && (calculatedLength + length) > totalLength) {
            logger.log(Level.TRACE, "requested more bytes to write than possible.");

            length = (int) (totalLength - calculatedLength);
            // sanity
            if (length < 0) {
                length = 0;
            }
        }
        // TODO throw an exception if length==0 ? (to indicate end of file ?)
        if (length > 0) {
            handleImplicitConversions(data, offset, length);
            dataOutputStream.write(data, offset, length);
            calculatedLength += length;
            // if we converted something, need to undo the conversion to
            // guarantee integrity of data
            handleImplicitConversions(data, offset, length);
        }
        logger.log(Level.TRACE, "calculated (total) length: " + calculatedLength + " bytes = " + (calculatedLength / getFormat().getFrameSize()) + " frames");

        return length;
    }

    /**
     * Writes the header of the audio file.
     */
    protected abstract void writeHeader() throws IOException;

    /**
     * Closes the stream.
     * This does write remaining buffered data to the destination,
     * backpatch the header, if necessary, and closes the destination.
     */
    @Override
    public void close() throws IOException {
        logger.log(Level.TRACE, "called");

        // flush?
        if (doBackPatching) {
            logger.log(Level.TRACE, "patching header");

            patchHeader();
        }
        dataOutputStream.close();
    }

    protected void patchHeader() throws IOException {
        logger.log(Level.TRACE, "called");
        // DO NOTHING
    }

    protected void setLengthFromCalculatedLength() {
        length = calculatedLength;
    }
}
