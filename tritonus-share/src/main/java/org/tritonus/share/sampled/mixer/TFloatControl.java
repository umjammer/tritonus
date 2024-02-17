/*
 *  Copyright (c) 2001 by Matthias Pfisterer
 *
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
 *
 */

package org.tritonus.share.sampled.mixer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.FloatControl;

import static java.lang.System.getLogger;


/**
 * Base class for classes implementing Line.
 */
public class TFloatControl extends FloatControl implements TControllable {

    private static final Logger logger= getLogger("org.tritonus.TraceControl");

    private TControlController m_controller;

    public TFloatControl(FloatControl.Type type,
                         float fMinimum,
                         float fMaximum,
                         float fPrecision,
                         int nUpdatePeriod,
                         float fInitialValue,
                         String strUnits) {
        super(type,
                fMinimum,
                fMaximum,
                fPrecision,
                nUpdatePeriod,
                fInitialValue,
                strUnits);
        logger.log(Level.TRACE, "TFloatControl.<init>: begin");

        m_controller = new TControlController();

        logger.log(Level.TRACE, "TFloatControl.<init>: end");
    }

    public TFloatControl(FloatControl.Type type,
                         float fMinimum,
                         float fMaximum,
                         float fPrecision,
                         int nUpdatePeriod,
                         float fInitialValue,
                         String strUnits,
                         String strMinLabel,
                         String strMidLabel,
                         String strMaxLabel) {
        super(type,
                fMinimum,
                fMaximum,
                fPrecision,
                nUpdatePeriod,
                fInitialValue,
                strUnits,
                strMinLabel,
                strMidLabel,
                strMaxLabel);
        logger.log(Level.TRACE, "TFloatControl.<init>: begin");

        m_controller = new TControlController();

        logger.log(Level.TRACE, "TFloatControl.<init>: end");
    }

    @Override
    public void setParentControl(TCompoundControl compoundControl) {
        m_controller.setParentControl(compoundControl);
    }

    @Override
    public TCompoundControl getParentControl() {
        return m_controller.getParentControl();
    }

    @Override
    public void commit() {
        m_controller.commit();
    }
}


