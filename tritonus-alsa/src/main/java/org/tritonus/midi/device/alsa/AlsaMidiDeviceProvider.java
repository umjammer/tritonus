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

import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.getLogger;


public class AlsaMidiDeviceProvider extends MidiDeviceProvider {

    private static final Logger logger = getLogger("org.tritonus.TraceMidiDeviceProvider");

    // perhaps move to superclass
    private static final MidiDevice.Info[] EMPTY_INFO_ARRAY = new MidiDevice.Info[0];
    private static final int READ_CAPABILITY = AlsaSeq.SND_SEQ_PORT_CAP_READ | AlsaSeq.SND_SEQ_PORT_CAP_SUBS_READ;
    private static final int WRITE_CAPABILITY = AlsaSeq.SND_SEQ_PORT_CAP_WRITE | AlsaSeq.SND_SEQ_PORT_CAP_SUBS_WRITE;

    private static List<MidiDevice> devices;
    private static AlsaSeq alsaSeq;

    public AlsaMidiDeviceProvider() {
        logger.log(TRACE, "begin");

        synchronized (AlsaMidiDeviceProvider.class) {
            if (devices == null) {
                devices = new ArrayList<>();
                logger.log(TRACE, "creating AlsaSeq...");

//                try {
                alsaSeq = new AlsaSeq("Tritonus ALSA device manager");
//                } catch (Throwable t) { logger.log(Level.TRACE, t); }
                logger.log(TRACE, "...done");

                scanPorts();
            }
        }

        logger.log(TRACE, "end");
    }

    @Override
    public MidiDevice.Info[] getDeviceInfo() {
        logger.log(TRACE, "begin");

        List<MidiDevice.Info> infoList = new ArrayList<>();
        for (MidiDevice device : devices) {
            MidiDevice.Info info = device.getDeviceInfo();
            infoList.add(info);
        }
        MidiDevice.Info[] infos = infoList.toArray(EMPTY_INFO_ARRAY);

        logger.log(TRACE, "end");

        return infos;
    }

    @Override
    public MidiDevice getDevice(MidiDevice.Info info) {
        logger.log(TRACE, "begin");

        MidiDevice returnedDevice = null;
        for (MidiDevice device : devices) {
            MidiDevice.Info info2 = device.getDeviceInfo();
            if (info != null && info.equals(info2)) {
                returnedDevice = device;
                break;
            }
        }
        if (returnedDevice == null) {
            throw new IllegalArgumentException("no device for " + info);
        }

        logger.log(TRACE, "end");

        return returnedDevice;
    }

    private void scanPorts() {
        logger.log(TRACE, "begin");

        Iterator<?> clients = alsaSeq.getClientInfos();
        while (clients.hasNext()) {
            AlsaSeqClientInfo clientInfo = (AlsaSeqClientInfo) clients.next();
            int client = clientInfo.getClient();
            logger.log(TRACE, "client: " + client);

            Iterator<?> ports = alsaSeq.getPortInfos(client);
            while (ports.hasNext()) {
                AlsaSeqPortInfo portInfo = (AlsaSeqPortInfo) ports.next();
                handlePort(clientInfo, portInfo);
            }
        }

        logger.log(TRACE, "end");
    }

    private void handlePort(AlsaSeqClientInfo clientInfo, AlsaSeqPortInfo portInfo) {
        int client = clientInfo.getClient();
        int port = portInfo.getPort();
        int type = portInfo.getType();
        int capability = portInfo.getCapability();
        int synthVoices = portInfo.getSynthVoices();
        logger.log(TRACE, "port: " + port);
        logger.log(TRACE, "type: " + type);
        logger.log(TRACE, "cap: " + capability);
        logger.log(TRACE, "midi channels: " + portInfo.getMidiChannels());
        logger.log(TRACE, "midi voices: " + portInfo.getMidiVoices());
        logger.log(TRACE, "synth voices: " + portInfo.getSynthVoices());
        if ((type & AlsaSeq.SND_SEQ_PORT_TYPE_MIDI_GENERIC) != 0) {
//logger.log(Level.TRACE, "generic midi");
            MidiDevice device = null;
            if ((type & (AlsaSeq.SND_SEQ_PORT_TYPE_SYNTH | AlsaSeq.SND_SEQ_PORT_TYPE_DIRECT_SAMPLE | AlsaSeq.SND_SEQ_PORT_TYPE_SAMPLE)) != 0) {
                boolean writeSubscriptionAllowed = (capability & WRITE_CAPABILITY) == WRITE_CAPABILITY;
                if (writeSubscriptionAllowed) {
                    device = new AlsaSynthesizer(client, port, synthVoices);
                } else {
                    logger.log(TRACE, "port does not allows write subscription, not used");
                }
            } else { // ordinary midi port
                boolean readSubscriptionAllowed = (capability & READ_CAPABILITY) == READ_CAPABILITY;
                boolean writeSubscriptionAllowed = (capability & WRITE_CAPABILITY) == WRITE_CAPABILITY;
                if (readSubscriptionAllowed || writeSubscriptionAllowed) {
                    device = new AlsaMidiDevice(client, port, readSubscriptionAllowed, writeSubscriptionAllowed);
                } else {
                    logger.log(TRACE, "port allows neither read nor write subscription, not used");
                }
            }
            if (device != null) {
                devices.add(device);
            }
        }
    }
}
