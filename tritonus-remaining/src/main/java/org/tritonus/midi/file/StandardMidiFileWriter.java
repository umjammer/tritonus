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

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;
import javax.sound.midi.spi.MidiFileWriter;

import static java.lang.System.getLogger;


/**
 * Writer for Standard Midi Files.
 * This writer can write type 0 and type 1 files. It cannot write type
 * 2 files.
 */
public class StandardMidiFileWriter extends MidiFileWriter {

    private static final Logger logger = getLogger(StandardMidiFileWriter.class.getName());

    /**
     * TODO
     */
    public static boolean USE_RUNNING_STATUS = true;

    /**
     * TODO
     */
    public static boolean CANCEL_RUNNING_STATUS_ON_META_AND_SYSEX = true;

    /**
     * Return supported MIDI file types.
     * This writer supports Standard MIDI File (SMF) types 0 and 1.
     * So these numbers are returned here.
     *
     * @return an array of supported SMF types.
     */
    @Override
    public int[] getMidiFileTypes() {
        return new int[] {0, 1};
    }

    /**
     * Return the supported MIDI file types for a given Sequence.
     * This writer supports Standard MIDI File (SMF) types 0 and 1.
     * Depending on the Sequence, either 0 or 1 is returned.
     *
     * @return and array of supported SMF types. It contains 0 if
     * the Sequence has one track, 1 otherwise.
     */
    @Override
    public int[] getMidiFileTypes(Sequence sequence) {
        Track[] tracks = sequence.getTracks();
        if (tracks.length == 1) {
            return new int[] {0};
        } else {
            return new int[] {1};
        }
    }

    /**
     * Write a Sequence as Standard MIDI File (SMF) to an OutputStream.
     * A byte stream representing the passed Sequence is written
     * to the output stream in the given file type.
     *
     * @return The number of bytes written to the output stream.
     */
    @Override
    public int write(Sequence sequence, int fileType, OutputStream outputStream) throws IOException {
        if (!isFileTypeSupported(fileType, sequence)) {
            throw new IllegalArgumentException("file type is not supported for this sequence");
        }
        Track[] tracks = sequence.getTracks();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.writeInt(MidiConstants.HEADER_MAGIC);
        dataOutputStream.writeInt(6); // header length
        dataOutputStream.writeShort(fileType);
        dataOutputStream.writeShort(tracks.length);
        float divisionType = sequence.getDivisionType();
        int resolution = sequence.getResolution();
        int division = 0;
        if (divisionType == Sequence.PPQ) {
            division = resolution & 0x7fff;
        } else {
            // TODO
        }
        dataOutputStream.writeShort(division); // unsigned?
        int bytesWritten = 14;
        for (Track aTrack : tracks) {
            bytesWritten += writeTrack(aTrack, dataOutputStream);
        }

        return bytesWritten;
    }

    /**
     * Write a Sequence as Standard MIDI File (SMF) to a File.
     * A byte stream representing the passed Sequence is written
     * to the file in the given file type.
     *
     * @return The number of bytes written to the file.
     */
    @Override
    public int write(Sequence sequence, int fileType, File file) throws IOException {
        OutputStream outputStream = Files.newOutputStream(file.toPath());
        int bytes = write(sequence, fileType, outputStream);
        outputStream.close();
        return bytes;
    }

    /**
     * Write a Track to a DataOutputStream.
     *
     * @return The number of bytes written.
     */
    private static int writeTrack(Track track, DataOutputStream dataOutputStream) throws IOException {
        // The number of bytes written. This is used as return
        // value for this method.
        int length = 0;
        if (dataOutputStream != null) {
            dataOutputStream.writeInt(MidiConstants.TRACK_MAGIC);
        }
        // This is a recursive call!
        // It is to find out the length of the track without
        // actually writing. Having the second parameter as
        // null tells writeTrack() and its subordinate
        // methods to not write out data bytes.
        int trackLength = 0;
        if (dataOutputStream != null) {
            trackLength = writeTrack(track, null);
        }
        if (dataOutputStream != null) {
            dataOutputStream.writeInt(trackLength);
        }
        MidiEvent previousEvent = null;
        int[] runningStatusByte = new int[1];
        runningStatusByte[0] = -1;
        for (int _event = 0; _event < track.size(); _event++) {
            MidiEvent event = track.get(_event);
            length += writeEvent(event, previousEvent, runningStatusByte, dataOutputStream);
            previousEvent = event;
        }
        return length;
    }

