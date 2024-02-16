/*
 * Packet.java
 *
 * This file is part of Tritonus: http://www.tritonus.org/
 */

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

import org.tritonus.share.TDebug;
import vavi.sound.sampled.jna.ogg.OggLibrary;
import vavi.sound.sampled.jna.ogg.ogg_packet;


/**
 * Wrapper for ogg_packet.
 */
public class Packet {

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
        if (TDebug.TraceOggNative) {
            TDebug.out("<init>: begin");
        }
        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of ogg_packet failed");
        }
        if (TDebug.TraceOggNative) {
            TDebug.out("<init>: end");
        }
    }

    private int malloc() {
        if (TDebug.TraceOggNative) {
            TDebug.out("malloc: begin");
        }
        handle = new ogg_packet();
        if (TDebug.TraceOggNative) {
            TDebug.out(String.format("malloc: handle: %s", handle));
        }
        if (TDebug.TraceOggNative) {
            TDebug.out("malloc: end");
        }
        return 0;
    }

    public void free() {
        if (TDebug.TraceOggNative) {
            TDebug.out("free: begin");
        }
        handle = null;
        if (TDebug.TraceOggNative) {
            TDebug.out("free: end");
        }
    }

    /**
     * Calls ogg_packet_clear().
     */
    public void clear() {
        if (TDebug.TraceOggNative) {
            TDebug.out("clear: begin");
        }
        OggLibrary.INSTANCE.ogg_packet_clear(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("clear: end");
        }
    }

    /**
     * Accesses packet and bytes.
     */
    public byte[] getData() {
        if (TDebug.TraceOggNative) {
            TDebug.out("getData: begin");
        }
        byte[] abData = new byte[handle.bytes.intValue()];
        handle.packet.read(0, abData, 0, handle.bytes.intValue());
        if (TDebug.TraceOggNative) {
            TDebug.out("getData: end");
        }
        return abData;
    }

    /**
     * Accesses b_o_s.
     */
    public boolean isBos() {
        if (TDebug.TraceOggNative) {
            TDebug.out("isBos: begin");
        }
        if (TDebug.TraceOggNative) {
            TDebug.out(String.format("isBos: b_o_s: %d", handle.b_o_s.intValue()));
        }
        boolean bReturn = handle.b_o_s.intValue() != 0;
        if (TDebug.TraceOggNative) {
            TDebug.out("isBos: end");
        }
        return bReturn;
    }

    /**
     * Accesses e_o_s.
     */
    public boolean isEos() {
        if (TDebug.TraceOggNative) {
            TDebug.out("isEos: begin");
        }
        boolean bReturn = handle.e_o_s.intValue() != 0;
        if (TDebug.TraceOggNative) {
            TDebug.out("isEos: end");
        }
        return bReturn;
    }
}

/* Packet.java */
