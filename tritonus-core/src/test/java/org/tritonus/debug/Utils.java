/*
 *  Copyright (c) 1999 - 2002 by Matthias Pfisterer
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

package org.tritonus.debug;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.aspectj.lang.JoinPoint;

import static java.lang.System.getLogger;


/**
 * Utility methods for the debugging aspects.
 */
public class Utils {

    private static final Logger logger = getLogger(Utils.class.getName());

    /**
     * Indentation step.
     * This value determines how many spaces are added/removed
     * for each step of indentation.
     */
    private static final int INDENTATION_STEP = 2;

    /**
     * Indentation string.
     * This string is used to generate the appropriate number of spaces.
     */
    private static final String INDENTATION_STRING = "                                                                                ";

    /**
     * Current indentation.
     * This holds the current number of blanks to add before each line.
     * The value starts with -INDENTATION_STEP because the first call to
     * outSteppingIn will increase this value prior to printing.
     */
    private static int indentation = -INDENTATION_STEP;

    public static void outEnteringJoinPoint(JoinPoint joinPoint) {
        outSteppingIn("-> " + getSignature(joinPoint));
    }

    public static void outLeavingJoinPoint(JoinPoint joinPoint) {
        outSteppingOut("<- " + getSignature(joinPoint));
    }

    private static String getSignature(JoinPoint joinPoint) {
        return joinPoint.getStaticPart().getSignature().toShortString();
    }

    /**
     * Print message, increasing the indentation.
     */
    public static void outSteppingIn(String message) {
        indentation += INDENTATION_STEP;
        out(message);
    }

    /**
     * Print message, decreasing the indentation.
     */
    public static void outSteppingOut(String message) {
        out(message);
        indentation -= INDENTATION_STEP;
    }

    /**
     * Print message with the current indentation.
     */
    public static void out(String message) {
        logger.log(Level.TRACE, INDENTATION_STRING.substring(0, indentation) + message);
    }
}
