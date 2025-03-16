/*
 *  Copyright (c) 1999 - 2003 by Matthias Pfisterer
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

package org.tritonus.sampled.convert.jorbis;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import org.tritonus.share.sampled.AudioFormats;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;
import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider;

import static java.lang.System.getLogger;


/**
 * Pure-java decoder for ogg vorbis streams.
 * The FormatConversionProvider uses the pure-java
 * ogg vorbis decoder from www.jcraft.com/jorbis/.
 * <p>
 * See vorbis spec for more info:
 * http://xiph.org/vorbis/doc/Vorbis_I_spec.html
 *
 * @author Matthias Pfisterer
 */
public class JorbisFormatConversionProvider extends TEncodingFormatConversionProvider {

    private static final Logger logger = getLogger("org.tritonus.TraceAudioConverter");

    // only used as abbreviation
    private static final AudioFormat.Encoding VORBIS = new AudioFormat.Encoding("VORBIS");
    private static final AudioFormat.Encoding PCM_SIGNED = new AudioFormat.Encoding("PCM_SIGNED");

    private static final AudioFormat[] INPUT_FORMATS = {
            // mono
            // TODO mechanism to make the double specification with
            // different endianess obsolete.
            new AudioFormat(VORBIS, -1.0F, -1, 1, -1, -1.0F, false),
            new AudioFormat(VORBIS, -1.0F, -1, 1, -1, -1.0F, true),
            // stereo
            new AudioFormat(VORBIS, -1.0F, -1, 2, -1, -1.0F, false),
            new AudioFormat(VORBIS, -1.0F, -1, 2, -1, -1.0F, true),
            // TODO other channel configurations
    };

