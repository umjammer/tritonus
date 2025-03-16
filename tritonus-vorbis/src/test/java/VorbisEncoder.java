import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.tritonus.lowlevel.ogg.Packet;
import org.tritonus.lowlevel.ogg.Page;
import org.tritonus.lowlevel.ogg.StreamState;
import org.tritonus.lowlevel.vorbis.Block;
import org.tritonus.lowlevel.vorbis.Comment;
import org.tritonus.lowlevel.vorbis.DspState;
import org.tritonus.lowlevel.vorbis.Info;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;
import vavix.util.Checksum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static vavi.sound.SoundUtil.volume;


/**
 * VorbisEncoder
 */
@PropsEntity(url = "file:local.properties")
public class VorbisEncoder {

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    static final double volume = Double.parseDouble(System.getProperty("vavi.test.volume", "0.2"));

    @Property(name = "wav")
    String wav = "src/test/resources/test.wav";

    @Property(name = "ogg")
    String ogg = "src/test/resources/test.ogg";

    @Property(name = "play.wav")
    String playWav = "src/test/resources/test.wav";

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    // TODO works, but output is not stereo???
    @Test
    void test1() throws Exception {
        Path out = Path.of("tmp", "out.ogg");
        if (!Files.exists(out.getParent())) {
            Files.createDirectories(out.getParent());
        }

        encode(Path.of(wav), out);

        assertEquals(Checksum.getChecksum(out), Checksum.getChecksum(Paths.get(ogg)));
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test2() throws Exception {
        Path out = Path.of("tmp", "out2.pcm");
        if (!Files.exists(out.getParent())) {
            Files.createDirectories(out.getParent());
        }

        encode(Path.of(playWav), out);

        AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(Files.newInputStream(out)));
        Debug.println(ais.getFormat());

        AudioFormat lineFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                44100,
                16,
                2,
                4,
                44100,
                false);

