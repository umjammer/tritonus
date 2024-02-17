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

import org.tritonus.saol.sablecc.node.AGlobaldeclGlobaldecl;
import org.tritonus.saol.sablecc.node.ARoutedefGlobaldef;
import org.tritonus.saol.sablecc.node.ARtparamGlobaldef;
import org.tritonus.saol.sablecc.node.ASenddefGlobaldef;
import org.tritonus.saol.sablecc.node.ASeqdefGlobaldef;


public class GlobalSemanticsCheck
        extends IOGTCommonSemanticsCheck {

    private static final boolean DEBUG = true;
    private static final int[] LEGAL_VARIABLE_TYPES = new int[]
            {
                    WidthAndRate.RATE_I,
                    WidthAndRate.RATE_K,
                    WidthAndRate.RATE_A,
                    WidthAndRate.RATE_OPARRAY,
            };

    private VariableTable m_globalVariableTable;

    public GlobalSemanticsCheck(VariableTable globalVariableTable,
                                NodeSemanticsTable nodeSemanticsTable) {
        super(nodeSemanticsTable);
        m_globalVariableTable = globalVariableTable;
    }


////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////

    @Override
    public void inAGlobaldeclGlobaldecl(AGlobaldeclGlobaldecl node) {
    }

    @Override
    public void outAGlobaldeclGlobaldecl(AGlobaldeclGlobaldecl node) {
    }

    @Override
    public void inARtparamGlobaldef(ARtparamGlobaldef node) {
    }

    @Override
    public void outARtparamGlobaldef(ARtparamGlobaldef node) {
    }

    @Override
    public void inARoutedefGlobaldef(ARoutedefGlobaldef node) {
    }

    @Override
    public void outARoutedefGlobaldef(ARoutedefGlobaldef node) {
    }

    @Override
    public void inASenddefGlobaldef(ASenddefGlobaldef node) {
    }

    @Override
    public void outASenddefGlobaldef(ASenddefGlobaldef node) {
    }

    @Override
    public void inASeqdefGlobaldef(ASeqdefGlobaldef node) {
    }

    @Override
    public void outASeqdefGlobaldef(ASeqdefGlobaldef node) {
    }


////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////

    @Override
    protected VariableTable getOwnVariableTable() {
        return m_globalVariableTable;
    }

    @Override
    protected VariableTable getGlobalVariableTable() {
        return null;
    }

    @Override
    protected int[] getLegalVariableTypes() {
        return LEGAL_VARIABLE_TYPES;
    }
}