    /**
     * TODO
     */
    private static int writeEvent(MidiEvent event,
                                  MidiEvent previousEvent,
                                  int[] runningStatusByte,
                                  DataOutputStream dataOutputStream) throws IOException {
        // The number of bytes written. This is used as return
        // value for this method.
        int length = 0;
        long tickDelta = 0;
        if (previousEvent != null) {
            tickDelta = event.getTick() - previousEvent.getTick();
        }
        if (tickDelta < 0) {
            logger.log(Level.TRACE, "warning: events not in order");
        }
        // add bytes according to coded length of delta
        length += writeVariableLengthQuantity(tickDelta, dataOutputStream);
        MidiMessage message = event.getMessage();
//        int dataLength = message.getLength();
        if (message instanceof ShortMessage) {
            length += writeShortMessage((ShortMessage) message, runningStatusByte, dataOutputStream);
        } else if (message instanceof SysexMessage) {
            length += writeSysexMessage((SysexMessage) message, runningStatusByte, dataOutputStream);
        } else if (message instanceof MetaMessage) {
            length += writeMetaMessage((MetaMessage) message, runningStatusByte, dataOutputStream);
        } else {
            logger.log(Level.TRACE, "warning: unknown message class");
        }
        return length;
    }

    /**
     * TODO
     */
    private static int writeShortMessage(ShortMessage message,
                                         int[] runningStatusByte,
                                         DataOutputStream dataOutputStream) throws IOException {
        // The number of bytes written. This is used as return
        // value for this method.
        int length = 0;
        int dataLength = message.getLength();
        if (USE_RUNNING_STATUS && runningStatusByte[0] == message.getStatus()) {
            // Write without status byte.
            if (dataOutputStream != null) {
                dataOutputStream.write(message.getMessage(), 1, dataLength - 1);
            }
            length += dataLength - 1;
        } else {
            // Write with status byte.
            if (dataOutputStream != null) {
                dataOutputStream.write(message.getMessage(), 0, dataLength);
            }
            length += dataLength;
            runningStatusByte[0] = message.getStatus();
        }
        return length;
    }

    /**
     * TODO
     */
    private static int writeSysexMessage(SysexMessage message,
                                         int[] runningStatusByte,
                                         DataOutputStream dataOutputStream) throws IOException {
        // The number of bytes written. This is used as return
        // value for this method.
        int length = 0;
        int dataLength = message.getLength();
        if (CANCEL_RUNNING_STATUS_ON_META_AND_SYSEX) {
            runningStatusByte[0] = -1;
        }
        if (dataOutputStream != null) {
            dataOutputStream.write(message.getStatus());
        }
        length++;
        length += writeVariableLengthQuantity(dataLength - 1, dataOutputStream);
        if (dataOutputStream != null) {
            dataOutputStream.write(message.getData(), 0, dataLength - 1);
        }
        length += dataLength - 1;
        return length;
    }

    /**
     * TODO
     */
    private static int writeMetaMessage(MetaMessage message,
                                        int[] runningStatusByte,
                                        DataOutputStream dataOutputStream) throws IOException {
        // The number of bytes written. This is used as return
        // value for this method.
        int length = 0;
        byte[] data = message.getData();
        int dataLength = data.length;
        if (CANCEL_RUNNING_STATUS_ON_META_AND_SYSEX) {
            runningStatusByte[0] = -1;
        }
        if (dataOutputStream != null) {
            dataOutputStream.write(message.getStatus());
            dataOutputStream.write(message.getType());
        }
        length += 2;
        length += writeVariableLengthQuantity(dataLength, dataOutputStream);
        if (dataOutputStream != null) {
            dataOutputStream.write(data);
        }
        length += dataLength;
        return length;
    }

    /**
     * TODO
     * outputStream == 0 signals to only calculate the number of
     * needed to represent the value.
     */
    private static int writeVariableLengthQuantity(long value, OutputStream outputStream) throws IOException {
        // The number of bytes written. This is used as return
        // value for this method.
        int length = 0;
        // IDEA: use a loop
        boolean writingStarted = false;
        int _byte = (int) ((value >> 21) & 0x7f);
        if (_byte != 0) {
            if (outputStream != null) {
                outputStream.write(_byte | 0x80);
            }
            length++;
            writingStarted = true;
        }
        _byte = (int) ((value >> 14) & 0x7f);
        if (_byte != 0 || writingStarted) {
            if (outputStream != null) {
                outputStream.write(_byte | 0x80);
            }
            length++;
            writingStarted = true;
        }
        _byte = (int) ((value >> 7) & 0x7f);
        if (_byte != 0 || writingStarted) {
            if (outputStream != null) {
                outputStream.write(_byte | 0x80);
            }
            length++;
        }
        _byte = (int) (value & 0x7f);
        if (outputStream != null) {
            outputStream.write(_byte);
        }
        length++;
        return length;
    }
}
