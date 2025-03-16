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
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;

import static java.lang.System.getLogger;


/**
 * Base class for classes implementing Line.
 */
public class TCompoundControl extends CompoundControl implements TControllable {

    private static final Logger logger= getLogger("org.tritonus.TraceControl");

    private final TControlController controller;

    public TCompoundControl(CompoundControl.Type type, Control[] memberControls) {
        super(type, memberControls);
        logger.log(Level.TRACE, "begin");

        controller = new TControlController();

        logger.log(Level.TRACE, "end");
    }

    @Override
    public void setParentControl(TCompoundControl compoundControl) {
        controller.setParentControl(compoundControl);
    }

    @Override
    public TCompoundControl getParentControl() {
        return controller.getParentControl();
    }

    @Override
    public void commit() {
        controller.commit();
    }
}
