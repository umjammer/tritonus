/*
 * Copyright (c) 2008 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tritonus.midi.device.fluidsynth.FluidSynthesizer;
import vavi.sound.midi.MidiUtil;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;


/**
 * Test1.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 080701 nsano initial version <br>
 */
@PropsEntity(url = "file:local.properties")
public class Test1 {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    @Property(name = "test.midi")
    String midi = "../tritonus-midishare/src/test/resources/sounds/trippygaia1.mid";

    @Property(name = "tritonus.fluidsynth.defaultsoundbank")
    String sf = "/usr/local/Cellar/fluid-synth/2.3.5/share/soundfonts/default.sf2";

    static final float volume = Float.parseFloat(System.getProperty("vavi.test.volume", "0.2"));

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }

        System.setProperty("tritonus.fluidsynth.defaultsoundbank", sf);
Debug.println("soundfont: " + sf);
        if (!Files.exists(Path.of(sf))) {
            throw new IllegalStateException("soundfont file set by 'tritonus.fluidsynth.defaultsoundbank' does not exist.");
        }
    }

    @Test
    void test() throws Exception {
        main(new String[] {midi});
    }

    /**
     * @param args 0: midi
     */
    public static void main(String[] args) throws Exception {
        File file = new File(args[0]);
Debug.println("midi: " + args[0]);

        Sequence sequence = MidiSystem.getSequence(file);
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

        CountDownLatch countDownLatch = new CountDownLatch(1);
        MetaEventListener mel = meta -> {
Debug.println("META: " + meta.getType());
            if (meta.getType() == 47) {
                countDownLatch.countDown();
            }
        };

        sequencer.setSequence(sequence);
        sequencer.addMetaEventListener(mel);
        sequencer.start();
        MidiUtil.volume(synthesizer.getReceiver(), volume);
Debug.println("START");
        if (!System.getProperty("vavi.test", "").equals("ide")) {
            Thread.sleep(5 * 1000);
            sequencer.stop();
Debug.println("STOP");
        } else {
            countDownLatch.await();
        }
Debug.println("END");
        sequencer.stop();
        sequencer.removeMetaEventListener(mel);
        sequencer.close();
    }
}
