/*
 *  Copyright (c) 2004 by Matthias Pfisterer
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

package org.tritonus.test.api.sampled.mixer;

import javax.sound.sampled.Mixer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Class for tests of javax.sound.sampled.Mixer.
 */
public class MixerTestCase
        extends BaseMixerTestCase {

    @Test
    public void testGetMixerInfo()
            throws Exception {
        Check check = mixer -> {
            Mixer.Info info = mixer.getMixerInfo();
            assertNotNull(info, "getMixerInfo()");
            assertNotNull(info.getName(), "MixerInfo.getName()");
            assertNotNull(info.getVendor(), "MixerInfo.getVendor()");
            assertNotNull(info.getDescription(), "MixerInfo.getDescription()");
            assertNotNull(info.getVersion(), "MixerInfo.getVersion()");
        };
        checkMixer(check);
    }

    @Test
    public void testOpenClose()
            throws Exception {
        Check check = mixer -> {
            assertFalse(mixer.isOpen(), "closed");
            mixer.open();
            assertTrue(mixer.isOpen(), "open");
            mixer.close();
            assertFalse(mixer.isOpen(), "closed");
        };
        checkMixer(check);
    }
}
