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


/**
 * Representation of one opcode implementation.
 * This class is used for entries in the opcode table.
 */
public class OpcodeEntry {

    private final String opcodeName;
    private final OpcodeClass opcodeClass;
    private final String methodName;
    private final int rate;
    // TODO parameter description, including dummy params

    // if opcode and method name are the same
    public OpcodeEntry(String opcodeName, OpcodeClass opcodeClass, int rate) {
        this(opcodeName, opcodeClass, opcodeName, rate);
    }

    public OpcodeEntry(String opcodeName, OpcodeClass opcodeClass, String methodName, int rate) {
        this.opcodeName = opcodeName;
        this.opcodeClass = opcodeClass;
        this.methodName = methodName;
        this.rate = rate;
    }

    public String getOpcodeName() {
        return opcodeName;
    }

    public OpcodeClass getOpcodeClass() {
        return opcodeClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getRate() {
        return rate;
    }
}
