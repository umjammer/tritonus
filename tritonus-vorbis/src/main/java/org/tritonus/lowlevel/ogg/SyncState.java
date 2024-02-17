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

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import vavi.sound.sampled.jna.ogg.OggLibrary;
import vavi.sound.sampled.jna.ogg.ogg_page;
import vavi.sound.sampled.jna.ogg.ogg_sync_state;

import static java.lang.System.getLogger;


/**
 * Wrapper for ogg_sync_state.
 */
public class SyncState {

    private static final Logger logger= getLogger("org.tritonus.TraceOggNative");

    /**
     * Holds the pointer to ogg_sync_state
     * for the code.
     * This must be long to be 64bit-clean.
     */
    private ogg_sync_state handle;

    public SyncState() {
        logger.log(Level.TRACE, "<init>: begin");

        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of ogg_sync_state failed");
        }

        logger.log(Level.TRACE, "<init>: end");
    }

    private int malloc() {
        logger.log(Level.TRACE, "malloc: begin");

        handle = new ogg_sync_state();
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
     * Calls ogg_sync_init().
     */
    public void init() {
        logger.log(Level.TRACE, "init: begin");

        OggLibrary.INSTANCE.ogg_sync_init(handle);

        logger.log(Level.TRACE, "init: end");
    }

    /**
     * Calls ogg_sync_clear().
     */
    public void clear() {
        logger.log(Level.TRACE, "clear: begin");

        OggLibrary.INSTANCE.ogg_sync_clear(handle);

        logger.log(Level.TRACE, "clear: end");
    }

    /**
     * Calls ogg_sync_reset().
     */
    public void reset() {
        logger.log(Level.TRACE, "reset: begin");

        OggLibrary.INSTANCE.ogg_sync_reset(handle);

        logger.log(Level.TRACE, "reset: end");
    }

    /**
     * Calls ogg_sync_destroy().
     */
    public void destroy() {
        logger.log(Level.TRACE, "destroy: begin");

        OggLibrary.INSTANCE.ogg_sync_destroy(handle);

        logger.log(Level.TRACE, "destroy: end");
    }

    /**
     * Calls ogg_sync_buffer()
     * and ogg_sync_wrote().
     */
    public int write(byte[] abBuffer, int nBytes) {
        logger.log(Level.TRACE, "write: begin");

        Pointer buffer = OggLibrary.INSTANCE.ogg_sync_buffer(handle, new NativeLong(nBytes));
        buffer.write(0, abBuffer, 0, nBytes);
        int nReturn = OggLibrary.INSTANCE.ogg_sync_wrote(handle, new NativeLong(nBytes));

        logger.log(Level.TRACE, "write: end");

        return nReturn;
    }

    /**
     * Calls ogg_sync_pageseek().
     */
    public int pageseek(Page page) {
        logger.log(Level.TRACE, "pageseek: begin");

        ogg_page pageHandle = page.getHandle();
        NativeLong nReturn = OggLibrary.INSTANCE.ogg_sync_pageseek(handle, pageHandle);

        logger.log(Level.TRACE, "pageseek: end");

        return nReturn.intValue();
    }

    /**
     * Calls ogg_sync_pageout().
     */
    public int pageOut(Page page) {
        logger.log(Level.TRACE, "pageOut: begin");

        ogg_page pageHandle = page.getHandle();
        int nReturn = OggLibrary.INSTANCE.ogg_sync_pageout(handle, pageHandle);

        logger.log(Level.TRACE, "pageOut: end");

        return nReturn;
    }
}
