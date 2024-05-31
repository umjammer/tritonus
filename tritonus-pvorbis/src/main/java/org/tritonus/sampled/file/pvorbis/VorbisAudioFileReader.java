/*
 *  Copyright (c) 2001 - 2004 by Matthias Pfisterer
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

package org.tritonus.sampled.file.pvorbis;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import biniu.ogg.Packet;
import biniu.ogg.Page;
import biniu.ogg.StreamState;
import biniu.ogg.SyncState;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import org.tritonus.share.sampled.file.TAudioFileReader;

import static java.lang.System.getLogger;
import static org.tritonus.sampled.convert.pvorbis.VorbisFormatConversionProvider.VORBIS;
import static org.tritonus.sampled.file.pvorbis.VorbisAudioFileWriter.OGG;


/**
 * @author Matthias Pfisterer
 */
public class VorbisAudioFileReader extends TAudioFileReader {

    private static final Logger logger= getLogger("org.tritonus.TraceAudioFileReader");

    private static final int INITAL_READ_LENGTH = 4096;
    private static final int MARK_LIMIT = INITAL_READ_LENGTH + 1;

    public VorbisAudioFileReader() {
        super(MARK_LIMIT, true);
    }

    @Override
    protected AudioFileFormat getAudioFileFormat(InputStream inputStream, long fileLengthInBytes)
            throws UnsupportedAudioFileException, IOException {

        logger.log(Level.TRACE, "begin");
        SyncState oggSyncState = new SyncState();
        StreamState oggStreamState = new StreamState();
        Page oggPage = new Page();
        Packet oggPacket = new Packet();

        int bytes;

        // Decode setup

        oggSyncState.init(); // Now we can read pages

        // grab some data at the head of the stream.  We want the first page
        // (which is guaranteed to be small and only contain the Vorbis
        // stream initial header) We need the first page to get the stream
        // serialno.

        // submit a 4k block to libvorbis' Ogg layer
        byte[] buffer = new byte[INITAL_READ_LENGTH];
        bytes = inputStream.read(buffer);
        logger.log(Level.TRACE, "read bytes from input stream: " + bytes);

        int result = oggSyncState.write(buffer, bytes);
        logger.log(Level.TRACE, "SyncState.write() returned " + result);


        // Get the first page.
        if (oggSyncState.pageOut(oggPage) != 1) {
            // have we simply run out of data?  If so, we're done.
            if (bytes < INITAL_READ_LENGTH) {
                logger.log(Level.TRACE, "stream ended prematurely");

                logger.log(Level.TRACE, "throwing exception");

                // IDEA: throw EOFException?
                oggSyncState.clear();
                oggStreamState.clear();
                oggPacket.clear();
                throw new UnsupportedAudioFileException("not a Vorbis stream: ended prematurely");
            }
            logger.log(Level.TRACE, "not in Ogg bitstream format");

            logger.log(Level.TRACE, "throwing exception");

            oggSyncState.clear();
            oggStreamState.clear();
            oggPacket.clear();
            throw new UnsupportedAudioFileException("not a Vorbis stream: not in Ogg bitstream format");
        }

        // Get the serial number and set up the rest of decode.
        // serialNo first; use it to set up a logical stream
        int serialNo = oggPage.serialNo();
 logger.log(Level.TRACE, "serial no.: " + serialNo);

        oggStreamState.init(serialNo);

        // extract the initial header from the first page and verify that the
        // Ogg bitstream is in fact Vorbis data

        // I handle the initial header first instead of just having the code
        // read all three Vorbis headers at once because reading the initial
        // header is an easy way to identify a Vorbis bitstream and it's
        // useful to see that functionality separated out.

        if (oggStreamState.pageIn(oggPage) < 0) {
            logger.log(Level.TRACE, "can't read first page of Ogg bitstream data");
            logger.log(Level.TRACE, "throwing exception");

            // error; stream version mismatch perhaps
            oggSyncState.clear();
            oggStreamState.clear();
            oggPacket.clear();
            throw new UnsupportedAudioFileException("not a Vorbis stream: can't read first page of Ogg bitstream data");
        }

        if (oggStreamState.packetOut(oggPacket) != 1) {
            logger.log(Level.TRACE, "can't read initial header packet");

            logger.log(Level.TRACE, "throwing exception");

            // no page? must not be vorbis
            oggSyncState.clear();
            oggStreamState.clear();
            oggPacket.clear();
            throw new UnsupportedAudioFileException("not a Vorbis stream: can't read initial header packet");
        }

        byte[] data = oggPacket.packetByte;
        if (logger.isLoggable(Level.TRACE)) {
            StringBuilder sb = new StringBuilder();
            for (byte abDatum : data) {
                sb.append(" ").append(abDatum);
            }
            logger.log(Level.TRACE, "packet data: " + sb);
        }

        int packetType = data[0];
        logger.log(Level.TRACE, "packet type: " + packetType);

        if (packetType != 1) {
            logger.log(Level.TRACE, "first packet is not the identification header");

            logger.log(Level.TRACE, "throwing exception");

            oggSyncState.clear();
            oggStreamState.clear();
            oggPacket.clear();
            throw new UnsupportedAudioFileException("not a Vorbis stream: first packet is not the identification header");
        }
        if (data[1] != 'v' ||
                data[2] != 'o' ||
                data[3] != 'r' ||
                data[4] != 'b' ||
                data[5] != 'i' ||
                data[6] != 's') {
            logger.log(Level.TRACE, "not a vorbis header packet");

            logger.log(Level.TRACE, "throwing exception");

            oggSyncState.clear();
            oggStreamState.clear();
            oggPacket.clear();
            throw new UnsupportedAudioFileException("not a Vorbis stream: not a vorbis header packet");
        }
        if (!oggPacket.isBos()) {
            logger.log(Level.TRACE, "initial packet not marked as beginning of stream");

            logger.log(Level.TRACE, "throwing exception");

            oggSyncState.clear();
            oggStreamState.clear();
            oggPacket.clear();
            throw new UnsupportedAudioFileException("not a Vorbis stream: initial packet not marked as beginning of stream");
        }
        int version = (data[7] & 0xFF) + 256 * (data[8] & 0xFF) + 65536 * (data[9] & 0xFF) + 16777216 * (data[10] & 0xFF);
        logger.log(Level.TRACE, "version: " + version);

        if (version != 0) {
            logger.log(Level.TRACE, "wrong vorbis version");

            logger.log(Level.TRACE, "throwing exception");

            oggSyncState.clear();
            oggStreamState.clear();
            oggPacket.clear();
            throw new UnsupportedAudioFileException("not a Vorbis stream: wrong vorbis version");
        }
        int channels = (data[11] & 0xFF);
        float sampleRate = (data[12] & 0xFF) + 256 * (data[13] & 0xFF) + 65536 * (data[14] & 0xFF) + 16777216 * (data[15] & 0xFF);
        logger.log(Level.TRACE, "channels: " + channels);

        logger.log(Level.TRACE, "rate: " + sampleRate);


        // These are only used for error checking.
//int bitrate_upper = data[16] + 256 * data[17] + 65536 * data[18] + 16777216 * data[19];
//int bitrate_nominal = data[20] + 256 * data[21] + 65536 * data[22] + 16777216 * data[23];
//int bitrate_lower = data[24] + 256 * data[25] + 65536 * data[26] + 16777216 * data[27];

        int[] blockSizes = new int[2];
        blockSizes[0] = 1 << (data[28] & 0xF);
        blockSizes[1] = 1 << ((data[28] >>> 4) & 0xF);
        logger.log(Level.TRACE, "blockSizes[0]: " + blockSizes[0]);

        logger.log(Level.TRACE, "blockSizes[1]: " + blockSizes[1]);


        if (sampleRate < 1.0F ||
                channels < 1 ||
                blockSizes[0] < 8 ||
                blockSizes[1] < blockSizes[0] ||
                (data[29] & 0x1) != 1) {
            logger.log(Level.TRACE, "illegal values in initial header");

            logger.log(Level.TRACE, "throwing exception");

            oggSyncState.clear();
            oggStreamState.clear();
            oggPacket.clear();
            throw new UnsupportedAudioFileException("not a Vorbis stream: illegal values in initial header");
        }

        oggSyncState.clear();
        oggStreamState.clear();
        oggPacket.clear();

        // If the file size is known, we derive the number of frames
        // ('frame size') from it.
        // If the values don't fit into integers, we leave them at
        // NOT_SPECIFIED. 'Unknown' is considered less incorrect than
        // a wrong value.
        // [fb] not specifying it causes Sun's Wave file writer to write rubbish
        int byteSize = AudioSystem.NOT_SPECIFIED;
        if (fileLengthInBytes != AudioSystem.NOT_SPECIFIED && fileLengthInBytes <= Integer.MAX_VALUE) {
            byteSize = (int) fileLengthInBytes;
        }
        int frameSize = AudioSystem.NOT_SPECIFIED;
        // Can we calculate a useful size?
        // Peeking into ogginfo gives the insight that the only
        // way seems to be reading through the file. This is
        // something we do not want, at least not by default.
        // frameSize = (int) (lFileSizeInBytes / ...;

        AudioFormat format = new AudioFormat(
                VORBIS,
                sampleRate,
                AudioSystem.NOT_SPECIFIED,
                channels,
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED,
                true); // this value is chosen arbitrarily
        logger.log(Level.TRACE, "AudioFormat: " + format);

        AudioFileFormat.Type type = OGG;
        AudioFileFormat audioFileFormat = new TAudioFileFormat(type, format, frameSize, byteSize);
        logger.log(Level.TRACE, "AudioFileFormat: " + audioFileFormat);

        logger.log(Level.TRACE, "end");

        return audioFileFormat;
    }
}
