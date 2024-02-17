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

import org.tritonus.saol.sablecc.analysis.DepthFirstAdapter;
import org.tritonus.saol.sablecc.node.ASrateRtparam;
import org.tritonus.saol.sablecc.node.TInteger;


/**
 * GlobalsSearcher.java
 * <p>
 * This file is part of Tritonus: http://www.tritonus.org/
 */
public class GlobalsSearcher extends DepthFirstAdapter {

    private SAOLGlobals m_saolGlobals;

    public GlobalsSearcher(SAOLGlobals saolGlobals) {
        m_saolGlobals = saolGlobals;
    }

    public SAOLGlobals getSAOLGlobals() {
        return m_saolGlobals;
    }

    @Override
    public void outASrateRtparam(ASrateRtparam node) {
        TInteger integer = node.getInteger();
        String strInt = integer.getText();
        int nARate = Integer.parseInt(strInt);
        getSAOLGlobals().setARate(nARate);
    }

    // When we see a number, we print it.
//  public void caseTNumber(TNumber node) {
//   System.out.print(node);
//  }

    // out of alternative {plus} in Expr, we print the plus.
//  public void outAPlusExpr(APlusExpr node) {
//   System.out.print(node.getPlus());
//  }

    // out of alternative {minus} in Expr, we print the minus.
//  public void outAMinusExpr(AMinusExpr node) {
//   System.out.print(node.getMinus());
//  }

    // out of alternative {mult} in Factor, we print the mult.
//  public void outAMultFactor(AMultFactor node) {
//   System.out.print(node.getMult());
//  }

    // out of alternative {div} in Factor, we print the div.
//  public void outADivFactor(ADivFactor node) {
//   System.out.print(node.getDiv());
//  }

    // out of alternative {mod} in Factor, we print the mod.
//  public void outAModFactor(AModFactor node) {
//   System.out.print(node.getMod());
//  }
}


