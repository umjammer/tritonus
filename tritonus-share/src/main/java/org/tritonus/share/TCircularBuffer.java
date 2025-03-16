/*
 *  Copyright (c) 1999 by Matthias Pfisterer
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

package org.tritonus.share;


import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import static java.lang.System.getLogger;


public class TCircularBuffer {

    private static final Logger logger= getLogger("org.tritonus.TraceCircularBuffer");

    private final boolean blockingRead;
    private final boolean blockingWrite;
    private final byte[] data;
    private final int size;
    private long readPos;
    private long writePos;
    private final Trigger trigger;
    private boolean open;

    public TCircularBuffer(int size, boolean blockingRead, boolean blockingWrite, Trigger trigger) {
        this.blockingRead = blockingRead;
        this.blockingWrite = blockingWrite;
        this.size = size;
        data = new byte[this.size];
        readPos = 0;
        writePos = 0;
        this.trigger = trigger;
        open = true;
    }

    public void close() {
        open = false;
        // TODO call notify() ?
    }

    private boolean isOpen() {
        return open;
    }

    public int availableRead() {
        return (int) (writePos - readPos);
    }

    public int availableWrite() {
        return size - availableRead();
    }

    private int getReadPos() {
        return (int) (readPos % size);
    }

    private int getWritePos() {
        return (int) (writePos % size);
    }

    public int read(byte[] data) {
        return read(data, 0, data.length);
    }

    public int read(byte[] data, int offset, int length) {
        if (logger.isLoggable(Level.TRACE)) {
            logger.log(Level.TRACE, "called.");
            dumpInternalState();
        }
        if (!isOpen()) {
            if (availableRead() > 0) {
                length = Math.min(length, availableRead());
                logger.log(Level.TRACE, "reading rest in closed buffer, length: " + length);

            } else {
                logger.log(Level.TRACE, "< not open. returning -1.");

                return -1;
            }
        }
        synchronized (this) {
            if (trigger != null && availableRead() < length) {
                logger.log(Level.TRACE, "executing trigger.");

                trigger.execute();
            }
            if (!blockingRead) {
                length = Math.min(availableRead(), length);
            }
            int remainingBytes = length;
            while (remainingBytes > 0) {
                while (availableRead() == 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        logger.log(Level.ERROR, e.getMessage(), e);
                    }
                }
                int available = Math.min(availableRead(), remainingBytes);
                while (available > 0) {
                    int toRead = Math.min(available, size - getReadPos());
                    System.arraycopy(this.data, getReadPos(), data, offset, toRead);
                    readPos += toRead;
                    offset += toRead;
                    available -= toRead;
                    remainingBytes -= toRead;
                }
                notifyAll();
            }
            if (logger.isLoggable(Level.TRACE)) {
                logger.log(Level.TRACE, "After read:");
                dumpInternalState();
                logger.log(Level.TRACE, "< completed. Read " + length + " bytes");
            }
            return length;
        }
    }

    public int write(byte[] data) {
        return write(data, 0, data.length);
    }

    public int write(byte[] data, int offset, int length) {
        if (logger.isLoggable(Level.TRACE)) {
            logger.log(Level.TRACE, "called; length: " + length);
            dumpInternalState();
        }
        synchronized (this) {
            logger.log(Level.TRACE, "entered synchronized block.");

            if (!blockingWrite) {
                length = Math.min(availableWrite(), length);
            }
            int remainingBytes = length;
            while (remainingBytes > 0) {
                while (availableWrite() == 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        logger.log(Level.ERROR, e.getMessage(), e);
                    }
                }
                int available = Math.min(availableWrite(), remainingBytes);
                while (available > 0) {
                    int toWrite = Math.min(available, size - getWritePos());
//logger.log(Level.DEBUG, "src buf size= " + data.length + ", offset = " + offset + ", dst buf size=" + data.length + " write pos=" + getWritePos() + " len=" + toWrite);
                    System.arraycopy(data, offset, this.data, getWritePos(), toWrite);
                    writePos += toWrite;
                    offset += toWrite;
                    available -= toWrite;
                    remainingBytes -= toWrite;
                }
                notifyAll();
            }
            if (logger.isLoggable(Level.TRACE)) {
                logger.log(Level.TRACE, "After write:");
                dumpInternalState();
                logger.log(Level.TRACE, "< completed. Wrote " + length + " bytes");
            }
            return length;
        }
    }

    private void dumpInternalState() {
        logger.log(Level.TRACE, "readPos  = " + readPos + " ^= " + getReadPos());
        logger.log(Level.TRACE, "writePos = " + writePos + " ^= " + getWritePos());
        logger.log(Level.TRACE, "availableRead()  = " + availableRead());
        logger.log(Level.TRACE, "availableWrite() = " + availableWrite());
    }

    public interface Trigger {

        void execute();
    }
}
