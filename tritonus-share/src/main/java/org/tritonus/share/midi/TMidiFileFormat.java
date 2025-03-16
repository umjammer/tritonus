/*
 *  Copyright (c) 2000 by Matthias Pfisterer
 *
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

package org.tritonus.share.midi;

import javax.sound.midi.MidiFileFormat;


/**
 * A MidiFileFormat that has information about the number of tracks.
 * This class is used by org.tritonus.midi.file.StandardMidiFileReader.
 * Its purpose is to carry the number of tracks from
 * getMidiFileFormat() to getSequence().
 */
public class TMidiFileFormat extends MidiFileFormat {

    private final int trackCount;

    public TMidiFileFormat(int type,
                           float divisionType,
                           int resolution,
                           int byteLength,
                           long microsecondLength,
                           int trackCount) {
        super(type, divisionType, resolution, byteLength, microsecondLength);
        this.trackCount = trackCount;
    }

    public int getTrackCount() {
        return trackCount;
    }
}
