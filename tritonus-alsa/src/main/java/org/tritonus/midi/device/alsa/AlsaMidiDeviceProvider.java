/*
 *  Copyright (c) 1999 - 2001 by Matthias Pfisterer
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

package org.tritonus.midi.device.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.spi.MidiDeviceProvider;

import org.tritonus.lowlevel.alsa.AlsaSeq;
import org.tritonus.lowlevel.alsa.AlsaSeqClientInfo;
import org.tritonus.lowlevel.alsa.AlsaSeqPortInfo;

import static java.lang.System.getLogger;


public class AlsaMidiDeviceProvider extends MidiDeviceProvider {

    private static final Logger logger = getLogger("org.tritonus.TraceMidiDeviceProvider");

    // perhaps move to superclass
    private static final MidiDevice.Info[] EMPTY_INFO_ARRAY = new MidiDevice.Info[0];
    private static final int READ_CAPABILITY = AlsaSeq.SND_SEQ_PORT_CAP_READ | AlsaSeq.SND_SEQ_PORT_CAP_SUBS_READ;
    private static final int WRITE_CAPABILITY = AlsaSeq.SND_SEQ_PORT_CAP_WRITE | AlsaSeq.SND_SEQ_PORT_CAP_SUBS_WRITE;

    private static List<MidiDevice> m_devices;
    private static AlsaSeq m_alsaSeq;

    public AlsaMidiDeviceProvider() {
        logger.log(Level.TRACE, "AlsaMidiDeviceProvider.<init>(): begin");

        synchronized (AlsaMidiDeviceProvider.class) {
            if (m_devices == null) {
                m_devices = new ArrayList<>();
                logger.log(Level.TRACE, "AlsaMidiDeviceProvider.<init>(): creating AlsaSeq...");

//                try {
                m_alsaSeq = new AlsaSeq("Tritonus ALSA device manager");
//                } catch (Throwable t) { logger.log(Level.TRACE, t); }
                logger.log(Level.TRACE, "AlsaMidiDeviceProvider.<init>(): ...done");

                scanPorts();
            }
        }

        logger.log(Level.TRACE, "AlsaMidiDeviceProvider.<init>(): end");
    }

    @Override
    public MidiDevice.Info[] getDeviceInfo() {
        logger.log(Level.TRACE, "AlsaMidiDeviceProvider.getDeviceInfo(): begin");

        List<MidiDevice.Info> infoList = new ArrayList<>();
        for (MidiDevice device : m_devices) {
            MidiDevice.Info info = device.getDeviceInfo();
            infoList.add(info);
        }
        MidiDevice.Info[] infos = infoList.toArray(EMPTY_INFO_ARRAY);

        logger.log(Level.TRACE, "AlsaMidiDeviceProvider.getDeviceInfo(): end");

        return infos;
    }

    @Override
    public MidiDevice getDevice(MidiDevice.Info info) {
        logger.log(Level.TRACE, "AlsaMidiDeviceProvider.getDevice(): begin");

        MidiDevice returnedDevice = null;
        for (MidiDevice device : m_devices) {
            MidiDevice.Info info2 = device.getDeviceInfo();
            if (info != null && info.equals(info2)) {
                returnedDevice = device;
                break;
            }
        }
        if (returnedDevice == null) {
            throw new IllegalArgumentException("no device for " + info);
        }

        logger.log(Level.TRACE, "AlsaMidiDeviceProvider.getDevice(): end");

        return returnedDevice;
    }

    private void scanPorts() {
        logger.log(Level.TRACE, "AlsaMidiDeviceProvider.scanPorts(): begin");

        Iterator<?> clients = m_alsaSeq.getClientInfos();
        while (clients.hasNext()) {
            AlsaSeqClientInfo clientInfo = (AlsaSeqClientInfo) clients.next();
            int nClient = clientInfo.getClient();
            logger.log(Level.TRACE, "AlsaMidiDeviceProvider.scanPorts(): client: " + nClient);

            Iterator<?> ports = m_alsaSeq.getPortInfos(nClient);
            while (ports.hasNext()) {
                AlsaSeqPortInfo portInfo = (AlsaSeqPortInfo) ports.next();
                handlePort(clientInfo, portInfo);
            }
        }

        logger.log(Level.TRACE, "AlsaMidiDeviceProvider.scanPorts(): end");
    }

    private void handlePort(AlsaSeqClientInfo clientInfo, AlsaSeqPortInfo portInfo) {
        int nClient = clientInfo.getClient();
        int nPort = portInfo.getPort();
        int nType = portInfo.getType();
        int nCapability = portInfo.getCapability();
        int nSynthVoices = portInfo.getSynthVoices();
        logger.log(Level.TRACE, "AlsaMidiDeviceProvider.scanPorts(): port: " + nPort);
        logger.log(Level.TRACE, "AlsaMidiDeviceProvider.scanPorts(): type: " + nType);
        logger.log(Level.TRACE, "AlsaMidiDeviceProvider.scanPorts(): cap: " + nCapability);
        logger.log(Level.TRACE, "AlsaMidiDeviceProvider.scanPorts(): midi channels: " + portInfo.getMidiChannels());
        logger.log(Level.TRACE, "AlsaMidiDeviceProvider.scanPorts(): midi voices: " + portInfo.getMidiVoices());
        logger.log(Level.TRACE, "AlsaMidiDeviceProvider.scanPorts(): synth voices: " + portInfo.getSynthVoices());
        if ((nType & AlsaSeq.SND_SEQ_PORT_TYPE_MIDI_GENERIC) != 0) {
//            logger.log(Level.TRACE, "generic midi");
            MidiDevice device = null;
            if ((nType & (AlsaSeq.SND_SEQ_PORT_TYPE_SYNTH | AlsaSeq.SND_SEQ_PORT_TYPE_DIRECT_SAMPLE | AlsaSeq.SND_SEQ_PORT_TYPE_SAMPLE)) != 0) {
                boolean bWriteSubscriptionAllowed = (nCapability & WRITE_CAPABILITY) == WRITE_CAPABILITY;
                if (bWriteSubscriptionAllowed) {
                    device = new AlsaSynthesizer(nClient, nPort, nSynthVoices);
                } else {
                    logger.log(Level.TRACE, "AlsaMidiDeviceProvider.getDevice(): port does not allows write subscription, not used");
                }
            } else { // ordinary midi port
                boolean bReadSubscriptionAllowed = (nCapability & READ_CAPABILITY) == READ_CAPABILITY;
                boolean bWriteSubscriptionAllowed = (nCapability & WRITE_CAPABILITY) == WRITE_CAPABILITY;
                if (bReadSubscriptionAllowed || bWriteSubscriptionAllowed) {
                    device = new AlsaMidiDevice(nClient, nPort, bReadSubscriptionAllowed, bWriteSubscriptionAllowed);
                } else {
                    logger.log(Level.TRACE, "AlsaMidiDeviceProvider.getDevice(): port allows neither read nor write subscription, not used");
                }
            }
            if (device != null) {
                m_devices.add(device);
            }
        }
    }
}
