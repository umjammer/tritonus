/*
 *  Copyright (c) 2000 - 2001 by Matthias Pfisterer
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

package org.tritonus.lowlevel.ogg;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import vavi.sound.sampled.jna.ogg.OggLibrary;
import vavi.sound.sampled.jna.ogg.ogg_packet;

import static java.lang.System.getLogger;


/**
 * Wrapper for ogg_packet.
 */
public class Packet {

    private static final Logger logger = getLogger(Packet.class.getName());

    /**
     * Holds the pointer to ogg_packet
     * for the code.
     * This must be long to be 64bit-clean.
     */
    private ogg_packet handle;

    public ogg_packet getHandle() {
        return handle;
    }

    public Packet() {
        logger.log(Level.TRACE, "begin");

        int ret = malloc();
        if (ret < 0) {
            throw new RuntimeException("malloc of ogg_packet failed");
        }

        logger.log(Level.TRACE, "end");
    }

    private int malloc() {
        logger.log(Level.TRACE, "begin");

        handle = new ogg_packet();
        logger.log(Level.TRACE, "malloc: handle: %s".formatted(handle));

        logger.log(Level.TRACE, "end");

        return 0;
    }

    public void free() {
        logger.log(Level.TRACE, "begin");

        handle = null;

        logger.log(Level.TRACE, "end");
    }

    /**
     * Calls ogg_packet_clear().
     */
    public void clear() {
        logger.log(Level.TRACE, "begin");

        OggLibrary.INSTANCE.ogg_packet_clear(handle);

        logger.log(Level.TRACE, "end");
    }

    /**
     * Accesses packet and bytes.
     */
    public byte[] getData() {
        logger.log(Level.TRACE, "begin");

        byte[] data = new byte[handle.bytes.intValue()];
        handle.packet.read(0, data, 0, handle.bytes.intValue());

        logger.log(Level.TRACE, "end");

        return data;
    }

    /**
     * Accesses b_o_s.
     */
    public boolean isBos() {
        logger.log(Level.TRACE, "begin");

        logger.log(Level.TRACE, "b_o_s: %d".formatted(handle.b_o_s.intValue()));

        boolean ret = handle.b_o_s.intValue() != 0;

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Accesses e_o_s.
     */
    public boolean isEos() {
        logger.log(Level.TRACE, "begin");

        boolean ret = handle.e_o_s.intValue() != 0;

        logger.log(Level.TRACE, "end");

        return ret;
    }
}
