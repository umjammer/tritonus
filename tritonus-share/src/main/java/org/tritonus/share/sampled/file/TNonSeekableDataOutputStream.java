
/*
 *  Copyright (c) 1999 by Florian Bomers
 *  Copyright (c) 2000 by Matthias Pfisterer
 *
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
 *
 */

package org.tritonus.share.sampled.file;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * A TDataOutputStream that does not allow seeking.
 *
 * @author Florian Bomers
 * @author Matthias Pfisterer
 */
public class TNonSeekableDataOutputStream
        extends DataOutputStream
        implements TDataOutputStream {

    public TNonSeekableDataOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    @Override
    public boolean supportsSeek() {
        return false;
    }

    @Override
    public void seek(long position)
            throws IOException {
        throw new IllegalArgumentException("TNonSeekableDataOutputStream: Call to seek not allowed.");
    }

    @Override
    public long getFilePointer()
            throws IOException {
        throw new IllegalArgumentException("TNonSeekableDataOutputStream: Call to getFilePointer not allowed.");
    }

    @Override
    public long length()
            throws IOException {
        throw new IllegalArgumentException("TNonSeekableDataOutputStream: Call to length not allowed.");
    }

    @Override
    public void writeLittleEndian32(int value)
            throws IOException {
        writeByte(value & 0xFF);
        writeByte((value >> 8) & 0xFF);
        writeByte((value >> 16) & 0xFF);
        writeByte((value >> 24) & 0xFF);
    }

    @Override
    public void writeLittleEndian16(short value)
            throws IOException {
        writeByte(value & 0xFF);
        writeByte((value >> 8) & 0xFF);
    }
}


