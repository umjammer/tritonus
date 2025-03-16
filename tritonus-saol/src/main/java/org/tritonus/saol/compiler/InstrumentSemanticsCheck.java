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

import org.tritonus.saol.sablecc.node.AInstrdeclInstrdecl;
import org.tritonus.saol.sablecc.node.AIntListIntList;
import org.tritonus.saol.sablecc.node.AMiditagMiditag;


public class InstrumentSemanticsCheck extends IOTCommonSemanticsCheck {

    private static final boolean DEBUG = true;

    private static final int[] LEGAL_VARIABLE_TYPES = new int[] {
            WidthAndRate.RATE_I,
            WidthAndRate.RATE_K,
            WidthAndRate.RATE_A,
            WidthAndRate.RATE_OPARRAY,
    };

    private final VariableTable globalVariableTable;
    private final VariableTable localVariableTable;

    public InstrumentSemanticsCheck(VariableTable globalVariableTable,
                                    VariableTable localVariableTable,
                                    NodeSemanticsTable nodeSemanticsTable) {
        super(nodeSemanticsTable);
        this.globalVariableTable = globalVariableTable;
        this.localVariableTable = localVariableTable;
    }

    @Override
    public void inAInstrdeclInstrdecl(AInstrdeclInstrdecl node) {
//        String instrumentName = node.getIdentifier().getText();
//        className = PACKAGE_PREFIX + instrumentName;
//        classGen = new ClassGen(className,
//                SUPERCLASS_NAME,
//                "<generated>",
//                Constants.ACC_PUBLIC | Constants.ACC_SUPER,
//                null);
//        constantPoolGen = classGen.getConstantPool();
//        instructionFactory = new InstructionFactory(constantPoolGen);
//        methods[METHOD_CONSTR] = new InstrumentMethod(classGen, "<init>");
//        methods[METHOD_I] = new InstrumentMethod(classGen, "doIPass");
//        methods[METHOD_K] = new InstrumentMethod(classGen, "doKPass");
//        methods[METHOD_A] = new InstrumentMethod(classGen, "doAPass");
//        methods[METHOD_CONSTR].appendInstruction(InstructionConstants.ALOAD_0);
//        Instruction invokeSuperInstruction = m_instructionFactory.createInvoke(SUPERCLASS_NAME, "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL);
////        Instruction invokeSuperInstruction = m_instructionFactory.createInvoke(SUPERCLASS_NAME, SUPERCLASS_CONSTRUCTOR_NAME, Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL);
//        methods[METHOD_CONSTR].appendInstruction(invokeSuperInstruction);
    }

    @Override
    public void outAInstrdeclInstrdecl(AInstrdeclInstrdecl node) {
//        for (int i = 0; i < methods.length; i++) {
//            methods[i].finish();
//        }
//        JavaClass javaClass = classGen.getJavaClass();
//        try {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            javaClass.dump(baos);
//            byte[] data = baos.toByteArray();
//            Class instrumentClass = m_classLoader.findClass(className, abData);
//            instrumentMap.put(className, instrumentClass);
//            if (DEBUG) {
//                javaClass.dump(className + CLASSFILENAME_SUFFIX);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void inAMiditagMiditag(AMiditagMiditag node) {
    }

    @Override
    public void outAMiditagMiditag(AMiditagMiditag node) {
    }

    @Override
    public void inAIntListIntList(AIntListIntList node) {
    }

    @Override
    public void outAIntListIntList(AIntListIntList node) {
    }

    @Override
    protected VariableTable getOwnVariableTable() {
        return localVariableTable;
    }

    @Override
    protected VariableTable getGlobalVariableTable() {
        return globalVariableTable;
    }

    @Override
    protected int[] getLegalVariableTypes() {
        return LEGAL_VARIABLE_TYPES;
    }
}
