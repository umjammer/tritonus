/*
 *  Copyright (c) 2002 by Matthias Pfisterer
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

package org.tritonus.saol.engine.opcodes;

import org.tritonus.saol.compiler.WidthAndRate;


/**
 * The tune-related opcodes (Section 5.9.5).
 * These opcodes depend on a tune value. This tune value
 * is global, but only inside the current orchestra. To allow
 * the concurrent rendering of multiple orchestras, the tune value
 * cannot be global (in programming language terms), but has to
 * be encapsulated in class instances. So this class has to be
 * instantiated once per orchestra rendering.
 */
public final class PitchOpcodes {

    private static final float DEFAULT_TUNE = 440.0F;

    private float tune;

    public PitchOpcodes() {
        tune = DEFAULT_TUNE;
    }

    public static void buildOpcodeTable(OpcodeTable opcodeTable) {
        OpcodeClass staticClass = new OpcodeClass("org.tritonus.saol.engine.opcodes.PitchOpcodes", OpcodeClass.TYPE_STATIC);
        OpcodeClass instanceClass = new OpcodeClass("org.tritonus.saol.engine.opcodes.PitchOpcodes", OpcodeClass.TYPE_RUNTIME_INSTANCE);

        opcodeTable.addEntry(new OpcodeEntry("gettune", instanceClass, WidthAndRate.RATE_X));
        opcodeTable.addEntry(new OpcodeEntry("settune", instanceClass, WidthAndRate.RATE_K));

        opcodeTable.addEntry(new OpcodeEntry("octpch", staticClass, WidthAndRate.RATE_X));
        opcodeTable.addEntry(new OpcodeEntry("pchoct", staticClass, WidthAndRate.RATE_X));
        opcodeTable.addEntry(new OpcodeEntry("cpspch", instanceClass, WidthAndRate.RATE_X));
        opcodeTable.addEntry(new OpcodeEntry("pchcps", instanceClass, WidthAndRate.RATE_X));
        opcodeTable.addEntry(new OpcodeEntry("cpsoct", instanceClass, WidthAndRate.RATE_X));
        opcodeTable.addEntry(new OpcodeEntry("octcps", instanceClass, WidthAndRate.RATE_X));
        opcodeTable.addEntry(new OpcodeEntry("midipch", staticClass, WidthAndRate.RATE_X));
        opcodeTable.addEntry(new OpcodeEntry("pchmidi", staticClass, WidthAndRate.RATE_X));
        opcodeTable.addEntry(new OpcodeEntry("midioct", staticClass, WidthAndRate.RATE_X));
        opcodeTable.addEntry(new OpcodeEntry("octmidi", staticClass, WidthAndRate.RATE_X));
        opcodeTable.addEntry(new OpcodeEntry("midicps", instanceClass, WidthAndRate.RATE_X));
        opcodeTable.addEntry(new OpcodeEntry("cpsmidi", instanceClass, WidthAndRate.RATE_X));
    }

    public float getTune() {
        return tune;
    }

    public float setTune(float x) {
        tune = x;
        return x;
    }

    public static float octpch(float x) {
        return getOctValue(getPchOctave(x), getPchNote(x));
    }

    public static float pchoct(float x) {
        return getPchValue(getOctOctave(x), getOctNote(x));
    }

    public float cpspch(float x) {
        return getCpsValue(getPchOctave(x), getPchNote(x));
    }

    public float pchcps(float x) {
        return getPchValue(getCpsOctave(x), getCpsNote(x));
    }

    public float cpsoct(float x) {
        return getCpsValue(getOctOctave(x), getOctNote(x));
    }

    public float octcps(float x) {
        return getOctValue(getCpsOctave(x), getCpsNote(x));
    }

    public static float midipch(float x) {
        return getMidiValue(getPchOctave(x), getPchNote(x));
    }

    public static float pchmidi(float x) {
        return getPchValue(getMidiOctave(x), getMidiNote(x));
    }

    public static float midioct(float x) {
        return getMidiValue(getOctOctave(x), getOctNote(x));
    }

    public static float octmidi(float x) {
        return getOctValue(getMidiOctave(x), getMidiNote(x));
    }

    public float midicps(float x, float y) {
        return getMidiValue(getCpsOctave(x), getCpsNote(x));
    }

    public float cpsmidi(float x) {
        return getCpsValue(getMidiOctave(x), getMidiNote(x));
    }

    /*
     * helper methds
     */

 /*
   These methods use the following conventions:

   octave:
   ...
    6
    7
    8 octave of middle c
    9
   10
   ...

   note:
    0 C
    1 C#
    2 D
    3 D#
    4 E
    5 F
    6 F#
    7 G
    8 G#
    9 A
   10 A#
   11 B
 */

    private float getCpsValue(int octave, int note) {
        return getTune() * (float) Math.pow(2.0, (octave - 8) + (note - 9) / 12.0);
    }

    private int getCpsOctave(float cps) {
        double dRelativePitch = cps / getTune();
        double dTone = Math.log(dRelativePitch) * (1.0 / Math.log(2.0));
        return (int) dTone + 8;
    }

    private int getCpsNote(float cps) {
        double dRelativePitch = cps / getTune();
        double dTone = Math.log(dRelativePitch) * (1.0 / Math.log(2.0));
        return (int) ((dTone - (int) dTone) * 12.0);
    }

    private static float getMidiValue(int octave, int note) {
        return (octave - 3) * 12 + note;
    }

    private static int getMidiOctave(float midi) {
        return (int) midi / 12 + 3;
    }

    private static int getMidiNote(float midi) {
        return (int) midi % 12;
    }

    private static float getOctValue(int octave, int note) {
        return octave + note * (1.0F / 12.0F);
    }

    private static int getOctOctave(float oct) {
        return (int) oct;
    }

    private static int getOctNote(float oct) {
        return (int) ((oct - getOctOctave(oct)) * 12.0F);
    }

    private static float getPchValue(int octave, int note) {
        return octave + note * 0.01F;
    }

    private static int getPchOctave(float pch) {
        return (int) pch;
    }

    private static int getPchNote(float pch) {
        return (int) ((pch - getPchOctave(pch)) * 100.0F);
    }

    private static float notImplemented() {
        throw new RuntimeException("opcode not implemented");
    }
}
