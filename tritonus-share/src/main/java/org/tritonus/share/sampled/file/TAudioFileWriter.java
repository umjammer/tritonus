/*
 *  Copyright (c) 1999, 2000 by Matthias Pfisterer
 *  Copyright (c) 1999, 2000 by Florian Bomers
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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Collection;
import java.util.Iterator;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.AudioFileWriter;

import org.tritonus.share.ArraySet;
import org.tritonus.share.sampled.AudioFormats;
import org.tritonus.share.sampled.AudioUtils;
import org.tritonus.share.sampled.TConversionTool;

import static java.lang.System.getLogger;


/**
 * Common base class for implementing classes of AudioFileWriter.
 * <p>It provides often-used functionality and the new architecture using
 * an AudioOutputStream.
 * <p>There should be only one set of audio formats supported by any given
 * class of TAudioFileWriter. This class assumes implicitly that all
 * supported file types have a common set of audio formats they can handle.
 *
 * @author Matthias Pfisterer
 * @author Florian Bomers
 */
public abstract class TAudioFileWriter extends AudioFileWriter {

    private static final Logger logger= getLogger("org.tritonus.TraceAudioFileWriter");

    protected static final int ALL = AudioSystem.NOT_SPECIFIED;

    protected static final AudioFormat.Encoding PCM_SIGNED = AudioFormat.Encoding.PCM_SIGNED;
    protected static final AudioFormat.Encoding PCM_UNSIGNED = AudioFormat.Encoding.PCM_UNSIGNED;

    /**
     * Buffer length for the loop in the write() method.
     * This is in bytes. Perhaps it should be in frames to give an
     * equal amount of latency.
     */
    private static final int BUFFER_LENGTH = 16384;

    // only needed for Collection.toArray()
    protected static final AudioFileFormat.Type[] NULL_TYPE_ARRAY = new AudioFileFormat.Type[0];

    /**
     * The audio file types (AudioFileFormat.Type) that can be
     * handled by the AudioFileWriter.
     */
    private Collection<AudioFileFormat.Type> m_audioFileTypes;

    /**
     * The AudioFormats that can be handled by the
     * AudioFileWriter.
     */
    // IDEA: implement a special collection that uses matches() to test whether an element is already in
    private Collection<AudioFormat> m_audioFormats;

    /**
     * Inheriting classes should call this constructor
     * in order to make use of the functionality of TAudioFileWriter.
     */
    protected TAudioFileWriter(Collection<AudioFileFormat.Type> fileTypes, Collection<AudioFormat> audioFormats) {
        logger.log(Level.TRACE, "TAudioFileWriter.<init>(): begin");

        m_audioFileTypes = fileTypes;
        m_audioFormats = audioFormats;

        logger.log(Level.TRACE, "TAudioFileWriter.<init>(): end");
    }

    @Override
    public AudioFileFormat.Type[] getAudioFileTypes() {
        return m_audioFileTypes.toArray(NULL_TYPE_ARRAY);
    }

    @Override
    public boolean isFileTypeSupported(AudioFileFormat.Type fileType) {
        return m_audioFileTypes.contains(fileType);
    }

