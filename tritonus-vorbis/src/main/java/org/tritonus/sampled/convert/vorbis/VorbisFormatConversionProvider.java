/*
 *  Copyright (c) 1999 - 2003 by Matthias Pfisterer
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

package org.tritonus.sampled.convert.vorbis;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.tritonus.lowlevel.ogg.Packet;
import org.tritonus.lowlevel.ogg.Page;
import org.tritonus.lowlevel.ogg.StreamState;
import org.tritonus.lowlevel.ogg.SyncState;
import org.tritonus.lowlevel.vorbis.Block;
import org.tritonus.lowlevel.vorbis.Comment;
import org.tritonus.lowlevel.vorbis.DspState;
import org.tritonus.lowlevel.vorbis.Info;
import org.tritonus.share.sampled.AudioFormats;
import org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream;
import org.tritonus.share.sampled.convert.TEncodingFormatConversionProvider;

import static java.lang.System.getLogger;


/**
 * ConversionProvider for ogg vorbis encoding.
 * This FormatConversionProvider uses the native libraries libogg,
 * libvorbis and libvorbisenc to implement encoding to ogg vorbis.
 * <p>
 * This file is part of Tritonus: http://www.tritonus.org/
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
        super(List.of(INPUT_FORMATS),
                List.of(INPUT_FORMATS)
                // true, // new behaviour
                // false // bidirectional .. constants UNIDIR../BIDIR..?
        );
        logger.log(Level.TRACE, "begin");

        logger.log(Level.TRACE, "end");
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        logger.log(Level.TRACE, "begin");

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

        logger.log(Level.TRACE, "end");

        return convertedAudioInputStream;
    }

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
        newTargetFormat = new AudioFormat(
                targetFormat.getEncoding(),
                sourceFormat.getSampleRate(),
                newTargetFormat.getSampleSizeInBits(),
                newTargetFormat.getChannels(),
                newTargetFormat.getFrameSize(),
                sourceFormat.getSampleRate(),
                newTargetFormat.isBigEndian(),
                targetFormat.properties());

        logger.log(Level.TRACE, "really new target format: " + newTargetFormat);

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

        private AudioInputStream decodedStream;
        private byte[] readbuffer;

        private StreamState streamState;
        private Page page;
        private Packet packet;

        private Info info;
        private Comment comment;
        private DspState dspState;
        private Block block;

        private boolean eos = false;

        public EncodedVorbisAudioInputStream(AudioFormat outputFormat, AudioInputStream inputStream) {
            super(outputFormat, AudioSystem.NOT_SPECIFIED, 262144, 16384);
            logger.log(Level.TRACE, "begin");

            decodedStream = inputStream;
            readbuffer = new byte[READ * getFrameSize()];
            Object property;

            property = outputFormat.getProperty("vbr");
            boolean useVBR = DEFAULT_VBR;
            if (property instanceof Boolean) {
                useVBR = (Boolean) property;

                logger.log(Level.TRACE, "using VBR: " + useVBR);
            }

            property = outputFormat.getProperty("quality");
            float quality = DEFAULT_QUALITY;
            if (property instanceof Integer) {
                quality = (Integer) property / 10.0F;
                useVBR = true;

                logger.log(Level.TRACE, "using quality (automatically switching VBR on): " + quality);
            }

            int nominalBitrate = DEFAULT_NOM_BITRATE;
            int minBitrate = DEFAULT_MIN_BITRATE;
            int maxBitrate = DEFAULT_MAX_BITRATE;
            property = outputFormat.getProperty("bitrate");
            if (property instanceof Integer) {
                nominalBitrate = (Integer) property; // / 1024
                minBitrate = nominalBitrate;
                maxBitrate = nominalBitrate;
                useVBR = false;

                logger.log(Level.TRACE, "using nominal bitrate (automatically switching VBR off): " + nominalBitrate);
            }

            property = outputFormat.getProperty("vorbis.min_bitrate");
            if (property instanceof Integer) {
                minBitrate = (Integer) property; // / 1024
            }

            property = outputFormat.getProperty("vorbis.max_bitrate");
            if (property instanceof Integer) {
                maxBitrate = (Integer) property / 1024;
            }

            streamState = new StreamState();
            page = new Page();
            packet = new Packet();

            info = new Info();
            comment = new Comment();
            dspState = new DspState();
            block = new Block();

            info.init();

            int sampleRate = (int) inputStream.getFormat().getSampleRate();
            logger.log(Level.TRACE, "sample rate: " + sampleRate);

            logger.log(Level.TRACE, "channels: " + getChannels());

            if (useVBR) {
                logger.log(Level.TRACE, "using VBR with quality: " + quality);

                info.encodeInitVBR(getChannels(), sampleRate, quality);
            } else {
logger.log(Level.TRACE, "using fixed bitrate(max/nom/min): " + maxBitrate + "/" + nominalBitrate + "/" + minBitrate);

                info.encodeInit(getChannels(), sampleRate, maxBitrate, nominalBitrate, minBitrate);
            }

            comment.init();
            comment.addTag("ENCODER", "Tritonus libvorbis wrapper");

            dspState.initAnalysis(info);
            block.init(dspState);

            Random random;
            property = outputFormat.getProperty("vorbis.test");
            if (property instanceof Boolean test && test) {
                logger.log(Level.TRACE, "use test random seed");
                random = new Random(314159265358979L);
            } else {
                random = new Random(System.currentTimeMillis());
            }
            streamState.init(random.nextInt());

            Packet header = new Packet();
            Packet headerComm = new Packet();
            Packet headerCode = new Packet();

            dspState.headerOut(comment, header, headerComm, headerCode);
            streamState.packetIn(header);
            streamState.packetIn(headerComm);
            streamState.packetIn(headerCode);

            while (true) {
                int result = streamState.flush(page);
                if (result == 0) {
                    break;
                }
                getCircularBuffer().write(page.getHeader());
                getCircularBuffer().write(page.getBody());
            }

            logger.log(Level.TRACE, "end");
        }

        @Override
        public void execute() {
            logger.log(Level.TRACE, "begin");

            int frameSize = getFrameSize();
            int channels = getChannels();
            boolean bigEndian = isBigEndian();
            int bytesPerSample = frameSize / channels;
            int sampleSizeInBits = bytesPerSample * 8;
            float scale = (float) Math.pow(2.0, sampleSizeInBits - 1);
            logger.log(Level.TRACE, "frame size: " + frameSize);
            logger.log(Level.TRACE, "channels: " + channels);
            logger.log(Level.TRACE, "big endian: " + bigEndian);
            logger.log(Level.TRACE, "sample size (bits): " + sampleSizeInBits);
            logger.log(Level.TRACE, "bytes per sample: " + bytesPerSample);
            logger.log(Level.TRACE, "scale: " + scale);

            while (!eos && writeMore()) {
                logger.log(Level.TRACE, "writeMore(): " + writeMore());

                int bytes;
                try {
                    bytes = decodedStream.read(readbuffer);
                    logger.log(Level.TRACE, "read from PCM stream: " + bytes);

                } catch (IOException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                    streamState.clear();
                    block.clear();
                    dspState.clear();
                    comment.clear();
                    info.clear();
                    try {
                        close();
                    } catch (IOException e1) {
                        logger.log(Level.ERROR, e1.getMessage(), e1);
                    }
                    logger.log(Level.TRACE, "<");

                    return;
                }

                if (bytes == 0 || bytes == -1) {
                    logger.log(Level.TRACE, "EOS reached; calling DspState.write(0)");

                    dspState.write(null, 0);
                    eos = true;
                    break;
                } else {
                    int frames = bytes / frameSize;
                    logger.log(Level.TRACE, "processing frames: " + frames);

                    float[][] buffer = new float[channels][READ];
                    // uninterleave samples
                    for (int i = 0; i < frames; i++) {
                        for (int channel = 0; channel < channels; channel++) {
                            int sample;
                            sample = bytesToInt16(readbuffer, i * frameSize + channel * bytesPerSample, bigEndian);
                            buffer[channel][i] = sample / scale;
                        }
                    }
                    dspState.write(buffer, frames);
                }

                while (dspState.blockOut(block) == 1) {
                    block.analysis(null);
                    block.addBlock();
                    while (dspState.flushPacket(packet) != 0) {
                        streamState.packetIn(packet);
                        while (!eos /*&& writeMore()*/) {
                            int result = streamState.pageOut(page);
                            if (result == 0) {
                                break;
                            }
                            getCircularBuffer().write(page.getHeader());
                            getCircularBuffer().write(page.getBody());

                            if (page.isEos()) {
                                eos = true;
                                logger.log(Level.TRACE, "page has detected EOS");
                            }
                        }
                    }
                }
            }

            if (eos) {
                logger.log(Level.TRACE, "EOS; shutting down encoder");

                streamState.clear();
                block.clear();
                dspState.clear();
                comment.clear();
                info.clear();
                getCircularBuffer().close();
                try {
                    close();
                } catch (IOException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
            }

            logger.log(Level.TRACE, "end");
        }

        private int getChannels() {
            return decodedStream.getFormat().getChannels();
        }

        private int getFrameSize() {
            return decodedStream.getFormat().getFrameSize();
        }

        private boolean isBigEndian() {
            return decodedStream.getFormat().isBigEndian();
        }

        @Override
        public void close() throws IOException {
            super.close();
            decodedStream.close();
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
     * TODO Class should be private, but is public due to a bug (?) in the
     *      aspectj compiler.
     */
    /* private */ public static class DecodedVorbisAudioInputStream extends TAsynchronousFilteredAudioInputStream {

        private static final int INPUT_BUFFER_SIZE = 4096;
        private static final int BUFFER_MULTIPLE = 4;
        private static final int BUFFER_SIZE = BUFFER_MULTIPLE * 256 * 2;
        private static final int CONVSIZE = BUFFER_SIZE * 2;

        private final InputStream oggBitStream;

        private byte[] inputBuffer;
        // Ogg structures
        private SyncState oggSyncState = null;
        private StreamState oggStreamState = null;
        private Page oggPage = null;
        private Packet oggPacket = null;

        // Vorbis structures
        private Info vorbisInfo = null;
        private Comment vorbisComment = null;
        private DspState vorbisDspState = null;
        private Block vorbisBlock = null;

        // private List	songComments = new ArrayList();
        // is altered later in a dubious way
        // $$fb field not used
//        private int convSize = -1; // BUFFER_SIZE * 2;
        // TODO further checking
        private final byte[] convBuffer = new byte[CONVSIZE];
        private float[][] pcmOut;

        private boolean headersExpected;

        /**
         * Constructor.
         */
        public DecodedVorbisAudioInputStream(AudioFormat outputFormat, AudioInputStream bitStream) {
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
            inputBuffer = new byte[INPUT_BUFFER_SIZE];
            oggSyncState = new SyncState();
            oggStreamState = new StreamState();
            oggPage = new Page();
            oggPacket = new Packet();

            vorbisInfo = new Info();
            vorbisComment = new Comment();
            vorbisDspState = new DspState();
            vorbisBlock = new Block();
            vorbisBlock.init(vorbisDspState);

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
                    logger.log(Level.TRACE, e);

                    closePhysicalStream();
                    vorbisInfo.free();
                    vorbisComment.free();
                    vorbisDspState.free();
                    vorbisBlock.free();
                    logger.log(Level.TRACE, "end");

                    return;
                }
                decodeDataPacket();
            }
            if (oggPacket.isEos()) {
                logger.log(Level.TRACE, "end of vorbis stream reached");

                // The end of the vorbis stream is reached.
                // So we shut down the logical bitstream and
                // vorbis structures.
                oggStreamState.clear();
                vorbisBlock.clear();
                vorbisDspState.clear();
                vorbisInfo.clear();
                headersExpected = true;
            }

            logger.log(Level.TRACE, "end");
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
            oggStreamState.init(oggPage.getSerialNo());
            vorbisInfo.init();
            vorbisComment.init();
            if (oggStreamState.pageIn(oggPage) < 0) {
                throw new IOException("can't read first page of Ogg bitstream data, perhaps stream version mismatch");
            }
            if (oggStreamState.packetOut(oggPacket) != 1) {
                throw new IOException("can't read initial header packet");
            }
            if (vorbisInfo.headerIn(vorbisComment, oggPacket) < 0) {
                throw new IOException("packet is not a vorbis header");
            }
        }

        /**
         * Read the comment header and the codebook header pages.
         */
        private void readCommentAndCodebookHeaders() throws IOException {
            for (int i = 0; i < 2; i++) {
                readOggPacket();
                if (vorbisInfo.headerIn(vorbisComment, oggPacket) < 0) {
                    throw new IOException("packet is not a vorbis header");
                }
            }
        }

        /**
         *
         */
        private void processComments() {
//            byte[][] ptr = vorbisComment.user_comments;
//            String currComment = "";
//            songComments.clear();
//            for (int j = 0; j < ptr.length; j++) {
//                if (ptr[j] == null) {
//                    break;
//                }
//                currComment = (new String(ptr[j], 0, ptr[j].length - 1)).trim();
//                songComments.add(currComment);
//                if (currComment.toUpperCase().startsWith("ARTIST")) {
//                    String artistLabelValue = currComment.substring(7);
//                } else if (currComment.toUpperCase().startsWith("TITLE")) {
//                    String titleLabelValue = currComment.substring(6);
//                    String miniDragLabel = currComment.substring(6);
//                }
//logger.log(Level.TRACE, "Comment: " + currComment);
//            currComment = "Bitstream: " + vorbisInfo.getChannels() + " channel," + vorbisInfo.rate + "Hz";
//            songComments.add(currComment);
//logger.log(Level.TRACE, currComment);
//logger.log(Level.TRACE, "Encoded by: " + vorbisComment.getVendor());
//            songComments.add(currComment);
//logger.log(Level.TRACE, currComment);
        }

        /**
         * Setup structures needed for vorbis decoding.
         * Precondition: vorbisInfo has to be initialized completely
         * (i.e. all three headers are read).
         */
        private void setupVorbisStructures() {
            // $$fb field not used...
//            convsize = BUFFER_SIZE / vorbisInfo.getChannels();
            vorbisDspState.initSynthesis(vorbisInfo);
            vorbisBlock.init(vorbisDspState);
            pcmOut = new float[vorbisInfo.getChannels()][];
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
                vorbisDspState.blockIn(vorbisBlock);
            }
            while ((samples = vorbisDspState.pcmOut(pcmOut)) > 0) {
                // convert floats to signed ints and interleave
                for (int channel = 0; channel < vorbisInfo.getChannels(); channel++) {
                    int pointer = channel * getSampleSizeInBytes();
                    for (int j = 0; j < samples; j++) {
                        float fVal = pcmOut[channel][j];
                        clipAndWriteSample(fVal, pointer);
                        pointer += getFrameSize();
                    }
                }
                vorbisDspState.read(samples);
                getCircularBuffer().write(convBuffer, 0, getFrameSize() * samples);
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
                int result = oggStreamState.packetOut(oggPacket);
                if (result == 1) {
                    return;
                }
                if (result == -1) {
                    throw new IOException("can't read packet");
                }
                readOggPage();
                if (oggStreamState.pageIn(oggPage) < 0) {
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
                int result = oggSyncState.pageOut(oggPage);
                if (result == 1) {
                    return;
                }
                // we need more data from the stream
                // TODO call stream.read() directly
                int bytes = readFromStream(inputBuffer, 0, inputBuffer.length);
                // TODO This clause should become obsolete; readFromStream() should
                // propagate exceptions directly.
                if (bytes == -1) {
                    throw new EOFException();
                }
                oggSyncState.write(inputBuffer, bytes);
            }
        }

        /**
         * Read raw data from to ogg bitstream.
         * Reads from  {@link #oggBitStream oggBitStream} a
         * specified number of bytes into a buffer, starting
         * at a specified buffer index.
         *
         * @param buffer  the where the read data should be put into. Its length has to be at least start + length.
         * @param start offset to read
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
