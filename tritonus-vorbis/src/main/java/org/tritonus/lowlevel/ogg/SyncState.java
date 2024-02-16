/*
 * SyncState.java
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

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.tritonus.share.TDebug;
import vavi.sound.sampled.jna.ogg.OggLibrary;
import vavi.sound.sampled.jna.ogg.ogg_page;
import vavi.sound.sampled.jna.ogg.ogg_sync_state;


/**
 * Wrapper for ogg_sync_state.
 */
public class SyncState {

    /**
     * Holds the pointer to ogg_sync_state
     * for the code.
     * This must be long to be 64bit-clean.
     */
    private ogg_sync_state handle;

    public SyncState() {
        if (TDebug.TraceOggNative) { TDebug.out("<init>: begin"); }
        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of ogg_sync_state failed");
        }
        if (TDebug.TraceOggNative) { TDebug.out("<init>: end"); }
    }

    private int malloc() {
        if (TDebug.TraceOggNative) { TDebug.out("malloc: begin"); }
        handle = new ogg_sync_state();
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
     * Calls ogg_sync_init().
     */
    public void init() {
        if (TDebug.TraceOggNative) { TDebug.out("init: begin"); }
        OggLibrary.INSTANCE.ogg_sync_init(handle);
        if (TDebug.TraceOggNative) { TDebug.out("init: end"); }
    }

    /**
     * Calls ogg_sync_clear().
     */
    public void clear() {
        if (TDebug.TraceOggNative) { TDebug.out("clear: begin"); }
        OggLibrary.INSTANCE.ogg_sync_clear(handle);
        if (TDebug.TraceOggNative) { TDebug.out("clear: end"); }
    }

    /**
     * Calls ogg_sync_reset().
     */
    public void reset() {
        if (TDebug.TraceOggNative) { TDebug.out("reset: begin"); }
        OggLibrary.INSTANCE.ogg_sync_reset(handle);
        if (TDebug.TraceOggNative) { TDebug.out("reset: end"); }
    }

    /**
     * Calls ogg_sync_destroy().
     */
    public void destroy() {
        if (TDebug.TraceOggNative) { TDebug.out("destroy: begin"); }
        OggLibrary.INSTANCE.ogg_sync_destroy(handle);
        if (TDebug.TraceOggNative) { TDebug.out("destroy: end"); }
    }

    /**
     * Calls ogg_sync_buffer()
     * and ogg_sync_wrote().
     */
    public int write(byte[] abBuffer, int nBytes) {
        if (TDebug.TraceOggNative) { TDebug.out("write: begin"); }
        Pointer buffer = OggLibrary.INSTANCE.ogg_sync_buffer(handle, new NativeLong(nBytes));
        buffer.write(0, abBuffer,0, nBytes);
        int nReturn = OggLibrary.INSTANCE.ogg_sync_wrote(handle, new NativeLong(nBytes));
        if (TDebug.TraceOggNative) { TDebug.out("write: end"); }
        return nReturn;
    }

    /**
     * Calls ogg_sync_pageseek().
     */
    public int pageseek(Page page) {
        if (TDebug.TraceOggNative) { TDebug.out("pageseek: begin"); }
        ogg_page pageHandle = page.getHandle();
        NativeLong nReturn = OggLibrary.INSTANCE.ogg_sync_pageseek(handle, pageHandle);
        if (TDebug.TraceOggNative) { TDebug.out("pageseek: end"); }
        return nReturn.intValue();
    }

    /**
     * Calls ogg_sync_pageout().
     */
    public int pageOut(Page page) {
        if (TDebug.TraceOggNative) { TDebug.out("pageOut: begin"); }
        ogg_page pageHandle = page.getHandle();
        int nReturn = OggLibrary.INSTANCE.ogg_sync_pageout(handle, pageHandle);
        if (TDebug.TraceOggNative) { TDebug.out("pageOut: end"); }
        return nReturn;
    }
}

/* SyncState.java */