    @Override
    public AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream audioInputStream) {
        // rewrote this method. We need to check for *each*
        // file type, whether the format is supported !
        // $$fb 2000-08-16
        AudioFormat format = audioInputStream.getFormat();
        ArraySet<AudioFileFormat.Type> res = new ArraySet<>();
        for (AudioFileFormat.Type thisType : m_audioFileTypes) {
            if (isAudioFormatSupportedImpl(format, thisType)) {
                res.add(thisType);
            }
        }
        return res.toArray(NULL_TYPE_ARRAY);
    }

    @Override
    public boolean isFileTypeSupported(AudioFileFormat.Type fileType, AudioInputStream audioInputStream) {
        // finally this method works reliably !
        // $$fb 2000-08-16
        return isFileTypeSupported(fileType)
                && (isAudioFormatSupportedImpl(audioInputStream.getFormat(), fileType)
                || findConvertableFormat(audioInputStream.getFormat(), fileType) != null);
        // we may soft it up by including the possibility of endian/sign
        // changing for PCM formats.
        // I prefer to return false if the format is not exactly supported
        // but still execute the write, if only sign/endian changing is necessary.
    }

    @Override
    public int write(AudioInputStream audioInputStream, AudioFileFormat.Type fileType, File file) throws IOException {
        logger.log(Level.TRACE, ">TAudioFileWriter.write(.., File): called");
        logger.log(Level.TRACE, "class: " + getClass().getName());
        // $$fb added this check
        if (!isFileTypeSupported(fileType)) {
            logger.log(Level.TRACE, "< file type is not supported");

            throw new IllegalArgumentException("file type is not supported.");
        }

        AudioFormat inputFormat = audioInputStream.getFormat();
        logger.log(Level.TRACE, "input format: " + inputFormat);

        AudioFormat outputFormat;
        boolean bNeedsConversion;
        if (isAudioFormatSupportedImpl(inputFormat, fileType)) {
            logger.log(Level.TRACE, "input format is supported directely");

            outputFormat = inputFormat;
            bNeedsConversion = false;
        } else {
            logger.log(Level.TRACE, "input format is not supported directely; trying to find a convertable format");

            outputFormat = findConvertableFormat(inputFormat, fileType);
            if (outputFormat != null) {
                bNeedsConversion = true;
                // made consistent with new conversion trials
                // if 8 bit and only endianness changed, don't convert !
                // $$fb 2000-08-16
                if (outputFormat.getSampleSizeInBits() == 8
                        && outputFormat.getEncoding().equals(inputFormat.getEncoding())) {
                    bNeedsConversion = false;
                }
            } else {
                logger.log(Level.TRACE, "< input format is not supported and not convertable.");

                throw new IllegalArgumentException("format not supported and not convertable");
            }
        }
        long lLengthInBytes = AudioUtils.getLengthInBytes(audioInputStream);
        TDataOutputStream dataOutputStream = new TSeekableDataOutputStream(file);
        AudioOutputStream audioOutputStream = getAudioOutputStream(
                outputFormat,
                lLengthInBytes,
                fileType,
                dataOutputStream);
        int written = writeImpl(audioInputStream, audioOutputStream, bNeedsConversion);
        logger.log(Level.TRACE, "< wrote " + written + " bytes.");

        return written;
    }

    @Override
    public int write(AudioInputStream audioInputStream, AudioFileFormat.Type fileType,
                     OutputStream outputStream) throws IOException {
        // $$fb added this check
        if (!isFileTypeSupported(fileType)) {
            throw new IllegalArgumentException("file type is not supported.");
        }
        logger.log(Level.TRACE, ">TAudioFileWriter.write(.., OutputStream): called");
        logger.log(Level.TRACE, "class: " + getClass().getName());
        AudioFormat inputFormat = audioInputStream.getFormat();
        logger.log(Level.TRACE, "input format: " + inputFormat);

        AudioFormat outputFormat;
        boolean bNeedsConversion;
        if (isAudioFormatSupportedImpl(inputFormat, fileType)) {
            logger.log(Level.TRACE, "input format is supported directely");

            outputFormat = inputFormat;
            bNeedsConversion = false;
        } else {
            logger.log(Level.TRACE, "input format is not supported directely; trying to find a convertable format");

            outputFormat = findConvertableFormat(inputFormat, fileType);
            if (outputFormat != null) {
                bNeedsConversion = true;
                // $$fb 2000-08-16 made consistent with new conversion trials
                // if 8 bit and only endianness changed, don't convert !
                if (outputFormat.getSampleSizeInBits() == 8
                        && outputFormat.getEncoding().equals(inputFormat.getEncoding())) {
                    bNeedsConversion = false;
                }
            } else {
                logger.log(Level.TRACE, "< format is not supported");

                throw new IllegalArgumentException("format not supported and not convertable");
            }
        }
        long lLengthInBytes = AudioUtils.getLengthInBytes(audioInputStream);
        TDataOutputStream dataOutputStream = new TNonSeekableDataOutputStream(outputStream);
        AudioOutputStream audioOutputStream = getAudioOutputStream(
                outputFormat,
                lLengthInBytes,
                fileType,
                dataOutputStream);
        int written = writeImpl(audioInputStream, audioOutputStream, bNeedsConversion);
        logger.log(Level.TRACE, "< wrote " + written + " bytes.");

        return written;
    }

    protected int writeImpl(AudioInputStream audioInputStream,
                            AudioOutputStream audioOutputStream,
                            boolean bNeedsConversion) throws IOException {
        logger.log(Level.TRACE, ">TAudioFileWriter.writeImpl(): called");
        logger.log(Level.TRACE, "class: " + getClass().getName());
        int nTotalWritten = 0;
        AudioFormat outputFormat = audioOutputStream.getFormat();

        // TODO handle case when frame size is unknown ?
        int nBytesPerSample = outputFormat.getFrameSize() / outputFormat.getChannels();

        // $$fb 2000-07-18: BUFFER_LENGTH must be a multiple of frame size...
        int nBufferSize = (BUFFER_LENGTH / outputFormat.getFrameSize()) * outputFormat.getFrameSize();
        byte[] abBuffer = new byte[nBufferSize];
        while (true) {
            logger.log(Level.TRACE, "trying to read (bytes): " + abBuffer.length);

            int nBytesRead = audioInputStream.read(abBuffer);
            logger.log(Level.TRACE, "read (bytes): " + nBytesRead);

            if (nBytesRead == -1) {
                break;
            }
            if (bNeedsConversion) {
                TConversionTool.changeOrderOrSign(abBuffer, 0,
                        nBytesRead, nBytesPerSample);
            }
            int nWritten = audioOutputStream.write(abBuffer, 0, nBytesRead);
            nTotalWritten += nWritten;
        }
        logger.log(Level.TRACE, "<TAudioFileWriter.writeImpl(): after main loop. Wrote " + nTotalWritten + " bytes");

        audioOutputStream.close();
        // TODO get bytes written for header etc. from AudioOutputStrem and add to nTotalWrittenBytes
        return nTotalWritten;
    }

    /**
     * Returns the AudioFormat that can be handled for the given file type.
     * In this simple implementation, all handled AudioFormats are
     * returned (i.e. the fileType argument is ignored). If the
     * handled AudioFormats depend on the file type, this method
     * has to be overwritten by subclasses.
     */
    protected Iterator<AudioFormat> getSupportedAudioFormats(AudioFileFormat.Type fileType) {
        return m_audioFormats.iterator();
    }

    /**
     * Checks whether the passed <b>AudioFormat</b> can be handled.
     * In this simple implementation, it is only checked if the
     * passed AudioFormat matches one of the generally handled
     * formats (i.e. the fileType argument is ignored). If the
     * handled AudioFormats depend on the file type, this method
     * or getSupportedAudioFormats() (on which this method relies)
     * has to be  overwritten by subclasses.
     * <p>
     * This is the central method for checking if a FORMAT is supported.
     * Inheriting classes can overwrite this for performance
     * or to exclude/include special type/format combinations.
     * <p>
     * This method is only called when the <code>fileType</code>
     * is in the list of supported file types ! Overriding
     * classes <b>need not</b> check this.
     * <p>
     * $$fb 2000-08-16 changed name, changed documentation. Semantics !
     * </p>
     */
    protected boolean isAudioFormatSupportedImpl(AudioFormat audioFormat, AudioFileFormat.Type fileType) {
        logger.log(Level.TRACE, "> TAudioFileWriter.isAudioFormatSupportedImpl(): format to test: " + audioFormat);
        logger.log(Level.TRACE, "class: " + getClass().getName());
        Iterator<AudioFormat> audioFormats = getSupportedAudioFormats(fileType);
        while (audioFormats.hasNext()) {
            AudioFormat handledFormat = audioFormats.next();
            logger.log(Level.TRACE, "matching against format : " + handledFormat);

            if (AudioFormats.matches(handledFormat, audioFormat)) {
                logger.log(Level.TRACE, "<...succeeded.");

                return true;
            }
        }
        logger.log(Level.TRACE, "< ... failed");

        return false;
    }

    protected abstract AudioOutputStream getAudioOutputStream(
            AudioFormat audioFormat,
            long lLengthInBytes,
            AudioFileFormat.Type fileType,
            TDataOutputStream dataOutputStream) throws IOException;

    private AudioFormat findConvertableFormat(
            AudioFormat inputFormat,
            AudioFileFormat.Type fileType) {
        logger.log(Level.TRACE, "TAudioFileWriter.findConvertableFormat(): input format: " + inputFormat);

        if (!isFileTypeSupported(fileType)) {
            logger.log(Level.TRACE, "< input file type is not supported.");

            return null;
        }
        AudioFormat.Encoding inputEncoding = inputFormat.getEncoding();
        if ((inputEncoding.equals(PCM_SIGNED) || inputEncoding.equals(PCM_UNSIGNED))
                && inputFormat.getSampleSizeInBits() == 8) {
            AudioFormat outputFormat = convertFormat(inputFormat, true, false);
            logger.log(Level.TRACE, "trying output format: " + outputFormat);

            if (isAudioFormatSupportedImpl(outputFormat, fileType)) {
                logger.log(Level.TRACE, "< ... succeeded");

                return outputFormat;
            }
            // $$fb 2000-08-16: added trial of other endianness for 8bit. We try harder !
            outputFormat = convertFormat(inputFormat, false, true);
            logger.log(Level.TRACE, "trying output format: " + outputFormat);

            if (isAudioFormatSupportedImpl(outputFormat, fileType)) {
                logger.log(Level.TRACE, "< ... succeeded");

                return outputFormat;
            }
            outputFormat = convertFormat(inputFormat, true, true);
            logger.log(Level.TRACE, "trying output format: " + outputFormat);

            if (isAudioFormatSupportedImpl(outputFormat, fileType)) {
                logger.log(Level.TRACE, "< ... succeeded");

                return outputFormat;
            }
            logger.log(Level.TRACE, "< ... failed");

            return null;
        } else if (inputEncoding.equals(PCM_SIGNED) &&
                (inputFormat.getSampleSizeInBits() == 16 ||
                        inputFormat.getSampleSizeInBits() == 24 ||
                        inputFormat.getSampleSizeInBits() == 32)) {
            // TODO possible to allow all sample sized > 8 bit?
            // $$ fb: don't think that this is necessary. Well, let's talk about that in 5 years :)
            AudioFormat outputFormat = convertFormat(inputFormat, false, true);
            logger.log(Level.TRACE, "trying output format: " + outputFormat);

            if (isAudioFormatSupportedImpl(outputFormat, fileType)) {
                logger.log(Level.TRACE, "< ... succeeded");

                return outputFormat;
            } else {
                logger.log(Level.TRACE, "< ... failed");

                return null;
            }
        } else {
            logger.log(Level.TRACE, "< ... failed");

            return null;
        }
    }

    // $$fb 2000-08-16: added convenience method
    private AudioFormat convertFormat(AudioFormat format, boolean changeSign, boolean changeEndian) {
        AudioFormat.Encoding enc = PCM_SIGNED;
        if (format.getEncoding().equals(PCM_UNSIGNED) != changeSign) {
            enc = PCM_UNSIGNED;
        }
        return new AudioFormat(
                enc,
                format.getSampleRate(),
                format.getSampleSizeInBits(),
                format.getChannels(),
                format.getFrameSize(),
                format.getFrameRate(),
                format.isBigEndian() ^ changeEndian);
    }
}
