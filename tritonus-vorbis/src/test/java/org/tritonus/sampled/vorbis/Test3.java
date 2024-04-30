/*
 * Copyright (c) 2024 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package org.tritonus.sampled.vorbis;

import java.nio.file.Files;
import java.nio.file.Paths;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import vavi.sound.SoundUtil;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * tritonus-vorbis.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2024/03/16 umjammer initial version <br>
 */
@PropsEntity(url = "file://${user.dir}/local.properties")
class Test3 {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    static final double volume = Double.parseDouble(System.getProperty("vavi.test.volume", "0.2"));

    @Property
    String ogg = "src/test/resources/test.ogg";

    @BeforeAll
    static void setupAll() throws Exception {
        Files.createDirectories(Paths.get("tmp"));
    }

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    /**
     * @param args none
     */
    public static void main(String[] args) throws Exception {
        for (AudioFileFormat.Type type : AudioSystem.getAudioFileTypes()) {
            System.err.println(type);
        }
        Test3 app = new Test3();
        PropsEntity.Util.bind(app);
        app.test2();
    }

    @Test
    @DisplayName("decoding")
    void test2() throws Exception {
        AudioInputStream originalAudioInputStream = AudioSystem.getAudioInputStream(Paths.get(ogg).toFile());
        AudioFormat originalAudioFormat = originalAudioInputStream.getFormat();
        System.err.println(originalAudioFormat);
        AudioFormat targetAudioFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                originalAudioFormat.getSampleRate(),
                16,
                2,
                4,
                originalAudioFormat.getSampleRate(),
                false);
        Debug.println(targetAudioFormat);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(targetAudioFormat, originalAudioInputStream);
        AudioFormat audioFormat = audioInputStream.getFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.addLineListener(event -> Debug.println(event.getType()));

        byte[] buf = new byte[8192];
        line.open(audioFormat, buf.length);
        SoundUtil.volume(line, volume);
        line.start();
        int r;
        while (true) {
            r = audioInputStream.read(buf, 0, buf.length);
            if (r < 0) {
                break;
            }
            line.write(buf, 0, r);
        }
        line.drain();
        line.close();
    }
}
