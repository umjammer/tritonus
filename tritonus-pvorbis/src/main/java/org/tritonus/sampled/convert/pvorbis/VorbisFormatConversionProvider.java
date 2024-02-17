/*
 *  Copyright (c) 1999 - 2004 by Matthias Pfisterer
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

package org.tritonus.sampled.convert.pvorbis;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import biniu.ogg.Packet;
import biniu.ogg.Page;
import biniu.ogg.StreamState;
import biniu.ogg.SyncState;
import biniu.vorbis.Block;
import biniu.vorbis.Comment;
import biniu.vorbis.DspState;
import biniu.vorbis.Info;
import biniu.vorbis.VorbisEnc;
import org.tritonus.share.sampled.AudioFormats;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;
import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider;
import vavi.util.Debug;

import static java.lang.System.getLogger;


/**
 * ConversionProvider for ogg vorbis encoding.
 * This FormatConversionProvider uses the native libraries libogg,
 * libvorbis and libvorbisenc to implement encoding to ogg vorbis.
 *
 * @author Matthias Pfisterer
 */
public class VorbisFormatConversionProvider extends TEncodingFormatConversionProvider {

    private static final Logger logger= getLogger("org.tritonus.TraceAudioConverter");

    // only used as abbreviation
    public static final AudioFormat.Encoding VORBIS = new AudioFormat.Encoding("VORBIS");
    private static final AudioFormat.Encoding PCM_SIGNED = new AudioFormat.Encoding("PCM_SIGNED");

    private static final AudioFormat[] INPUT_FORMATS = {
            // mono, 16 bit signed
            new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, false),
            new AudioFormat(PCM_SIGNED, -1.0F, 16, 1, 2, -1.0F, true),
            // stereo, 16 bit signed
            new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, false),
            new AudioFormat(PCM_SIGNED, -1.0F, 16, 2, 4, -1.0F, true),
            // TODO other channel configurations

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

