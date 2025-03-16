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
    private final Collection<AudioFileFormat.Type> audioFileTypes;

    /**
     * The AudioFormats that can be handled by the
     * AudioFileWriter.
     * <p>
     * IDEA: implement a special collection that uses matches() to test whether an element is already in
     */
    private final Collection<AudioFormat> audioFormats;

    /**
     * Inheriting classes should call this constructor
     * in order to make use of the functionality of TAudioFileWriter.
     */
    protected TAudioFileWriter(Collection<AudioFileFormat.Type> fileTypes, Collection<AudioFormat> audioFormats) {
        logger.log(Level.TRACE, "begin");

        audioFileTypes = fileTypes;
        this.audioFormats = audioFormats;

        logger.log(Level.TRACE, "end");
    }

    @Override
    public AudioFileFormat.Type[] getAudioFileTypes() {
        return audioFileTypes.toArray(NULL_TYPE_ARRAY);
    }

    @Override
    public boolean isFileTypeSupported(AudioFileFormat.Type fileType) {
        return audioFileTypes.contains(fileType);
    }

    @Override
    public AudioFileFormat.Type[] getAudioFileTypes(AudioInputStream audioInputStream) {
        // rewrote this method. We need to check for *each*
        // file type, whether the format is supported !
        // $$fb 2000-08-16
        AudioFormat format = audioInputStream.getFormat();
        ArraySet<AudioFileFormat.Type> res = new ArraySet<>();
        for (AudioFileFormat.Type thisType : audioFileTypes) {
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
                || findConvertibleFormat(audioInputStream.getFormat(), fileType) != null);
        // we may soft it up by including the possibility of endian/sign
        // changing for PCM formats.
        // I prefer to return false if the format is not exactly supported
        // but still execute the write, if only sign/endian changing is necessary.
    }

    @Override
    public int write(AudioInputStream audioInputStream, AudioFileFormat.Type fileType, File file) throws IOException {
        logger.log(Level.TRACE, "called");
        logger.log(Level.TRACE, "class: " + getClass().getName());
        // $$fb added this check
        if (!isFileTypeSupported(fileType)) {
            logger.log(Level.TRACE, "< file type is not supported");

            throw new IllegalArgumentException("file type is not supported.");
        }

        AudioFormat inputFormat = audioInputStream.getFormat();
        logger.log(Level.TRACE, "input format: " + inputFormat);

        AudioFormat outputFormat;
        boolean needsConversion;
        if (isAudioFormatSupportedImpl(inputFormat, fileType)) {
            logger.log(Level.TRACE, "input format is supported directly");

            outputFormat = inputFormat;
            needsConversion = false;
        } else {
            logger.log(Level.TRACE, "input format is not supported directly; trying to find a convertible format");

            outputFormat = findConvertibleFormat(inputFormat, fileType);
            if (outputFormat != null) {
                needsConversion = true;
                // made consistent with new conversion trials
                // if 8 bit and only endianness changed, don't convert !
                // $$fb 2000-08-16
                if (outputFormat.getSampleSizeInBits() == 8
                        && outputFormat.getEncoding().equals(inputFormat.getEncoding())) {
                    needsConversion = false;
                }
            } else {
                logger.log(Level.TRACE, "< input format is not supported and not convertible.");

                throw new IllegalArgumentException("format not supported and not convertible");
            }
        }
        long lengthInBytes = AudioUtils.getLengthInBytes(audioInputStream);
        TDataOutputStream dataOutputStream = new TSeekableDataOutputStream(file);
        AudioOutputStream audioOutputStream = getAudioOutputStream(
                outputFormat,
                lengthInBytes,
                fileType,
                dataOutputStream);
        int written = writeImpl(audioInputStream, audioOutputStream, needsConversion);
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
        logger.log(Level.TRACE, "called");
        logger.log(Level.TRACE, "class: " + getClass().getName());
        AudioFormat inputFormat = audioInputStream.getFormat();
        logger.log(Level.TRACE, "input format: " + inputFormat);

        AudioFormat outputFormat;
        boolean needsConversion;
        if (isAudioFormatSupportedImpl(inputFormat, fileType)) {
            logger.log(Level.TRACE, "input format is supported directly");

            outputFormat = inputFormat;
            needsConversion = false;
        } else {
            logger.log(Level.TRACE, "input format is not supported directly; trying to find a convertible format");

            outputFormat = findConvertibleFormat(inputFormat, fileType);
            if (outputFormat != null) {
                needsConversion = true;
                // $$fb 2000-08-16 made consistent with new conversion trials
                // if 8 bit and only endianness changed, don't convert !
                if (outputFormat.getSampleSizeInBits() == 8
                        && outputFormat.getEncoding().equals(inputFormat.getEncoding())) {
                    needsConversion = false;
                }
            } else {
                logger.log(Level.TRACE, "< format is not supported");

                throw new IllegalArgumentException("format not supported and not convertable");
            }
        }
        long lengthInBytes = AudioUtils.getLengthInBytes(audioInputStream);
        TDataOutputStream dataOutputStream = new TNonSeekableDataOutputStream(outputStream);
        AudioOutputStream audioOutputStream = getAudioOutputStream(
                outputFormat,
                lengthInBytes,
                fileType,
                dataOutputStream);
        int written = writeImpl(audioInputStream, audioOutputStream, needsConversion);
        logger.log(Level.TRACE, "< wrote " + written + " bytes.");

        return written;
    }

    protected int writeImpl(AudioInputStream audioInputStream,
                            AudioOutputStream audioOutputStream,
                            boolean needsConversion) throws IOException {
        logger.log(Level.TRACE, "called");
        logger.log(Level.TRACE, "class: " + getClass().getName());
        int totalWritten = 0;
        AudioFormat outputFormat = audioOutputStream.getFormat();

        // TODO handle case when frame size is unknown ?
        int bytesPerSample = outputFormat.getFrameSize() / outputFormat.getChannels();

        // $$fb 2000-07-18: BUFFER_LENGTH must be a multiple of frame size...
        int bufferSize = (BUFFER_LENGTH / outputFormat.getFrameSize()) * outputFormat.getFrameSize();
        byte[] buffer = new byte[bufferSize];
        while (true) {
            logger.log(Level.TRACE, "trying to read (bytes): " + buffer.length);

            int bytesRead = audioInputStream.read(buffer);
            logger.log(Level.TRACE, "read (bytes): " + bytesRead);

            if (bytesRead == -1) {
                break;
            }
            if (needsConversion) {
                TConversionTool.changeOrderOrSign(buffer, 0,
                        bytesRead, bytesPerSample);
            }
            int written = audioOutputStream.write(buffer, 0, bytesRead);
            totalWritten += written;
        }
        logger.log(Level.TRACE, "after main loop. Wrote " + totalWritten + " bytes");

        audioOutputStream.close();
        // TODO get bytes written for header etc. from AudioOutputStrem and add to nTotalWrittenBytes
        return totalWritten;
    }

    /**
     * Returns the AudioFormat that can be handled for the given file type.
     * In this simple implementation, all handled AudioFormats are
     * returned (i.e. the fileType argument is ignored). If the
     * handled AudioFormats depend on the file type, this method
     * has to be overwritten by subclasses.
     */
    protected Iterator<AudioFormat> getSupportedAudioFormats(AudioFileFormat.Type fileType) {
        return audioFormats.iterator();
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
        logger.log(Level.TRACE, "> format to test: " + audioFormat);
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
            long lengthInBytes,
            AudioFileFormat.Type fileType,
            TDataOutputStream dataOutputStream) throws IOException;

    private AudioFormat findConvertibleFormat(
            AudioFormat inputFormat,
            AudioFileFormat.Type fileType) {
        logger.log(Level.TRACE, "input format: " + inputFormat);

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
