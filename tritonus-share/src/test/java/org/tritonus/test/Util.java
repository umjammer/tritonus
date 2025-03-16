/*
 * Util.java
 */
/*
 *  Copyright (c) 2001 - 2002 by Matthias Pfisterer
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

package org.tritonus.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class Util {

    public static void dumpByteArray(byte[] buf) {
        for (byte b : buf) {
            System.out.print(" " + b);
        }
        System.out.println();
    }

    // returns true if equal
    public static boolean compareByteArrays(byte[] b1, int offset1, byte[] b2, int offset2, int length) {
        for (int i = 0; i < length; i++) {
            if (b1[i + offset1] != b2[i + offset2]) {
                return false;
            }
        }
        return true;
    }

    public static byte[] getByteArrayFromFile(File file) throws IOException {
        long length = file.length();
        byte[] data = new byte[(int) length];
        FileInputStream fis = new FileInputStream(file);
        int bytesRemaining = (int) length;
        int writeStart = 0;
        while (bytesRemaining > 0) {
            int bytesRead = fis.read(data, writeStart, bytesRemaining);
            bytesRemaining -= bytesRead;
            writeStart += bytesRead;
        }
        fis.close();
        return data;
    }

    public static void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException ignored) {
        }
    }
}
