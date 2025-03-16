/*
 *  Copyright (c) 2003 by Matthias Pfisterer
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

package org.tritonus.share.sampled.convert;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;


/**
 * AudioInputStream base class.  This class implements "dynamic"
 * properties. "Dynamic" properties are properties that may change
 * during the life time of the objects. This is typically used to
 * pass information like the current frame number, volume of subbands
 * and similar values. "Dynamic" properties are different from
 * properties in AudioFormat and AudioFileFormat, which are
 * considered "static", as they aren't allowed to change after
 * creating of the object, thereby maintaining the immutable
 * character of these classes.
 */
public class TAudioInputStream extends AudioInputStream {

    private Map<String, Object> properties;
    private Map<String, Object> unmodifiableProperties;

    /**
     * Constructor without properties.
     * Creates an empty properties map.
     */
    public TAudioInputStream(InputStream inputStream, AudioFormat audioFormat, long lengthInFrames) {
        super(inputStream, audioFormat, lengthInFrames);
        initMaps(new HashMap<>());
    }

    /**
     * Constructor with properties.
     * The passed properties map is not copied. This allows subclasses
     * to change values in the map after creation, and the changes are
     * reflected in the map the application program can obtain.
     */
    public TAudioInputStream(InputStream inputStream,
                             AudioFormat audioFormat,
                             long lengthInFrames,
                             Map<String, Object> properties) {
        super(inputStream, audioFormat, lengthInFrames);
        initMaps(properties);
    }

    private void initMaps(Map<String, Object> properties) {
        // Here, we make a shallow copy of the map. It's unclear if this
        // is sufficient (of if a deep copy should be made).
        this.properties = properties;
        unmodifiableProperties = Collections.unmodifiableMap(this.properties);
    }

    /**
     * Obtain a Map containing the properties.  This method returns a
     * Map that cannot be modified by the application program, but
     * reflects changes to the map made by the implementation.
     *
     * @return a map containing the properties.
     */
    public Map<String, Object> properties() {
        return unmodifiableProperties;
    }

    /**
     * Set a property.  Unlike in AudioFormat and AudioFileFormat,
     * this method may be used anywhere by subclasses - it is not
     * restricted to be used in the constructor.
     */
    protected void setProperty(String key, Object value) {
        properties.put(key, value);
    }
}
