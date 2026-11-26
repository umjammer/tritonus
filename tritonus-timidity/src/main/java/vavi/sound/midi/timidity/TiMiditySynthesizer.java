/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.midi.timidity;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.VoiceStatus;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.tritonus.share.midi.TDirectSynthesizer;
import org.tritonus.share.midi.TMidiChannel;

import static java.lang.System.getLogger;


/**
 * A synthesizer using TiMidity++ (libtimidity41).
 * <p>
 * the native library renders pcm, this class pulls it and writes to a {@link SourceDataLine}.
 * <p>
 * system properties
 * <ul>
 *  <li>{@code tritonus.timidity.cfg} ... timidity.cfg path</li>
 *  <li>{@code tritonus.timidity.defaultsoundbank} ... soundfont (sf2/sf3/sfz/dls) path</li>
 *  <li>{@code tritonus.timidity.rate} ... output sample rate, default 44100</li>
 *  <li>{@code tritonus.timidity.verbosity} ... native message verbosity, default -1 (silent)</li>
 * </ul>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-11-26 nsano initial version <br>
 */
public class TiMiditySynthesizer extends TDirectSynthesizer implements Synthesizer {

    private static final Logger logger = getLogger(TiMiditySynthesizer.class.getName());

    /** the native library has global state */
    private static final AtomicBoolean nativeInUse = new AtomicBoolean();

    /** frames rendered at once */
    private static final int BLOCK_FRAMES = 512;

    private static final int CHANNELS = 2;

    private static final int FRAME_SIZE = 2 * CHANNELS;

    private final TiMidityMidiChannel[] channels = new TiMidityMidiChannel[16];

    private int rate;

    private volatile float gain = 1f;

    private SourceDataLine line;

    private Thread renderer;

    private volatile boolean running;

    /** for offline rendering w/o audio line */
    private boolean lineEnabled = true;

    public TiMiditySynthesizer(MidiDevice.Info info) {
        super(info);
        for (int i = 0; i < channels.length; i++) {
            channels[i] = new TiMidityMidiChannel(i);
        }
    }

    /**
     * when false, no audio line is opened and the caller pulls pcm by {@link #render(byte[], int)}.
     * must be called before {@link #open()}.
     */
    public void setLineEnabled(boolean lineEnabled) {
        this.lineEnabled = lineEnabled;
    }

    /** @param gain linear gain applied to the output, 1.0 is unity */
    public void setGain(float gain) {
        this.gain = Math.max(0f, gain);
    }

    public float getGain() {
        return gain;
    }

    /** @return output format, valid after open */
    public AudioFormat getFormat() {
        return new AudioFormat(rate, 16, CHANNELS, true, ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
    }

    @Override
    protected void openImpl() throws MidiUnavailableException {
        if (!nativeInUse.compareAndSet(false, true)) {
            throw new MidiUnavailableException("timidity synthesizer is already in use (only one instance is available)");
        }
        try {
            TiMidity.setVerbosity(Integer.getInteger("tritonus.timidity.verbosity", -1));

            String cfg = System.getProperty("tritonus.timidity.cfg");
            if (cfg != null && cfg.isEmpty()) cfg = null;
            String sf = System.getProperty("tritonus.timidity.defaultsoundbank");
            String cfgString = null;
            if (sf != null && !sf.isEmpty()) {
                if (!Files.exists(Path.of(sf))) {
                    logger.log(Level.WARNING, "soundfont does not exist: " + sf);
                }
                cfgString = "soundfont \"" + sf.replace("\"", "\\\"") + "\"\n";
            }
            int requestedRate = Integer.getInteger("tritonus.timidity.rate", 44100);

            int r = TiMidity.init(cfg, cfgString, requestedRate);
            if (r != 0) {
                throw new MidiUnavailableException("timidity initialization failed: " + r + ", cfg: " + cfg + ", soundfont: " + sf);
            }
            rate = TiMidity.getRate();
            logger.log(Level.DEBUG, "timidity " + TiMidity.version() + ", rate: " + rate);

            if (lineEnabled) {
                startLine();
            }
        } catch (MidiUnavailableException | RuntimeException | Error e) {
            TiMidity.close();
            nativeInUse.set(false);
            throw e;
        }
    }

    private void startLine() throws MidiUnavailableException {
        AudioFormat format = getFormat();
        try {
            line = AudioSystem.getSourceDataLine(format);
            line.open(format, BLOCK_FRAMES * FRAME_SIZE * 8);
        } catch (LineUnavailableException | IllegalArgumentException e) {
            throw (MidiUnavailableException) new MidiUnavailableException(e.getMessage()).initCause(e);
        }
        line.start();

        running = true;
        renderer = new Thread(this::renderLoop, "timidity-renderer");
        renderer.setDaemon(true);
        renderer.setPriority(Thread.MAX_PRIORITY);
        renderer.start();
    }

    private void renderLoop() {
        byte[] buf = new byte[BLOCK_FRAMES * FRAME_SIZE];
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate(buf.length, 2);
            while (running) {
                int frames = render(segment, buf, BLOCK_FRAMES);
                if (frames < 0) {
                    logger.log(Level.ERROR, "render error: " + frames);
                    break;
                }
                line.write(buf, 0, frames * FRAME_SIZE);
            }
        } catch (Throwable t) {
            logger.log(Level.ERROR, t.getMessage(), t);
        }
    }

