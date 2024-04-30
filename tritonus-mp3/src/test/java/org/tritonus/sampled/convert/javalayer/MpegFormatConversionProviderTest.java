/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.tritonus.sampled.convert.javalayer;

import java.io.ByteArrayInputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static java.lang.System.getLogger;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.tritonus.sampled.convert.javalayer.MpegFormatConversionProvider.MPEG1L3;


/**
 * MpegFormatConversionProviderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2024/04/29 umjammer initial version <br>
 */
class MpegFormatConversionProviderTest {

    private static final Logger logger = getLogger(MpegFormatConversionProviderTest.class.getName());

    int passed = 0;

    @BeforeAll
    static void setup() {
        logger.log(Level.INFO, "jna.library.path: " + System.getProperty("jna.library.path"));
        logger.log(Level.INFO, "vavi.test.volume: " + System.getProperty("vavi.test.volume"));
    }

    private void test(AudioFormat target, AudioFormat source, boolean failSupported, boolean failAIS, int testNum) {
        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(new byte[8]), source, 8);
        MpegFormatConversionProvider provider = new MpegFormatConversionProvider();
        boolean isConversionSupported = provider.isConversionSupported(target, source);
        AudioInputStream convertedAIS = null;
        try {
            convertedAIS = provider.getAudioInputStream(target, ais);
        } catch (Exception e) {
            // ignore
        }
        boolean failed = (failSupported == isConversionSupported) || (failAIS != (convertedAIS == null));
        if (failed || logger.isLoggable(Level.TRACE)) {
            if (failed) {
                logger.log(Level.DEBUG, (testNum) + ".ERROR:");
            } else {
                logger.log(Level.DEBUG, (testNum) + ".PASSED:");
            }
            logger.log(Level.DEBUG, "    source: " + source);
            logger.log(Level.DEBUG, "    target: " + target);
            if (failSupported == isConversionSupported) {
                logger.log(Level.DEBUG, "  isConversionSupported() erroneously returned " + isConversionSupported);
            } else {
                logger.log(Level.DEBUG, "  isConversionSupported() correctly returned " + isConversionSupported);
            }
            if (convertedAIS != null) {
                if (failAIS) {
                    logger.log(Level.DEBUG, "  converted stream was erroneously returned with format:");
                } else {
                    logger.log(Level.DEBUG, "  converted stream was correctly returned with format:");
                }
                logger.log(Level.DEBUG, "  converted format: " + convertedAIS.getFormat());
            } else {
                if (failAIS) {
                    logger.log(Level.DEBUG, "  converted stream was correctly not returned.");
                } else {
                    logger.log(Level.DEBUG, "  converted stream was erroneously not returned.");
                }
            }
        } else {
            logger.log(Level.DEBUG, (testNum) + ".OK");
        }
        assertFalse(failed);
        passed++;
    }

    @Test
    void test1() {
        int testNum = 0;

        // negative tests: should not be able to convert mp3 to mp3
        AudioFormat source = new AudioFormat(MPEG1L3, 44100, -1, 2, -1, -1, false);
        AudioFormat target = new AudioFormat(MPEG1L3, 44100, 16, 2, 4, 44100, false);
        test(target, source, true, true, testNum++);
        source = new AudioFormat(MPEG1L3, 44100, -1, 2, -1, -1, false);
        target = new AudioFormat(MPEG1L3, -1, 16, -1, -1, -1, false);
        test(target, source, true, true, testNum++);
        source = new AudioFormat(MPEG1L3, 44100, -1, 2, -1, -1, false);
        target = new AudioFormat(MPEG1L3, -1, 32, 2, 8, -1, false);
        test(target, source, true, true, testNum++);

        // negative test: should not claim to convert channels
        source = new AudioFormat(MPEG1L3, 44100, -1, 1, -1, -1, false);
        target = new AudioFormat(PCM_SIGNED, -1, 16, 2, 4, -1, false);
        test(target, source, true, true, testNum++);
        source = new AudioFormat(MPEG1L3, 44100, -1, 2, -1, -1, false);
        target = new AudioFormat(PCM_SIGNED, -1, 16, 1, 2, -1, false);
        test(target, source, true, true, testNum++);

        // negative test: should not claim to convert sample rate
        source = new AudioFormat(MPEG1L3, 44100, -1, 2, -1, -1, false);
        target = new AudioFormat(PCM_SIGNED, 8000, 16, 2, 4, 8000, false);
        test(target, source, true, true, testNum++);

        // positive test: should convert MP3 to PCM
        source = new AudioFormat(MPEG1L3, 44100, -1, 2, -1, -1, false);
        target = new AudioFormat(PCM_SIGNED, 44100, 16, 2, 4, 44100, false);
        test(target, source, false, false, testNum++);

        // positive test: should convert MP3 to PCM
        source = new AudioFormat(MPEG1L3, 44100, -1, 1, -1, -1, false);
        target = new AudioFormat(PCM_SIGNED, 44100, 16, 1, 2, 44100, false);
        test(target, source, false, false, testNum++);

        // special case: can check isSupported with -1 for both fields, but should not return an AIS
        source = new AudioFormat(MPEG1L3, -1, -1, 1, -1, -1, false);
        target = new AudioFormat(PCM_SIGNED, -1, 16, 1, 2, -1, false);
        test(target, source, false, true, testNum++);
        source = new AudioFormat(MPEG1L3, 8000, -1, -1, -1, -1, false);
        target = new AudioFormat(PCM_SIGNED, 8000, 16, -1, -1, 8000, false);
        test(target, source, false, true, testNum++);

        logger.log(Level.DEBUG, "Passed " + passed + " tests of " + testNum);
    }
}