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
import vavi.sound.sampled.jna.ogg.OggLibrary;
import vavi.sound.sampled.jna.ogg.ogg_page;

import static java.lang.System.getLogger;


/**
 * Wrapper for ogg_page.
 */
public class Page {

    private static final Logger logger= getLogger("org.tritonus.TraceOggNative");

    /**
     * Holds the pointer to ogg_page
     * for the code.
     * This must be long to be 64bit-clean.
     */
    private ogg_page handle;

    public ogg_page getHandle() {
        return handle;
    }

    public Page() {
        logger.log(Level.TRACE, "begin");

        int ret = malloc();
        if (ret < 0) {
            throw new RuntimeException("malloc of ogg_page failed");
        }

        logger.log(Level.TRACE, "end");
    }

    private int malloc() {
        logger.log(Level.TRACE, "begin");

        handle = new ogg_page();
        logger.log(Level.TRACE, "handle: %s".formatted(handle));

        logger.log(Level.TRACE, "end");

        return 0;
    }

    public void free() {
        logger.log(Level.TRACE, "begin");

        handle = null;

        logger.log(Level.TRACE, "end");
    }

    /**
     * Calls ogg_page_version().
     */
    public int getVersion() {
        logger.log(Level.TRACE, "begin");

        int ret = OggLibrary.INSTANCE.ogg_page_version(handle);

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Calls ogg_page_continued().
     */
    public boolean isContinued() {
        logger.log(Level.TRACE, "begin");

        int ret = OggLibrary.INSTANCE.ogg_page_continued(handle);

        logger.log(Level.TRACE, "end");

        return ret != 0;
    }

    /**
     * Calls ogg_page_packets().
     */
    public int getPackets() {
        logger.log(Level.TRACE, "begin");

        int ret = OggLibrary.INSTANCE.ogg_page_packets(handle);

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Calls ogg_page_bos().
     */
    public boolean isBos() {
        logger.log(Level.TRACE, "begin");

        int ret = OggLibrary.INSTANCE.ogg_page_bos(handle);

        logger.log(Level.TRACE, "end");

        return ret != 0;
    }

    /**
     * Calls ogg_page_eos().
     */
    public boolean isEos() {
        logger.log(Level.TRACE, "begin");

        int ret = OggLibrary.INSTANCE.ogg_page_eos(handle);

        logger.log(Level.TRACE, "end");

        return ret != 0;
    }

    /**
     * Calls ogg_page_granulepos().
     */
    public long getGranulePos() {
        logger.log(Level.TRACE, "begin");

        long ret = OggLibrary.INSTANCE.ogg_page_granulepos(handle);

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Calls ogg_page_serialno().
     */
    public int getSerialNo() {
        logger.log(Level.TRACE, "begin");

        int ret = OggLibrary.INSTANCE.ogg_page_serialno(handle);

        logger.log(Level.TRACE, "end");

        return ret;
    }

    /**
     * Calls ogg_page_pageno().
     */
    public int getPageNo() {
        logger.log(Level.TRACE, "begin");

        NativeLong ret = OggLibrary.INSTANCE.ogg_page_pageno(handle);

        logger.log(Level.TRACE, "end");

        return ret.intValue();
    }

    /**
     * Calls ogg_page_checksum_set().
     */
    public void setChecksum() {
        logger.log(Level.TRACE, "begin");

        OggLibrary.INSTANCE.ogg_page_checksum_set(handle);

        logger.log(Level.TRACE, "end");
    }

    public byte[] getHeader() {
        logger.log(Level.TRACE, "begin");

        byte[] byteArray = new byte[handle.header_len.intValue()];
        handle.header.read(0, byteArray, 0, handle.header_len.intValue());

        logger.log(Level.TRACE, "end");

        return byteArray;
    }

    public byte[] getBody() {
        logger.log(Level.TRACE, "begin");

        byte[] byteArray = new byte[handle.body_len.intValue()];
        handle.body.read(0, byteArray, 0, handle.body_len.intValue());

        logger.log(Level.TRACE, "end");

        return byteArray;
    }
}
