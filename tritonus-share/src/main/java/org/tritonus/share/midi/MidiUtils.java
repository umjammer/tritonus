/*
 *  Copyright (c) 1999 by Matthias Pfisterer
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


/**
 * Helper methods for reading and writing MIDI files.
 */
public final class MidiUtils {

    private static final Logger logger= getLogger("org.tritonus.TraceAllExceptions");

    private MidiUtils() {}

    public static int getUnsignedInteger(byte b) {
        return (b < 0) ? b + 256 : b;
    }

    public static int get14bitValue(int lsb, int msb) {
        return (lsb & 0x7F) | ((msb & 0x7F) << 7);
    }

    public static int get14bitMSB(int value) {
        return (value >> 7) & 0x7F;
    }

    public static int get14bitLSB(int value) {
        return value & 0x7F;
    }

    public static byte[] getVariableLengthQuantity(long value) {
        ByteArrayOutputStream data = new ByteArrayOutputStream();
        try {
            writeVariableLengthQuantity(value, data);
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        return data.toByteArray();
    }

    public static int writeVariableLengthQuantity(long value, OutputStream outputStream) throws IOException {
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
