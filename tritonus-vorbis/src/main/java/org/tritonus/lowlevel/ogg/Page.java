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

import com.sun.jna.NativeLong;
import org.tritonus.share.TDebug;
import vavi.sound.sampled.jna.ogg.OggLibrary;
import vavi.sound.sampled.jna.ogg.ogg_page;


/**
 * Wrapper for ogg_page.
 */
public class Page {

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
        if (TDebug.TraceOggNative) {
            TDebug.out("<init>: begin");
        }
        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of ogg_page failed");
        }
        if (TDebug.TraceOggNative) {
            TDebug.out("<init>: end");
        }
    }

    private int malloc() {
        if (TDebug.TraceOggNative) {
            TDebug.out("malloc: begin");
        }
        handle = new ogg_page();
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
     * Calls ogg_page_version().
     */
    public int getVersion() {
        int nReturn;
        if (TDebug.TraceOggNative) {
            TDebug.out("getVersion: begin");
        }
        nReturn = OggLibrary.INSTANCE.ogg_page_version(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("getVersion: end");
        }
        return nReturn;
    }

    /**
     * Calls ogg_page_continued().
     */
    public boolean isContinued() {
        if (TDebug.TraceOggNative) {
            TDebug.out("isContinued: begin");
        }
        int nReturn = OggLibrary.INSTANCE.ogg_page_continued(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("isContinued: end");
        }
        return nReturn != 0;
    }

    /**
     * Calls ogg_page_packets().
     */
    public int getPackets() {
        if (TDebug.TraceOggNative) {
            TDebug.out("getPackets: begin");
        }
        int nReturn = OggLibrary.INSTANCE.ogg_page_packets(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("getPackets: end");
        }
        return nReturn;
    }

    /**
     * Calls ogg_page_bos().
     */
    public boolean isBos() {
        if (TDebug.TraceOggNative) {
            TDebug.out("isBos: begin");
        }
        int nReturn = OggLibrary.INSTANCE.ogg_page_bos(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("isBos: end");
        }
        return nReturn != 0;
    }

    /**
     * Calls ogg_page_eos().
     */
    public boolean isEos() {
        if (TDebug.TraceOggNative) {
            TDebug.out("isEos: begin");
        }
        int nReturn = OggLibrary.INSTANCE.ogg_page_eos(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("isEos: end");
        }
        return nReturn != 0;
    }

    /**
     * Calls ogg_page_granulepos().
     */
    public long getGranulePos() {
        if (TDebug.TraceOggNative) {
            TDebug.out("getGranulePos: begin");
        }
        long lReturn = OggLibrary.INSTANCE.ogg_page_granulepos(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("getGranulePos: end");
        }
        return lReturn;
    }

    /**
     * Calls ogg_page_serialno().
     */
    public int getSerialNo() {
        if (TDebug.TraceOggNative) {
            TDebug.out("getSerialNo: begin");
        }
        int nReturn = OggLibrary.INSTANCE.ogg_page_serialno(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("getSerialNo: end");
        }
        return nReturn;
    }

    /**
     * Calls ogg_page_pageno().
     */
    public int getPageNo() {
        if (TDebug.TraceOggNative) {
            TDebug.out("getPageNo: begin");
        }
        NativeLong nReturn = OggLibrary.INSTANCE.ogg_page_pageno(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("getPageNo: end");
        }
        return nReturn.intValue();
    }

    /**
     * Calls ogg_page_checksum_set().
     */
    public void setChecksum() {
        if (TDebug.TraceOggNative) {
            TDebug.out("setChecksum: begin");
        }
        OggLibrary.INSTANCE.ogg_page_checksum_set(handle);
        if (TDebug.TraceOggNative) {
            TDebug.out("setChecksum: end");
        }
    }

    public byte[] getHeader() {
        if (TDebug.TraceOggNative) {
            TDebug.out("getHeader: begin");
        }
        byte[] byteArray = new byte[handle.header_len.intValue()];
        handle.header.read(0, byteArray, 0, handle.header_len.intValue());
        if (TDebug.TraceOggNative) {
            TDebug.out("getHeader: end");
        }
        return byteArray;
    }

    public byte[] getBody() {
        if (TDebug.TraceOggNative) {
            TDebug.out("getBody: begin");
        }
        byte[] byteArray = new byte[handle.body_len.intValue()];
        handle.body.read(0, byteArray, 0, handle.body_len.intValue());
        if (TDebug.TraceOggNative) {
            TDebug.out("getBody: end");
        }
        return byteArray;
    }
}


