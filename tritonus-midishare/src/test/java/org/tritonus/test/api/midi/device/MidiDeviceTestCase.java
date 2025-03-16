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

package org.tritonus.test.api.midi.device;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Class for tests of javax.sound.midi.MidiDevice.
 */
public class MidiDeviceTestCase extends BaseMidiDeviceTestCase {

    @Test
    public void testGetDeviceInfo() throws Exception {
        Check check = device -> {
            MidiDevice.Info info = device.getDeviceInfo();
            assertNotNull(info, "getDeviceInfo()");
            assertNotNull(info.getName(), "DeviceInfo.getName()");
            assertNotNull(info.getVendor(), "DeviceInfo.getVendor()");
            assertNotNull(info.getDescription(), "DeviceInfo.getDescription()");
            assertNotNull(info.getVersion(), "DeviceInfo.getVersion()");
        };
        checkMidiDevice(check);
    }

    @Test
    public void testOpenClose() throws Exception {
        Check check = device -> {
            assertFalse(device.isOpen(), "closed");
            device.open();
            assertTrue(device.isOpen(), "open");
            device.close();
            assertFalse(device.isOpen(), "closed");
        };
        checkMidiDevice(check);
    }

    @Test
    public void testGetMicrosecondPosition() throws Exception {
        Check check = device -> {
            long position = device.getMicrosecondPosition();
            assertTrue(position == -1 || position == 0, "getMicrosecondPosition() before open");
            device.open();
            position = device.getMicrosecondPosition();
            assertTrue(position == -1 || position >= 0, "getMicrosecondPosition() after open");
            device.close();
            position = device.getMicrosecondPosition();
            assertTrue(position == -1 || position == 0, "getMicrosecondPosition() after close");
        };
        checkMidiDevice(check);
    }

    @Test
    public void testGetMaxReceivers() throws Exception {
        Check check = device -> {
            int max = device.getMaxReceivers();
            assertTrue(max == -1 || max == 0, "getMaxReceivers()");
        };
        checkMidiDevice(check);
    }

    @Test
    public void testGetMaxTransmitters() throws Exception {
        Check check = device -> {
            int max = device.getMaxTransmitters();
            assertTrue(max == -1 || max == 0, "getMaxTransmitters()");
        };
        checkMidiDevice(check);
    }

    @Test
    public void testGetReceiver() throws Exception {
        Check check = device -> {
            int max = device.getMaxReceivers();
            if (max != 0) {
                max = (max == -1) ? 100 : max;
                Receiver[] receivers = new Receiver[max];
                for (int i = 0; i < max; i++) {
                    receivers[i] = device.getReceiver();
                    assertNotNull(receivers[i], "getReceiver()");
                    for (int j = 0; j < i - 1; j++) {
                        assertNotSame(receivers[i], receivers[j], "Receiver objects unique");
                    }
                }
                for (int i = 0; i < max; i++) {
                    receivers[i].close();
                }
            }
        };
        checkMidiDevice(check);
    }

    @Test
    public void testGetReceivers() throws Exception {
        Check check = device -> {
            assertEquals(0, device.getReceivers().size(), "getReceivers() length");
            int max = device.getMaxReceivers();
            if (max != 0) {
                max = (max == -1) ? 100 : max;
                Receiver[] receivers = new Receiver[max];
                for (int i = 0; i < max; i++) {
                    receivers[i] = device.getReceiver();
                    assertTrue(device.getReceivers().contains(receivers[i]), "Receiver in getReceivers()");
                }
                assertEquals(max, device.getReceivers().size(), "getReceivers() length");
                for (int i = 0; i < max; i++) {
                    receivers[i].close();
                    assertFalse(device.getReceivers().contains(receivers[i]), "Receiver not in getReceivers()");
                }
            }
            assertEquals(0, device.getReceivers().size(), "getReceivers() length");
        };
        checkMidiDevice(check);
    }

    @Test
    public void testGetTransmitter() throws Exception {
        Check check = device -> {
            int max = device.getMaxTransmitters();
            if (max != 0) {
                max = (max == -1) ? 100 : max;
                Transmitter[] transmitters = new Transmitter[max];
                for (int i = 0; i < max; i++) {
                    transmitters[i] = device.getTransmitter();
                    assertNotNull(transmitters[i], "getTransmitter()");
                    for (int j = 0; j < i - 1; j++) {
                        assertNotSame(transmitters[i], transmitters[j], "Transmitter objects unique");
                    }
                }
                for (int i = 0; i < max; i++) {
                    transmitters[i].close();
                }
            }
        };
        checkMidiDevice(check);
    }

    @Test
    public void testGetTransmitters() throws Exception {
        Check check = device -> {
            assertEquals(0, device.getTransmitters().size(), "getTransmitters() length");
            int max = device.getMaxTransmitters();
            if (max != 0) {
                max = (max == -1) ? 100 : max;
                Transmitter[] transmitters = new Transmitter[max];
                for (int i = 0; i < max; i++) {
                    transmitters[i] = device.getTransmitter();
                    assertTrue(device.getTransmitters().contains(transmitters[i]), "Transmitter in getTransmitters()");
                }
                assertEquals(max, device.getTransmitters().size(), "getTransmitters() length");
                for (int i = 0; i < max; i++) {
                    transmitters[i].close();
                    assertFalse(device.getTransmitters().contains(transmitters[i]), "Transmitter not in getTransmitters()");
                }
            }
            assertEquals(0, device.getTransmitters().size(), "getTransmitters() length");
        };
        checkMidiDevice(check);
    }
}