        AudioInputStream oais = AudioSystem.getAudioInputStream(lineFormat, ais);

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, lineFormat, AudioSystem.NOT_SPECIFIED);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.addLineListener(event -> Debug.println(event.getType()));

        byte[] buf = new byte[8192];
        line.open(lineFormat, buf.length);
        volume(line, volume);
        line.start();
        int r;
        while (true) {
            r = oais.read(buf, 0, buf.length);
            if (r < 0) {
                break;
            }
            line.write(buf, 0, r);
        }
        line.drain();
        line.close();
    }

    // ----

    private static final int READ = 1024;

    /** out of the data segment, not the stack */
    private static final byte[] readBuffer = new byte[READ * 4 + 44];

    void encode(Path wav, Path ogg) throws Exception {
        // take physical pages, weld into a logical stream of packets
        StreamState os = new StreamState();
        // one Ogg bitstream page. Vorbis packets are inside
        Page og = new Page();
        // one raw packet of data for decode
        Packet op = new Packet();
        // struct that stores all the static vorbis bitstream settings
        Info vi = new Info();
        // struct that stores all the user comments
        Comment vc = new Comment();

        // central working state for the packet->PCM decoder
        DspState vd = new DspState();
        // local working space for packet->PCM decode
        Block vb = new Block();

        boolean eos = false;

        // we cheat on the WAV header; we just bypass 44 bytes and never
        // verify that it matches 16bit/stereo/44.1kHz.  This is just an
        // example, after all.

        AudioInputStream ais = null;
        try {
            ais = AudioSystem.getAudioInputStream(new BufferedInputStream(Files.newInputStream(wav)));
        } catch (UnsupportedAudioFileException e) {
            throw new IllegalArgumentException(e);
        }
        AudioFormat format = ais.getFormat();
        if (format.getChannels() != 2 || format.getSampleSizeInBits() != 16) {
            throw new IllegalArgumentException("need 16 bit stereo!");
        }
        OutputStream output = new BufferedOutputStream(Files.newOutputStream(ogg));

        // Encode setup

        // choose an encoding mode
        // (quality mode .4: 44kHz stereo coupled, roughly 128kbps VBR)
        vi.init();

        Debug.println(format);
        vi.encodeInitVBR(format.getChannels(),
                (int) format.getSampleRate(),
                0.1F); // max compression

        // add a comment
        vc.init();
        vc.addTag("ENCODER", "tritonus-vorbis.test");

        // set up the analysis state and auxiliary encoding storage
        vd.initAnalysis(vi);
        vb.init(vd);

        // set up our packet->stream encoder
        // pick a random serial number; that way we can more likely build
        // chained streams just by concatenation
        Random random = new Random(314159265358979L); // fixed seed for test
        os.init(random.nextInt());

        // Vorbis streams begin with three headers; the initial header (with
        // most of the codec setup parameters) which is mandated by the Ogg
        // bitstream spec.  The second header holds any comment fields.  The
        // third header holds the bitstream codebook.  We merely need to
        // make the headers, then pass them to libvorbis one at a time;
        // libvorbis handles the additional Ogg bitstream constraints

        Packet header = new Packet();
        Packet headerComm = new Packet();
        Packet headerCode = new Packet();

//Debug.printf("%s, %s, %s, %s, %s%n", vd.getHandle(), vc.getHandle(), header.getHandle(), headerComm.getHandle(), headerCode.getHandle());
        vd.headerOut(vc, header, headerComm, headerCode);
        os.packetIn(header); // automatically placed in its own page
        os.packetIn(headerComm);
        os.packetIn(headerCode);

        // We don't have to write out here, but doing so makes streaming
        // much easier, so we do, flushing ALL pages. This ensures the actual
        // audio data will start on a new page
        while (!eos) {
            int result = os.flush(og);
            if (result == 0)
                break;
            output.write(og.getHeader());
            output.write(og.getBody());
        }

        while (!eos) {
            int bytes = ais.read(readBuffer, 0, READ * 4); // stereo hardwired here

            if (bytes == 0 || bytes == -1) {
                // end of file.  this can be done implicitly in the mainline,
                // but it's easier to see here in non-clever fashion.
                // Tell the library we're at end of stream so that it can handle
                // the last frame and mark end of stream in the output properly
                vd.write(null, 0);
                if (bytes == -1) {
                    Debug.println("EOF");
                    break;
                }

            } else {
                // data to encode

                // expose the buffer to submit data
                float[][] buffer = new float[format.getChannels()][READ];
//                float[][] buffer = vd.buffer(READ);

                // uninterleave samples
                for (int i = 0; i < bytes / 4; i++) {
                    int sample = (readBuffer[i * 4 + 1] << 8) | (0x00ff & readBuffer[i * 4 + 0]);
                    float _sample = sample / 32768.0F;
                    buffer[0][i] = _sample;
                    sample = (readBuffer[i * 4 + 3] << 8) | (0x00ff & readBuffer[i * 4 + 2]);
                    _sample = sample / 32768.f;
                    buffer[1][i] = _sample;
                }

                // tell the library how much we actually submitted
                vd.write(buffer, bytes / 4);
            }

            // vorbis does some data preanalysis, then divvies up blocks for
            // more involved (potentially parallel) processing.  Get a single
            // block for encoding now
            while (vd.blockOut(vb) == 1) {

                // analysis, assume we want to use bitrate management
                vb.analysis(null);
                vb.addBlock();

                while (vd.flushPacket(op) != 0) {
                    // weld the packet into the bitstream
                    os.packetIn(op);

                    // write out pages (if any)
                    while (!eos) {
                        int result = os.pageOut(og);
                        if (result == 0)
                            break;
                        output.write(og.getHeader());
                        output.write(og.getBody());

                        // this could be set above, but for illustrative purposes, I do
                        // it here (to show that vorbis does know where the stream ends)

                        if (og.isEos()) {
                            eos = true;
                        }
                    }
                }
            }
        }

        // clean up and exit.  vorbis_info_clear() must be called last
        os.clear();
        vb.clear();
        vd.clear();
        vc.clear();
        vi.clear();

        output.flush();
        output.close();

        // ogg_page and ogg_packet structs always point to storage in
        // libvorbis. They're never freed or manipulated directly
        Debug.println("Done.");
    }

    private static int bytesToInt16(byte[] buffer, int byteOffset, boolean bigEndian) {
        return bigEndian ?
                ((buffer[byteOffset] << 8) | (buffer[byteOffset + 1] & 0xFF)) :
                ((buffer[byteOffset + 1] << 8) | (buffer[byteOffset] & 0xFF));
    }

    /**
     * @param args 0: , 1:
     */
    public static void main(String[] args) throws Exception {
        Path in = Path.of(args[0]);
        Path out = Path.of(args[1]);
        if (!Files.exists(out.getParent())) {
            Files.createDirectories(out.getParent());
        }
        VorbisEncoder app = new VorbisEncoder();
        app.encode(in, out);
    }
}
