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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ArrayType;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.CompoundInstruction;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.GOTO;
import org.apache.bcel.generic.IFEQ;
import org.apache.bcel.generic.IFGE;
import org.apache.bcel.generic.IFGT;
import org.apache.bcel.generic.IFLE;
import org.apache.bcel.generic.IFLT;
import org.apache.bcel.generic.IFNE;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionConst;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;
import org.tritonus.saol.engine.AbstractInstrument;
import org.tritonus.saol.sablecc.analysis.DepthFirstAdapter;
import org.tritonus.saol.sablecc.node.*;


/**
 * InstrumentCompilation.java
 * <p>
 * This file is part of Tritonus: http://www.tritonus.org/
 */
public class InstrumentCompilation extends DepthFirstAdapter {

    private static final boolean DEBUG = true;

    // may become "org.tritonus.saol.generated."
    private static final String PACKAGE_PREFIX = "";
    private static final String CLASSFILENAME_PREFIX = "src/";
    private static final String CLASSFILENAME_SUFFIX = ".class";
    private static final String SUPERCLASS_NAME = "org.tritonus.saol.engine.AbstractInstrument";
    private static final String SUPERCLASS_CONSTRUCTOR_NAME = "AbstractInstrument";

    private static final int METHOD_CONSTR = WidthAndRate.RATE_UNKNOWN;
    private static final int METHOD_I = WidthAndRate.RATE_I;
    private static final int METHOD_K = WidthAndRate.RATE_K;
    private static final int METHOD_A = WidthAndRate.RATE_A;

    private static final Type FLOAT_ARRAY = new ArrayType(Type.FLOAT, 1);

    private SAOLGlobals m_saolGlobals;

    // maps instrument names (String) to classes (Class)
    private Map<String, Class<AbstractInstrument>> m_instrumentMap;
    private Map<Node, Object> m_nodeAttributes;
    private String m_strClassName;
    private ClassGen m_classGen;
    private ConstantPoolGen m_constantPoolGen;
    //    private MethodGen m_methodGen;
//    private InstructionList m_instructionList;
    private InstructionFactory m_instructionFactory;
//    private BranchInstruction m_pendingBranchInstruction;

    // TODO: should be made obsolete by using node attributes
    private boolean m_bOpvardecls;
    private MemoryClassLoader m_classLoader = new MemoryClassLoader();

    // 0: constructor
    // 1: doIPass()
    // 2: doKPass()
    // 3: doAPass()
    private InstrumentMethod[] m_aMethods;

    public InstrumentCompilation(SAOLGlobals saolGlobals, Map<String, Class<AbstractInstrument>> instrumentMap) {
        m_saolGlobals = saolGlobals;
        m_instrumentMap = instrumentMap;
        m_nodeAttributes = new HashMap<>();
        m_aMethods = new InstrumentMethod[4];
    }

    @Override
    public void inAInstrdeclInstrdecl(AInstrdeclInstrdecl node) {
        String strInstrumentName = node.getIdentifier().getText();
        m_strClassName = PACKAGE_PREFIX + strInstrumentName;
        m_classGen = new ClassGen(m_strClassName,
                SUPERCLASS_NAME,
                "<generated>",
                Const.ACC_PUBLIC | Const.ACC_SUPER,
                null);
        m_constantPoolGen = m_classGen.getConstantPool();
        m_instructionFactory = new InstructionFactory(m_constantPoolGen);
        m_aMethods[METHOD_CONSTR] = new InstrumentMethod(m_classGen, "<init>");
        m_aMethods[METHOD_I] = new InstrumentMethod(m_classGen, "doIPass");
        m_aMethods[METHOD_K] = new InstrumentMethod(m_classGen, "doKPass");
        m_aMethods[METHOD_A] = new InstrumentMethod(m_classGen, "doAPass");
        m_aMethods[METHOD_CONSTR].appendInstruction(InstructionConst.ALOAD_0);
        Instruction invokeSuperInstruction = m_instructionFactory.createInvoke(SUPERCLASS_NAME, "<init>", Type.VOID, Type.NO_ARGS, Const.INVOKESPECIAL);
//        Instruction invokeSuperInstruction = m_instructionFactory.createInvoke(SUPERCLASS_NAME, SUPERCLASS_CONSTRUCTOR_NAME, Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL);
        m_aMethods[METHOD_CONSTR].appendInstruction(invokeSuperInstruction);
    }

