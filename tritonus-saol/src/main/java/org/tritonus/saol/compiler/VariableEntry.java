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


public class VariableEntry extends WidthAndRate {

    private final String variableName;
    private final boolean imports;
    private final boolean exports;

    public VariableEntry(String variableName,
                         int width,
                         int rate,
                         boolean imports,
                         boolean exports) {
        super(width, rate);
        this.variableName = variableName;
        this.imports = imports;
        this.exports = exports;
    }

    public String getVariableName() {
        return variableName;
    }

    public boolean getImports() {
        return imports;
    }

    public boolean getExports() {
        return exports;
    }
}
