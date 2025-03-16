/*
 *  Copyright (c) 2003 by Matthias Pfisterer
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

package org.tritonus.test.api.midi.misc;

import java.util.HashMap;
import java.util.Map;
import javax.sound.midi.MidiFileFormat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class MidiFileFormatTestCase {

    private static final float DELTA = 1E-9F;

    @Test
    public void testGetValues() {
        checkGetValues(0, 0.0F, 0, 0, 0L, false);
        checkGetValues(0, 0.0F, 0, 0, 0L, true);
        checkGetValues(2, -1.0F, 25, 725, 600000L, false);
        checkGetValues(2, -1.0F, 25, 725, 600000L, true);
    }

    private static void checkGetValues(int type, float divisionType,
                                       int resolution, int byteLength, long microsecondLength, boolean withMap) {
        MidiFileFormat fileFormat;
        if (withMap) {
            Map<String, Object> prop = new HashMap<>();
            fileFormat = new MidiFileFormat(type, divisionType, resolution, byteLength, microsecondLength, prop);
        } else {
            fileFormat = new MidiFileFormat(type, divisionType, resolution, byteLength, microsecondLength);
        }
        assertEquals(type, fileFormat.getType(), "type");
        assertEquals(divisionType, fileFormat.getDivisionType(), DELTA, "division type");
        assertEquals(resolution, fileFormat.getResolution(), "resolution");
        assertEquals(byteLength, fileFormat.getByteLength(), "byte length");
        assertEquals(microsecondLength, fileFormat.getMicrosecondLength(), "microsecond length");
    }

    @Test
    public void testNoMap() {
        MidiFileFormat fileFormat = new MidiFileFormat(0, 0.0F, 0, 0, 0L);
        Map<String, Object> propReturn = fileFormat.properties();
        assertNotNull(propReturn);
        assertTrue(propReturn.isEmpty());
        Object result = propReturn.get("bitrate");
        assertNull(result);
    }

    @Test
    public void testNullMap() {
        assertThrows(NullPointerException.class,
                () -> new MidiFileFormat(0, 0.0F, 0, 0, 0L, null));
    }

    @Test
    public void testEmptyMap() {
        Map<String, Object> prop = new HashMap<>();
        var fileFormat = new MidiFileFormat(0, 0.0F, 0, 0, 0L, prop);
        Map<String, Object> propReturn = fileFormat.properties();
        assertTrue(propReturn.isEmpty());
        Object result = propReturn.get("bitrate");
        assertNull(result);
    }

    @Test
    public void testCopying() {
        Map<String, Object> prop = new HashMap<>();
        prop.put("bitrate", 22.5F);
        var fileFormat = new MidiFileFormat(0, 0, 0, 0, 0, prop);
        Map<String, Object> propReturn = fileFormat.properties();
        assertNotSame(prop, propReturn);
        prop.put("bitrate", 42.5F);
        Object result = propReturn.get("bitrate");
        assertEquals(22.5F, result);
    }

    @Test
    public void testUnmodifiable() {
        Map<String, Object> prop = new HashMap<>();
        var fileFormat = new MidiFileFormat(0, 0.0F, 0, 0, 0L, prop);
        Map<String, Object> propReturn = fileFormat.properties();
        assertThrows(UnsupportedOperationException.class, () -> propReturn.put("author", "Matthias Pfisterer"),
                "returned Map allows modifications");
    }

    @Test
    public void testGet() {
        Map<String, Object> prop = new HashMap<>();
        prop.put("bitrate", 22.5F);
        prop.put("author", "Matthias Pfisterer");
        var fileFormat = new MidiFileFormat(0, 0.0F, 0, 0, 0L, prop);
        Map<String, Object> propReturn = fileFormat.properties();
        assertEquals(22.5F, propReturn.get("bitrate"));
        assertEquals("Matthias Pfisterer", propReturn.get("author"));
    }
}
