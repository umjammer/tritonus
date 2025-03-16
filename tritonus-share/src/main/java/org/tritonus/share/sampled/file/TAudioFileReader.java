/*
 *  Copyright (c) 1999 by Matthias Pfisterer
 *  Copyright (c) 2001 by Florian Bomers
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

package org.tritonus.share.sampled.file;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

import static java.lang.System.getLogger;


/**
 * Base class for audio file readers.
 * This is Tritonus' base class for classes that provide the facility
 * of detecting an audio file type and reading its header.
 * Classes should be derived from this class or one of its subclasses
 * rather than from javax.sound.sampled.spi.AudioFileReader.
 *
 * @author Matthias Pfisterer
 * @author Florian Bomers
 */
public abstract class TAudioFileReader extends AudioFileReader {

    private static final Logger logger = getLogger("org.tritonus.TraceAudioFileReader");

    private int markLimit;
    private final boolean rereading;

    protected TAudioFileReader(int markLimit) {
        this(markLimit, false);
    }

    protected TAudioFileReader(int markLimit, boolean rereading) {
        this.markLimit = markLimit;
        this.rereading = rereading;
    }

    protected int getMarkLimit() {
        return markLimit;
    }

    protected void setMarkLimit(int limit) {
        markLimit = limit;
    }

    private boolean isRereading() {
        return rereading;
    }

    /**
     * Get an AudioFileFormat object for a File.
     * This method calls getAudioFileFormat(InputStream, long).
     * Subclasses should not override this method unless there are
     * really severe reasons. Normally, it is sufficient to
     * implement getAudioFileFormat(InputStream, long).
     *
     * @param file the file to read from.
     * @return an AudioFileFormat instance containing
     * information from the header of the file passed in.
     */
    @Override
    public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
        logger.log(Level.TRACE, "begin (class: " + getClass().getSimpleName() + ")");

        long fileLengthInBytes = file.length();
        InputStream inputStream = Files.newInputStream(file.toPath());
        AudioFileFormat audioFileFormat;
        try {
            audioFileFormat = getAudioFileFormat(inputStream, fileLengthInBytes);
        } finally {
            inputStream.close();
        }
        logger.log(Level.TRACE, "end");

