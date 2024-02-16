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

import java.util.ArrayList;
import java.util.List;

import org.tritonus.saol.sablecc.analysis.DepthFirstAdapter;
import org.tritonus.saol.sablecc.node.*;


/**
 * IOGTCommonSemanticsCheck.java
 * <p>
 * This file is part of Tritonus: http://www.tritonus.org/
 */
public abstract class IOGTCommonSemanticsCheck
        extends DepthFirstAdapter {

    private static final boolean DEBUG = true;

    private NodeSemanticsTable m_nodeSemanticsTable;

    public IOGTCommonSemanticsCheck(NodeSemanticsTable nodeSemanticsTable) {
        m_nodeSemanticsTable = nodeSemanticsTable;
    }

    //

    public void inAIdentlistIdentlist(AIdentlistIdentlist node) {
    }

    public void outAIdentlistIdentlist(AIdentlistIdentlist node) {
    }

    public void inAIdentlistTailIdentlistTail(AIdentlistTailIdentlistTail node) {
    }

    public void outAIdentlistTailIdentlistTail(AIdentlistTailIdentlistTail node) {
    }

    public void inAParamlistParamlist(AParamlistParamlist node) {
    }

    public void outAParamlistParamlist(AParamlistParamlist node) {
    }

    public void inAParamlistTailParamlistTail(AParamlistTailParamlistTail node) {
    }

    public void outAParamlistTailParamlistTail(AParamlistTailParamlistTail node) {
    }

    public void inATablemapVardecl(ATablemapVardecl node) {
    }

    public void outATablemapVardecl(ATablemapVardecl node) {
    }

    public void outASigvarOpvardecl(ASigvarOpvardecl node) {
        boolean bImports = false;
        boolean bExports = false;
        if (node.getTaglist() != null) {
            NodeSemantics taglistSemantics = getNodeSemantics(node.getTaglist());
            String strImEx = (String) taglistSemantics.getAux();
            if (strImEx.indexOf('I') >= 0) {
                bImports = true;
            }
            if (strImEx.indexOf('E') >= 0) {
                bExports = true;
            }
            // TODO: check if matching global variable exists
        }
        int nRate = getNodeSemantics(node.getStype()).getRate();
        if (!isLegalVariableType(nRate)) {
            throw new RuntimeException("illegal variable type used");
        }
        @SuppressWarnings("unchecked")
        List<VariableEntry> instruments = (List<VariableEntry>) getNodeSemantics(node.getNamelist()).getAux();
        for (VariableEntry instrument : instruments) {
            VariableEntry variable = instrument;
            variable = new VariableEntry(variable.getVariableName(),
                    variable.getWidth(),
                    nRate,
                    bImports,
                    bExports);
            getOwnVariableTable().add(variable);
        }
    }


    public void outATablevarOpvardecl(ATablevarOpvardecl node) {
        boolean bImports = false;
        boolean bExports = false;
        // for tables, this is not optional
        NodeSemantics taglistSemantics = getNodeSemantics(node.getTaglist());
        String strImEx = (String) taglistSemantics.getAux();
        if (strImEx.indexOf('I') >= 0) {
            bImports = true;
        }
        if (strImEx.indexOf('E') >= 0) {
            bExports = true;
        }
        // TODO: check if matching global variable exists

        int nRate = WidthAndRate.RATE_TABLE;
        @SuppressWarnings("unchecked")
        List<VariableEntry> instruments = (List<VariableEntry>) getNodeSemantics(node.getNamelist()).getAux();
        for (VariableEntry instrument : instruments) {
            VariableEntry variable = instrument;
            variable = new VariableEntry(variable.getVariableName(),
                    variable.getWidth(),
                    nRate,
                    bImports,
                    bExports);
            getOwnVariableTable().add(variable);
        }
    }

    public void inATableOpvardecl(ATableOpvardecl node) {
    }

    public void outATableOpvardecl(ATableOpvardecl node) {
    }

    public void inAParamdeclParamdecl(AParamdeclParamdecl node) {
    }

    public void outAParamdeclParamdecl(AParamdeclParamdecl node) {
    }

    public void inANamelistNamelist(ANamelistNamelist node) {
        List<?> list = new ArrayList<>();
        NodeSemantics nodeSemantics = new NodeSemantics(list);
        setNodeSemantics(node, nodeSemantics);
    }

    public void outANamelistNamelist(ANamelistNamelist node) {
        VariableEntry variableEntry = (VariableEntry) getNodeSemantics(node.getName()).getAux();
        NodeSemantics nodeSemantics = getNodeSemantics(node);
        @SuppressWarnings("unchecked")
        List<VariableEntry> list = (List<VariableEntry>) nodeSemantics.getAux();
        list.add(variableEntry);
    }

    public void outANamelistTailNamelistTail(ANamelistTailNamelistTail node) {
        VariableEntry variableEntry = (VariableEntry) getNodeSemantics(node.getName()).getAux();
        NodeSemantics nodeSemantics = getNodeSemantics(node.parent());
        @SuppressWarnings("unchecked")
        List<VariableEntry> list = (List<VariableEntry>) nodeSemantics.getAux();
        list.add(variableEntry);
    }

    public void outASimpleName(ASimpleName node) {
        String strVariableName = node.getIdentifier().getText();
        handleName(node, strVariableName, 1);
    }

    public void outAIndexedName(AIndexedName node) {
        String strVariableName = node.getIdentifier().getText();
        String strInteger = node.getInteger().getText();
        int nInteger = Integer.parseInt(strInteger);
        handleName(node, strVariableName, nInteger);
    }

    public void outAInchannelsName(AInchannelsName node) {
        String strVariableName = node.getIdentifier().getText();
        handleName(node, strVariableName, WidthAndRate.WIDTH_INCHANNELS);
    }

    public void outAOutchannelsName(AOutchannelsName node) {
        String strVariableName = node.getIdentifier().getText();
        handleName(node, strVariableName, WidthAndRate.WIDTH_OUTCHANNELS);
    }

    // TODO: check if gathering of variable name can be generalized
    private void handleName(Node node, String strVariableName, int nWidth) {
        VariableEntry variableEntry = new VariableEntry(strVariableName, nWidth, WidthAndRate.RATE_UNKNOWN, false, false);
        NodeSemantics nodeSemantics = new NodeSemantics(variableEntry);
        setNodeSemantics(node, nodeSemantics);
    }

    public void outAIvarStype(AIvarStype node) {
        NodeSemantics nodeSemantics = new NodeSemantics(WidthAndRate.WIDTH_UNKNOWN, WidthAndRate.RATE_I);
        setNodeSemantics(node, nodeSemantics);
    }

    public void outAKsigStype(AKsigStype node) {
        NodeSemantics nodeSemantics = new NodeSemantics(WidthAndRate.WIDTH_UNKNOWN, WidthAndRate.RATE_K);
        setNodeSemantics(node, nodeSemantics);
    }

    public void outAAsigStype(AAsigStype node) {
        NodeSemantics nodeSemantics = new NodeSemantics(WidthAndRate.WIDTH_UNKNOWN, WidthAndRate.RATE_A);
        setNodeSemantics(node, nodeSemantics);
    }

    public void outAOparrayStype(AOparrayStype node) {
        NodeSemantics nodeSemantics = new NodeSemantics(WidthAndRate.WIDTH_UNKNOWN, WidthAndRate.RATE_OPARRAY);
        setNodeSemantics(node, nodeSemantics);
    }

    public void outAXsigStype(AXsigStype node) {
        NodeSemantics nodeSemantics = new NodeSemantics(WidthAndRate.WIDTH_UNKNOWN, WidthAndRate.RATE_X);
        setNodeSemantics(node, nodeSemantics);
    }

    public void inATabledeclTabledecl(ATabledeclTabledecl node) {
    }

    public void outATabledeclTabledecl(ATabledeclTabledecl node) {
    }

    public void outAImportsTaglist(AImportsTaglist node) {
        NodeSemantics nodeSemantics = new NodeSemantics("I");
        setNodeSemantics(node, nodeSemantics);
    }

    public void outAExportsTaglist(AExportsTaglist node) {
        NodeSemantics nodeSemantics = new NodeSemantics("E");
        setNodeSemantics(node, nodeSemantics);
    }

    public void outAImportsexportsTaglist(AImportsexportsTaglist node) {
        NodeSemantics nodeSemantics = new NodeSemantics("IE");
        setNodeSemantics(node, nodeSemantics);
    }

    public void outAExportsimportsTaglist(AExportsimportsTaglist node) {
        NodeSemantics nodeSemantics = new NodeSemantics("IE");
        setNodeSemantics(node, nodeSemantics);
    }

    public void inAAopcodeOptype(AAopcodeOptype node) {
    }

    public void outAAopcodeOptype(AAopcodeOptype node) {
    }


    public void inAKopcodeOptype(AKopcodeOptype node) {
    }

    public void outAKopcodeOptype(AKopcodeOptype node) {
    }

    public void inAIopcodeOptype(AIopcodeOptype node) {
    }

    public void outAIopcodeOptype(AIopcodeOptype node) {
    }

    public void inAOpcodeOptype(AOpcodeOptype node) {
    }

    public void outAOpcodeOptype(AOpcodeOptype node) {
    }

    public void inAAltExpr(AAltExpr node) {
        // TODO:
    }

    public void outAAltExpr(AAltExpr node) {
        // TODO:
    }

    public void outAOrOrexpr(AOrOrexpr node) {
        // TODO:
    }

    public void outAAndAndexpr(AAndAndexpr node) {
        // TODO:
    }

    public void outANeqEqualityexpr(ANeqEqualityexpr node) {
    }

    public void outAEqEqualityexpr(AEqEqualityexpr node) {
    }

    public void inAGtRelationalexpr(AGtRelationalexpr node) {
    }

    public void outAGtRelationalexpr(AGtRelationalexpr node) {
    }

    public void outALtRelationalexpr(ALtRelationalexpr node) {
    }

    public void outALteqRelationalexpr(ALteqRelationalexpr node) {
    }

    public void outAGteqRelationalexpr(AGteqRelationalexpr node) {
    }

    public void outAPlusAddexpr(APlusAddexpr node) {
    }

    public void outAMinusAddexpr(AMinusAddexpr node) {
    }

    public void outAMultFactor(AMultFactor node) {
    }

    public void outADivFactor(ADivFactor node) {
    }

    public void outANotUnaryminusterm(ANotUnaryminusterm node) {
    }

    public void outANotNotterm(ANotNotterm node) {
    }

    public void outAIdentifierTerm(AIdentifierTerm node) {
    }

    public void outAConstantTerm(AConstantTerm node) {
    }

    public void inAIndexedTerm(AIndexedTerm node) {
    }

    /**
     * The array reference still is on the stack. Now,
     * also the array index (as a float) is on the stack.
     * It has to be transformed to integer.
     */
    public void outAIndexedTerm(AIndexedTerm node) {
//   // TODO: correct rounding (1.5 -> 2.0)
//   m_aMethods[METHOD_A].appendInstruction(InstructionConstants.F2I);
//   // and now fetch the value from the array
//   setNodeAttribute(node, InstructionConstants.FALOAD);
    }

    public void inASasbfTerm(ASasbfTerm node) {
    }

    public void outASasbfTerm(ASasbfTerm node) {
    }

    public void inAFunctionTerm(AFunctionTerm node) {
    }

    public void outAFunctionTerm(AFunctionTerm node) {
    }

    public void inAIndexedfunctionTerm(AIndexedfunctionTerm node) {
    }

    public void outAIndexedfunctionTerm(AIndexedfunctionTerm node) {
    }

    public void inAExprlistExprlist(AExprlistExprlist node) {
    }

    public void outAExprlistExprlist(AExprlistExprlist node) {
    }

    public void inAExprlistTailExprlistTail(AExprlistTailExprlistTail node) {
    }

    public void outAExprlistTailExprlistTail(AExprlistTailExprlistTail node) {
    }

    public void inAExprstrlistExprstrlist(AExprstrlistExprstrlist node) {
    }

    public void outAExprstrlistExprstrlist(AExprstrlistExprstrlist node) {
    }

    public void inAExprstrlistTailExprstrlistTail(AExprstrlistTailExprstrlistTail node) {
    }

    public void outAExprstrlistTailExprstrlistTail(AExprstrlistTailExprstrlistTail node) {
    }

    public void inAExprExprOrString(AExprExprOrString node) {
    }

    public void outAExprExprOrString(AExprExprOrString node) {
    }

    public void inAStringExprOrString(AStringExprOrString node) {
    }

    public void outAStringExprOrString(AStringExprOrString node) {
    }

    public void inAIntegerConst(AIntegerConst node) {
    }

    public void outAIntegerConst(AIntegerConst node) {
    }

    public void outANumberConst(ANumberConst node) {
    }

    //

    protected abstract VariableTable getOwnVariableTable();

    protected abstract VariableTable getGlobalVariableTable();

    protected abstract int[] getLegalVariableTypes();

    protected boolean isLegalVariableType(int nType) {
        int[] anLegalTypes = getLegalVariableTypes();
        for (int anLegalType : anLegalTypes) {
            if (anLegalType == nType) {
                return true;
            }
        }
        return false;
    }

    protected void setNodeSemantics(Node node, NodeSemantics nodeSemantics) {
        m_nodeSemanticsTable.setNodeSemantics(node, nodeSemantics);
    }

    protected NodeSemantics getNodeSemantics(Node node) {
        return m_nodeSemanticsTable.getNodeSemantics(node);
    }
}


/* IOGTCommonSemanticsCheck.java */
