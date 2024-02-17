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

/*
|<---            this code is formatted to fit into 80 columns             --.|
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
        logger.log(Level.TRACE, "<init>: begin");

        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of ogg_packet failed");
        }

        logger.log(Level.TRACE, "<init>: end");
    }

    private int malloc() {
        logger.log(Level.TRACE, "malloc: begin");

        handle = new ogg_packet();
        logger.log(Level.TRACE, String.format("malloc: handle: %s", handle));

        logger.log(Level.TRACE, "malloc: end");

        return 0;
    }

    public void free() {
        logger.log(Level.TRACE, "free: begin");

        handle = null;

        logger.log(Level.TRACE, "free: end");
    }

    /**
     * Calls ogg_packet_clear().
     */
    public void clear() {
        logger.log(Level.TRACE, "clear: begin");

        OggLibrary.INSTANCE.ogg_packet_clear(handle);

        logger.log(Level.TRACE, "clear: end");
    }

    /**
     * Accesses packet and bytes.
     */
    public byte[] getData() {
        logger.log(Level.TRACE, "getData: begin");

        byte[] abData = new byte[handle.bytes.intValue()];
        handle.packet.read(0, abData, 0, handle.bytes.intValue());

        logger.log(Level.TRACE, "getData: end");

        return abData;
    }

    /**
     * Accesses b_o_s.
     */
    public boolean isBos() {
        logger.log(Level.TRACE, "isBos: begin");

        logger.log(Level.TRACE, String.format("isBos: b_o_s: %d", handle.b_o_s.intValue()));

        boolean bReturn = handle.b_o_s.intValue() != 0;

        logger.log(Level.TRACE, "isBos: end");

        return bReturn;
    }

    /**
     * Accesses e_o_s.
     */
    public boolean isEos() {
        logger.log(Level.TRACE, "isEos: begin");

        boolean bReturn = handle.e_o_s.intValue() != 0;

        logger.log(Level.TRACE, "isEos: end");

        return bReturn;
    }
}
