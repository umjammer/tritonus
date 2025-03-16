/*
 * SkipTest.java
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


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;


public class SkipTest {

    private static final int LOAD_METHOD_STREAM = 1;
    private static final int LOAD_METHOD_FILE = 2;
    private static final int LOAD_METHOD_URL = 3;

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsageAndExit();
        }
        int loadMethod = LOAD_METHOD_FILE;
        boolean checkAudioInputStream;
        int currentArg = 0;
        while (currentArg < args.length) {
            if (args[currentArg].equals("-h")) {
                printUsageAndExit();
//            } else if (args[currentArg].equals("-s")) {
//                loadMethod = LOAD_METHOD_STREAM;
//            } else if (args[currentArg].equals("-f")) {
//                loadMethod = LOAD_METHOD_FILE;
//            } else if (args[currentArg].equals("-u")) {
//                loadMethod = LOAD_METHOD_URL;
//            } else if (args[currentArg].equals("-i")) {
//                checkAudioInputStream = true;
            }

            currentArg++;
        }
        checkAudioInputStream = true;
        String source = args[currentArg - 2];
        long skip = Long.parseLong(args[currentArg - 1]);
        String filename = null;
        AudioFileFormat aff = null;
        AudioInputStream ais = null;
        try {
            switch (loadMethod) {
            case LOAD_METHOD_STREAM:
                InputStream inputStream = System.in;
                aff = AudioSystem.getAudioFileFormat(inputStream);
                filename = "<standard input>";
                if (checkAudioInputStream) {
                    ais = AudioSystem.getAudioInputStream(inputStream);
                }
                break;

            case LOAD_METHOD_FILE:
                File file = new File(source);
                aff = AudioSystem.getAudioFileFormat(file);
                filename = file.getCanonicalPath();
                if (checkAudioInputStream) {
                    ais = AudioSystem.getAudioInputStream(file);
                }
                break;

            case LOAD_METHOD_URL:
                URL url = new URL(source);
                aff = AudioSystem.getAudioFileFormat(url);
                filename = url.toString();
                if (checkAudioInputStream) {
                    ais = AudioSystem.getAudioInputStream(url);
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        if (aff == null) {
            System.out.println("Cannot determine format");
        } else {

//            AudioFormat format = aff.getFormat();
//            System.out.println("---------------------------------------------------------------------------");
//            System.out.println("Source: " + filename);
//            System.out.println("Type: " + aff.getType());
//            System.out.println("AudioFormat: " + format);
//            System.out.println("---------------------------------------------------------------------------");
//            String audioLength = null;
//            if (aff.getFrameLength() != AudioSystem.NOT_SPECIFIED) {
//                audioLength = "" + aff.getFrameLength() + " frames (= " + aff.getFrameLength() * format.getFrameSize() + " bytes)";
//            } else {
//                audioLength = "unknown";
//            }
//            System.out.println("Length of audio data: " + audioLength);
//            String fileLength = null;
//            if (aff.getByteLength() != AudioSystem.NOT_SPECIFIED) {
//                fileLength = "" + aff.getByteLength() + " bytes)";
//            } else {
//                fileLength = "unknown";
//            }
//            System.out.println("Total length of file (including headers): " + fileLength);

            if (checkAudioInputStream) {
                // System.out.println("[AudioInputStream says:] Length of audio data: " + ais.getFrameLength() + " frames (= " + ais.getFrameLength() * ais.getFormat().getFrameSize() + " bytes)");
                System.out.println("frame length: " + ais.getFrameLength());
                System.out.println("frame size: " + ais.getFormat().getFrameSize());
                System.out.println("AIS class:" + ais);
                System.out.println("now skipping...");
                long skipped = 0;
                try {
                    skipped = ais.skip(skip);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("skipped: " + skipped);
            }
            System.out.println("---------------------------------------------------------------------------");
        }
    }

    private static void printUsageAndExit() {
        System.out.println("SkipTest: usage:");
        System.out.println("\tjava SkipTest <audiofile> <skip>");
        System.exit(1);
    }
}