    private static final AudioFormat[] OUTPUT_FORMATS = {
            // mono, 16 bit signed
            new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, false),
            new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, true),
            // stereo, 16 bit signed
            new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, false),
            new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, true),
            // TODO other channel configurations
    };

    /**
     * Constructor.
     *
     * TODO check interaction with base class
     */
    public JorbisFormatConversionProvider() {
        super(List.of(INPUT_FORMATS),
                List.of(OUTPUT_FORMATS)
                // true, // new behaviour
                // false // bidirectional .. constants UNIDIR../BIDIR..?
        );
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        // The AudioInputStream to return.
        AudioInputStream convertedAudioInputStream;

        logger.log(Level.TRACE, "begin");
        logger.log(Level.TRACE, "checking if conversion supported");
        logger.log(Level.TRACE, "from: " + audioInputStream.getFormat());
        logger.log(Level.TRACE, "to: " + targetFormat);

        // what is this ???
        targetFormat = getDefaultTargetFormat(targetFormat, audioInputStream.getFormat());
        if (isConversionSupported(targetFormat, audioInputStream.getFormat())) {
            logger.log(Level.TRACE, "conversion supported; trying to create DecodedJorbisAudioInputStream");

            convertedAudioInputStream = new DecodedJorbisAudioInputStream(targetFormat, audioInputStream);
        } else {
            logger.log(Level.TRACE, "conversion not supported; throwing IllegalArgumentException");
            throw new IllegalArgumentException("conversion not supported");
        }

        logger.log(Level.TRACE, "end");

        return convertedAudioInputStream;
    }

    // TODO recheck !!
    protected AudioFormat getDefaultTargetFormat(AudioFormat targetFormat, AudioFormat sourceFormat) {
        logger.log(Level.TRACE, "target format: " + targetFormat);

        logger.log(Level.TRACE, "source format: " + sourceFormat);

        AudioFormat newTargetFormat = null;
        // return first of the matching formats
        // pre-condition: the predefined target formats (FORMATS2) must be well-defined !
        for (AudioFormat format : getCollectionTargetFormats()) {
            if (AudioFormats.matches(targetFormat, format)) {
                newTargetFormat = format;
            }
        }
        if (newTargetFormat == null) {
            throw new IllegalArgumentException("conversion not supported");
        }
        logger.log(Level.TRACE, "new target format: " + newTargetFormat);

        // hacked together...
        // ... only works for PCM target encoding ...
        newTargetFormat = new AudioFormat(targetFormat.getEncoding(),
                sourceFormat.getSampleRate(),
                newTargetFormat.getSampleSizeInBits(),
                newTargetFormat.getChannels(),
                newTargetFormat.getFrameSize(),
                sourceFormat.getSampleRate(),
                newTargetFormat.isBigEndian());

        logger.log(Level.TRACE, "really new target format: " + newTargetFormat);

        return newTargetFormat;
    }

    /**
     * AudioInputStream returned on decoding of ogg vorbis.
     * An instance of this class is returned if you call
     * AudioSystem.getAudioInputStream(AudioFormat, AudioInputStream)
     * to decode an ogg/vorbis stream. This class contains the logic
     * of maintaining buffers and calling the decoder.
     * <p>
     * Class should be private, but is public due to a bug (?) in the
     * aspectj compiler.
     */
    /* private */ public static class DecodedJorbisAudioInputStream extends TAsynchronousFilteredAudioInputStream {

        private static final int BUFFER_MULTIPLE = 4;
        private static final int BUFFER_SIZE = BUFFER_MULTIPLE * 256 * 2;
        private static final int CONVSIZE = BUFFER_SIZE * 2;

        private final InputStream oggBitStream;

        // Ogg structures
        private SyncState oggSyncState = null;
        private StreamState oggStreamState = null;
        private Page oggPage = null;
        private Packet oggPacket = null;

        // Vorbis structures
        private Info vorbisInfo = null;
        private Comment vorbisComment = null;
        private DspState vorbisDspState = null;
        // actually is an ogg structure
        private Block vorbisBlock = null;

        private final List<String> songComments = new ArrayList<>();
        // is altered later in a dubious way
        private int convSize = -1; // BUFFER_SIZE * 2;
        // TODO further checking
        private final byte[] convBuffer = new byte[CONVSIZE];
        private float[][][] pcm = null;
        private int[] index = null;

        // TODO introduce state variable
        private boolean headersExpected;

        /**
         * Constructor.
         */
        public DecodedJorbisAudioInputStream(AudioFormat outputFormat, AudioInputStream bitStream) {
            super(outputFormat, AudioSystem.NOT_SPECIFIED);
            logger.log(Level.TRACE, "begin");

            oggBitStream = bitStream;
            headersExpected = true;
            initJorbis();

            logger.log(Level.TRACE, "end");
        }

        /**
         * Initializes all the jOrbis and jOgg vars that are used for song playback.
         */
        private void initJorbis() {
            oggSyncState = new SyncState();
            oggStreamState = new StreamState();
            oggPage = new Page();
            oggPacket = new Packet();

            vorbisInfo = new Info();
            vorbisComment = new Comment();
            vorbisDspState = new DspState();
            vorbisBlock = new Block(vorbisDspState);

            oggSyncState.init();
        }

        /**
         * Callback from circular buffer.
         */
        @Override
        public void execute() {
            logger.log(Level.TRACE, "begin");

            if (headersExpected) {
                logger.log(Level.TRACE, "reading headers...");

                // Headers (+ Comments).
                try {
                    readHeaders();
                } catch (IOException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);

                    closePhysicalStream();
                    logger.log(Level.TRACE, "end");

                    return;
                }
                headersExpected = false;
                setupVorbisStructures();
            }
            logger.log(Level.TRACE, "decoding...");

            // Decoding !
            while (writeMore()) {
                try {
                    readOggPacket();
                } catch (IOException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);

                    closePhysicalStream();
                    logger.log(Level.TRACE, "end");

                    return;
                }
                decodeDataPacket();
            }
            if (oggPacket.e_o_s != 0) {
                logger.log(Level.TRACE, "end of vorbis stream reached");

                shutDownLogicalStream();
            }

            logger.log(Level.TRACE, "end");
        }

        /**
         * The end of the vorbis stream is reached.
         * So we shut down the logical bitstream and
         * vorbis structures.
         */
        private void shutDownLogicalStream() {
            oggStreamState.clear();
            vorbisBlock.clear();
            vorbisDspState.clear();
            vorbisInfo.clear();
            headersExpected = true;
        }

        private void closePhysicalStream() {
            logger.log(Level.TRACE, "begin");

            oggSyncState.clear();
            try {
                if (oggBitStream != null) {
                    oggBitStream.close();
                }
                getCircularBuffer().close();
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }

            logger.log(Level.TRACE, "end");
        }

        /**
         * Read and process all three vorbis headers.
         */
        private void readHeaders() throws IOException {
            readIdentificationHeader();
            readCommentAndCodebookHeaders();
            processComments();
        }

        /**
         * Read the vorbis identification header.
         *
         * @throws IOException when an error occurs
         */
        private void readIdentificationHeader() throws IOException {
            readOggPage();
            oggStreamState.init(oggPage.serialno());
            vorbisInfo.init();
            vorbisComment.init();
            if (oggStreamState.pagein(oggPage) < 0) {
                throw new IOException("can't read first page of Ogg bitstream data, perhaps stream version mismatch");
            }
            if (oggStreamState.packetout(oggPacket) != 1) {
                throw new IOException("can't read initial header packet");
            }
            if (vorbisInfo.synthesis_headerin(vorbisComment, oggPacket) < 0) {
                throw new IOException("packet is not a vorbis header");
            }
        }

        /**
         * Read the comment header and the codebook header pages.
         */
        private void readCommentAndCodebookHeaders() throws IOException {
            for (int i = 0; i < 2; i++) {
                readOggPacket();
                if (vorbisInfo.synthesis_headerin(vorbisComment, oggPacket) < 0) {
                    throw new IOException("packet is not a vorbis header");
                }
            }
        }

        /** */
        private void processComments() {
            byte[][] ptr = vorbisComment.user_comments;
            String currComment;
            songComments.clear();
            for (byte[] bytes : ptr) {
                if (bytes == null) {
                    break;
                }
                currComment = (new String(bytes, 0, bytes.length - 1)).trim();
                songComments.add(currComment);

//                if (currComment.toUpperCase().startsWith("ARTIST")) {
//                    String artistLabelValue = currComment.substring(7);
//                } else if (currComment.toUpperCase().startsWith("TITLE")) {
//                    String titleLabelValue = currComment.substring(6);
//                    String miniDragLabel = currComment.substring(6);
//                }

                logger.log(Level.TRACE, "Comment: " + currComment);
            }
            currComment = "Bitstream: " + vorbisInfo.channels + " channel," + vorbisInfo.rate + "Hz";
            songComments.add(currComment);
            logger.log(Level.TRACE, currComment);

            if (logger.isLoggable(Level.TRACE))
                currComment = "Encoded by: " + new String(vorbisComment.vendor, 0, vorbisComment.vendor.length - 1);
            songComments.add(currComment);
            logger.log(Level.TRACE, currComment);
        }

        /**
         * Setup structures needed for vorbis decoding.
         * Precondition: vorbisInfo has to be initialized completely
         * (i.e. all three headers are read).
         */
        private void setupVorbisStructures() {
            convSize = BUFFER_SIZE / vorbisInfo.channels;
            vorbisDspState.synthesis_init(vorbisInfo);
            vorbisBlock.init(vorbisDspState);
            pcm = new float[1][][];
            index = new int[vorbisInfo.channels];
        }

        /**
         * Decode a packet of vorbis data.
         * This method assumes that a packet is available in
         * {@link #oggPacket oggPacket}. The content of this
         * packet is run through the decoder. The resulting
         * PCM data are written to the circular buffer.
         */
        private void decodeDataPacket() {
            int samples;
            if (vorbisBlock.synthesis(oggPacket) == 0) { // test for success!
                vorbisDspState.synthesis_blockin(vorbisBlock);
            }
            while ((samples = vorbisDspState.synthesis_pcmout(pcm, index)) > 0) {
                float[][] pcmf = this.pcm[0];
                int bout = (Math.min(samples, convSize));
                // convert floats to signed ints and interleave
                for (int channel = 0; channel < vorbisInfo.channels; channel++) {
                    int pointer = channel * getSampleSizeInBytes();
                    int mono = index[channel];
                    for (int j = 0; j < bout; j++) {
                        float val = pcmf[channel][mono + j];
                        clipAndWriteSample(val, pointer);
                        pointer += getFrameSize();
                    }
                }
                vorbisDspState.synthesis_read(bout);
                getCircularBuffer().write(convBuffer, 0, getFrameSize() * bout);
            }
        }

        /**
         * Scale and clip the sample and write it to convBuffer.
         */
        private void clipAndWriteSample(float sample, int pointer) {
            int _sample;
            // TODO check if clipping is necessary
            if (sample > 1.0F) {
                sample = 1.0F;
            }
            if (sample < -1.0F) {
                sample = -1.0F;
            }
            switch (getFormat().getSampleSizeInBits()) {
            case 16:
                _sample = (int) (sample * 32767.0F);
                if (isBigEndian()) {
                    convBuffer[pointer++] = (byte) (_sample >> 8);
                    convBuffer[pointer] = (byte) (_sample & 0xFF);
                } else {
                    convBuffer[pointer++] = (byte) (_sample & 0xFF);
                    convBuffer[pointer] = (byte) (_sample >> 8);
                }
                break;

            case 24:
                _sample = (int) (sample * 8388607.0F);
                if (isBigEndian()) {
                    convBuffer[pointer++] = (byte) (_sample >> 16);
                    convBuffer[pointer++] = (byte) ((_sample >>> 8) & 0xFF);
                    convBuffer[pointer] = (byte) (_sample & 0xFF);
                } else {
                    convBuffer[pointer++] = (byte) (_sample & 0xFF);
                    convBuffer[pointer++] = (byte) ((_sample >>> 8) & 0xFF);
                    convBuffer[pointer] = (byte) (_sample >> 16);
                }
                break;

            case 32:
                _sample = (int) (sample * 2147483647.0F);
                if (isBigEndian()) {
                    convBuffer[pointer++] = (byte) (_sample >> 24);
                    convBuffer[pointer++] = (byte) ((_sample >>> 16) & 0xFF);
                    convBuffer[pointer++] = (byte) ((_sample >>> 8) & 0xFF);
                    convBuffer[pointer] = (byte) (_sample & 0xFF);
                } else {
                    convBuffer[pointer++] = (byte) (_sample & 0xFF);
                    convBuffer[pointer++] = (byte) ((_sample >>> 8) & 0xFF);
                    convBuffer[pointer++] = (byte) ((_sample >>> 16) & 0xFF);
                    convBuffer[pointer] = (byte) (_sample >> 24);
                }
                break;
            }
        }

        /**
         * Read an ogg packet.
         * This method does everything necessary to read an ogg
         * packet. If needed, it calls
         * {@link #readOggPage readOggPage()}, which, in turn, may
         * read more data from the stream. The resulting packet is
         * placed in {@link #oggPacket oggPacket} (for which the
         * reference is not altered; is has to be initialized before).
         */
        private void readOggPacket() throws IOException {
            while (true) {
                int result = oggStreamState.packetout(oggPacket);
                if (result == 1) {
                    return;
                }
                if (result == -1) {
                    throw new IOException("can't read packet");
                }
                readOggPage();
                if (oggStreamState.pagein(oggPage) < 0) {
                    throw new IOException("can't read page of Ogg bitstream data");
                }
            }
        }

        /**
         * Read an ogg page.
         * This method does everything necessary to read an ogg
         * page. If needed, it reads more data from the stream.
         * The resulting page is
         * placed in {@link #oggPage oggPage} (for which the
         * reference is not altered; is has to be initialized before).
         * <p>
         * Note: this method doesn't deliver the page read to a
         * StreamState object (which assembles pages to packets).
         * This has to be done by the caller.
         */
        private void readOggPage() throws IOException {
            while (true) {
                int result = oggSyncState.pageout(oggPage);
                if (result == 1) {
                    return;
                }
                // we need more data from the stream
                int index = oggSyncState.buffer(BUFFER_SIZE);
                // TODO call stream.read() directly
                int bytes = readFromStream(oggSyncState.data, index, BUFFER_SIZE);
                // TODO This clause should become obsolete; readFromStream() should
                // propagate exceptions directly.
                if (bytes == -1) {
                    throw new EOFException();
                }
                oggSyncState.wrote(bytes);
            }
        }

        /**
         * Read raw data from to ogg bitstream.
         * Reads from  {@link #oggBitStream oggBitStream} a
         * specified number of bytes into a buffer, starting
         * at a specified buffer index.
         *
         * @param buffer  the where the read data should be put into. Its length has to be at least start + length.
         * @param start
         * @param length the number of bytes to read
         * @return the number of bytes read (maybe 0) or
         * -1 if there is no more data in the stream.
         */
        private int readFromStream(byte[] buffer, int start, int length) throws IOException {
            return oggBitStream.read(buffer, start, length);
        }

        /** */
        private int getSampleSizeInBytes() {
            return getFormat().getFrameSize() / getFormat().getChannels();
        }

        /** */
        private int getFrameSize() {
            return getFormat().getFrameSize();
        }

        /**
         * Returns if this stream (the decoded one) is big endian.
         *
         * @return true if this stream is big endian.
         */
        private boolean isBigEndian() {
            return getFormat().isBigEndian();
        }

        @Override
        public void close() throws IOException {
            super.close();
            oggBitStream.close();
        }
    }
}
