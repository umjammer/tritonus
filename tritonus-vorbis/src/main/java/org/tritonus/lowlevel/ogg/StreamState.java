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
import vavi.sound.sampled.jna.ogg.ogg_page;
import vavi.sound.sampled.jna.ogg.ogg_stream_state;

import static java.lang.System.getLogger;


/**
 * Wrapper for ogg_stream_state.
 */
public class StreamState {

    private static final Logger logger= getLogger("org.tritonus.TraceOggNative");

    /**
     * Holds the pointer to ogg_stream_state
     * for the code.
     * This must be long to be 64bit-clean.
     */
    private ogg_stream_state handle;

    public StreamState() {
        logger.log(Level.TRACE, "<init>: begin");

        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of ogg_stream_state failed");
        }

        logger.log(Level.TRACE, "<init>: end");
    }

    private int malloc() {
        logger.log(Level.TRACE, "malloc: begin");

        handle = new ogg_stream_state();
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
     * Calls ogg_stream_init().
     */
    public int init(int nSerialNo) {
        logger.log(Level.TRACE, "init: begin");

        int nReturn = OggLibrary.INSTANCE.ogg_stream_init(handle, nSerialNo);

        logger.log(Level.TRACE, "init: end");

        return nReturn;
    }

    /**
     * Calls ogg_stream_clear().
     */
    public int clear() {
        logger.log(Level.TRACE, "clear: begin");

        int nReturn = OggLibrary.INSTANCE.ogg_stream_clear(handle);

        logger.log(Level.TRACE, "clear: end");

        return nReturn;
    }

    /**
     * Calls ogg_stream_reset().
     */
    public int reset() {
        logger.log(Level.TRACE, "reset: begin");

        int nReturn = OggLibrary.INSTANCE.ogg_stream_reset(handle);

        logger.log(Level.TRACE, "reset: end");

        return nReturn;
    }

    /**
     * Calls ogg_stream_destroy().
     */
    public int destroy() {
        logger.log(Level.TRACE, "destroy: begin");

        int nReturn = OggLibrary.INSTANCE.ogg_stream_destroy(handle);

        logger.log(Level.TRACE, "destroy: end");

        return nReturn;
    }

    /**
     * Calls ogg_stream_eos().
     */
    public boolean isEOSReached() {
        logger.log(Level.TRACE, "isEOSReached: begin");

        int nReturn = OggLibrary.INSTANCE.ogg_stream_eos(handle);

        logger.log(Level.TRACE, "isEOSReached: end");

        return nReturn != 0;
    }

    /**
     * Calls ogg_stream_packetin().
     */
    public int packetIn(Packet packet) {
        logger.log(Level.TRACE, "packetIn: begin");

        ogg_packet packetHandle = packet.getHandle();
        int nReturn = OggLibrary.INSTANCE.ogg_stream_packetin(handle, packetHandle);

        logger.log(Level.TRACE, "packetIn: end");

        return nReturn;
    }

    /**
     * Calls ogg_stream_pageout().
     */
    public int pageOut(Page page) {
        logger.log(Level.TRACE, "pageOut: begin");

        ogg_page pageHandle = page.getHandle();
        int nReturn = OggLibrary.INSTANCE.ogg_stream_pageout(handle, pageHandle);

        logger.log(Level.TRACE, "pageOut: end");

        return nReturn;
    }

    /**
     * Calls ogg_stream_flush().
     */
    public int flush(Page page) {
        logger.log(Level.TRACE, "flush: begin");

        ogg_page pageHandle = page.getHandle();
        int nReturn = OggLibrary.INSTANCE.ogg_stream_flush(handle, pageHandle);

        logger.log(Level.TRACE, "flush: end");

        return nReturn;
    }

    /**
     * Calls ogg_stream_pagein().
     */
    public int pageIn(Page page) {
        logger.log(Level.TRACE, "pageIn: begin");

        ogg_page pageHandle = page.getHandle();
        int nReturn = OggLibrary.INSTANCE.ogg_stream_pagein(handle, pageHandle);

        logger.log(Level.TRACE, "pageIn: end");

        return nReturn;
    }

    /**
     * Calls ogg_stream_packetout().
     */
    public int packetOut(Packet packet) {
        logger.log(Level.TRACE, "packetOut: begin");

        ogg_packet packetHandle = packet.getHandle();
        int nReturn = OggLibrary.INSTANCE.ogg_stream_packetout(handle, packetHandle);

        logger.log(Level.TRACE, "packetOut: end");

        return nReturn;
    }

    /**
     * Calls ogg_stream_packetpeek().
     */
    public int packetPeek(Packet packet) {
        logger.log(Level.TRACE, "packetPeek: begin");

        ogg_packet packetHandle = packet.getHandle();
        int nReturn = OggLibrary.INSTANCE.ogg_stream_packetpeek(handle, packetHandle);

        logger.log(Level.TRACE, "packetPeek: end");

        return nReturn;
    }
}
