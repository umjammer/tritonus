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


/**
 * NodeSemantics.
 * <p>
 * This file is part of Tritonus: http://www.tritonus.org/
 */
public class NodeSemantics extends WidthAndRate {

    /**
     * Auxiliary information.
     */
    private final Object aux;

    public NodeSemantics(int width, int rate) {
        this(width, rate, null);
    }

    public NodeSemantics(Object aux) {
        this(WidthAndRate.WIDTH_UNKNOWN, WidthAndRate.RATE_UNKNOWN, aux);
    }

    public NodeSemantics(int width, int rate, Object aux) {
        super(width, rate);
        this.aux = aux;
    }

    public Object getAux() {
        return aux;
    }
}
