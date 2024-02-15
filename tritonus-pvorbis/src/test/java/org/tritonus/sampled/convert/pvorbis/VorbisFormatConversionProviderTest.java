/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.tritonus.sampled.convert.pvorbis;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.tritonus.share.TDebug;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;
import vavix.util.Checksum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.tritonus.sampled.file.pvorbis.VorbisAudioFileWriter.VORBIS;


/**
 * FlacFormatConversionProviderTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2024/02/12 umjammer initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class VorbisFormatConversionProviderTest {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    static {
        System.setProperty("vavi.util.logging.VaviFormatter.extraClassMethod", "org\\.tritonus\\.share\\.TDebug#out");

        TDebug.TraceAudioConverter = false;
    }

    @Property
    String wav = "src/test/resources/test.wav";

    @Property
    String ogg = "src/test/resources/test.ogg";

    // TODO FileChannel#transfarXXX doesn't work???
    @Test
    @DisplayName("encoding")
    void test1() throws Exception {
        AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(Files.newInputStream(Path.of(wav))));
        AudioFormat inFormat = ais.getFormat();
Debug.println(inFormat);

        Map<String, Object> props = new HashMap<>();
        props.put("vorbis.test", true);
        props.put("quality", 1);

        AudioFormat outFormat = new AudioFormat(
                VorbisFormatConversionProvider.VORBIS,
                -1f,
                -1,
                2,
                -1,
                -1f,
                false,
                props);
Debug.println(outFormat);
        AudioInputStream aout = AudioSystem.getAudioInputStream(outFormat, ais);

        Path out = Paths.get("tmp", "out.ogg");
        OutputStream fos = Files.newOutputStream(out, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        byte[] buf = new byte[8192];
        while (true) {
            int r = aout.read(buf, 0, buf.length);
            if (r < 0) {
                break;
            }
            fos.write(buf, 0, r);
        }
        fos.close();
        aout.close();

        assertEquals(Checksum.getChecksum(out), Checksum.getChecksum(Paths.get(ogg)));
    }

    @Test
    @DisplayName("writer")
    void test4() throws Exception {
        AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(Files.newInputStream(Path.of(wav))));
        AudioFormat inFormat = ais.getFormat();
Debug.println(inFormat);

        Map<String, Object> props = new HashMap<>();
        props.put("vorbis.test", true);
        props.put("quality", 1);

        AudioFormat outFormat = new AudioFormat(
                VorbisFormatConversionProvider.VORBIS,
                inFormat.getSampleRate(),
                -1,
                inFormat.getChannels(),
                -1,
                -1f,
                false,
                props);
Debug.println(outFormat);
        AudioInputStream aout = AudioSystem.getAudioInputStream(outFormat, ais);

        Path out2 = Paths.get("tmp", "out2.ogg");
        AudioSystem.write(aout, VORBIS, new BufferedOutputStream(Files.newOutputStream(out2)));

        assertEquals(Checksum.getChecksum(out2), Checksum.getChecksum(Paths.get(ogg)));
    }

    @Test
    @DisplayName("writer")
    void test3() throws Exception {
        AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(Files.newInputStream(Path.of(ogg))));
        AudioFormat inFormat = ais.getFormat();
Debug.println(inFormat);

        Map<String, Object> props = new HashMap<>();
        props.put("vorbis.test", true);
        props.put("quality", 1);

        // TODO is this only way to set props to AudioSystem#write() method?
        AudioFormat outFormat = new AudioFormat(
                inFormat.getEncoding(),
                inFormat.getSampleRate(),
                inFormat.getSampleSizeInBits(),
                inFormat.getChannels(),
                inFormat.getFrameSize(),
                inFormat.getFrameRate(),
                inFormat.isBigEndian(),
                props);
Debug.println(outFormat);
        AudioInputStream aout = AudioSystem.getAudioInputStream(outFormat, ais);

        Path out2 = Paths.get("tmp", "out2.ogg");
        AudioSystem.write(aout, VORBIS, new BufferedOutputStream(Files.newOutputStream(out2)));

        assertEquals(Checksum.getChecksum(out2), Checksum.getChecksum(Paths.get(ogg)));
    }
}

/* */
