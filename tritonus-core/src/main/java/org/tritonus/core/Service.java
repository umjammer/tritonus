/*
 *  Copyright (c) 2000 by Matthias Pfisterer
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

package org.tritonus.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.tritonus.share.ArraySet;

import static java.lang.System.getLogger;


public class Service {

    private static final Logger logger = getLogger("org.tritonus.TraceService");

    private static final String BASE_NAME = "META-INF/services/";

    /**
     * Determines if the order of service providers is reversed.
     * If this is true, the Iterator returned by providers(Class)
     * iterates through the service provider classes backwards.
     * This means that service providers that are in the user class
     * path are first, then service providers in the extension class
     * path, then those in the boot class path.
     * This behaviour has the advantage that 'built-in' providers
     * (those in the boot class path) can be 'shadowed' by
     * providers in the extension and user class path.
     */
    private static final boolean REVERSE_ORDER = true;

    public static Iterator<?> providers(Class<?> cls) {
        logger.log(Level.TRACE, "begin");

        String fullName = BASE_NAME + cls.getName();
        logger.log(Level.TRACE, "full name: " + fullName);

        List<Object> instancesList = createInstancesList(fullName);
        Iterator<Object> iterator = instancesList.iterator();

        logger.log(Level.TRACE, "end");

        return iterator;
    }

    private static List<Object> createInstancesList(String fullName) {
        logger.log(Level.TRACE, "begin");

        List<Object> providers = new ArrayList<>();
        Iterator<String> classNames = createClassNames(fullName);
        if (classNames != null) {
            while (classNames.hasNext()) {
                String className = classNames.next();
                logger.log(Level.TRACE, "Class name: " + className);

                try {
                    ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
                    Class<?> cls = Class.forName(className, true, systemClassLoader);
                    logger.log(Level.TRACE, "now creating instance of " + cls);

                    Object instance = cls.getDeclaredConstructor().newInstance();
                    if (REVERSE_ORDER) {
                        providers.add(0, instance);
                    } else {
                        providers.add(instance);
                    }
                } catch (Throwable e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
            }
        }

        logger.log(Level.TRACE, "end");

        return providers;
    }

    private static Iterator<String> createClassNames(String fullName) {
        logger.log(Level.TRACE, "begin");

        Set<String> providers = new ArraySet<>();
        Enumeration<?> configs = null;
        try {
            configs = ClassLoader.getSystemResources(fullName);
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        if (configs != null) {
            while (configs.hasMoreElements()) {
                URL configFileUrl = (URL) configs.nextElement();
                logger.log(Level.TRACE, "config: " + configFileUrl);

                InputStream input = null;
                try {
                    input = configFileUrl.openStream();
                } catch (IOException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
                if (input != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    try {
                        String line = reader.readLine();
                        while (line != null) {
                            line = line.trim();
                            int pos = line.indexOf('#');
                            if (pos >= 0) {
                                line = line.substring(0, pos);
                            }
                            if (!line.isEmpty()) {
                                providers.add(line);
                                logger.log(Level.TRACE, "adding class name: " + line);

                            }
                            line = reader.readLine();
                        }
                    } catch (IOException e) {
                        logger.log(Level.ERROR, e.getMessage(), e);
                    }
                }
            }
        }
        Iterator<String> iterator = providers.iterator();

        logger.log(Level.TRACE, "end");

        return iterator;
    }
}
