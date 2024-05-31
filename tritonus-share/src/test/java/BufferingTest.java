/*
 * BufferingTest.java
 */
/*
 *  Copyright (c) 1999, 2000 by Matthias Pfisterer
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


import java.io.BufferedInputStream;
import java.io.FileInputStream;


public class BufferingTest {

    public static void main(String[] args) throws Exception {
//        byte[] data = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
//        ByteArrayInputStream bais = new ByteArrayInputStream(data);
//        System.out.println(bais.markSupported());
//        bais.mark(15);
//        bais.reset();

        FileInputStream fis = new FileInputStream("BufferingTest.java");
        System.out.println("FileInputStream supports mark: " + fis.markSupported());

        BufferedInputStream bis = new BufferedInputStream(fis, 5);
        byte[] read1 = new byte[9];
        byte[] read2 = new byte[9];
        byte[] read3 = new byte[9];
        byte[] read4 = new byte[9];
        bis.mark(9);
        bis.read(read1);
        bis.mark(9);
        bis.read(read2);
        bis.reset();
        bis.read(read3);
        bis.reset();
        bis.read(read4);

        for (int i = 0; i < read1.length; i++) {
            if (read1[i] != read4[i]) {
                System.out.println("1 difference!!");
            }
        }
        for (int i = 0; i < read1.length; i++) {
            if (read2[i] != read3[i]) {
                System.out.println("2 difference!!");
            }
        }
    }
}
