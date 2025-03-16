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


public class TemplateSemanticsCheck extends IOTCommonSemanticsCheck {

    // TODO verify
    private static final int[] LEGAL_VARIABLE_TYPES = new int[] {
            WidthAndRate.RATE_I,
            WidthAndRate.RATE_K,
            WidthAndRate.RATE_A,
            WidthAndRate.RATE_OPARRAY,
    };

    private VariableTable globalVariableTable;
    private VariableTable localVariableTable;

    public TemplateSemanticsCheck(VariableTable globalVariableTable,
                                  VariableTable localVariableTable,
                                  NodeSemanticsTable nodeSemanticsTable) {
        super(nodeSemanticsTable);
        this.globalVariableTable = globalVariableTable;
        this.localVariableTable = localVariableTable;
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
