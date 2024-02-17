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

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.tritonus.share.TDebug;
import vavi.sound.sampled.jna.codec.CodecLibrary;
import vavi.sound.sampled.jna.codec.vorbis_comment;


/**
 * Wrapper for vorbis_info.
 */
public class Comment {

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
        if (TDebug.TraceVorbisNative) {
            TDebug.out("Comment.<init>(): begin");
        }
        int nReturn = malloc();
        if (nReturn < 0) {
            throw new RuntimeException("malloc of vorbis_comment failed");
        }
        if (TDebug.TraceVorbisNative) {
            TDebug.out("Comment.<init>(): end");
        }
    }

    private int malloc() {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("malloc(): begin");
        }
        handle = new vorbis_comment();
        if (TDebug.TraceVorbisNative) {
            TDebug.out(String.format("malloc(): handle: %s", handle));
        }
        if (TDebug.TraceVorbisNative) {
            TDebug.out("malloc(): end");
        }
        return 0;
    }

    public void free() {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("free(): begin");
        }
        handle = null;
        if (TDebug.TraceVorbisNative) {
            TDebug.out("free(): end");
        }
    }

    /**
     * Calls vorbis_comment_init().
     */
    public void init() {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("init(): begin");
        }
        CodecLibrary.INSTANCE.vorbis_comment_init(handle);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("init(): end");
        }
    }

    /**
     * Calls vorbis_comment_add().
     */
    public void addComment(String strComment) {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("addComment(): begin");
        }
        CodecLibrary.INSTANCE.vorbis_comment_add(handle, strComment);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("addComment(): end");
        }
    }

    /**
     * Calls vorbis_comment_add_tag().
     */
    public void addTag(String strTag, String strComment) {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("addTag(): begin");
        }
        CodecLibrary.INSTANCE.vorbis_comment_add_tag(handle, strTag, strComment);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("addTag(): end");
        }
    }

    /**
     * Calls vorbis_comment_query_count().
     */
    public int queryCount(String strTag) {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("queryCount(): begin");
        }
        int nReturn = CodecLibrary.INSTANCE.vorbis_comment_query_count(handle, strTag);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("queryCount(): end");
        }
        return nReturn;
    }

    /**
     * Calls vorbis_comment_query().
     */
    public String query(String strTag, int nIndex) {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("query(): begin");
        }
        Pointer result = CodecLibrary.INSTANCE.vorbis_comment_query(handle, strTag, nIndex);
        String strReturn = result.getString(0);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("query(): end");
        }
        return strReturn;
    }

    /**
     * Accesses user_comments, comment_lengths and comments.
     */
    public String[] getUserComments() {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("getUserComments(): begin");
        }
        String[] stringArray = new String[handle.comments];
        for (int i = 0; i < handle.comments; i++) {
            String string = handle.user_comments.getValue().getString((long) i * Native.POINTER_SIZE);
            stringArray[i] = string;
        }
        if (TDebug.TraceVorbisNative) {
            TDebug.out("getUserComments(): end");
        }
        return stringArray;
    }

    /**
     * Accesses vendor.
     */
    public String getVendor() {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("getVendor(): begin");
        }
        String strReturn = handle.vendor.getString(0);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("getVendor(): end");
        }
        return strReturn;
    }

    /**
     * Calls vorbis_comment_clear().
     */
    public void clear() {
        if (TDebug.TraceVorbisNative) {
            TDebug.out("clear(): begin");
        }
        CodecLibrary.INSTANCE.vorbis_comment_clear(handle);
        if (TDebug.TraceVorbisNative) {
            TDebug.out("clear(): end");
        }
    }

//  /**
//   * Calls vorbis_commentheader_out().
//   */
//  public void headerOut(Packet packet);
}


