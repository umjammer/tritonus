/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tritonus.midi.device.fluidsynth.FluidSynthesizer;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;

import static vavi.sound.midi.MidiUtil.volume;


/**
 * TestCase.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 080701 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
class TestCase {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "test.midi")
    String midi = "../tritonus-midishare/src/test/resources/sounds/trippygaia1.mid";

    @Property(name = "tritonus.fluidsynth.defaultsoundbank")
    String sf = "/usr/local/Cellar/fluid-synth/2.3.5/share/soundfonts/default.sf2";

    @Property(name = "vavi.test.volume.midi")
    float volume = 0.2f;

    static boolean onIde = System.getProperty("vavi.test", "").equals("ide");
    static long time = onIde ? 1000 * 1000 : 10 * 1000;

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }

        System.setProperty("tritonus.fluidsynth.defaultsoundbank", sf);
Debug.println("soundfont: " + sf);
        if (!Files.exists(Path.of(sf))) {
Debug.println(Level.WARNING, "soundfont file set by 'tritonus.fluidsynth.defaultsoundbank' does not exist.");
        }
    }

    @Test
    void test() throws Exception {
Debug.println("midi: " + midi);
        Sequence sequence = MidiSystem.getSequence(Path.of(midi).toFile());
Debug.println("sequence: " + sequence);

        Synthesizer synthesizer = MidiSystem.getSynthesizer();
        synthesizer.open();
Debug.println("synthesizer: " + synthesizer);
        if (synthesizer instanceof FluidSynthesizer) {
            float gain = volume;
            ((FluidSynthesizer) synthesizer).setGain(gain);
Debug.println("set gain: " + gain);
        } else {
            throw new IllegalStateException("this is FluidSynthesizer test");
        }

        // if MidiSystem#getSequencer()'s argument connected is set true
        // the sequencer uses a synthesizer created by MidiSystem instead of yours.
        Sequencer sequencer = MidiSystem.getSequencer(false);
        sequencer.open();
Debug.println("sequencer: " + sequencer);
        // tell the sequencer to use your synthesizer instance which volume is downed
        sequencer.getTransmitter().setReceiver(synthesizer.getReceiver());

        CountDownLatch cdl = new CountDownLatch(1);
        MetaEventListener mel = meta -> {
Debug.println("META: " + meta.getType());
            if (meta.getType() == 47) cdl.countDown();
        };

        volume(synthesizer.getReceiver(), volume);
        sequencer.setSequence(sequence);
        sequencer.addMetaEventListener(mel);
        sequencer.start();
Debug.println("START");
if (!onIde) {
 Thread.sleep(time);
 sequencer.stop();
 Debug.println("STOP");
} else {
            cdl.await();
}
Debug.println("END");
        sequencer.stop();
        sequencer.removeMetaEventListener(mel);
        sequencer.close();
    }

    /**
     * @param args 0: midi
     */
    public static void main(String[] args) throws Exception {
        TestCase app = new TestCase();
        app.setup();

        app.midi = args[0];
        app.test();
    }
}
