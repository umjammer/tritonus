/*
 *  Copyright (c) 2000 by Matthias Pfisterer
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


import java.io.InputStream;
import java.util.Properties;


public class GlobalInfo {

    static {
        try {
            try (InputStream is = GlobalInfo.class.getResourceAsStream("/META-INF/maven/org.tritonus/tritonus-share/pom.properties")) {
                if (is != null) {
                    Properties props = new Properties();
                    props.load(is);
                    VERSION = props.getProperty("version", "undefined in pom.properties");
                } else {
                    VERSION = System.getProperty("vavi.test.version", "undefined");
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static final String VENDOR = "Tritonus is free software. See http://www.tritonus.org/";
    private static final String VERSION;

    public static String getVendor() {
        return VENDOR;
    }

    public static String getVersion() {
        return VERSION;
    }
}



