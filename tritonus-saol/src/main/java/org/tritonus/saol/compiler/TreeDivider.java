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

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.tritonus.saol.sablecc.analysis.DepthFirstAdapter;
import org.tritonus.saol.sablecc.node.AGlobaldeclGlobaldecl;
import org.tritonus.saol.sablecc.node.AInstrdeclInstrdecl;
import org.tritonus.saol.sablecc.node.AOpcodedeclOpcodedecl;
import org.tritonus.saol.sablecc.node.ATemplatedeclTemplatedecl;

import static java.lang.System.getLogger;


public class TreeDivider extends DepthFirstAdapter {

    private static final Logger logger = getLogger(TreeDivider.class.getName());
    
    private static final boolean DEBUG = true;

    private InstrumentTable m_instrumentTable;
    private UserOpcodeTable m_opcodeTable;
    private TemplateTable m_templateTable;
    private AGlobaldeclGlobaldecl m_globalNode;

    public TreeDivider(InstrumentTable instrumentTable,
                       UserOpcodeTable opcodeTable,
                       TemplateTable templateTable) {
        m_instrumentTable = instrumentTable;
        m_opcodeTable = opcodeTable;
        m_templateTable = templateTable;
        m_globalNode = null;
    }

    public AGlobaldeclGlobaldecl getGlobalNode() {
        return m_globalNode;
    }

    @Override
    public void inAInstrdeclInstrdecl(AInstrdeclInstrdecl node) {
        String strInstrumentName = node.getIdentifier().getText();
        InstrumentEntry instrument = new InstrumentEntry(strInstrumentName, node);
        m_instrumentTable.add(instrument);
    }

    @Override
    public void inAOpcodedeclOpcodedecl(AOpcodedeclOpcodedecl node) {
        String strOpcodeName = node.getIdentifier().getText();
        UserOpcodeEntry opcode = new UserOpcodeEntry(strOpcodeName, node);
        m_opcodeTable.add(opcode);
    }

    @Override
    public void inAGlobaldeclGlobaldecl(AGlobaldeclGlobaldecl node) {
        logger.log(Level.TRACE, "TreeDivider.inAGlobaldeclGlobaldecl()");
        m_globalNode = node;
    }

    @Override
    public void inATemplatedeclTemplatedecl(ATemplatedeclTemplatedecl node) {
        // hack to make compile
        String strTemplateName = "---";
        // String strTemplateName = node.getIdentifier().getText();
        TemplateEntry template = new TemplateEntry(strTemplateName, node);
        m_templateTable.add(template);
    }
}


