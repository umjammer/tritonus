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

package org.tritonus.saol.compiler;

import org.tritonus.saol.sablecc.node.AAopcodeOptype;
import org.tritonus.saol.sablecc.node.AIopcodeOptype;
import org.tritonus.saol.sablecc.node.AKopcodeOptype;
import org.tritonus.saol.sablecc.node.AOpcodeOptype;
import org.tritonus.saol.sablecc.node.AOpcodedeclOpcodedecl;
import org.tritonus.saol.sablecc.node.AParamdeclParamdecl;
import org.tritonus.saol.sablecc.node.AParamlistParamlist;
import org.tritonus.saol.sablecc.node.AParamlistTailParamlistTail;


public class OpcodeSemanticsCheck
        extends IOTCommonSemanticsCheck {

    private static final boolean DEBUG = true;
    private static final int[] LEGAL_VARIABLE_TYPES = new int[]
            {
                    WidthAndRate.RATE_I,
                    WidthAndRate.RATE_K,
                    WidthAndRate.RATE_A,
                    WidthAndRate.RATE_X,
                    WidthAndRate.RATE_OPARRAY,
            };

    private VariableTable m_globalVariableTable;
    private VariableTable m_localVariableTable;

    public OpcodeSemanticsCheck(VariableTable globalVariableTable,
                                VariableTable localVariableTable,
                                NodeSemanticsTable nodeSemanticsTable) {
        super(nodeSemanticsTable);
        m_globalVariableTable = globalVariableTable;
        m_localVariableTable = localVariableTable;
    }


////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////

    @Override
    public void inAOpcodedeclOpcodedecl(AOpcodedeclOpcodedecl node) {
    }

    @Override
    public void outAOpcodedeclOpcodedecl(AOpcodedeclOpcodedecl node) {
    }

    @Override
    public void inAAopcodeOptype(AAopcodeOptype node) {
    }

    @Override
    public void outAAopcodeOptype(AAopcodeOptype node) {
    }

    @Override
    public void inAKopcodeOptype(AKopcodeOptype node) {
    }

    @Override
    public void outAKopcodeOptype(AKopcodeOptype node) {
    }

    @Override
    public void inAIopcodeOptype(AIopcodeOptype node) {
    }

    @Override
    public void outAIopcodeOptype(AIopcodeOptype node) {
    }

    @Override
    public void inAOpcodeOptype(AOpcodeOptype node) {
    }

    @Override
    public void outAOpcodeOptype(AOpcodeOptype node) {
    }

    @Override
    public void inAParamlistParamlist(AParamlistParamlist node) {
    }

    @Override
    public void outAParamlistParamlist(AParamlistParamlist node) {
    }

    @Override
    public void inAParamlistTailParamlistTail(AParamlistTailParamlistTail node) {
    }

    @Override
    public void outAParamlistTailParamlistTail(AParamlistTailParamlistTail node) {
    }

    @Override
    public void inAParamdeclParamdecl(AParamdeclParamdecl node) {
    }

    @Override
    public void outAParamdeclParamdecl(AParamdeclParamdecl node) {
    }


////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////

    @Override
    protected VariableTable getOwnVariableTable() {
        return m_localVariableTable;
    }

    @Override
    protected VariableTable getGlobalVariableTable() {
        return m_globalVariableTable;
    }

    @Override
    protected int[] getLegalVariableTypes() {
        return LEGAL_VARIABLE_TYPES;
    }
}


