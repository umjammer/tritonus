/*
 *  Copyright (c) 1999, 2000 by Matthias Pfisterer
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

package org.tritonus.midi.file;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.nio.file.Files;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiFileFormat;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;
import javax.sound.midi.spi.MidiFileReader;

import org.tritonus.share.midi.TMidiFileFormat;

import static java.lang.System.getLogger;


/**
 * TODO
 */
public class StandardMidiFileReader extends MidiFileReader {

    private static final Logger logger= getLogger("org.tritonus.TraceAllExceptions");

    /**
     * TODO
     */
    public static boolean CANCEL_RUNNING_STATUS_ON_META_AND_SYSEX = true;

    private static final int STATUS_NONE = 0;
    private static final int STATUS_ONE_BYTE = 1;
    private static final int STATUS_TWO_BYTES = 2;
    private static final int STATUS_SYSEX = 3;
    private static final int STATUS_META = 4;

    /**
     * TODO
     */
    @Override
    public MidiFileFormat getMidiFileFormat(InputStream inputStream) throws InvalidMidiDataException, IOException {
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int headerMagic = dataInputStream.readInt();
        if (headerMagic != MidiConstants.HEADER_MAGIC) {
            throw new InvalidMidiDataException("not a MIDI file: wrong header magic");
        }
        int headerLength = dataInputStream.readInt();
        if (headerLength < 6) {
            throw new InvalidMidiDataException("corrupt MIDI file: wrong header length");
        }
        int type = dataInputStream.readShort();
        if (type < 0 || type > 2) {
            throw new InvalidMidiDataException("corrupt MIDI file: illegal type");
        }
        if (type == 2) {
            throw new InvalidMidiDataException("this implementation doesn't support type 2 MIDI files");
        }
        int tracks = dataInputStream.readShort();
        if (tracks <= 0) {
            throw new InvalidMidiDataException("corrupt MIDI file: number of tracks must be positive");
        }
        if (type == 0 && tracks != 1) {
            throw new InvalidMidiDataException("corrupt MIDI file:  type 0 files must contain exactely one track");
        }
        int division = dataInputStream.readUnsignedShort();
        float divisionType;
        int resolution;
        if ((division & 0x8000) != 0) { // frame division
            // TODO
            int frameType = -((division >>> 8) & 0xFF);
            divisionType = switch (frameType) {
                case 24 -> Sequence.SMPTE_24;
                case 25 -> Sequence.SMPTE_25;
                case 29 -> Sequence.SMPTE_30DROP;
                case 30 -> Sequence.SMPTE_30;
                default -> throw new InvalidMidiDataException("corrupt MIDI file: illegal frame division type");
            };
            resolution = division & 0xff;
        } else { // BPM division
            divisionType = Sequence.PPQ;
            resolution = division & 0x7fff;
        }
        // skip additional bytes in the header
        dataInputStream.skipBytes(headerLength - 6);
        MidiFileFormat midiFileFormat = new TMidiFileFormat(
                type,
                divisionType,
                resolution,
                MidiFileFormat.UNKNOWN_LENGTH,
                MidiFileFormat.UNKNOWN_LENGTH,
                tracks);
        return midiFileFormat;
    }

    /**
     * TODO
     */
    @Override
    public MidiFileFormat getMidiFileFormat(URL url) throws InvalidMidiDataException, IOException {
        try (InputStream inputStream = url.openStream()) {
            return getMidiFileFormat(inputStream);
        }
    }

    /**
     * TODO
     */
    @Override
    public MidiFileFormat getMidiFileFormat(File file) throws InvalidMidiDataException, IOException {
//        inputStream = new BufferedInputStream(inputStream, 1024);
        try (InputStream inputStream = new FileInputStream(file)) {
            return getMidiFileFormat(inputStream);
        }
    }

