/*
 * saint.java
 *
 * Standalone program to demonstrate the usage of the Saint class.
 */

import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.LineUnavailableException;

import gnu.getopt.Getopt;

import org.tritonus.lowlevel.saint.Saint;


public class saint {

    private static class SupportedFormat {

        private String name;
        private int number;
        private AudioFormat.Encoding encoding;
        private int sampleSize;
        private boolean bigEndian;

        // sample size is in bits
        public SupportedFormat(String name,
                               int number,
                               AudioFormat.Encoding encoding,
                               int sampleSize,
                               boolean bigEndian) {
            this.name = name;
            this.number = number;
            this.encoding = encoding;
            this.sampleSize = sampleSize;
        }

        public String getName() {
            return name;
        }

        public int getNumber() {
            return number;
        }

        public AudioFormat.Encoding getEncoding() {
            return encoding;
        }

        public int getSampleSize() {
            return sampleSize;
        }

        public boolean getBigEndian() {
            return bigEndian;
        }
    }

    private static final SupportedFormat[] SUPPORTED_FORMATS = {
            new SupportedFormat("s8", Saint.SND_PCM_SFMT_S8, PCM_SIGNED, 8, true),
            new SupportedFormat("u8", Saint.SND_PCM_SFMT_U8, PCM_UNSIGNED, 8, true),
            new SupportedFormat("s16l", Saint.SND_PCM_SFMT_S16_LE, PCM_SIGNED, 16, false),
            new SupportedFormat("s16b", Saint.SND_PCM_SFMT_S16_BE, PCM_SIGNED, 16, true),
            new SupportedFormat("u16l", Saint.SND_PCM_SFMT_U16_LE, PCM_UNSIGNED, 16, false),
            new SupportedFormat("u16b", Saint.SND_PCM_SFMT_U16_BE, PCM_UNSIGNED, 16, true),
            new SupportedFormat("s24l", Saint.SND_PCM_SFMT_S24_LE, PCM_SIGNED, 24, false),
            new SupportedFormat("s24b", Saint.SND_PCM_SFMT_S24_BE, PCM_SIGNED, 24, true),
            new SupportedFormat("u24l", Saint.SND_PCM_SFMT_U24_LE, PCM_UNSIGNED, 24, false),
            new SupportedFormat("u24b", Saint.SND_PCM_SFMT_U24_BE, PCM_UNSIGNED, 24, true),
            new SupportedFormat("s32l", Saint.SND_PCM_SFMT_S32_LE, PCM_SIGNED, 32, false),
            new SupportedFormat("s32b", Saint.SND_PCM_SFMT_S32_BE, PCM_SIGNED, 32, true),
            new SupportedFormat("u32l", Saint.SND_PCM_SFMT_U32_LE, PCM_UNSIGNED, 32, false),
            new SupportedFormat("u32b", Saint.SND_PCM_SFMT_U32_BE, PCM_UNSIGNED, 32, true),
            new SupportedFormat("f32l", Saint.SND_PCM_SFMT_S32_LE, PCM_SIGNED /* obviously wrong */, 32, false),
            new SupportedFormat("f32b", Saint.SND_PCM_SFMT_S32_BE, PCM_SIGNED /* obviously wrong */, 32, true),
    };

    private static final int DEFAULT_FORMAT = 2;

    public static void main(String[] args) throws Exception {
        InputStream bitstream = null;
        InputStream orchestra = null;
        InputStream score = null;
        OutputStream output = null;
        int outputFormatIndex = DEFAULT_FORMAT;
        boolean lineOutput = false;
        int c;

        Getopt g = new Getopt("saint", args, "hVb:c:s:o:f:");
        while ((c = g.getopt()) != -1) {
            switch (c) {
            case 'h':
                printUsageAndExit();

            case 'V':
                printVersionAndExit();

            case 'b':
                bitstream = new FileInputStream(g.getOptarg());
                break;

            case 'c':
                orchestra = new FileInputStream(g.getOptarg());
                break;

            case 's':
                score = new FileInputStream(g.getOptarg());
                break;

            case 'o':
                if (g.getOptarg().equals("-")) {
                    output = System.out;
                }
                if (g.getOptarg().equals("+")) {
                    lineOutput = true;
                } else {
                    output = new FileOutputStream(g.getOptarg());
                }
                break;

            case 'f':
                int newOutputFormatIndex = -1;
                for (int i = 0; i < SUPPORTED_FORMATS.length; i++) {
                    if (SUPPORTED_FORMATS[i].getName().equals(g.getOptarg())) {
                        newOutputFormatIndex = i;
                        // nNewOutputFormat = SUPPORTED_FORMATS[i].getNumber();
                    }
                }
                if (newOutputFormatIndex != -1) {
                    outputFormatIndex = newOutputFormatIndex;
                } else {
                    System.err.println("warning: output format " + g.getOptarg() + "not supported; using default output format");
                }
                break;
            }
        }
        if (output == null && !lineOutput) {
            System.out.println("no output specified!");
            printUsageAndExit();
        }
        Saint saint = null;
        // specifying a bitstream overrides specifying orchestra & score files.
        if (bitstream != null) {
            saint = new Saint(bitstream);
        } else if (orchestra != null) {
            if (score != null) {
                saint = new Saint(orchestra, score);
            } else {
                System.out.println("no score file specified!");
                printUsageAndExit();
            }
        } else {
            System.out.println("neither bitstream nor orchestra specified!");
            printUsageAndExit();
        }
        System.err.println("output will be produces with " + saint.getChannelCount() + " channel(s) at " + saint.getSamplingRate() + " Hz\n");
        if (lineOutput) {
            AudioFormat format = new AudioFormat(
                    SUPPORTED_FORMATS[outputFormatIndex].getEncoding(),
                    saint.getSamplingRate(),
                    SUPPORTED_FORMATS[outputFormatIndex].getSampleSize(),
                    saint.getChannelCount(),
                    // TODO 24 bit is not handled correctely!!!
                    saint.getChannelCount() * SUPPORTED_FORMATS[outputFormatIndex].getSampleSize() / 8,
                    saint.getSamplingRate(),
                    SUPPORTED_FORMATS[outputFormatIndex].getBigEndian());
            DataLine.Info info = new DataLine.Info(
                    SourceDataLine.class, format);
            SourceDataLine line = null;
            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                // TODO (Tritonus) check if calling without arguments should work
                line.open(format);
                line.start();
            } catch (LineUnavailableException e) {
            }
            output = new SourceDataLineOutputStream(line);
        }
        saint.setOutput(output, SUPPORTED_FORMATS[outputFormatIndex].getNumber());
        saint.run();
        try {
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printUsageAndExit() {
        System.out.println("usage:");
        System.out.println("\t[TODO]:");
        System.exit(1);
    }

    public static void printVersionAndExit() {
        System.out.println("saint (new) version 0.1");
        System.exit(0);
    }
}
