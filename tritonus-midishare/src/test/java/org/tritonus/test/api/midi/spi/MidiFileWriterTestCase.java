/*
 *  Copyright (c) 2001 - 2002 by Matthias Pfisterer
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

package org.tritonus.test.api.midi.spi;

//import javax.sound.midi.InvalidMidiDataException;
//import javax.sound.midi.MidiDevice;
//import javax.sound.midi.spi.MidiFileWriter;


/**
 * Tests for class javax.sound.midi.spi.MidiFileWriter.
 */
public class MidiFileWriterTestCase {

//    public void testIsDeviceSupported() throws Exception {
//        MidiDevice.Info info = new TestInfo("name", "vendor", "description", "version");
//        checkIsDeviceSupported(new MidiDevice.Info[0], info, false);
//        checkIsDeviceSupported(new MidiDevice.Info[] {info}, info, true);
//        checkIsDeviceSupported(new MidiDevice.Info[] {info}, null, false);
//    }

//    private void checkIsDeviceSupported(MidiDevice.Info[] supportedInfos,
//                                        MidiDevice.Info testInfo,
//                                        boolean expectedResult) throws Exception {
//        MidiFileWriter provider = new TestMidiFileWriter(supportedInfos);
//        assertFalse(expectedResult ^ provider.isDeviceSupported(testInfo), "empty supported array");
//    }

//    /**
//     * Concrete subclass of MidiFileWriter.
//     */
//    private class TestMidiFileWriter extends MidiFileWriter {
//
//        MidiDevice.Info[] supportedInfos;
//
//        public TestMidiFileWriter(MidiDevice.Info[] supportedInfos) {
//            this.supportedInfos = supportedInfos;
//        }
//
//        public MidiDevice.Info[] getDeviceInfo() {
//            return supportedInfos;
//        }
//
//        public MidiDevice getDevice(MidiDevice.Info info) {
//            return null;
//        }
//    }

//    /**
//     * Accessible subclass of MidiDevice.Info.
//     */
//    private class TestInfo extends MidiDevice.Info {
//
//        public TestInfo(String name, String vendor, String description, String version) {
//            super(name, vendor, description, version);
//        }
//    }
}
