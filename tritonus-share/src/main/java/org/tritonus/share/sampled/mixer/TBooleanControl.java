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
import javax.sound.sampled.BooleanControl;

import static java.lang.System.getLogger;


/**
 * Base class for classes implementing BooleanControl.
 */
public class TBooleanControl extends BooleanControl implements TControllable {

    private static final Logger logger = getLogger(TBooleanControl.class.getName());

    private final TControlController controller;

    public TBooleanControl(BooleanControl.Type type, boolean initialValue) {
        this(type, initialValue, null);
    }

    public TBooleanControl(BooleanControl.Type type, boolean initialValue, TCompoundControl parentControl) {
        super(type, initialValue);
        logger.log(Level.TRACE, "begin");

        controller = new TControlController();

        logger.log(Level.TRACE, "end");
    }

    public TBooleanControl(BooleanControl.Type type,
                           boolean initialValue,
                           String trueStateLabel,
                           String falseStateLabel) {
        this(type, initialValue, trueStateLabel, falseStateLabel, null);
    }

    public TBooleanControl(BooleanControl.Type type,
                           boolean initialValue,
                           String trueStateLabel,
                           String falseStateLabel,
                           TCompoundControl parentControl) {
        super(type, initialValue, trueStateLabel, falseStateLabel);
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
