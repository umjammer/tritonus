/*
 *  Copyright (c) 1999 - 2001 by Matthias Pfisterer
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

package org.tritonus.share;

public class TSettings {

    public static boolean SHOW_ACCESS_CONTROL_EXCEPTIONS = false;
    private static final String PROPERTY_PREFIX = "tritonus.";

    public static boolean AlsaUsePlughw = getBooleanProperty("AlsaUsePlughw");

    private static boolean getBooleanProperty(String strName) {
        String strPropertyName = PROPERTY_PREFIX + strName;
        String strValue = System.getProperty(strPropertyName, "false");
        // TDebug.out("property: " + strPropertyName + "=" + strValue);
        boolean bValue = strValue.equalsIgnoreCase("true");
        // TDebug.out("bValue: " + bValue);
        return bValue;
    }
}