    @Override
    public void outAInstrdeclInstrdecl(AInstrdeclInstrdecl node) {
        for (InstrumentMethod m_aMethod : m_aMethods) {
            m_aMethod.finish();
        }
        JavaClass javaClass = m_classGen.getJavaClass();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javaClass.dump(baos);
            byte[] abData = baos.toByteArray();
            @SuppressWarnings("unchecked")
            Class<AbstractInstrument> instrumentClass = (Class<AbstractInstrument>) m_classLoader.findClass(m_strClassName, abData);
            m_instrumentMap.put(m_strClassName, instrumentClass);
            if (DEBUG) {
                javaClass.dump(m_strClassName + CLASSFILENAME_SUFFIX);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void inABlockBlock(ABlockBlock node) {
    }

    @Override
    public void outABlockBlock(ABlockBlock node) {
    }

    @Override
    public void outAAssignmentStatement(AAssignmentStatement node) {
        Instruction instruction = (Instruction) getNodeAttribute(node.getLvalue());
        m_aMethods[METHOD_A].appendInstruction(instruction);
    }

    @Override
    public void inAExpressionStatement(AExpressionStatement node) {
    }

    @Override
    public void outAExpressionStatement(AExpressionStatement node) {
    }

    @Override
    public void inAIfStatement(AIfStatement node) {
    }

    @Override
    public void outAIfStatement(AIfStatement node) {
    }

    @Override
    public void caseAIfStatement(AIfStatement node) {
        inAIfStatement(node);
        if (node.getIf() != null) {
            node.getIf().apply(this);
        }
        if (node.getLPar() != null) {
            node.getLPar().apply(this);
        }
        if (node.getExpr() != null) {
            node.getExpr().apply(this);
        }
        if (node.getRPar() != null) {
            node.getRPar().apply(this);
        }
        m_aMethods[METHOD_A].appendInstruction(InstructionConst.FCONST_0);
        m_aMethods[METHOD_A].appendInstruction(InstructionConst.FCMPL);
        BranchInstruction ifeq = new IFEQ(null);
        m_aMethods[METHOD_A].appendInstruction(ifeq);
        if (node.getLBrace() != null) {
            node.getLBrace().apply(this);
        }
        if (node.getBlock() != null) {
            node.getBlock().apply(this);
        }
        if (node.getRBrace() != null) {
            node.getRBrace().apply(this);
        }
        m_aMethods[METHOD_A].setPendingBranchInstruction(ifeq);
        outAIfStatement(node);
    }

    @Override
    public void inAIfElseStatement(AIfElseStatement node) {
    }

    @Override
    public void outAIfElseStatement(AIfElseStatement node) {
    }

    @Override
    public void inAWhileStatement(AWhileStatement node) {
    }

    @Override
    public void outAWhileStatement(AWhileStatement node) {
    }

    @Override
    public void inAInstrumentStatement(AInstrumentStatement node) {
    }

    @Override
    public void outAInstrumentStatement(AInstrumentStatement node) {
    }

    @Override
    public void inAOutputStatement(AOutputStatement node) {
    }

    @Override
    public void outAOutputStatement(AOutputStatement node) {
    }

    @Override
    public void inASpatializeStatement(ASpatializeStatement node) {
    }

    @Override
    public void outASpatializeStatement(ASpatializeStatement node) {
    }

    @Override
    public void inAOutbusStatement(AOutbusStatement node) {
    }

    @Override
    public void outAOutbusStatement(AOutbusStatement node) {
    }

    @Override
    public void inAExtendStatement(AExtendStatement node) {
    }

    @Override
    public void outAExtendStatement(AExtendStatement node) {
    }

    @Override
    public void inATurnoffStatement(ATurnoffStatement node) {
    }

    @Override
    public void outATurnoffStatement(ATurnoffStatement node) {
    }

    @Override
    public void inAReturnStatement(AReturnStatement node) {
    }

    @Override
    public void outAReturnStatement(AReturnStatement node) {
    }

    /**
     * This is needed at the very end, when the putfield
     * instruction is executed.
     */
    @Override
    public void outASimpleLvalue(ASimpleLvalue node) {
        m_aMethods[METHOD_A].appendInstruction(InstructionConst.ALOAD_0);
        String strVariableName = node.getIdentifier().getText();
        // TODO: use getClassName()
        // set the instruction to be executed after the rvalue is calculated
        Instruction instruction = getInstructionFactory().createPutField(m_strClassName, strVariableName, Type.FLOAT);
        setNodeAttribute(node, instruction);
    }

    @Override
    public void inAIndexedLvalue(AIndexedLvalue node) {
        // push the array reference onto the stack
        String strVariableName = node.getIdentifier().getText();
        m_aMethods[METHOD_A].appendGetField(strVariableName);
    }

    /**
     * The array reference still is on the stack. Now,
     * also the array index (as a float) is on the stack.
     * It has to be transformed to integer.
     */
    @Override
    public void outAIndexedLvalue(AIndexedLvalue node) {
        // TODO: correct rounding (1.5 -> 2.0)
        m_aMethods[METHOD_A].appendInstruction(InstructionConst.F2I);
        // set the instruction to be executed after the rvalue is calculated
        setNodeAttribute(node, InstructionConst.FASTORE);
    }

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
    public void inASigvarOpvardecl(ASigvarOpvardecl node) {
        m_bOpvardecls = true;
    }

    @Override
    public void outASigvarOpvardecl(ASigvarOpvardecl node) {
        m_bOpvardecls = false;
    }

    @Override
    public void inAParamdeclParamdecl(AParamdeclParamdecl node) {
    }

    @Override
    public void outAParamdeclParamdecl(AParamdeclParamdecl node) {
    }

    @Override
    public void inANamelistNamelist(ANamelistNamelist node) {
    }

    @Override
    public void outANamelistNamelist(ANamelistNamelist node) {
    }

    @Override
    public void inANamelistTailNamelistTail(ANamelistTailNamelistTail node) {
    }

    @Override
    public void outANamelistTailNamelistTail(ANamelistTailNamelistTail node) {
    }

    @Override
    public void outASimpleName(ASimpleName node) {
        if (m_bOpvardecls) {
            String strVariableName = node.getIdentifier().getText();
            addLocalVariable(strVariableName);
        }
    }

    @Override
    public void outAIndexedName(AIndexedName node) {
        if (m_bOpvardecls) {
            String strVariableName = node.getIdentifier().getText();
            String strInteger = node.getInteger().getText();
            int nInteger = Integer.parseInt(strInteger);
            addLocalArray(strVariableName);
            // code to allocate array in constructor
            m_aMethods[METHOD_CONSTR].appendInstruction(InstructionConst.ALOAD_0);
            Instruction instruction = getInstructionFactory().createNewArray(Type.FLOAT, (short) nInteger);
            m_aMethods[METHOD_CONSTR].appendInstruction(instruction);
            m_aMethods[METHOD_CONSTR].appendPutField(strVariableName);
        }
    }

    @Override
    public void outAInchannelsName(AInchannelsName node) {
        // TODO:
    }

    @Override
    public void outAOutchannelsName(AOutchannelsName node) {
        // TODO:
    }

    @Override
    public void outAIvarStype(AIvarStype node) {
        setNodeAttribute(node, new WidthAndRate(WidthAndRate.WIDTH_UNKNOWN, WidthAndRate.RATE_I));
    }

    @Override
    public void outAKsigStype(AKsigStype node) {
        setNodeAttribute(node, new WidthAndRate(WidthAndRate.WIDTH_UNKNOWN, WidthAndRate.RATE_K));
    }

    @Override
    public void outAAsigStype(AAsigStype node) {
        setNodeAttribute(node, new WidthAndRate(WidthAndRate.WIDTH_UNKNOWN, WidthAndRate.RATE_A));
    }

    @Override
    public void outAOparrayStype(AOparrayStype node) {
        // TODO:
    }

    @Override
    public void inATabledeclTabledecl(ATabledeclTabledecl node) {
    }

    @Override
    public void outATabledeclTabledecl(ATabledeclTabledecl node) {
    }

    @Override
    public void inAImportsTaglist(AImportsTaglist node) {
    }

    @Override
    public void outAImportsTaglist(AImportsTaglist node) {
    }

    @Override
    public void inAExportsTaglist(AExportsTaglist node) {
    }

    @Override
    public void outAExportsTaglist(AExportsTaglist node) {
    }

    @Override
    public void inAImportsexportsTaglist(AImportsexportsTaglist node) {
    }

    @Override
    public void outAImportsexportsTaglist(AImportsexportsTaglist node) {
    }

    @Override
    public void inAExportsimportsTaglist(AExportsimportsTaglist node) {
    }

    @Override
    public void outAExportsimportsTaglist(AExportsimportsTaglist node) {
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
        // TODO:
    }

    @Override
    public void outAAltExpr(AAltExpr node) {
        // TODO:
    }

    @Override
    public void outAOrOrexpr(AOrOrexpr node) {
        // TODO:
    }

    @Override
    public void outAAndAndexpr(AAndAndexpr node) {
        // TODO:
    }

    @Override
    public void outANeqEqualityexpr(ANeqEqualityexpr node) {
        BranchInstruction branch = new IFNE(null);
        m_aMethods[METHOD_A].appendRelationalOperation(branch);
    }

    @Override
    public void outAEqEqualityexpr(AEqEqualityexpr node) {
        BranchInstruction branch = new IFEQ(null);
        m_aMethods[METHOD_A].appendRelationalOperation(branch);
    }

    @Override
    public void inAGtRelationalexpr(AGtRelationalexpr node) {
    }

    @Override
    public void outAGtRelationalexpr(AGtRelationalexpr node) {
        BranchInstruction branch = new IFGT(null);
        m_aMethods[METHOD_A].appendRelationalOperation(branch);
    }

    @Override
    public void outALtRelationalexpr(ALtRelationalexpr node) {
        BranchInstruction branch = new IFLT(null);
        m_aMethods[METHOD_A].appendRelationalOperation(branch);
    }

    @Override
    public void outALteqRelationalexpr(ALteqRelationalexpr node) {
        BranchInstruction branch = new IFLE(null);
        m_aMethods[METHOD_A].appendRelationalOperation(branch);
    }

    @Override
    public void outAGteqRelationalexpr(AGteqRelationalexpr node) {
        BranchInstruction branch = new IFGE(null);
        m_aMethods[METHOD_A].appendRelationalOperation(branch);
    }

    @Override
    public void outAPlusAddexpr(APlusAddexpr node) {
        m_aMethods[METHOD_A].appendInstruction(InstructionConst.FADD);
    }

    @Override
    public void outAMinusAddexpr(AMinusAddexpr node) {
        m_aMethods[METHOD_A].appendInstruction(InstructionConst.FSUB);
    }

    @Override
    public void outAMultFactor(AMultFactor node) {
        m_aMethods[METHOD_A].appendInstruction(InstructionConst.FMUL);
    }

    @Override
    public void outADivFactor(ADivFactor node) {
        m_aMethods[METHOD_A].appendInstruction(InstructionConst.FDIV);
    }

    @Override
    public void outANotUnaryminusterm(ANotUnaryminusterm node) {
        m_aMethods[METHOD_A].appendInstruction(InstructionConst.FNEG);
    }

    @Override
    public void outANotNotterm(ANotNotterm node) {
        m_aMethods[METHOD_A].appendInstruction(InstructionConst.FCONST_0);
        m_aMethods[METHOD_A].appendInstruction(InstructionConst.FCMPL);
        BranchInstruction branch0 = new IFNE(null);
        m_aMethods[METHOD_A].appendInstruction(branch0);
        m_aMethods[METHOD_A].appendInstruction(InstructionConst.FCONST_1);
        BranchInstruction branch1 = new GOTO(null);
        m_aMethods[METHOD_A].appendInstruction(branch1);
        m_aMethods[METHOD_A].setPendingBranchInstruction(branch0);
        m_aMethods[METHOD_A].appendInstruction(InstructionConst.FCONST_0);
        m_aMethods[METHOD_A].setPendingBranchInstruction(branch1);
    }

    @Override
    public void outAIdentifierTerm(AIdentifierTerm node) {
        String strVariableName = node.getIdentifier().getText();
        m_aMethods[METHOD_A].appendGetField(strVariableName);
    }

    @Override
    public void outAConstantTerm(AConstantTerm node) {
        Object constant = getNodeAttribute(node.getConst());
        if (constant instanceof Integer ||
                constant instanceof Float) {
            float fValue = ((Number) constant).floatValue();
            m_aMethods[METHOD_A].appendFloatConstant(fValue);
        } else {
            throw new RuntimeException("constant is neither int nor float");
        }
    }

    @Override
    public void inAIndexedTerm(AIndexedTerm node) {
        // push the array reference onto the stack
        String strVariableName = node.getIdentifier().getText();
        m_aMethods[METHOD_A].appendGetField(strVariableName);
    }

    /**
     * The array reference still is on the stack. Now,
     * also the array index (as a float) is on the stack.
     * It has to be transformed to integer.
     */
    @Override
    public void outAIndexedTerm(AIndexedTerm node) {
        // TODO: correct rounding (1.5 -> 2.0)
        m_aMethods[METHOD_A].appendInstruction(InstructionConst.F2I);
        // and now fetch the value from the array
        setNodeAttribute(node, InstructionConst.FALOAD);
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
        String strInteger = node.getInteger().getText();
        setNodeAttribute(node, Integer.parseInt(strInteger));
    }

    @Override
    public void outANumberConst(ANumberConst node) {
        String strNumber = node.getNumber().getText();
        setNodeAttribute(node, Float.parseFloat(strNumber));
    }

    // helper methods

    private void setNodeAttribute(Node node, Object attribute) {
        m_nodeAttributes.put(node, attribute);
    }

    private Object getNodeAttribute(Node node) {
        return m_nodeAttributes.get(node);
    }

    private void addLocalVariable(String strVariableName) {
        FieldGen fieldGen;
        fieldGen = new FieldGen(Const.ACC_PRIVATE,
                Type.FLOAT,
                strVariableName,
                m_constantPoolGen);
        m_classGen.addField(fieldGen.getField());

    }

    private void addLocalArray(String strVariableName) {
        FieldGen fieldGen;
        fieldGen = new FieldGen(Const.ACC_PRIVATE,
                FLOAT_ARRAY,
                strVariableName,
                m_constantPoolGen);
        m_classGen.addField(fieldGen.getField());

    }

    /**
     * Returns the InstructionFactory.
     * This method is mainly for use by inner classes.
     * A bit dangerous, since that has to be one
     * InstructionFactory per generated class.
     */
    private InstructionFactory getInstructionFactory() {
        return m_instructionFactory;
    }

    private class InstrumentMethod {

        private ClassGen m_classGen;
        private MethodGen m_methodGen;
        private InstructionList m_instructionList;
        private BranchInstruction m_pendingBranchInstruction;

        public InstrumentMethod(ClassGen classGen, String strMethodName) {
            m_classGen = classGen;
            m_instructionList = new InstructionList();
            m_methodGen = new MethodGen(
                    Const.ACC_PUBLIC,
                    Type.VOID,
                    new Type[] {new ObjectType("org.tritonus.saol.engine.RTSystem")},
                    new String[] {"rtSystem"},
                    strMethodName,
                    m_classGen.getClassName(),
                    m_instructionList,
                    m_classGen.getConstantPool());
        }

        /**
         * Append an instruction to the method's Instruction
         * list. If a BranchInstruction is pending, it is
         * targetted here.
         */
        public InstructionHandle appendInstruction(Instruction instruction) {
            // System.out.println("instruction: " + instruction);
            InstructionHandle target;
            if (instruction instanceof BranchInstruction) {
                target = m_instructionList.append((BranchInstruction) instruction);
            } else if (instruction instanceof CompoundInstruction) {
                target = m_instructionList.append((CompoundInstruction) instruction);
            } else {
                target = m_instructionList.append(instruction);
            }
            if (m_pendingBranchInstruction != null) {
                m_pendingBranchInstruction.setTarget(target);
                m_pendingBranchInstruction = null;
            }
            return target;
        }

        /**
         * Set the 'pending' BranchInstruction.
         * This is a mechanism to avoid NOPs. If a BranchInstruction
         * has to be targeted at an instruction that is immediately
         * following, but has not been generated yet, set this
         * BranchInstruction as the pending BranchInstruction.
         * The next instruction added to the method's InstructionList
         * with appendInstruction() will become the target of
         * the pending BranchInstruction.
         */
        public void setPendingBranchInstruction(BranchInstruction branchInstruction) {
            if (m_pendingBranchInstruction != null) {
                throw new RuntimeException("pending branch instruction already set");
            }
            m_pendingBranchInstruction = branchInstruction;
        }

        public void appendGetField(String strVariableName) {
            // System.out.println("class name: " + m_strClassName);
            // System.out.println("var name: " + strVariableName);
            appendInstruction(InstructionConst.ALOAD_0);
            Instruction instruction = getInstructionFactory().createGetField(m_strClassName, strVariableName, Type.FLOAT);
            appendInstruction(instruction);
        }

        /**
         * NOTE: this method does not append an ALOAD_0 instruction!
         */
        public void appendPutField(String strVariableName) {
            // System.out.println("class name: " + m_strClassName);
            // System.out.println("var name: " + strVariableName);
            Instruction instruction = getInstructionFactory().createPutField(m_strClassName, strVariableName, Type.FLOAT);
            appendInstruction(instruction);
        }

        public void appendIntegerConstant(int nValue) {
            Instruction instruction;
            switch (nValue) {
            case -1:
                instruction = InstructionConst.ICONST_M1;
                break;
            case 0:
                instruction = InstructionConst.ICONST_0;
                break;
            case 1:
                instruction = InstructionConst.ICONST_1;
                break;
            case 2:
                instruction = InstructionConst.ICONST_2;
                break;
            case 3:
                instruction = InstructionConst.ICONST_3;
                break;
            case 4:
                instruction = InstructionConst.ICONST_4;
                break;
            case 5:
                instruction = InstructionConst.ICONST_5;
                break;
            default:
                int nConstantIndex = m_constantPoolGen.addInteger(nValue);
                instruction = new LDC(nConstantIndex);
            }
            appendInstruction(instruction);
        }

        public void appendFloatConstant(float fValue) {
            Instruction instruction;
            if (fValue == 0.0) {
                instruction = InstructionConst.FCONST_0;
            } else if (fValue == 1.0) {
                instruction = InstructionConst.FCONST_1;
            } else if (fValue == 2.0) {
                instruction = InstructionConst.FCONST_2;
            } else {
                int nConstantIndex = m_constantPoolGen.addFloat(fValue);
                instruction = new LDC(nConstantIndex);
            }
            appendInstruction(instruction);
        }

        public void appendRelationalOperation(BranchInstruction branch0) {
            appendInstruction(InstructionConst.FCMPL);
            appendInstruction(branch0);
            appendInstruction(InstructionConst.FCONST_0);
            BranchInstruction branch1 = new GOTO(null);
            appendInstruction(branch1);
            setPendingBranchInstruction(branch0);
            appendInstruction(InstructionConst.FCONST_1);
            setPendingBranchInstruction(branch1);
        }

        public void finish() {
            appendInstruction(InstructionConst.RETURN);
            m_methodGen.setMaxStack();
            m_classGen.addMethod(m_methodGen.getMethod());
        }
    }
}


