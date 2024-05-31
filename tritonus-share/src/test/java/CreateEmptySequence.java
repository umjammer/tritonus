/*
 *  Copyright (c) 2000 by Matthias Pfisterer
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


import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;


/**
 * Creates a Sequence with only an end-of-track message.
 */
public class CreateEmptySequence {

    public static void main(String[] args) {
        if (args.length != 4) {
            out("usage:");
            out("java CreateEmptySequence <duration> <tempo_in_MPQ> <resolution> <midifile>");
            System.exit(1);
        }
        long duration = Long.parseLong(args[0]);
        int tempoInMPQ = Integer.parseInt(args[1]);
        int resolution = Integer.parseInt(args[2]);
        String filename = args[3];
        out("Clock distance (µs): " + tempoInMPQ / 24);
        out("Tick distance (µs): " + tempoInMPQ / resolution);
        Sequence sequence = null;
        try {
            sequence = new Sequence(Sequence.PPQ,
                    resolution);
            Track track = sequence.createTrack();
            MetaMessage mm;
            MidiEvent me;

            mm = new MetaMessage();
            byte[] tempo = new byte[3];
            tempo[0] = (byte) ((tempoInMPQ >> 16) & 0xFF);
            tempo[1] = (byte) ((tempoInMPQ >> 8) & 0xFF);
            tempo[2] = (byte) ((tempoInMPQ) & 0xFF);
            mm.setMessage(0x51, tempo, 3);
            me = new MidiEvent(mm, 0);
            track.add(me);
            mm = new MetaMessage();
            mm.setMessage(0x2F, new byte[0], 0);
            me = new MidiEvent(mm, duration);
            track.add(me);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
        try {
            MidiSystem.write(sequence, 0, new File(filename));
        } catch (IOException e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }

        /*
         * This is only necessary because of a bug in the Sun jdk1.3
         */
        System.exit(0);
    }

    private static void out(String message) {
        System.out.println(message);
    }
}
