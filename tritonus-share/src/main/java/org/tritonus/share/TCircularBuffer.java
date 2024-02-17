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

    private boolean m_bBlockingRead;
    private boolean m_bBlockingWrite;
    private byte[] m_abData;
    private int m_nSize;
    private long m_lReadPos;
    private long m_lWritePos;
    private Trigger m_trigger;
    private boolean m_bOpen;

    public TCircularBuffer(int nSize, boolean bBlockingRead, boolean bBlockingWrite, Trigger trigger) {
        m_bBlockingRead = bBlockingRead;
        m_bBlockingWrite = bBlockingWrite;
        m_nSize = nSize;
        m_abData = new byte[m_nSize];
        m_lReadPos = 0;
        m_lWritePos = 0;
        m_trigger = trigger;
        m_bOpen = true;
    }

    public void close() {
        m_bOpen = false;
        // TODO call notify() ?
    }

    private boolean isOpen() {
        return m_bOpen;
    }

    public int availableRead() {
        return (int) (m_lWritePos - m_lReadPos);
    }

    public int availableWrite() {
        return m_nSize - availableRead();
    }

    private int getReadPos() {
        return (int) (m_lReadPos % m_nSize);
    }

    private int getWritePos() {
        return (int) (m_lWritePos % m_nSize);
    }

    public int read(byte[] abData) {
        return read(abData, 0, abData.length);
    }

    public int read(byte[] abData, int nOffset, int nLength) {
        if (logger.isLoggable(Level.TRACE)) {
            logger.log(Level.TRACE, ">TCircularBuffer.read(): called.");
            dumpInternalState();
        }
        if (!isOpen()) {
            if (availableRead() > 0) {
                nLength = Math.min(nLength, availableRead());
                logger.log(Level.TRACE, "reading rest in closed buffer, length: " + nLength);

            } else {
                logger.log(Level.TRACE, "< not open. returning -1.");

                return -1;
            }
        }
        synchronized (this) {
            if (m_trigger != null && availableRead() < nLength) {
                logger.log(Level.TRACE, "executing trigger.");

                m_trigger.execute();
            }
            if (!m_bBlockingRead) {
                nLength = Math.min(availableRead(), nLength);
            }
            int nRemainingBytes = nLength;
            while (nRemainingBytes > 0) {
                while (availableRead() == 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        logger.log(Level.ERROR, e.getMessage(), e);
                    }
                }
                int nAvailable = Math.min(availableRead(), nRemainingBytes);
                while (nAvailable > 0) {
                    int nToRead = Math.min(nAvailable, m_nSize - getReadPos());
                    System.arraycopy(m_abData, getReadPos(), abData, nOffset, nToRead);
                    m_lReadPos += nToRead;
                    nOffset += nToRead;
                    nAvailable -= nToRead;
                    nRemainingBytes -= nToRead;
                }
                notifyAll();
            }
            if (logger.isLoggable(Level.TRACE)) {
                logger.log(Level.TRACE, "After read:");
                dumpInternalState();
                logger.log(Level.TRACE, "< completed. Read " + nLength + " bytes");
            }
            return nLength;
        }
    }

    public int write(byte[] abData) {
        return write(abData, 0, abData.length);
    }

    public int write(byte[] abData, int nOffset, int nLength) {
        if (logger.isLoggable(Level.TRACE)) {
            logger.log(Level.TRACE, ">TCircularBuffer.write(): called; nLength: " + nLength);
            dumpInternalState();
        }
        synchronized (this) {
            logger.log(Level.TRACE, "entered synchronized block.");

            if (!m_bBlockingWrite) {
                nLength = Math.min(availableWrite(), nLength);
            }
            int nRemainingBytes = nLength;
            while (nRemainingBytes > 0) {
                while (availableWrite() == 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        logger.log(Level.ERROR, e.getMessage(), e);
                    }
                }
                int nAvailable = Math.min(availableWrite(), nRemainingBytes);
                while (nAvailable > 0) {
                    int nToWrite = Math.min(nAvailable, m_nSize - getWritePos());
//logger.log(Level.DEBUG, "src buf size= " + abData.length + ", offset = " + nOffset + ", dst buf size=" + m_abData.length + " write pos=" + getWritePos() + " len=" + nToWrite);
                    System.arraycopy(abData, nOffset, m_abData, getWritePos(), nToWrite);
                    m_lWritePos += nToWrite;
                    nOffset += nToWrite;
                    nAvailable -= nToWrite;
                    nRemainingBytes -= nToWrite;
                }
                notifyAll();
            }
            if (logger.isLoggable(Level.TRACE)) {
                logger.log(Level.TRACE, "After write:");
                dumpInternalState();
                logger.log(Level.TRACE, "< completed. Wrote " + nLength + " bytes");
            }
            return nLength;
        }
    }

    private void dumpInternalState() {
        logger.log(Level.TRACE, "m_lReadPos  = " + m_lReadPos + " ^= " + getReadPos());
        logger.log(Level.TRACE, "m_lWritePos = " + m_lWritePos + " ^= " + getWritePos());
        logger.log(Level.TRACE, "availableRead()  = " + availableRead());
        logger.log(Level.TRACE, "availableWrite() = " + availableWrite());
    }

    public interface Trigger {

        void execute();
    }
}