        return audioFileFormat;
    }

    /**
     * Get an AudioFileFormat object for a URL.
     * This method calls getAudioFileFormat(InputStream, long).
     * Subclasses should not override this method unless there are
     * really severe reasons. Normally, it is sufficient to
     * implement getAudioFileFormat(InputStream, long).
     *
     * @param url the URL to read from.
     * @return an AudioFileFormat instance containing
     * information from the header of the URL passed in.
     */
    @Override
    public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
        logger.log(Level.TRACE, "begin (class: " + getClass().getSimpleName() + ")");

        long fileLengthInBytes = getDataLength(url);
        InputStream inputStream = url.openStream();
        AudioFileFormat audioFileFormat;
        try {
            audioFileFormat = getAudioFileFormat(inputStream, fileLengthInBytes);
        } finally {
            inputStream.close();
        }

        logger.log(Level.TRACE, "end");

        return audioFileFormat;
    }

    /**
     * Get an AudioFileFormat object for an InputStream.
     * This method calls getAudioFileFormat(InputStream, long).
     * Subclasses should not override this method unless there are
     * really severe reasons. Normally, it is sufficient to
     * implement getAudioFileFormat(InputStream, long).
     *
     * @param inputStream the stream to read from.
     * @return an AudioFileFormat instance containing
     * information from the header of the stream passed in.
     */
    @Override
    public AudioFileFormat getAudioFileFormat(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        logger.log(Level.TRACE, "begin (class: " + getClass().getSimpleName() + ")");

        long fileLengthInBytes = AudioSystem.NOT_SPECIFIED;
        if (!inputStream.markSupported()) {
            inputStream = new BufferedInputStream(inputStream, getMarkLimit());
        }
        inputStream.mark(getMarkLimit());
        AudioFileFormat audioFileFormat;
        try {
            audioFileFormat = getAudioFileFormat(inputStream, fileLengthInBytes);
        } finally {
            // be executed only when there is an exception or
            // should it be done always?
            try {
                inputStream.reset();
            } catch (IOException f) {
                logger.log(Level.WARNING, f.toString());
            }
        }

        logger.log(Level.TRACE, "end");

        return audioFileFormat;
    }

    /**
     * Get an AudioFileFormat (internal implementation). Subclasses must
     * implement this method in a way specific to the file format they handle.
     * Note that depending on the implementation of this method, you should or
     * should not override getAudioInputStream(InputStream, long), too (see
     * comment there).
     *
     * @param inputStream       The InputStream to read from. It should be tested if
     *                          it is markable. If not, and it is re-reading, wrap it into a
     *                          BufferedInputStream with getMarkLimit() size.
     * @param fileLengthInBytes The size of the originating file, if known. If
     *                          it isn't known, AudioSystem.NOT_SPECIFIED should be passed.
     *                          This value may be used for byteLength in AudioFileFormat, if
     *                          this value can't be derived from the informmation in the file
     *                          header.
     * @return an AudioFileFormat instance containing information from the
     * header of the stream passed in as inputStream.
     */
    protected abstract AudioFileFormat getAudioFileFormat(InputStream inputStream, long fileLengthInBytes)
            throws UnsupportedAudioFileException, IOException;

    /**
     * Get an AudioInputStream object for a file.
     * This method calls getAudioInputStream(InputStream, long).
     * Subclasses should not override this method unless there are
     * really severe reasons. Normally, it is sufficient to
     * implement getAudioFileFormat(InputStream, long) and perhaps
     * override getAudioInputStream(InputStream, long).
     *
     * @param file the File object to read from.
     * @return an AudioInputStream instance containing
     * the audio data from this file.
     */
    @Override
    public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
        logger.log(Level.TRACE, "begin (class: " + getClass().getSimpleName() + ")");

        long fileLengthInBytes = file.length();
        InputStream inputStream = Files.newInputStream(file.toPath());
        AudioInputStream audioInputStream;
        try {
            audioInputStream = getAudioInputStream(inputStream, fileLengthInBytes);
        } catch (UnsupportedAudioFileException | IOException e) {
            inputStream.close();
            throw e;
        }

        logger.log(Level.TRACE, "end");

        return audioInputStream;
    }

    /**
     * Get an AudioInputStream object for a URL.
     * This method calls getAudioInputStream(InputStream, long).
     * Subclasses should not override this method unless there are
     * really severe reasons. Normally, it is sufficient to
     * implement getAudioFileFormat(InputStream, long) and perhaps
     * override getAudioInputStream(InputStream, long).
     *
     * @param url the URL to read from.
     * @return an AudioInputStream instance containing
     * the audio data from this URL.
     */
    @Override
    public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
        logger.log(Level.TRACE, "begin (class: " + getClass().getSimpleName() + ")");

        long fileLengthInBytes = getDataLength(url);
        InputStream inputStream = url.openStream();
        AudioInputStream audioInputStream;
        try {
            audioInputStream = getAudioInputStream(inputStream, fileLengthInBytes);
        } catch (UnsupportedAudioFileException | IOException e) {
            inputStream.close();
            throw e;
        }

        logger.log(Level.TRACE, "end");

        return audioInputStream;
    }

    /**
     * Get an AudioInputStream object for an InputStream.
     * This method calls getAudioInputStream(InputStream, long).
     * Subclasses should not override this method unless there are
     * really severe reasons. Normally, it is sufficient to
     * implement getAudioFileFormat(InputStream, long) and perhaps
     * override getAudioInputStream(InputStream, long).
     *
     * @param inputStream the stream to read from.
     * @return an AudioInputStream instance containing
     * the audio data from this stream.
     */
    @Override
    public AudioInputStream getAudioInputStream(InputStream inputStream) throws UnsupportedAudioFileException, IOException {
        logger.log(Level.TRACE, "begin (class: " + getClass().getSimpleName() + ")");

        long fileLengthInBytes = AudioSystem.NOT_SPECIFIED;
        AudioInputStream audioInputStream;
        if (!inputStream.markSupported()) {
            inputStream = new BufferedInputStream(inputStream, getMarkLimit());
            logger.log(Level.TRACE, "wrapped: " + inputStream.getClass().getName() + ", " + getMarkLimit());
        }
        inputStream.mark(getMarkLimit());
        try {
            audioInputStream = getAudioInputStream(inputStream, fileLengthInBytes);
        } finally {
            try {
                inputStream.reset();
            } catch (IOException f) {
                logger.log(Level.WARNING, f.toString());
            }
        }

        logger.log(Level.TRACE, "end");

        return audioInputStream;
    }

    /**
     * Get an AudioInputStream (internal implementation). This implementation
     * calls getAudioFileFormat() with the same arguments as passed in here.
     * Then, it constructs an AudioInputStream instance. This instance takes the
     * passed inputStream in the state it is left after getAudioFileFormat() did
     * its work. In other words, the implementation here assumes that
     * getAudioFileFormat() reads the entire header up to a position exactly
     * where the audio data starts. If this can't be realized for a certain
     * format, this method should be overridden.
     *
     * @param inputStream       The InputStream to read from. It should be tested if
     *                          it is markable. If not, and it is re-reading, wrap it into a
     *                          BufferedInputStream with getMarkLimit() size.
     * @param fileLengthInBytes The size of the originating file, if known. If
     *                          it isn't known, AudioSystem.NOT_SPECIFIED should be passed.
     *                          This value may be used for byteLength in AudioFileFormat, if
     *                          this value can't be derived from the information in the file
     *                          header.
     */
    protected AudioInputStream getAudioInputStream(InputStream inputStream, long fileLengthInBytes)
            throws UnsupportedAudioFileException, IOException {
        logger.log(Level.TRACE, "begin (class: " + getClass().getSimpleName() + ")");
        if (isRereading()) {
            if (!inputStream.markSupported()) {
                inputStream = new BufferedInputStream(inputStream, getMarkLimit());
            }
            inputStream.mark(getMarkLimit());
        }
        AudioFileFormat audioFileFormat = getAudioFileFormat(inputStream, fileLengthInBytes);
        if (isRereading()) {
            inputStream.reset();
        }
        AudioInputStream audioInputStream = new AudioInputStream(
                inputStream, audioFileFormat.getFormat(), audioFileFormat.getFrameLength());

        logger.log(Level.TRACE, "end");

        return audioInputStream;
    }

    protected static int calculateFrameSize(int sampleSize, int numChannels) {
        return ((sampleSize + 7) / 8) * numChannels;
    }

    private static long getDataLength(URL url) throws IOException {
        long fileLengthInBytes = AudioSystem.NOT_SPECIFIED;
        URLConnection connection = url.openConnection();
        connection.connect();
        int length = connection.getContentLength();
        if (length > 0) {
            fileLengthInBytes = length;
        }
        return fileLengthInBytes;
    }

    public static int readLittleEndianInt(InputStream is) throws IOException {
        int b0 = is.read();
        int b1 = is.read();
        int b2 = is.read();
        int b3 = is.read();
        if ((b0 | b1 | b2 | b3) < 0) {
            throw new EOFException();
        }
        return (b3 << 24) + (b2 << 16) + (b1 << 8) + (b0 << 0);
    }

    public static short readLittleEndianShort(InputStream is) throws IOException {
        int b0 = is.read();
        int b1 = is.read();
        if ((b0 | b1) < 0) {
            throw new EOFException();
        }
        return (short) ((b1 << 8) + (b0 << 0));
    }

    /**
     * Convert from IEEE extended
     * <pre>
     * Copyright (C) 1988-1991 Apple Computer, Inc.
     * All rights reserved.
     * </pre>
     * Machine-independent I/O routines for IEEE floating-point numbers.
     * <p>
     * NaN's and infinities are converted to HUGE_VAL or HUGE, which
     * happens to be infinity on IEEE machines.  Unfortunately, it is
     * impossible to preserve NaN's in a machine-independent way.
     * Infinities are, however, preserved on IEEE machines.
     * <p>
     * These routines have been tested on the following machines:
     * <ul>
     *    <li>Apple Macintosh, MPW 3.1 C compiler</li>
     *    <li>Apple Macintosh, THINK C compiler</li>
     *    <li>Silicon Graphics IRIS, MIPS compiler</li>
     *    <li>Cray X/MP and Y/MP</li>
     *    <li>Digital Equipment VAX</li>
     * </ul>
     * <p>
     * Implemented by Malcolm Slaney and Ken Turkowski.
     * <p>
     * Malcolm Slaney contributions during 1988-1990 include big- and little-
     * endian file I/O, conversion to and from Motorola's extended 80-bit
     * floating-point format, and conversions to and from IEEE single-
     * precision floating-point format.
     * <p>
     * In 1991, Ken Turkowski implemented the conversions to and from
     * IEEE double-precision format, added more precision to the extended
     * conversions, and accommodated conversions involving +/- infinity,
     * NaN's, and denormalized numbers.
     */
    public static double readIeeeExtended(DataInputStream dis) throws IOException {
        double f;
        int expon;
        long hiMant;
        long loMant;
        double HUGE = 3.4028234663852886E+038D;
        expon = dis.readUnsignedShort();
        long t1 = dis.readUnsignedShort();
        long t2 = dis.readUnsignedShort();
        hiMant = t1 << 16 | t2;
        t1 = dis.readUnsignedShort();
        t2 = dis.readUnsignedShort();
        loMant = t1 << 16 | t2;
        if (expon == 0 && hiMant == 0L && loMant == 0L) {
            f = 0.0D;
        } else {
            if (expon == 32767) {
                f = HUGE;
            } else {
                expon -= 16383;
                expon -= 31;
                f = hiMant * Math.pow(2D, expon);
                expon -= 32;
                f += loMant * Math.pow(2D, expon);
            }
        }
        return f;
    }
}