//    private static final AudioFormat[] OUTPUT_FORMATS = {
//            // mono
//            // TODO mechanism to make the double specification with
//            // different endianess obsolete.
//            new AudioFormat(VORBIS, -1.0F, -1, 1, -1, -1.0F, false),
//            new AudioFormat(VORBIS, -1.0F, -1, 1, -1, -1.0F, true),
//            // stereo
//            new AudioFormat(VORBIS, -1.0F, -1, 2, -1, -1.0F, false),
//            new AudioFormat(VORBIS, -1.0F, -1, 2, -1, -1.0F, true),
//            // TODO other channel configurations
//    };

    // Default settings for encoding.
    private static final boolean DEFAULT_VBR = true;
    private static final float DEFAULT_QUALITY = 0.5F;
    private static final int DEFAULT_MAX_BITRATE = 256;
    private static final int DEFAULT_NOM_BITRATE = 128;
    private static final int DEFAULT_MIN_BITRATE = 32;

    /**
     * Constructor.
     */
    public VorbisFormatConversionProvider() {
        super(
                Arrays.asList(INPUT_FORMATS),
                Arrays.asList(INPUT_FORMATS)
//                Arrays.asList(OUTPUT_FORMATS),
//                true, // new behaviour
//                false  // bidirectional .. constants UNIDIR../BIDIR..?
        );
        logger.log(Level.TRACE, "VorbisFormatConversionProvider.<init>(): begin");
        logger.log(Level.TRACE, "VorbisFormatConversionProvider.<init>(): end");
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        logger.log(Level.TRACE, ">VorbisFormatConversionProvider.getAudioInputStream(): begin");
        // The AudioInputStream to return.
        AudioInputStream convertedAudioInputStream;

        logger.log(Level.TRACE, "checking if conversion supported");
        logger.log(Level.TRACE, "from: " + audioInputStream.getFormat());
        logger.log(Level.TRACE, "to: " + targetFormat);

        // what is this ???
        targetFormat = getDefaultTargetFormat(targetFormat, audioInputStream.getFormat());
        if (isConversionSupported(targetFormat, audioInputStream.getFormat())) {
            if (targetFormat.getEncoding().equals(VORBIS)) {
                logger.log(Level.TRACE, "conversion supported; trying to create EncodedVorbisAudioInputStream");
                convertedAudioInputStream = new EncodedVorbisAudioInputStream(targetFormat, audioInputStream);
            } else {
                logger.log(Level.TRACE, "conversion supported; trying to create DecodedVorbisAudioInputStream");
                convertedAudioInputStream = new DecodedVorbisAudioInputStream(targetFormat, audioInputStream);
            }
        } else {
            logger.log(Level.TRACE, "<conversion not supported; throwing IllegalArgumentException");
            throw new IllegalArgumentException("conversion not supported");
        }
        logger.log(Level.TRACE, "<VorbisFormatConversionProvider.getAudioInputStream(): end");
        return convertedAudioInputStream;
    }

    protected AudioFormat getDefaultTargetFormat(AudioFormat targetFormat, AudioFormat sourceFormat) {
        logger.log(Level.TRACE, "VorbisFormatConversionProvider.getDefaultTargetFormat(): target format: " + targetFormat);
        logger.log(Level.TRACE, "VorbisFormatConversionProvider.getDefaultTargetFormat(): source format: " + sourceFormat);
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
        logger.log(Level.TRACE, "VorbisFormatConversionProvider.getDefaultTargetFormat(): new target format: " + newTargetFormat);
        // hacked together...
        // ... only works for PCM target encoding ...
        newTargetFormat = new AudioFormat(targetFormat.getEncoding(),
                sourceFormat.getSampleRate(),
                newTargetFormat.getSampleSizeInBits(),
                newTargetFormat.getChannels(),
                newTargetFormat.getFrameSize(),
                sourceFormat.getSampleRate(),
                newTargetFormat.isBigEndian(),
                targetFormat.properties());
        logger.log(Level.TRACE, "VorbisFormatConversionProvider.getDefaultTargetFormat(): really new target format: " + newTargetFormat);
        return newTargetFormat;
    }

    /**
     * AudioInputStream returned on encoding to ogg vorbis.
     * An instance of this class is returned if you call
     * AudioSystem.getAudioInputStream(AudioFormat, AudioInputStream)
     * to encode a PCM stream. This class contains the logic
     * of maintaining buffers and calling the encoder.
     */
    public static class EncodedVorbisAudioInputStream extends TAsynchronousFilteredAudioInputStream {

        /**
         * How many PCM frames to encode at once.
         */
        private static final int READ = 1024;

        private AudioInputStream m_decodedStream;
        private byte[] m_abReadbuffer;

        private StreamState m_streamState;
        private Page m_page;
        private Packet m_packet;

        private Info m_info;
        private Comment m_comment;
        private DspState m_dspState;
        private Block m_block;
        private VorbisEnc encoder;

        private boolean eos = false;

        public EncodedVorbisAudioInputStream(AudioFormat outputFormat, AudioInputStream inputStream) {
            super(outputFormat, AudioSystem.NOT_SPECIFIED, 262144, 16384);
            logger.log(Level.TRACE, ">EncodedVorbisAudioInputStream.<init>(): begin");
            m_decodedStream = inputStream;
            m_abReadbuffer = new byte[READ * getFrameSize()];
            Object property;
            Debug.println("properties: " + outputFormat.properties());

            property = outputFormat.getProperty("vbr");
            boolean bUseVBR = DEFAULT_VBR;
            if (property instanceof Boolean) {
                bUseVBR = (Boolean) property;
            }

            property = outputFormat.getProperty("quality");
            float fQuality = DEFAULT_QUALITY; // TODO cause .ArrayIndexOutOfBoundsException: Index 16 out of bounds for length 16 at VorbisEnc#setTonemask(VorbisEnc.java:228)
            if (property instanceof Integer) {
                fQuality = (Integer) property / 10.0F;
            }

            property = outputFormat.getProperty("bitrate");
            int nNominalBitrate = DEFAULT_NOM_BITRATE;
            if (property instanceof Integer) {
                nNominalBitrate = (Integer) property / 1024;
            }

            property = outputFormat.getProperty("vorbis.min_bitrate");
            int nMinBitrate = DEFAULT_MIN_BITRATE;
            if (property instanceof Integer) {
                nMinBitrate = (Integer) property / 1024;
            }

            property = outputFormat.getProperty("vorbis.max_bitrate");
            int nMaxBitrate = DEFAULT_MAX_BITRATE;
            if (property instanceof Integer) {
                nMaxBitrate = (Integer) property / 1024;
            }

            m_streamState = new StreamState();
            m_page = new Page();
            m_packet = new Packet();

            m_info = new Info();
            m_comment = new Comment();
            m_dspState = new DspState();
            m_block = new Block(m_dspState);

            encoder = new VorbisEnc();

            m_info.init();

            int nSampleRate = (int) inputStream.getFormat().getSampleRate();
            logger.log(Level.TRACE, "sample rate: " + nSampleRate);
            logger.log(Level.TRACE, "channels: " + getChannels());
            if (bUseVBR) {
                Debug.printf("VBR: ch: %d, rate: %d, q: %3.1f", getChannels(), nSampleRate, fQuality);
                int r = encoder.initVBR(m_info, getChannels(), nSampleRate, fQuality);
                if (r != 0)
                    throw new IllegalStateException("initVBR: unexpected return value: " + r);
            } else {
                Debug.printf("non VBR: ch: %d, rate: %d, q: %3.1f", getChannels(), nSampleRate, fQuality);
                int r = encoder.init(m_info, getChannels(), nSampleRate, nMaxBitrate, nNominalBitrate, nMinBitrate);
                if (r != 0)
                    throw new IllegalStateException("init: unexpected return value: " + r);
            }

            m_comment.init();
            m_comment.addTag("ENCODER", "Tritonus libvorbis via jna");
            property = outputFormat.getProperty("vorbis.comments");
            if (property instanceof List<?> comments) {
                logger.log(Level.TRACE, "<init>: comments present in target format");
                for (Object comm : comments) {
                    if (comm instanceof String) {
                        m_comment.addComment((String) comm);
                    }
                }
            }

            m_dspState.analysisInit(m_info);
            m_block.blockInit(m_dspState);

            Random random;
            property = outputFormat.getProperty("vorbis.test");
            if (property instanceof Boolean test && test) {
                Debug.println("use test random seed");
                random = new Random(314159265358979L);
            } else {
                random = new Random(System.currentTimeMillis());
            }
            m_streamState.init(random.nextInt());

            Packet header = new Packet();
            Packet headerComm = new Packet();
            Packet headerCode = new Packet();

            m_dspState.analysisHeaderOut(m_comment, header, headerComm, headerCode);
            m_streamState.packetIn(header);
            m_streamState.packetIn(headerComm);
            m_streamState.packetIn(headerCode);

            while (true) {
                boolean result = m_streamState.flush(m_page);
                if (!result) {
                    break;
                }
                getCircularBuffer().write(m_page.header_base, m_page.header, m_page.header_len);
                getCircularBuffer().write(m_page.body_base, m_page.body, m_page.body_len);
            }

            logger.log(Level.TRACE, "<init>: end");
        }

        @Override
        public void execute() {
            logger.log(Level.TRACE, ">execute(): begin");
            int nFrameSize = getFrameSize();
            int nChannels = getChannels();
            boolean bBigEndian = isBigEndian();
            int nBytesPerSample = nFrameSize / nChannels;
            int nSampleSizeInBits = nBytesPerSample * 8;
            float fScale = (float) Math.pow(2.0, nSampleSizeInBits - 1);
            logger.log(Level.TRACE, "frame size: " + nFrameSize);
            logger.log(Level.TRACE, "channels: " + nChannels);
            logger.log(Level.TRACE, "big endian: " + bBigEndian);
            logger.log(Level.TRACE, "sample size (bits): " + nSampleSizeInBits);
            logger.log(Level.TRACE, "bytes per sample: " + nBytesPerSample);
            logger.log(Level.TRACE, "scale: " + fScale);

            while (!eos && writeMore()) {
                logger.log(Level.TRACE, "writeMore: " + writeMore());
                int bytes;
                try {
                    bytes = m_decodedStream.read(m_abReadbuffer);
                    logger.log(Level.TRACE, "read from PCM stream: " + bytes);
                } catch (IOException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);

                    m_streamState.clear();
                    m_block.clear();
                    m_dspState.clear();
                    m_comment.clear();
                    m_info.clear();
                    try {
                        close(); // TODO is it ok as SPI?
                    } catch (IOException e1) {
                        logger.log(Level.ERROR, e1.getMessage(), e1);
                    }
                    logger.log(Level.TRACE, "<");
                    return;
                }

                if (bytes == 0 || bytes == -1) {
                    logger.log(Level.TRACE, "EOS reached; calling DspState.write(0)");
                    m_dspState.analysisWrote(0);
                } else {
                    int nFrames = bytes / nFrameSize;
                    logger.log(Level.TRACE, "processing frames: " + nFrames);
                    float[][] buffer = m_dspState.analysisBuffer(READ);
                    // uninterleave samples
                    for (int i = 0, l = m_dspState.pcm_current; i < bytes / 4; i++, l++) {
                        buffer[0][l] = ((m_abReadbuffer[i * 4 + 1] << 8) |
                                (0x00ff & (int) m_abReadbuffer[i * 4])) / 32768.f;
                        buffer[1][l] = ((m_abReadbuffer[i * 4 + 3] << 8) |
                                (0x00ff & (int) m_abReadbuffer[i * 4 + 2])) / 32768.f;
                    }
                    m_dspState.analysisWrote(bytes / 4);
                }

                while (m_block.analysisBlockOut()) {
                    m_block.analysis(null);
                    m_block.bitrateAddBlock();
                    while (m_dspState.bitrateFlushPacket(m_packet)) {
                        m_streamState.packetIn(m_packet);
                        while (!eos /* && writeMore() */) {
                            boolean result = m_streamState.pageOut(m_page);
                            if (!result) {
                                break;
                            }
                            getCircularBuffer().write(m_page.header_base, m_page.header, m_page.header_len);
                            getCircularBuffer().write(m_page.body_base, m_page.body, m_page.body_len);

                            if (m_page.eos()) {
                                eos = true;
                                logger.log(Level.TRACE, "page has detected EOS");
                            }
                        }
                    }
                }
            }

            if (eos) {
                logger.log(Level.TRACE, "EOS; shutting down encoder");

                m_streamState.clear();
                m_block.clear();
                m_dspState.clear();
                m_comment.clear();
                m_info.clear();
                getCircularBuffer().close();
                try {
                    close();
                } catch (IOException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
            }

            logger.log(Level.TRACE, "<execute(): end");
        }

        private int getChannels() {
            return m_decodedStream.getFormat().getChannels();
        }

        private int getFrameSize() {
            return m_decodedStream.getFormat().getFrameSize();
        }

        private boolean isBigEndian() {
            return m_decodedStream.getFormat().isBigEndian();
        }

        @Override
        public void close() throws IOException {
            super.close();
            m_decodedStream.close();
        }

        // copied from TConversionTool
        private static int bytesToInt16(byte[] buffer, int byteOffset, boolean bigEndian) {
            return bigEndian ?
                    ((buffer[byteOffset] << 8) | (buffer[byteOffset + 1] & 0xFF)) :
                    ((buffer[byteOffset + 1] << 8) | (buffer[byteOffset] & 0xFF));
        }
    }

    /**
     * AudioInputStream returned on decoding of ogg vorbis.
     * An instance of this class is returned if you call
     * AudioSystem.getAudioInputStream(AudioFormat, AudioInputStream)
     * to decode an ogg/vorbis stream. This class contains the logic
     * of maintaining buffers and calling the decoder.
     * <p>
     * TODO Class should be private, but is public due to a bug (?) in the aspectj compiler.
     */
    /* private */ public static class DecodedVorbisAudioInputStream extends TAsynchronousFilteredAudioInputStream {

        private static final int INPUT_BUFFER_SIZE = 4096;
        private static final int BUFFER_MULTIPLE = 4;
        private static final int BUFFER_SIZE = BUFFER_MULTIPLE * 256 * 2;
        private static final int CONVSIZE = BUFFER_SIZE * 2;

        private InputStream m_oggBitStream;

        private byte[] m_abInputBuffer;
        // Ogg structures
        private SyncState m_oggSyncState = null;
        private StreamState m_oggStreamState = null;
        private Page m_oggPage = null;
        private Packet m_oggPacket = null;

        // Vorbis structures
        private Info m_vorbisInfo = null;
        private Comment m_vorbisComment = null;
        private DspState m_vorbisDspState = null;
        private Block m_vorbisBlock = null;

        // private List m_songComments = new ArrayList();
        // is altered later in a dubious way
        @SuppressWarnings("unused")
        private int convsize = -1; // BUFFER_SIZE * 2;
        // TODO further checking
        private byte[] convbuffer = new byte[CONVSIZE];
        private float[][] m_aPcmOut;

        private boolean m_bHeadersExpected;

        /**
         * Constructor.
         */
        public DecodedVorbisAudioInputStream(AudioFormat outputFormat, AudioInputStream bitStream) {
            super(outputFormat, AudioSystem.NOT_SPECIFIED);
            logger.log(Level.TRACE, "DecodedVorbisAudioInputStream.<init>(): begin");

            m_oggBitStream = bitStream;
            m_bHeadersExpected = true;
            init_jorbis();

            logger.log(Level.TRACE, "DecodedVorbisAudioInputStream.<init>(): end");
        }

        /**
         * Initializes all the jOrbis and jOgg vars that are used for song playback.
         */
        private void init_jorbis() {
            m_abInputBuffer = new byte[INPUT_BUFFER_SIZE];
            m_oggSyncState = new SyncState();
            m_oggStreamState = new StreamState();
            m_oggPage = new Page();
            m_oggPacket = new Packet();

            m_vorbisInfo = new Info();
            m_vorbisComment = new Comment();
            m_vorbisDspState = new DspState();
            m_vorbisBlock = new Block(m_vorbisDspState);
            m_vorbisDspState.analysisInit(m_vorbisInfo);
            m_vorbisBlock.init(m_vorbisDspState);

            m_oggSyncState.init();
        }

        /**
         * Callback from circular buffer.
         */
        @Override
        public void execute() {
            logger.log(Level.TRACE, ">DecodedVorbisAudioInputStream.execute(): begin");

            if (m_bHeadersExpected) {
                logger.log(Level.TRACE, "reading headers...");
                // Headers (+ Comments).
                try {
                    readHeaders();
                } catch (IOException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);

                    closePhysicalStream();

                    logger.log(Level.TRACE, "<DecodedVorbisAudioInputStream.execute(): end");
                    return;
                }
                m_bHeadersExpected = false;
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

                    logger.log(Level.TRACE, "<DecodedVorbisAudioInputStream.execute(): end");
                    return;
                }
                decodeDataPacket();
            }
            if (m_oggPacket.isEos()) {
                logger.log(Level.TRACE, "end of vorbis stream reached");
                // The end of the vorbis stream is reached.
                // So we shut down the logical bitstream and
                // vorbis structures.
                m_oggStreamState.clear();
                m_vorbisBlock.clear();
                m_vorbisDspState.clear();
                m_vorbisInfo.clear();
                m_bHeadersExpected = true;
            }
            logger.log(Level.TRACE, "<DecodedVorbisAudioInputStream.execute(): end");
        }

        private void closePhysicalStream() {
            logger.log(Level.TRACE, "DecodedVorbisAudioInputStream.closePhysicalStream(): begin");

            m_oggSyncState.clear();
            try {
                if (m_oggBitStream != null) {
                    m_oggBitStream.close();
                }
                getCircularBuffer().close();
            } catch (Exception e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }

            logger.log(Level.TRACE, "DecodedVorbisAudioInputStream.closePhysicalStream(): end");
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
         * @throw IOException
         */
        private void readIdentificationHeader() throws IOException {
            readOggPage();
            m_oggStreamState.init(m_oggPage.serialNo());
            m_vorbisInfo.init();
            m_vorbisComment.init();
            if (m_oggStreamState.pageIn(m_oggPage) < 0) {
                throw new IOException("can't read first page of Ogg bitstream data, perhaps stream version mismatch");
            }
            if (m_oggStreamState.packetOut(m_oggPacket) != 1) {
                throw new IOException("can't read initial header packet");
            }
            if (m_vorbisInfo.headerIn(m_vorbisComment, m_oggPacket) < 0) {
                throw new IOException("packet is not a vorbis header");
            }
        }

        /**
         * Read the comment header and the codebook header pages.
         */
        private void readCommentAndCodebookHeaders() throws IOException {
            for (int i = 0; i < 2; i++) {
                readOggPacket();
                if (m_vorbisInfo.headerIn(m_vorbisComment, m_oggPacket) < 0) {
                    throw new IOException("packet is not a vorbis header");
                }
            }
        }

        /**
         *
         */
        private void processComments() {
            logger.log(Level.TRACE, "DecodedVorbisAudioInputStream.processComments(): begin");
            logger.log(Level.TRACE, "DecodedVorbisAudioInputStream.processComments(): encoded by: " + m_vorbisComment.getVendor());

            String[] astrComments = new String[m_vorbisComment.comments];
            logger.log(Level.TRACE, "user comments:");
            for (int i = 0; i < m_vorbisComment.comments; i++) {
                astrComments[i] = m_vorbisComment.getComment(i);
                logger.log(Level.TRACE, astrComments[i]);
            }
//            byte[][] ptr = m_vorbisComment.user_comments;
//            String currComment = "";
//            m_songComments.clear();
//            for (int j = 0; j < ptr.length; j++) {
//                if (ptr[j] == null) {
//                    break;
//                }
//                currComment = (new String(ptr[j], 0, ptr[j].length - 1)).trim();
//                m_songComments.add(currComment);
//                if (currComment.toUpperCase().startsWith("ARTIST")) {
//                    String artistLabelValue = currComment.substring(7);
//                } else if (currComment.toUpperCase().startsWith("TITLE")) {
//                    String titleLabelValue = currComment.substring(6);
//                    String miniDragLabel = currComment.substring(6);
//                }
//                logger.log(Level.TRACE, "Comment: " + currComment);
//            }
//            currComment = "Bitstream: " + m_vorbisInfo.getChannels() + " channel," + m_vorbisInfo.rate + "Hz";
//            m_songComments.add(currComment);
//            logger.log(Level.TRACE, currComment);
//            m_songComments.add(currComment);
//            logger.log(Level.TRACE, currComment);
            logger.log(Level.TRACE, "DecodedVorbisAudioInputStream.processComments(): end");
        }

        /**
         * Setup structures needed for vorbis decoding.
         * Precondition: m_vorbisInfo has to be initialized completely
         * (i.e. all three headers are read).
         */
        private void setupVorbisStructures() {
            convsize = BUFFER_SIZE / m_vorbisInfo.channels;
            m_vorbisDspState.analysisInit(m_vorbisInfo);
            m_vorbisBlock.init(m_vorbisDspState);
            m_aPcmOut = new float[m_vorbisInfo.channels][];
        }

        /**
         * Decode a packet of vorbis data.
         * This method assumes that a packet is available in
         * {@link #m_oggPacket m_oggPacket}. The content of this
         * packet is run through the decoder. The resulting
         * PCM data are written to the circular buffer.
         */
        private void decodeDataPacket() {
            int nSamples;
            if (m_vorbisBlock.analysis(m_oggPacket) == 0) { // test for success!
                m_vorbisDspState.blockIn(m_vorbisBlock);
            }
            float[][][] tmp = new float[1][][];
            tmp[0] = m_aPcmOut;
            while ((nSamples = m_vorbisDspState.pcmOut(tmp)) > 0) {
                m_aPcmOut = tmp[0];
                // convert floats to signed ints and
                // interleave
                for (int nChannel = 0; nChannel < m_vorbisInfo.channels; nChannel++) {
                    int pointer = nChannel * getSampleSizeInBytes();
                    for (int j = 0; j < nSamples; j++) {
                        float fVal = m_aPcmOut[nChannel][j];
                        clipAndWriteSample(fVal, pointer);
                        pointer += getFrameSize();
                    }
                }
                m_vorbisDspState.read(nSamples);
                getCircularBuffer().write(convbuffer, 0, getFrameSize() * nSamples);
            }
        }

        /**
         * Scale and clip the sample and write it to convbuffer.
         */
        private void clipAndWriteSample(float fSample, int nPointer) {
            int nSample;
            // TODO check if clipping is necessary
            if (fSample > 1.0F) {
                fSample = 1.0F;
            }
            if (fSample < -1.0F) {
                fSample = -1.0F;
            }
            switch (getFormat().getSampleSizeInBits()) {
            case 16:
                nSample = (int) (fSample * 32767.0F);
                if (isBigEndian()) {
                    convbuffer[nPointer++] = (byte) (nSample >> 8);
                    convbuffer[nPointer] = (byte) (nSample & 0xFF);
                } else {
                    convbuffer[nPointer++] = (byte) (nSample & 0xFF);
                    convbuffer[nPointer] = (byte) (nSample >> 8);
                }
                break;

            case 24:
                nSample = (int) (fSample * 8388607.0F);
                if (isBigEndian()) {
                    convbuffer[nPointer++] = (byte) (nSample >> 16);
                    convbuffer[nPointer++] = (byte) ((nSample >>> 8) & 0xFF);
                    convbuffer[nPointer] = (byte) (nSample & 0xFF);
                } else {
                    convbuffer[nPointer++] = (byte) (nSample & 0xFF);
                    convbuffer[nPointer++] = (byte) ((nSample >>> 8) & 0xFF);
                    convbuffer[nPointer] = (byte) (nSample >> 16);
                }
                break;

            case 32:
                nSample = (int) (fSample * 2147483647.0F);
                if (isBigEndian()) {
                    convbuffer[nPointer++] = (byte) (nSample >> 24);
                    convbuffer[nPointer++] = (byte) ((nSample >>> 16) & 0xFF);
                    convbuffer[nPointer++] = (byte) ((nSample >>> 8) & 0xFF);
                    convbuffer[nPointer] = (byte) (nSample & 0xFF);
                } else {
                    convbuffer[nPointer++] = (byte) (nSample & 0xFF);
                    convbuffer[nPointer++] = (byte) ((nSample >>> 8) & 0xFF);
                    convbuffer[nPointer++] = (byte) ((nSample >>> 16) & 0xFF);
                    convbuffer[nPointer] = (byte) (nSample >> 24);
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
         * placed in {@link #m_oggPacket m_oggPacket} (for which the
         * reference is not altered; is has to be initialized before).
         */
        private void readOggPacket() throws IOException {
            while (true) {
                int result = m_oggStreamState.packetOut(m_oggPacket);
                if (result == 1) {
                    return;
                }
                if (result == -1) {
                    throw new IOException("can't read packet");
                }
                readOggPage();
                if (m_oggStreamState.pageIn(m_oggPage) < 0) {
                    throw new IOException("can't read page of Ogg bitstream data");
                }
            }
        }

        /**
         * Read an ogg page.
         * This method does everything necessary to read an ogg
         * page. If needed, it reads more data from the stream.
         * The resulting page is
         * placed in {@link #m_oggPage m_oggPage} (for which the
         * reference is not altered; is has to be initialized before).
         * <p>
         * Note: this method doesn't deliver the page read to a
         * StreamState object (which assembles pages to packets).
         * This has to be done by the caller.
         */
        private void readOggPage() throws IOException {
            while (true) {
                int result = m_oggSyncState.pageOut(m_oggPage);
                if (result == 1) {
                    return;
                }
                // we need more data from the stream
                // TODO call stream.read() directly
                int nBytes = readFromStream(m_abInputBuffer, 0, m_abInputBuffer.length);
                // TODO This clause should become obsolete; readFromStream() should
                // propagate exceptions directly.
                if (nBytes == -1) {
                    throw new EOFException();
                }
                m_oggSyncState.write(m_abInputBuffer, nBytes);
            }
        }

        /**
         * Read raw data from to ogg bitstream.
         * Reads from  {@link #m_oggBitStream m_oggBitStream} a
         * specified number of bytes into a buffer, starting
         * at a specified buffer index.
         *
         * @param buffer  the where the read data should be put into. Its length has to be at least nStart + nLength.
         * @param nStart
         * @param nLength the number of bytes to read
         * @return the number of bytes read (maybe 0) or
         * -1 if there is no more data in the stream.
         */
        private int readFromStream(byte[] buffer, int nStart, int nLength) throws IOException {
            return m_oggBitStream.read(buffer, nStart, nLength);
        }

        /**  */
        private int getSampleSizeInBytes() {
            return getFormat().getFrameSize() / getFormat().getChannels();
        }

        /**  */
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
            m_oggBitStream.close();
        }
    }
}