    /**
     * TODO
     */
    @Override
    public Sequence getSequence(URL url) throws InvalidMidiDataException, IOException {
        InputStream inputStream = url.openStream();
        try {
            return getSequence(inputStream);
        } catch (InvalidMidiDataException | IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);

            inputStream.close();
            throw e;
        }
    }

    /**
     * TODO
     */
    @Override
    public Sequence getSequence(File file) throws InvalidMidiDataException, IOException {
        InputStream inputStream = Files.newInputStream(file.toPath());
//        inputStream = new BufferedInputStream(inputStream, 1024);
        try {
            return getSequence(inputStream);
        } catch (InvalidMidiDataException | IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);

            inputStream.close();
            throw e;
        }
    }

    /**
     * TODO
     */
    @Override
    public Sequence getSequence(InputStream inputStream) throws InvalidMidiDataException, IOException {
        MidiFileFormat midiFileFormat = getMidiFileFormat(inputStream);
        Sequence sequence = new Sequence(midiFileFormat.getDivisionType(), midiFileFormat.getResolution());
        DataInputStream dataInputStream = new DataInputStream(inputStream);
        int tracks = ((TMidiFileFormat) midiFileFormat).getTrackCount();
        for (int _track = 0; _track < tracks; _track++) {
            Track track = sequence.createTrack();
            readTrack(dataInputStream, track);
        }
        return sequence;
    }

    /**
     * TODO
     */
    private static void readTrack(DataInputStream dataInputStream, Track track)
            throws InvalidMidiDataException, IOException {
        // search for a "MTrk" chunk
        while (true) {
            int magic = dataInputStream.readInt();
            if (magic == MidiConstants.TRACK_MAGIC) {
                break;
            }
            int chunkLength = dataInputStream.readInt();
            if (chunkLength % 2 != 0) {
                chunkLength++;
            }
            dataInputStream.skip(chunkLength);
        }
        int trackChunkLength = dataInputStream.readInt();
        long ticks = 0;
        long[] remainingBytes = new long[1];
        remainingBytes[0] = trackChunkLength;
        int[] runningStatusByte = new int[1];
        // indicates no running status in effect
        runningStatusByte[0] = -1;
        while (remainingBytes[0] > 0) {
            long deltaTicks = readVariableLengthQuantity(dataInputStream, remainingBytes);
//logger.log(Level.TRACE, "delta ticks: " + deltaTicks);
            ticks += deltaTicks;
            MidiEvent event = readEvent(dataInputStream, remainingBytes, runningStatusByte, ticks);
            track.add(event);
        }
    }

    /**
     * TODO
     */
    private static MidiEvent readEvent(
            DataInputStream dataInputStream, long[] remainingBytes, int[] runningStatusByte, long ticks)
            throws InvalidMidiDataException, IOException {
        int statusByte = readUnsignedByte(dataInputStream, remainingBytes);
//logger.log(Level.TRACE, "status byte: " + statusByte);
        MidiMessage message = null;
        boolean runningStatusApplies = false;
        int savedByte = 0;
        if (statusByte < 0x80) {
            if (runningStatusByte[0] != -1) {
                runningStatusApplies = true;
                savedByte = statusByte;
                statusByte = runningStatusByte[0];
            } else {
                throw new InvalidMidiDataException("corrupt MIDI file: status byte missing");
            }
        }
        switch (getType(statusByte)) {
        case STATUS_ONE_BYTE:
            int _byte;
            if (runningStatusApplies) {
                _byte = savedByte;
            } else {
                _byte = readUnsignedByte(dataInputStream, remainingBytes);
                runningStatusByte[0] = statusByte;
            }
            ShortMessage shortMessage1 = new ShortMessage();
            shortMessage1.setMessage(statusByte, _byte, 0);
            message = shortMessage1;
            break;

        case STATUS_TWO_BYTES:
            int byte1;
            if (runningStatusApplies) {
                byte1 = savedByte;
            } else {
                byte1 = readUnsignedByte(dataInputStream, remainingBytes);
                runningStatusByte[0] = statusByte;
            }
            int byte2 = readUnsignedByte(dataInputStream, remainingBytes);
            ShortMessage shortMessage2 = new ShortMessage();
            shortMessage2.setMessage(statusByte, byte1, byte2);
            message = shortMessage2;
            break;

        case STATUS_SYSEX:
            if (CANCEL_RUNNING_STATUS_ON_META_AND_SYSEX) {
                runningStatusByte[0] = -1;
            }
            int sysexDataLength = (int) readVariableLengthQuantity(dataInputStream, remainingBytes);
            byte[] sysexData = new byte[sysexDataLength];
            for (int i = 0; i < sysexDataLength; i++) {
                int dataByte = readUnsignedByte(dataInputStream, remainingBytes);
                sysexData[i] = (byte) dataByte;
            }
            SysexMessage sysexMessage = new SysexMessage();
            sysexMessage.setMessage(statusByte, sysexData, sysexDataLength);
            message = sysexMessage;
            break;

        case STATUS_META:
            if (CANCEL_RUNNING_STATUS_ON_META_AND_SYSEX) {
                runningStatusByte[0] = -1;
            }
            int typeByte = readUnsignedByte(dataInputStream, remainingBytes);
            int metaDataLength = (int) readVariableLengthQuantity(dataInputStream, remainingBytes);
            byte[] metaData = new byte[metaDataLength];
            for (int i = 0; i < metaDataLength; i++) {
                int dataByte = readUnsignedByte(dataInputStream, remainingBytes);
                metaData[i] = (byte) dataByte;
            }
            MetaMessage metaMessage = new MetaMessage();
            metaMessage.setMessage(typeByte, metaData, metaDataLength);
            message = metaMessage;
            break;
        default:
        }
        MidiEvent event = new MidiEvent(message, ticks);
        return event;
    }

    // TODO use table

    /**
     * TODO
     */
    private static int getType(int statusByte) {
        if (statusByte < 0xf0) { // channel voice or mode command
            int command = statusByte & 0xf0;
            return switch (command) { // note off
                // note on
                // polyphonic key pressure
                // control change
                // pitch wheel change
                // program change
                case 0x80, 0x90, 0xa0, 0xb0, 0xe0 -> STATUS_TWO_BYTES;
                // channel pressure
                case 0xc0, 0xd0 -> STATUS_ONE_BYTE;
                default -> STATUS_NONE;
            };
        } else if (statusByte == 0xf0 || statusByte == 0xf7) {
            return STATUS_SYSEX;
        } else if (statusByte == 0xff) {
            return STATUS_META;
        } else {
            return STATUS_NONE;
        }
    }

    /**
     * TODO
     */
    public static long readVariableLengthQuantity(DataInputStream dataInputStream, long[] remainingBytes)
            throws InvalidMidiDataException, IOException {
        long value = 0;
        int byteCount = 0;
        while (byteCount < 4) {
            int _byte = readUnsignedByte(dataInputStream, remainingBytes);
            byteCount++;
            value <<= 7;
            value |= (_byte & 0x7f);
            if (_byte < 128) { // MSB is 0: last byte
                return value;
            }
        }

        throw new InvalidMidiDataException("not a MIDI file: unterminated variable-length quantity");
    }

    /**
     * TODO
     */
    public static int readUnsignedByte(DataInputStream dataInputStream, long[] remainingBytes) throws IOException {
        int _byte = dataInputStream.readUnsignedByte();
        // already done in DataInputStream.readUnsignedByte();
//        if (_byte < 0) {
//            throw new EOFException();
//        }
        remainingBytes[0]--;
        return _byte;
    }
}
