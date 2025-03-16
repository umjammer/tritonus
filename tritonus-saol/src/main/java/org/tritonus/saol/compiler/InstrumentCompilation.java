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
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
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

import static java.lang.System.getLogger;


/**
 * InstrumentCompilation.java
 * <p>
 * This file is part of Tritonus: http://www.tritonus.org/
 */
public class InstrumentCompilation extends DepthFirstAdapter {

    private static final Logger logger = getLogger(InstrumentCompilation.class.getName());

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

    private final SAOLGlobals saolGlobals;

    // maps instrument names (String) to classes (Class)
    private final Map<String, Class<AbstractInstrument>> instrumentMap;
    private final Map<Node, Object> nodeAttributes;
    private String className;
    private ClassGen classGen;
    private ConstantPoolGen constantPoolGen;
//    private MethodGen methodGen;
//    private InstructionList instructionList;
    private InstructionFactory instructionFactory;
//    private BranchInstruction pendingBranchInstruction;

    // TODO should be made obsolete by using node attributes
    private boolean opVarDecls;
    private MemoryClassLoader classLoader = new MemoryClassLoader();

    // 0: constructor
    // 1: doIPass()
    // 2: doKPass()
    // 3: doAPass()
    private InstrumentMethod[] methods;

    public InstrumentCompilation(SAOLGlobals saolGlobals, Map<String, Class<AbstractInstrument>> instrumentMap) {
        this.saolGlobals = saolGlobals;
        this.instrumentMap = instrumentMap;
        nodeAttributes = new HashMap<>();
        methods = new InstrumentMethod[4];
    }

    @Override
    public void inAInstrdeclInstrdecl(AInstrdeclInstrdecl node) {
        String instrumentName = node.getIdentifier().getText();
        className = PACKAGE_PREFIX + instrumentName;
        classGen = new ClassGen(className,
                SUPERCLASS_NAME,
                "<generated>",
                Const.ACC_PUBLIC | Const.ACC_SUPER,
                null);
        constantPoolGen = classGen.getConstantPool();
        instructionFactory = new InstructionFactory(constantPoolGen);
        methods[METHOD_CONSTR] = new InstrumentMethod(classGen, "<init>");
        methods[METHOD_I] = new InstrumentMethod(classGen, "doIPass");
        methods[METHOD_K] = new InstrumentMethod(classGen, "doKPass");
        methods[METHOD_A] = new InstrumentMethod(classGen, "doAPass");
        methods[METHOD_CONSTR].appendInstruction(InstructionConst.ALOAD_0);
        Instruction invokeSuperInstruction = instructionFactory.createInvoke(SUPERCLASS_NAME, "<init>", Type.VOID, Type.NO_ARGS, Const.INVOKESPECIAL);
//        Instruction invokeSuperInstruction = instructionFactory.createInvoke(SUPERCLASS_NAME, SUPERCLASS_CONSTRUCTOR_NAME, Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL);
        methods[METHOD_CONSTR].appendInstruction(invokeSuperInstruction);
    }

