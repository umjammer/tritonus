/*
 * StreamState.java
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
|<---            this code is formatted to fit into 80 columns             --->|
*/

package org.tritonus.lowlevel.ogg;

import org.tritonus.share.TDebug;
import vavi.sound.sampled.jna.ogg.OggLibrary;
import vavi.sound.sampled.jna.ogg.ogg_packet;
import vavi.sound.sampled.jna.ogg.ogg_page;
import vavi.sound.sampled.jna.ogg.ogg_stream_state;


/**
 * Wrapper for ogg_stream_state.
 */
public class StreamState {

    /**
     * Holds the pointer to ogg_stream_state
     * for the code.
     * This must be long to be 64bit-clean.
     */
    private ogg_stream_state handle;

    public StreamState() {
        if (TDebug.TraceOggNative) { TDebug.out("<init>: begin"); }
        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of ogg_stream_state failed");
        }
        if (TDebug.TraceOggNative) { TDebug.out("<init>: end"); }
    }

    private int malloc() {
        if (TDebug.TraceOggNative) { TDebug.out("malloc: begin"); }
        handle = new ogg_stream_state();
        if (TDebug.TraceOggNative) { TDebug.out(String.format("malloc: handle: %s", handle)); }
        if (TDebug.TraceOggNative) { TDebug.out("malloc: end"); }
        return 0;
    }

    public void free() {
        if (TDebug.TraceOggNative) { TDebug.out("free: begin"); }
        handle = null;
        if (TDebug.TraceOggNative) { TDebug.out("free: end"); }
    }

    /**
     * Calls ogg_stream_init().
     */
    public int init(int nSerialNo) {
        if (TDebug.TraceOggNative) { TDebug.out("init: begin"); }
        int nReturn = OggLibrary.INSTANCE.ogg_stream_init(handle, nSerialNo);
        if (TDebug.TraceOggNative) { TDebug.out("init: end"); }
        return nReturn;
    }

    /**
     * Calls ogg_stream_clear().
     */
    public int clear() {
        if (TDebug.TraceOggNative) { TDebug.out("clear: begin"); }
        int nReturn = OggLibrary.INSTANCE.ogg_stream_clear(handle);
        if (TDebug.TraceOggNative) { TDebug.out("clear: end"); }
        return nReturn;
    }

    /**
     * Calls ogg_stream_reset().
     */
    public int reset() {
        if (TDebug.TraceOggNative) { TDebug.out("reset: begin"); }
        int nReturn = OggLibrary.INSTANCE.ogg_stream_reset(handle);
        if (TDebug.TraceOggNative) { TDebug.out("reset: end"); }
        return nReturn;
    }

    /**
     * Calls ogg_stream_destroy().
     */
    public int destroy() {
        if (TDebug.TraceOggNative) { TDebug.out("destroy: begin"); }
        int nReturn = OggLibrary.INSTANCE.ogg_stream_destroy(handle);
        if (TDebug.TraceOggNative) { TDebug.out("destroy: end"); }
        return nReturn;
    }

    /**
     * Calls ogg_stream_eos().
     */
    public boolean isEOSReached() {
        if (TDebug.TraceOggNative) { TDebug.out("isEOSReached: begin"); }
        int nReturn = OggLibrary.INSTANCE.ogg_stream_eos(handle);
        if (TDebug.TraceOggNative) { TDebug.out("isEOSReached: end"); }
        return nReturn != 0;
    }

    /**
     * Calls ogg_stream_packetin().
     */
    public int packetIn(Packet packet) {
        if (TDebug.TraceOggNative) { TDebug.out("packetIn: begin"); }
        ogg_packet packetHandle = packet.getHandle();
        int nReturn = OggLibrary.INSTANCE.ogg_stream_packetin(handle, packetHandle);
        if (TDebug.TraceOggNative) { TDebug.out("packetIn: end"); }
        return nReturn;
    }

    /**
     * Calls ogg_stream_pageout().
     */
    public int pageOut(Page page) {
        if (TDebug.TraceOggNative) { TDebug.out("pageOut: begin"); }
        ogg_page pageHandle = page.getHandle();
        int	nReturn = OggLibrary.INSTANCE.ogg_stream_pageout(handle, pageHandle);
        if (TDebug.TraceOggNative) { TDebug.out("pageOut: end"); }
        return nReturn;
    }

    /**
     * Calls ogg_stream_flush().
     */
    public int flush(Page page) {
        if (TDebug.TraceOggNative) { TDebug.out("flush: begin"); }
        ogg_page pageHandle = page.getHandle();
        int nReturn = OggLibrary.INSTANCE.ogg_stream_flush(handle, pageHandle);
        if (TDebug.TraceOggNative) { TDebug.out("flush: end"); }
        return nReturn;
    }

    /**
     * Calls ogg_stream_pagein().
     */
    public int pageIn(Page page) {
        if (TDebug.TraceOggNative) { TDebug.out("pageIn: begin"); }
        ogg_page pageHandle = page.getHandle();
        int nReturn = OggLibrary.INSTANCE.ogg_stream_pagein(handle, pageHandle);
        if (TDebug.TraceOggNative) { TDebug.out("pageIn: end"); }
        return nReturn;
    }

    /**
     * Calls ogg_stream_packetout().
     */
    public int packetOut(Packet packet) {
        if (TDebug.TraceOggNative) { TDebug.out("packetOut: begin"); }
        ogg_packet packetHandle = packet.getHandle();
        int nReturn = OggLibrary.INSTANCE.ogg_stream_packetout(handle, packetHandle);
        if (TDebug.TraceOggNative) { TDebug.out("packetOut: end"); }
        return nReturn;
    }

    /**
     * Calls ogg_stream_packetpeek().
     */
    public int packetPeek(Packet packet) {
        if (TDebug.TraceOggNative) { TDebug.out("packetPeek: begin"); }
        ogg_packet packetHandle = packet.getHandle();
        int nReturn = OggLibrary.INSTANCE.ogg_stream_packetpeek(handle, packetHandle);
        if (TDebug.TraceOggNative) { TDebug.out("packetPeek: end"); }
        return nReturn;
    }
}

/* StreamState.java */
