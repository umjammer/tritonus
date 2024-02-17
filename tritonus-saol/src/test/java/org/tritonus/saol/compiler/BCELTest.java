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

import java.io.IOException;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.FieldGen;
import org.apache.bcel.generic.IFNE;
import org.apache.bcel.generic.InstructionConst;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.LDC;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Type;


/**
 * BCELTest.
 * <p>
 * Example program using the BCEL. BCEL is the Byte Code Engineering Library,
 * part of the jakarta project (http://jakarta.apache.org/bcel/).
 */
public class BCELTest {

    public static void main(String[] args) {
        String strClassName = "tone";
        ClassGen classGen = new ClassGen(strClassName,
                "AbstractInstrument",
                "<generated>",
                Const.ACC_PUBLIC | Const.ACC_SUPER,
                null);
        ConstantPoolGen constantPoolGen = classGen.getConstantPool();
        int nAInitValueIndex = constantPoolGen.addFloat(0.196307F);
        int nXInitValueIndex = constantPoolGen.addFloat(0.5F);
        FieldGen fieldGen;
        fieldGen = new FieldGen(Const.ACC_PRIVATE, Type.FLOAT, "a", constantPoolGen);
        classGen.addField(fieldGen.getField());
        fieldGen = new FieldGen(Const.ACC_PRIVATE, Type.FLOAT, "x", constantPoolGen);
        classGen.addField(fieldGen.getField());
        fieldGen = new FieldGen(Const.ACC_PRIVATE, Type.FLOAT, "y", constantPoolGen);
        classGen.addField(fieldGen.getField());
        fieldGen = new FieldGen(Const.ACC_PRIVATE, Type.FLOAT, "init", constantPoolGen);
        classGen.addField(fieldGen.getField());

        InstructionList il = new InstructionList();
        MethodGen methodGen;
        methodGen = new MethodGen(Const.ACC_PUBLIC,
                Type.VOID,
                new Type[] {
                        new ObjectType("RTSystem")
                },
                new String[] {
                        "rtSystem"
                },
                "doAPass",
                strClassName,
                il,
                constantPoolGen);

        InstructionFactory ifac = new InstructionFactory(constantPoolGen);
        il.append(InstructionConst.ALOAD_0);
        il.append(new LDC(nAInitValueIndex));
        il.append(ifac.createPutField(strClassName, "a", Type.FLOAT));
        il.append(InstructionConst.ALOAD_0);
        il.append(ifac.createGetField(strClassName, "init", Type.FLOAT));
        il.append(InstructionConst.FCONST_0);
        il.append(InstructionConst.FCMPL);
        IFNE ifne0 = new IFNE(null);
        il.append(ifne0);
        il.append(InstructionConst.ALOAD_0);
        il.append(InstructionConst.FCONST_1);
        il.append(ifac.createPutField(strClassName, "init", Type.FLOAT));
        il.append(InstructionConst.ALOAD_0);
        il.append(new LDC(nXInitValueIndex));
        il.append(ifac.createPutField(strClassName, "x", Type.FLOAT));
        InstructionHandle ih0 = il.append(InstructionConst.ALOAD_0);
        ifne0.setTarget(ih0);
        il.append(InstructionConst.ALOAD_0);
        il.append(ifac.createGetField(strClassName, "x", Type.FLOAT));
        il.append(InstructionConst.ALOAD_0);
        il.append(ifac.createGetField(strClassName, "a", Type.FLOAT));
        il.append(InstructionConst.ALOAD_0);
        il.append(ifac.createGetField(strClassName, "y", Type.FLOAT));
        il.append(InstructionConst.FMUL);
        il.append(InstructionConst.FSUB);
        il.append(ifac.createPutField(strClassName, "x", Type.FLOAT));
        il.append(InstructionConst.ALOAD_0);
        il.append(InstructionConst.ALOAD_0);
        il.append(ifac.createGetField(strClassName, "y", Type.FLOAT));
        il.append(InstructionConst.ALOAD_0);
        il.append(ifac.createGetField(strClassName, "a", Type.FLOAT));
        il.append(InstructionConst.ALOAD_0);
        il.append(ifac.createGetField(strClassName, "x", Type.FLOAT));
        il.append(InstructionConst.FMUL);
        il.append(InstructionConst.FADD);
        il.append(ifac.createPutField(strClassName, "y", Type.FLOAT));
        il.append(InstructionConst.ALOAD_1);
        il.append(InstructionConst.ALOAD_0);
        il.append(ifac.createGetField(strClassName, "y", Type.FLOAT));
        il.append(ifac.createInvoke("RTSystem",
                "output",
                Type.VOID,
                new Type[] {
                        Type.FLOAT
                },
                Const.INVOKEVIRTUAL));
        il.append(InstructionConst.RETURN);
        methodGen.setMaxStack();
        classGen.addMethod(methodGen.getMethod());
        classGen.addEmptyConstructor(Const.ACC_PUBLIC);
        JavaClass javaClass = classGen.getJavaClass();
        try {
            javaClass.dump("tmp/tone.class");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