    @Override
    public void outAInstrdeclInstrdecl(AInstrdeclInstrdecl node) {
        try {
            for (InstrumentMethod method : methods) {
                method.finish();
            }
            JavaClass javaClass = classGen.getJavaClass();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            javaClass.dump(baos);
            byte[] data = baos.toByteArray();
            @SuppressWarnings("unchecked")
            Class<AbstractInstrument> instrumentClass = (Class<AbstractInstrument>) classLoader.findClass(className, data);
            instrumentMap.put(className, instrumentClass);
            if (logger.isLoggable(Level.DEBUG)) {
                javaClass.dump(className + CLASSFILENAME_SUFFIX);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
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
        methods[METHOD_A].appendInstruction(instruction);
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
        methods[METHOD_A].appendInstruction(InstructionConst.FCONST_0);
        methods[METHOD_A].appendInstruction(InstructionConst.FCMPL);
        BranchInstruction ifeq = new IFEQ(null);
        methods[METHOD_A].appendInstruction(ifeq);
        if (node.getLBrace() != null) {
            node.getLBrace().apply(this);
        }
        if (node.getBlock() != null) {
            node.getBlock().apply(this);
        }
        if (node.getRBrace() != null) {
            node.getRBrace().apply(this);
        }
        methods[METHOD_A].setPendingBranchInstruction(ifeq);
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
        methods[METHOD_A].appendInstruction(InstructionConst.ALOAD_0);
        String variableName = node.getIdentifier().getText();
        // TODO use getClassName()
        // set the instruction to be executed after the rvalue is calculated
        Instruction instruction = getInstructionFactory().createPutField(className, variableName, Type.FLOAT);
        setNodeAttribute(node, instruction);
    }

    @Override
    public void inAIndexedLvalue(AIndexedLvalue node) {
        // push the array reference onto the stack
        String variableName = node.getIdentifier().getText();
        methods[METHOD_A].appendGetField(variableName);
    }

    /**
     * The array reference still is on the stack. Now,
     * also the array index (as a float) is on the stack.
     * It has to be transformed to integer.
     */
    @Override
    public void outAIndexedLvalue(AIndexedLvalue node) {
        // TODO correct rounding (1.5 -> 2.0)
        methods[METHOD_A].appendInstruction(InstructionConst.F2I);
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
        opVarDecls = true;
    }

    @Override
    public void outASigvarOpvardecl(ASigvarOpvardecl node) {
        opVarDecls = false;
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
        if (opVarDecls) {
            String variableName = node.getIdentifier().getText();
            addLocalVariable(variableName);
        }
    }

    @Override
    public void outAIndexedName(AIndexedName node) {
        if (opVarDecls) {
            String variableName = node.getIdentifier().getText();
            String text = node.getInteger().getText();
            int integer = Integer.parseInt(text);
            addLocalArray(variableName);
            // code to allocate array in constructor
            methods[METHOD_CONSTR].appendInstruction(InstructionConst.ALOAD_0);
            Instruction instruction = getInstructionFactory().createNewArray(Type.FLOAT, (short) integer);
            methods[METHOD_CONSTR].appendInstruction(instruction);
            methods[METHOD_CONSTR].appendPutField(variableName);
        }
    }

    @Override
    public void outAInchannelsName(AInchannelsName node) {
        // TODO
    }

    @Override
    public void outAOutchannelsName(AOutchannelsName node) {
        // TODO
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
        // TODO
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
        BranchInstruction branch = new IFNE(null);
        methods[METHOD_A].appendRelationalOperation(branch);
    }

    @Override
    public void outAEqEqualityexpr(AEqEqualityexpr node) {
        BranchInstruction branch = new IFEQ(null);
        methods[METHOD_A].appendRelationalOperation(branch);
    }

    @Override
    public void inAGtRelationalexpr(AGtRelationalexpr node) {
    }

    @Override
    public void outAGtRelationalexpr(AGtRelationalexpr node) {
        BranchInstruction branch = new IFGT(null);
        methods[METHOD_A].appendRelationalOperation(branch);
    }

    @Override
    public void outALtRelationalexpr(ALtRelationalexpr node) {
        BranchInstruction branch = new IFLT(null);
        methods[METHOD_A].appendRelationalOperation(branch);
    }

    @Override
    public void outALteqRelationalexpr(ALteqRelationalexpr node) {
        BranchInstruction branch = new IFLE(null);
        methods[METHOD_A].appendRelationalOperation(branch);
    }

    @Override
    public void outAGteqRelationalexpr(AGteqRelationalexpr node) {
        BranchInstruction branch = new IFGE(null);
        methods[METHOD_A].appendRelationalOperation(branch);
    }

    @Override
    public void outAPlusAddexpr(APlusAddexpr node) {
        methods[METHOD_A].appendInstruction(InstructionConst.FADD);
    }

    @Override
    public void outAMinusAddexpr(AMinusAddexpr node) {
        methods[METHOD_A].appendInstruction(InstructionConst.FSUB);
    }

    @Override
    public void outAMultFactor(AMultFactor node) {
        methods[METHOD_A].appendInstruction(InstructionConst.FMUL);
    }

    @Override
    public void outADivFactor(ADivFactor node) {
        methods[METHOD_A].appendInstruction(InstructionConst.FDIV);
    }

    @Override
    public void outANotUnaryminusterm(ANotUnaryminusterm node) {
        methods[METHOD_A].appendInstruction(InstructionConst.FNEG);
    }

    @Override
    public void outANotNotterm(ANotNotterm node) {
        methods[METHOD_A].appendInstruction(InstructionConst.FCONST_0);
        methods[METHOD_A].appendInstruction(InstructionConst.FCMPL);
        BranchInstruction branch0 = new IFNE(null);
        methods[METHOD_A].appendInstruction(branch0);
        methods[METHOD_A].appendInstruction(InstructionConst.FCONST_1);
        BranchInstruction branch1 = new GOTO(null);
        methods[METHOD_A].appendInstruction(branch1);
        methods[METHOD_A].setPendingBranchInstruction(branch0);
        methods[METHOD_A].appendInstruction(InstructionConst.FCONST_0);
        methods[METHOD_A].setPendingBranchInstruction(branch1);
    }

    @Override
    public void outAIdentifierTerm(AIdentifierTerm node) {
        String variableName = node.getIdentifier().getText();
        methods[METHOD_A].appendGetField(variableName);
    }

    @Override
    public void outAConstantTerm(AConstantTerm node) {
        Object constant = getNodeAttribute(node.getConst());
        if (constant instanceof Integer || constant instanceof Float) {
            float value = ((Number) constant).floatValue();
            methods[METHOD_A].appendFloatConstant(value);
        } else {
            throw new RuntimeException("constant is neither int nor float");
        }
    }

    @Override
    public void inAIndexedTerm(AIndexedTerm node) {
        // push the array reference onto the stack
        String variableName = node.getIdentifier().getText();
        methods[METHOD_A].appendGetField(variableName);
    }

    /**
     * The array reference still is on the stack. Now,
     * also the array index (as a float) is on the stack.
     * It has to be transformed to integer.
     */
    @Override
    public void outAIndexedTerm(AIndexedTerm node) {
        // TODO correct rounding (1.5 -> 2.0)
        methods[METHOD_A].appendInstruction(InstructionConst.F2I);
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
        String text = node.getInteger().getText();
        setNodeAttribute(node, Integer.parseInt(text));
    }

    @Override
    public void outANumberConst(ANumberConst node) {
        String text = node.getNumber().getText();
        setNodeAttribute(node, Float.parseFloat(text));
    }

    // helper methods

    private void setNodeAttribute(Node node, Object attribute) {
        nodeAttributes.put(node, attribute);
    }

    private Object getNodeAttribute(Node node) {
        return nodeAttributes.get(node);
    }

    private void addLocalVariable(String variableName) {
        FieldGen fieldGen;
        fieldGen = new FieldGen(Const.ACC_PRIVATE,
                Type.FLOAT,
                variableName,
                constantPoolGen);
        classGen.addField(fieldGen.getField());

    }

    private void addLocalArray(String variableName) {
        FieldGen fieldGen;
        fieldGen = new FieldGen(Const.ACC_PRIVATE,
                FLOAT_ARRAY,
                variableName,
                constantPoolGen);
        classGen.addField(fieldGen.getField());

    }

    /**
     * Returns the InstructionFactory.
     * This method is mainly for use by inner classes.
     * A bit dangerous, since that has to be one
     * InstructionFactory per generated class.
     */
    private InstructionFactory getInstructionFactory() {
        return instructionFactory;
    }

    private class InstrumentMethod {

        private final ClassGen classGen;
        private final MethodGen methodGen;
        private final InstructionList instructionList;
        private BranchInstruction pendingBranchInstruction;

        public InstrumentMethod(ClassGen classGen, String methodName) {
            this.classGen = classGen;
            instructionList = new InstructionList();
            methodGen = new MethodGen(
                    Const.ACC_PUBLIC,
                    Type.VOID,
                    new Type[] {new ObjectType("org.tritonus.saol.engine.RTSystem")},
                    new String[] {"rtSystem"},
                    methodName,
                    this.classGen.getClassName(),
                    instructionList,
                    this.classGen.getConstantPool());
        }

        /**
         * Append an instruction to the method's Instruction
         * list. If a BranchInstruction is pending, it is
         * targeted here.
         */
        public InstructionHandle appendInstruction(Instruction instruction) {
            // System.out.println("instruction: " + instruction);
            InstructionHandle target;
            if (instruction instanceof BranchInstruction) {
                target = instructionList.append((BranchInstruction) instruction);
            } else if (instruction instanceof CompoundInstruction) {
                target = instructionList.append((CompoundInstruction) instruction);
            } else {
                target = instructionList.append(instruction);
            }
            if (pendingBranchInstruction != null) {
                pendingBranchInstruction.setTarget(target);
                pendingBranchInstruction = null;
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
            if (pendingBranchInstruction != null) {
                throw new RuntimeException("pending branch instruction already set");
            }
            pendingBranchInstruction = branchInstruction;
        }

        public void appendGetField(String variableName) {
            // System.out.println("class name: " + className);
            // System.out.println("var name: " + variableName);
            appendInstruction(InstructionConst.ALOAD_0);
            Instruction instruction = getInstructionFactory().createGetField(className, variableName, Type.FLOAT);
            appendInstruction(instruction);
        }

        /**
         * NOTE: this method does not append an ALOAD_0 instruction!
         */
        public void appendPutField(String variableName) {
            // System.out.println("class name: " + className);
            // System.out.println("var name: " + variableName);
            Instruction instruction = getInstructionFactory().createPutField(className, variableName, Type.FLOAT);
            appendInstruction(instruction);
        }

        public void appendIntegerConstant(int value) {
            Instruction instruction = switch (value) {
                case -1 -> InstructionConst.ICONST_M1;
                case 0 -> InstructionConst.ICONST_0;
                case 1 -> InstructionConst.ICONST_1;
                case 2 -> InstructionConst.ICONST_2;
                case 3 -> InstructionConst.ICONST_3;
                case 4 -> InstructionConst.ICONST_4;
                case 5 -> InstructionConst.ICONST_5;
                default -> {
                    int constantIndex = constantPoolGen.addInteger(value);
                    yield new LDC(constantIndex);
                }
            };
            appendInstruction(instruction);
        }

        public void appendFloatConstant(float value) {
            Instruction instruction;
            if (value == 0.0) {
                instruction = InstructionConst.FCONST_0;
            } else if (value == 1.0) {
                instruction = InstructionConst.FCONST_1;
            } else if (value == 2.0) {
                instruction = InstructionConst.FCONST_2;
            } else {
                int constantIndex = constantPoolGen.addFloat(value);
                instruction = new LDC(constantIndex);
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
            methodGen.setMaxStack();
            classGen.addMethod(methodGen.getMethod());
        }
    }
}
