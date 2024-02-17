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
import javax.sound.sampled.EnumControl;

import static java.lang.System.getLogger;


/**
 * Base class for classes implementing Line.
 */
public class TEnumControl extends EnumControl implements TControllable {

    private static final Logger logger= getLogger("org.tritonus.TraceControl");

    private final TControlController m_controller;

    public TEnumControl(EnumControl.Type type, Object[] aValues, Object value) {
        super(type, aValues, value);
        logger.log(Level.TRACE, "TEnumControl.<init>: begin");

        m_controller = new TControlController();

        logger.log(Level.TRACE, "TEnumControl.<init>: end");
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
