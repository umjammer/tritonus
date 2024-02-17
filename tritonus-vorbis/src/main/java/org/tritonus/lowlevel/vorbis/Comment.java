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

package org.tritonus.lowlevel.vorbis;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import vavi.sound.sampled.jna.codec.CodecLibrary;
import vavi.sound.sampled.jna.codec.vorbis_comment;

import static java.lang.System.getLogger;


/**
 * Wrapper for vorbis_info.
 */
public class Comment {

    private static final Logger logger= getLogger("org.tritonus.TraceVorbisNative");

    /**
     * Holds the pointer to vorbis_info
     * for the code.
     * This must be long to be 64bit-clean.
     */
    private vorbis_comment handle;

    public vorbis_comment getHandle() {
        return handle;
    }

    public Comment() {
        logger.log(Level.TRACE, "Comment.<init>(): begin");

        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of vorbis_comment failed");
        }

        logger.log(Level.TRACE, "Comment.<init>(): end");
    }

    private int malloc() {
        logger.log(Level.TRACE, "malloc(): begin");

        handle = new vorbis_comment();
        logger.log(Level.TRACE, String.format("malloc(): handle: %s", handle));

        logger.log(Level.TRACE, "malloc(): end");

        return 0;
    }

    public void free() {
        logger.log(Level.TRACE, "free(): begin");

        handle = null;

        logger.log(Level.TRACE, "free(): end");
    }

    /**
     * Calls vorbis_comment_init().
     */
    public void init() {
        logger.log(Level.TRACE, "init(): begin");

        CodecLibrary.INSTANCE.vorbis_comment_init(handle);

        logger.log(Level.TRACE, "init(): end");
    }

    /**
     * Calls vorbis_comment_add().
     */
    public void addComment(String strComment) {
        logger.log(Level.TRACE, "addComment(): begin");

        CodecLibrary.INSTANCE.vorbis_comment_add(handle, strComment);

        logger.log(Level.TRACE, "addComment(): end");
    }

    /**
     * Calls vorbis_comment_add_tag().
     */
    public void addTag(String strTag, String strComment) {
        logger.log(Level.TRACE, "addTag(): begin");

        CodecLibrary.INSTANCE.vorbis_comment_add_tag(handle, strTag, strComment);

        logger.log(Level.TRACE, "addTag(): end");
    }

    /**
     * Calls vorbis_comment_query_count().
     */
    public int queryCount(String strTag) {
        logger.log(Level.TRACE, "queryCount(): begin");

        int nReturn = CodecLibrary.INSTANCE.vorbis_comment_query_count(handle, strTag);

        logger.log(Level.TRACE, "queryCount(): end");

        return nReturn;
    }

    /**
     * Calls vorbis_comment_query().
     */
    public String query(String strTag, int nIndex) {
        logger.log(Level.TRACE, "query(): begin");

        Pointer result = CodecLibrary.INSTANCE.vorbis_comment_query(handle, strTag, nIndex);
        String strReturn = result.getString(0);

        logger.log(Level.TRACE, "query(): end");

        return strReturn;
    }

    /**
     * Accesses user_comments, comment_lengths and comments.
     */
    public String[] getUserComments() {
        logger.log(Level.TRACE, "getUserComments(): begin");

        String[] stringArray = new String[handle.comments];
        for (int i = 0; i < handle.comments; i++) {
            String string = handle.user_comments.getValue().getString((long) i * Native.POINTER_SIZE);
            stringArray[i] = string;
        }

        logger.log(Level.TRACE, "getUserComments(): end");

        return stringArray;
    }

    /**
     * Accesses vendor.
     */
    public String getVendor() {
        logger.log(Level.TRACE, "getVendor(): begin");

        String strReturn = handle.vendor.getString(0);

        logger.log(Level.TRACE, "getVendor(): end");

        return strReturn;
    }

    /**
     * Calls vorbis_comment_clear().
     */
    public void clear() {
        logger.log(Level.TRACE, "clear(): begin");

        CodecLibrary.INSTANCE.vorbis_comment_clear(handle);

        logger.log(Level.TRACE, "clear(): end");
    }

//    /**
//     * Calls vorbis_commentheader_out().
//     */
//    public void headerOut(Packet packet);
}
