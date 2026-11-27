/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.midi.timidity;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static vavi.sound.midi.MidiUtil.volume;


/**
 * TiMiditySynthesizerTest.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-11-26 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
@DisabledIfEnvironmentVariable(named = "GITHUB_WORKFLOW", matches = ".*") // TODO need to build the shared library on CI
class TiMiditySynthesizerTest {

    static boolean localPropertiesExists() {
        return Files.exists(Path.of("local.properties"));
    }

    @Property(name = "test.midi")
    String midi = "../tritonus-midishare/src/test/resources/sounds/trippygaia1.mid";

    @Property(name = "vavi.test.volume.midi")
    float volume = 0.2f;

    static boolean onIde = System.getProperty("vavi.test", "").equals("ide");
    static long time = onIde ? 1000 * 1000 : 10 * 1000;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
        String sf = System.getProperty("tritonus.timidity.defaultsoundbank");
        String cfg = System.getProperty("tritonus.timidity.cfg");
        assumeTrue((sf != null && Files.exists(Path.of(sf))) || (cfg != null && Files.exists(Path.of(cfg))),
                "set 'tritonus.timidity.defaultsoundbank' or 'tritonus.timidity.cfg'");
Debug.println("soundfont: " + sf + ", cfg: " + cfg);
    }

    static TiMiditySynthesizer newSynthesizer() {
        TiMiditySynthesizerProvider provider = new TiMiditySynthesizerProvider();
        MidiDevice.Info[] infos = provider.getDeviceInfo();
        assumeTrue(infos.length == 1, "native library is not available");
        return (TiMiditySynthesizer) provider.getDevice(infos[0]);
    }

    /** sum of squares of 16bit samples */
    static double energy(byte[] buf, int frames) {
        double e = 0;
        for (int i = 0; i < frames * 2; i++) {
            short s = (short) ((buf[i * 2] & 0xff) | (buf[i * 2 + 1] << 8));
            e += (double) s * s;
        }
        return e;
    }

    @Test
    void testVersion() {
        newSynthesizer();
        String version = TiMidity.version();
Debug.println("version: " + version);
        assertTrue(version.startsWith("41"));
    }

    @Test
    void testOffline() throws Exception {
        TiMiditySynthesizer synthesizer = newSynthesizer();
        synthesizer.setLineEnabled(false);
        synthesizer.open();
        try {
            assertEquals(44100, synthesizer.getFormat().getSampleRate());

            byte[] buf = new byte[4410 * 4];

            // silence
            int n = synthesizer.render(buf, 4410);
            assertEquals(4410, n);
            assertEquals(0, energy(buf, n));

            // note on
            MidiChannel channel = synthesizer.getChannels()[0];
            channel.programChange(0);
            channel.noteOn(60, 100);
            assertEquals(0, channel.getProgram());
            double on = 0;
            for (int i = 0; i < 5; i++) {
                n = synthesizer.render(buf, 4410);
                on += energy(buf, n);
            }
Debug.println("note on energy: " + on);
            assertTrue(on > 0);

            // note off, wait for the release
            channel.noteOff(60);
            for (int i = 0; i < 30; i++) {
                synthesizer.render(buf, 4410);
            }
            n = synthesizer.render(buf, 4410);
            double off = energy(buf, n);
Debug.println("note off energy: " + off);
            assertTrue(off < on / 1000);

            // receiver, short message and sysex (gm system on, master volume)
            Receiver receiver = synthesizer.getReceiver();
            receiver.send(new javax.sound.midi.SysexMessage(new byte[] {(byte) 0xf0, 0x7e, 0x7f, 0x09, 0x01, (byte) 0xf7}, 6), -1);
            volume(receiver, 0.5f);
            receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 9, 7, 90), -1);
            receiver.send(new ShortMessage(ShortMessage.NOTE_ON, 9, 38, 120), -1); // snare
            assertEquals(90, synthesizer.getChannels()[9].getController(7));
            double drum = 0;
            for (int i = 0; i < 5; i++) {
                n = synthesizer.render(buf, 4410);
                drum += energy(buf, n);
            }
Debug.println("drum energy: " + drum);
            assertTrue(drum > 0);

            // mute
            synthesizer.getChannels()[1].setMute(true);
            synthesizer.getChannels()[1].noteOn(72, 100);
            for (int i = 0; i < 30; i++) {
                synthesizer.render(buf, 4410);
            }
            n = synthesizer.render(buf, 4410);
            assertTrue(energy(buf, n) < on / 1000, "muted");
        } finally {
            synthesizer.close();
        }
    }

    @Test
    void testSingleInstance() throws Exception {
        TiMiditySynthesizer s1 = newSynthesizer();
        s1.setLineEnabled(false);
        s1.open();
        try {
            TiMiditySynthesizer s2 = newSynthesizer();
            s2.setLineEnabled(false);
            assertThrows(MidiUnavailableException.class, s2::open);
        } finally {
            s1.close();
        }
        // reopen
        s1.open();
        s1.close();
    }

    @Test
    void testSpi() throws Exception {
        Synthesizer synthesizer = MidiSystem.getSynthesizer();
Debug.println("synthesizer: " + synthesizer.getDeviceInfo().getName());
        assertInstanceOf(TiMiditySynthesizer.class, synthesizer);
        assertTrue(Arrays.stream(MidiSystem.getMidiDeviceInfo()).anyMatch(i -> i.getName().contains("TiMidity")));
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void testPlay() throws Exception {
Debug.println("midi: " + midi);
        Sequence sequence = MidiSystem.getSequence(Path.of(midi).toFile());

        Synthesizer synthesizer = MidiSystem.getSynthesizer();
        synthesizer.open();
Debug.println("synthesizer: " + synthesizer);

        Sequencer sequencer = MidiSystem.getSequencer(false);
        sequencer.open();
        sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());

        CountDownLatch cdl = new CountDownLatch(1);
        MetaEventListener mel = meta -> {
            if (meta.getType() == 47) cdl.countDown();
        };

        volume(synthesizer.getReceiver(), volume);
        sequencer.setSequence(sequence);
        sequencer.addMetaEventListener(mel);
        sequencer.start();
Debug.println("START");
        cdl.await(time, TimeUnit.MILLISECONDS);
Debug.println("END");
        sequencer.stop();
        sequencer.removeMetaEventListener(mel);
        sequencer.close();
        synthesizer.close();
    }

    /**
     * @param args 0: midi
     */
    public static void main(String[] args) throws Exception {
        TiMiditySynthesizerTest app = new TiMiditySynthesizerTest();
        app.setup();
        app.midi = args[0];
        app.testPlay();
    }
}