    private int render(MemorySegment segment, byte[] buf, int frames) {
        int r = TiMidity.render(segment, frames);
        if (r <= 0) return r;
        int bytes = r * FRAME_SIZE;
        float g = gain;
        if (g == 1f) {
            MemorySegment.copy(segment, ValueLayout.JAVA_BYTE, 0, buf, 0, bytes);
        } else {
            ValueLayout.OfShort layout = ValueLayout.JAVA_SHORT_UNALIGNED.withOrder(ByteOrder.nativeOrder());
            boolean bigEndian = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
            for (int i = 0; i < r * CHANNELS; i++) {
                int v = Math.round(segment.getAtIndex(layout, i) * g);
                v = Math.clamp(v, Short.MIN_VALUE, Short.MAX_VALUE);
                if (bigEndian) {
                    buf[i * 2] = (byte) (v >> 8);
                    buf[i * 2 + 1] = (byte) v;
                } else {
                    buf[i * 2] = (byte) v;
                    buf[i * 2 + 1] = (byte) (v >> 8);
                }
            }
        }
        return r;
    }

    /**
     * pulls pcm (only when the line is disabled).
     *
     * @param buf destination, 16bit signed native endian stereo
     * @param frames frames to render, {@code buf.length >= frames * 4}
     * @return frames rendered
     */
    public int render(byte[] buf, int frames) {
        if (!isOpen()) throw new IllegalStateException("not open");
        if (lineEnabled) throw new IllegalStateException("line is enabled");
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment segment = arena.allocate((long) frames * FRAME_SIZE, 2);
            return render(segment, buf, frames);
        }
    }

    @Override
    protected void closeImpl() {
        running = false;
        if (renderer != null) {
            try {
                renderer.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            renderer = null;
        }
        if (line != null) {
            line.drain();
            line.stop();
            line.close();
            line = null;
        }
        TiMidity.close();
        nativeInUse.set(false);
        for (TiMidityMidiChannel channel : channels) {
            channel.clear();
        }
        super.closeImpl();
    }

    /** passes all messages to the native synthesizer directly */
    @Override
    protected void receive(MidiMessage message, long timeStamp) {
        if (!isOpen()) return;
        if (message instanceof ShortMessage shortMessage) {
            int command = shortMessage.getCommand();
            if (command < 0xf0) {
                TiMidityMidiChannel channel = channels[shortMessage.getChannel()];
                if (!channel.update(command, shortMessage.getData1(), shortMessage.getData2())) {
                    return;
                }
            }
            TiMidity.sendShort(shortMessage.getStatus(), shortMessage.getData1(), shortMessage.getData2());
        } else if (message instanceof SysexMessage sysexMessage) {
            byte[] data = sysexMessage.getMessage();
            if (data.length > 0 && (data[0] & 0xff) == SysexMessage.SYSTEM_EXCLUSIVE) {
                TiMidity.sendSysex(data);
            }
        }
        // meta messages are ignored
    }

    /** sends all notes off and resets controllers */
    public void reset() {
        if (isOpen()) {
            TiMidity.reset();
        }
    }

    @Override
    public int getMaxPolyphony() {
        return 256; // timidity41 DEFAULT_VOICES
    }

    @Override
    public long getLatency() {
        return rate == 0 ? 0 : (long) BLOCK_FRAMES * 8 * 1_000_000 / rate;
    }

    @Override
    public MidiChannel[] getChannels() {
        return channels;
    }

    @Override
    public VoiceStatus[] getVoiceStatus() {
        return new VoiceStatus[0];
    }

    @Override
    public boolean isSoundbankSupported(Soundbank soundbank) {
        return false;
    }

    @Override
    public boolean loadInstrument(Instrument instrument) {
        throw new IllegalArgumentException("soundbank is not supported, use 'tritonus.timidity.defaultsoundbank'");
    }

    @Override
    public void unloadInstrument(Instrument instrument) {
        throw new IllegalArgumentException("soundbank is not supported");
    }

    @Override
    public boolean remapInstrument(Instrument from, Instrument to) {
        throw new IllegalArgumentException("soundbank is not supported");
    }

    @Override
    public Soundbank getDefaultSoundbank() {
        return null;
    }

    @Override
    public Instrument[] getAvailableInstruments() {
        return new Instrument[0];
    }

    @Override
    public Instrument[] getLoadedInstruments() {
        return new Instrument[0];
    }

    @Override
    public boolean loadAllInstruments(Soundbank soundbank) {
        throw new IllegalArgumentException("soundbank is not supported");
    }

    @Override
    public void unloadAllInstruments(Soundbank soundbank) {
        throw new IllegalArgumentException("soundbank is not supported");
    }

    @Override
    public boolean loadInstruments(Soundbank soundbank, Patch[] patchList) {
        throw new IllegalArgumentException("soundbank is not supported");
    }

    @Override
    public void unloadInstruments(Soundbank soundbank, Patch[] patchList) {
        throw new IllegalArgumentException("soundbank is not supported");
    }

    /** true if any channel is solo */
    private boolean anySolo() {
        for (TiMidityMidiChannel channel : channels) {
            if (channel.solo) return true;
        }
        return false;
    }

    /**
     * MidiChannel, keeps the state for getters because the native side does not provide them.
     */
    private class TiMidityMidiChannel extends TMidiChannel {

        private final int[] controllers = new int[128];
        private final int[] polyPressure = new int[128];
        private int program;
        private int channelPressure;
        private int pitchBend = 8192;
        private boolean mute;
        private boolean solo;

        TiMidityMidiChannel(int channel) {
            super(channel);
            clear();
        }

        void clear() {
            Arrays.fill(controllers, 0);
            Arrays.fill(polyPressure, 0);
            controllers[7] = 100;
            controllers[10] = 64;
            controllers[11] = 127;
            program = 0;
            channelPressure = 0;
            pitchBend = 8192;
        }

        /**
         * updates the state.
         * @return false if the message should be dropped (mute/solo)
         */
        boolean update(int command, int data1, int data2) {
            switch (command) {
            case ShortMessage.NOTE_ON -> {
                if (data2 > 0 && (mute || (anySolo() && !solo))) {
                    return false;
                }
            }
            case ShortMessage.POLY_PRESSURE -> polyPressure[data1 & 0x7f] = data2;
            case ShortMessage.CONTROL_CHANGE -> {
                controllers[data1 & 0x7f] = data2;
                if (data1 == 121) { // reset all controllers
                    pitchBend = 8192;
                    channelPressure = 0;
                    Arrays.fill(polyPressure, 0);
                }
            }
            case ShortMessage.PROGRAM_CHANGE -> program = data1;
            case ShortMessage.CHANNEL_PRESSURE -> channelPressure = data1;
            case ShortMessage.PITCH_BEND -> pitchBend = (data1 & 0x7f) | ((data2 & 0x7f) << 7);
            default -> {
            }
            }
            return true;
        }

        private void send(int command, int data1, int data2) {
            if (!isOpen()) return;
            if (update(command, data1, data2)) {
                TiMidity.sendShort(command | getChannel(), data1, data2);
            }
        }

        @Override
        public void noteOn(int noteNumber, int velocity) {
            send(ShortMessage.NOTE_ON, noteNumber, velocity);
        }

        @Override
        public void noteOff(int noteNumber, int velocity) {
            send(ShortMessage.NOTE_OFF, noteNumber, velocity);
        }

        @Override
        public void setPolyPressure(int noteNumber, int pressure) {
            send(ShortMessage.POLY_PRESSURE, noteNumber, pressure);
        }

        @Override
        public int getPolyPressure(int noteNumber) {
            return polyPressure[noteNumber & 0x7f];
        }

        @Override
        public void setChannelPressure(int pressure) {
            send(ShortMessage.CHANNEL_PRESSURE, pressure, 0);
        }

        @Override
        public int getChannelPressure() {
            return channelPressure;
        }

        @Override
        public void controlChange(int controller, int value) {
            send(ShortMessage.CONTROL_CHANGE, controller, value);
        }

        @Override
        public int getController(int controller) {
            return controllers[controller & 0x7f];
        }

        @Override
        public void programChange(int program) {
            send(ShortMessage.PROGRAM_CHANGE, program, 0);
        }

        @Override
        public int getProgram() {
            return program;
        }

        @Override
        public void setPitchBend(int bend) {
            send(ShortMessage.PITCH_BEND, bend & 0x7f, (bend >> 7) & 0x7f);
        }

        @Override
        public int getPitchBend() {
            return pitchBend;
        }

        @Override
        public void setMute(boolean mute) {
            this.mute = mute;
            if (mute) allNotesOff();
        }

        @Override
        public boolean getMute() {
            return mute;
        }

        @Override
        public void setSolo(boolean solo) {
            this.solo = solo;
        }

        @Override
        public boolean getSolo() {
            return solo;
        }
    }
}
