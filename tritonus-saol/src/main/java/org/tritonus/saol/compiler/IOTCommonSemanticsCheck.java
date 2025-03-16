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

import org.tritonus.saol.sablecc.node.*;


/**
 * IOTCommonSemanticsCheck.
 *
 * This file is part of Tritonus: http://www.tritonus.org/
 */
public abstract class IOTCommonSemanticsCheck extends IOGTCommonSemanticsCheck {

    private static final boolean DEBUG = true;

    public IOTCommonSemanticsCheck(NodeSemanticsTable nodeSemanticsTable) {
        super(nodeSemanticsTable);
    }

    //

    @Override
    public void inAIdentlistIdentlist(AIdentlistIdentlist node) {
    }

    @Override
    public void outAIdentlistIdentlist(AIdentlistIdentlist node) {
    }

    @Override
    public void inAIdentlistTailIdentlistTail(AIdentlistTailIdentlistTail node) {
    }

    @Override
    public void outAIdentlistTailIdentlistTail(AIdentlistTailIdentlistTail node) {
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
    public void inATablemapVardecl(ATablemapVardecl node) {
    }

    @Override
    public void outATablemapVardecl(ATablemapVardecl node) {
    }

    @Override
    public void outASigvarOpvardecl(ASigvarOpvardecl node) {
        boolean imports = false;
        boolean exports = false;
        if (node.getTaglist() != null) {
            NodeSemantics tagListSemantics = getNodeSemantics(node.getTaglist());
            String text = (String) tagListSemantics.getAux();
            if (text.indexOf('I') >= 0) {
                imports = true;
            }
            if (text.indexOf('E') >= 0) {
                exports = true;
            }
            // TODO check if matching global variable exists
        }
        int rate = getNodeSemantics(node.getStype()).getRate();
        if (!isLegalVariableType(rate)) {
            throw new RuntimeException("illegal variable type used");
        }
        @SuppressWarnings("unchecked")
        List<VariableEntry> instruments = (List<VariableEntry>) getNodeSemantics(node.getNamelist()).getAux();
        for (VariableEntry instrument : instruments) {
            VariableEntry variable = instrument;
            variable = new VariableEntry(variable.getVariableName(),
                    variable.getWidth(),
                    rate,
                    imports,
                    exports);
            getOwnVariableTable().add(variable);
        }
    }

    @Override
    public void outATablevarOpvardecl(ATablevarOpvardecl node) {
        boolean imports = false;
        boolean exports = false;
        // for tables, this is not optional
        NodeSemantics taglistSemantics = getNodeSemantics(node.getTaglist());
        String text = (String) taglistSemantics.getAux();
        if (text.indexOf('I') >= 0) {
            imports = true;
        }
        if (text.indexOf('E') >= 0) {
            exports = true;
        }
        // TODO check if matching global variable exists

        int rate = WidthAndRate.RATE_TABLE;
        @SuppressWarnings("unchecked")
        List<VariableEntry> instruments = (List<VariableEntry>) getNodeSemantics(node.getNamelist()).getAux();
        for (VariableEntry instrument : instruments) {
            VariableEntry variable = instrument;
            variable = new VariableEntry(variable.getVariableName(),
                    variable.getWidth(),
                    rate,
                    imports,
                    exports);
            getOwnVariableTable().add(variable);
        }
    }

    @Override
    public void inATableOpvardecl(ATableOpvardecl node) {
    }

    @Override
    public void outATableOpvardecl(ATableOpvardecl node) {
    }

    @Override
    public void inAParamdeclParamdecl(AParamdeclParamdecl node) {
    }

    @Override
    public void outAParamdeclParamdecl(AParamdeclParamdecl node) {
    }

    @Override
    public void inANamelistNamelist(ANamelistNamelist node) {
        List<?> list = new ArrayList<>();
        NodeSemantics nodeSemantics = new NodeSemantics(list);
        setNodeSemantics(node, nodeSemantics);
    }

    @Override
    public void outANamelistNamelist(ANamelistNamelist node) {
        VariableEntry variableEntry = (VariableEntry) getNodeSemantics(node.getName()).getAux();
        NodeSemantics nodeSemantics = getNodeSemantics(node);
        @SuppressWarnings("unchecked")
        List<VariableEntry> list = (List<VariableEntry>) nodeSemantics.getAux();
        list.add(variableEntry);
    }

    @Override
    public void outANamelistTailNamelistTail(ANamelistTailNamelistTail node) {
        VariableEntry variableEntry = (VariableEntry) getNodeSemantics(node.getName()).getAux();
        NodeSemantics nodeSemantics = getNodeSemantics(node.parent());
        @SuppressWarnings("unchecked")
        List<VariableEntry> list = (List<VariableEntry>) nodeSemantics.getAux();
        list.add(variableEntry);
    }

    @Override
    public void outASimpleName(ASimpleName node) {
        String variableName = node.getIdentifier().getText();
        handleName(node, variableName, 1);
    }

    @Override
    public void outAIndexedName(AIndexedName node) {
        String variableName = node.getIdentifier().getText();
        String _integer = node.getInteger().getText();
        int integer = Integer.parseInt(_integer);
        handleName(node, variableName, integer);
    }

    @Override
    public void outAInchannelsName(AInchannelsName node) {
        String variableName = node.getIdentifier().getText();
        handleName(node, variableName, WidthAndRate.WIDTH_INCHANNELS);
    }

    @Override
    public void outAOutchannelsName(AOutchannelsName node) {
        String variableName = node.getIdentifier().getText();
        handleName(node, variableName, WidthAndRate.WIDTH_OUTCHANNELS);
    }

    // TODO check if gathering of variable name can be generalized
    private void handleName(Node node, String variableName, int width) {
        VariableEntry variableEntry = new VariableEntry(variableName, width, WidthAndRate.RATE_UNKNOWN, false, false);
        NodeSemantics nodeSemantics = new NodeSemantics(variableEntry);
        setNodeSemantics(node, nodeSemantics);
    }

    @Override
    public void outAIvarStype(AIvarStype node) {
        NodeSemantics nodeSemantics = new NodeSemantics(WidthAndRate.WIDTH_UNKNOWN, WidthAndRate.RATE_I);
        setNodeSemantics(node, nodeSemantics);
    }

    @Override
    public void outAKsigStype(AKsigStype node) {
        NodeSemantics nodeSemantics = new NodeSemantics(WidthAndRate.WIDTH_UNKNOWN, WidthAndRate.RATE_K);
        setNodeSemantics(node, nodeSemantics);
    }

    @Override
    public void outAAsigStype(AAsigStype node) {
        NodeSemantics nodeSemantics = new NodeSemantics(WidthAndRate.WIDTH_UNKNOWN, WidthAndRate.RATE_A);
        setNodeSemantics(node, nodeSemantics);
    }

    @Override
    public void outAOparrayStype(AOparrayStype node) {
        NodeSemantics nodeSemantics = new NodeSemantics(WidthAndRate.WIDTH_UNKNOWN, WidthAndRate.RATE_OPARRAY);
        setNodeSemantics(node, nodeSemantics);
    }

    @Override
    public void outAXsigStype(AXsigStype node) {
        NodeSemantics nodeSemantics = new NodeSemantics(WidthAndRate.WIDTH_UNKNOWN, WidthAndRate.RATE_X);
        setNodeSemantics(node, nodeSemantics);
    }

    @Override
    public void inATabledeclTabledecl(ATabledeclTabledecl node) {
    }

    @Override
    public void outATabledeclTabledecl(ATabledeclTabledecl node) {
    }

    @Override
    public void outAImportsTaglist(AImportsTaglist node) {
        NodeSemantics nodeSemantics = new NodeSemantics("I");
        setNodeSemantics(node, nodeSemantics);
    }

    @Override
    public void outAExportsTaglist(AExportsTaglist node) {
        NodeSemantics nodeSemantics = new NodeSemantics("E");
        setNodeSemantics(node, nodeSemantics);
    }

    @Override
    public void outAImportsexportsTaglist(AImportsexportsTaglist node) {
        NodeSemantics nodeSemantics = new NodeSemantics("IE");
        setNodeSemantics(node, nodeSemantics);
    }

    @Override
    public void outAExportsimportsTaglist(AExportsimportsTaglist node) {
        NodeSemantics nodeSemantics = new NodeSemantics("IE");
        setNodeSemantics(node, nodeSemantics);
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
    public void inAAltExpr(AAltExpr node) {
        // TODO
    }

    @Override
    public void outAAltExpr(AAltExpr node) {
        // TODO
    }

    @Override
    public void outAOrOrexpr(AOrOrexpr node) {
        // TODO
    }

    @Override
    public void outAAndAndexpr(AAndAndexpr node) {
        // TODO
    }

    @Override
    public void outANeqEqualityexpr(ANeqEqualityexpr node) {
    }

    @Override
    public void outAEqEqualityexpr(AEqEqualityexpr node) {
    }

    @Override
    public void inAGtRelationalexpr(AGtRelationalexpr node) {
    }

    @Override
    public void outAGtRelationalexpr(AGtRelationalexpr node) {
    }

    @Override
    public void outALtRelationalexpr(ALtRelationalexpr node) {
    }

    @Override
    public void outALteqRelationalexpr(ALteqRelationalexpr node) {
    }

    @Override
    public void outAGteqRelationalexpr(AGteqRelationalexpr node) {
    }

    @Override
    public void outAPlusAddexpr(APlusAddexpr node) {
    }

    @Override
    public void outAMinusAddexpr(AMinusAddexpr node) {
    }

    @Override
    public void outAMultFactor(AMultFactor node) {
    }

    @Override
    public void outADivFactor(ADivFactor node) {
    }

    @Override
    public void outANotUnaryminusterm(ANotUnaryminusterm node) {
    }

    @Override
    public void outANotNotterm(ANotNotterm node) {
    }

    @Override
    public void outAIdentifierTerm(AIdentifierTerm node) {
    }

    @Override
    public void outAConstantTerm(AConstantTerm node) {
    }

    @Override
    public void inAIndexedTerm(AIndexedTerm node) {
    }

    /**
     * The array reference still is on the stack. Now,
     * also the array index (as a float) is on the stack.
     * It has to be transformed to integer.
     */
    @Override
    public void outAIndexedTerm(AIndexedTerm node) {
//        // TODO correct rounding (1.5 -> 2.0)
//        methods[METHOD_A].appendInstruction(InstructionConstants.F2I);
//        // and now fetch the value from the array
//        setNodeAttribute(node, InstructionConstants.FALOAD);
    }

    @Override
    public void inASasbfTerm(ASasbfTerm node) {
    }

    @Override
    public void outASasbfTerm(ASasbfTerm node) {
    }

    @Override
    public void inAFunctionTerm(AFunctionTerm node) {
    }

    @Override
    public void outAFunctionTerm(AFunctionTerm node) {
    }

    @Override
    public void inAIndexedfunctionTerm(AIndexedfunctionTerm node) {
    }

    @Override
    public void outAIndexedfunctionTerm(AIndexedfunctionTerm node) {
    }

    @Override
    public void inAExprlistExprlist(AExprlistExprlist node) {
    }

    @Override
    public void outAExprlistExprlist(AExprlistExprlist node) {
    }

    @Override
    public void inAExprlistTailExprlistTail(AExprlistTailExprlistTail node) {
    }

    @Override
    public void outAExprlistTailExprlistTail(AExprlistTailExprlistTail node) {
    }

    @Override
    public void inAExprstrlistExprstrlist(AExprstrlistExprstrlist node) {
    }

    @Override
    public void outAExprstrlistExprstrlist(AExprstrlistExprstrlist node) {
    }

    @Override
    public void inAExprstrlistTailExprstrlistTail(AExprstrlistTailExprstrlistTail node) {
    }

    @Override
    public void outAExprstrlistTailExprstrlistTail(AExprstrlistTailExprstrlistTail node) {
    }

    @Override
    public void inAExprExprOrString(AExprExprOrString node) {
    }

    @Override
    public void outAExprExprOrString(AExprExprOrString node) {
    }

    @Override
    public void inAStringExprOrString(AStringExprOrString node) {
    }

    @Override
    public void outAStringExprOrString(AStringExprOrString node) {
    }

    @Override
    public void inAIntegerConst(AIntegerConst node) {
    }

    @Override
    public void outAIntegerConst(AIntegerConst node) {
    }

    @Override
    public void outANumberConst(ANumberConst node) {
    }
}
