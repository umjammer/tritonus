/*
 * VorbisDecoder.java
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.tritonus.lowlevel.ogg.Packet;
import org.tritonus.lowlevel.ogg.Page;
import org.tritonus.lowlevel.ogg.StreamState;
import org.tritonus.lowlevel.ogg.SyncState;
import org.tritonus.lowlevel.vorbis.Block;
import org.tritonus.lowlevel.vorbis.Comment;
import org.tritonus.lowlevel.vorbis.DspState;
import org.tritonus.lowlevel.vorbis.Info;
import org.tritonus.share.TDebug;
import vavi.util.Debug;
import vavi.util.properties.annotation.Property;
import vavi.util.properties.annotation.PropsEntity;
import vavix.util.Checksum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static vavi.sound.SoundUtil.volume;


@PropsEntity(url = "file:local.properties")
public class VorbisDecoder {

    static {
        System.setProperty("vavi.util.logging.VaviFormatter.extraClassMethod",
                "org\\.tritonus\\.share\\.TDebug#out");

        TDebug.TraceOggNative = false;
        TDebug.TraceVorbisNative = false;
    }

    static boolean localPropertiesExists() {
        return Files.exists(Paths.get("local.properties"));
    }

    static final double volume = Double.parseDouble(System.getProperty("vavi.test.volume", "0.2"));

    @Property(name = "ogg")
    String ogg = "src/test/resources/test.ogg";

    @Property(name = "pcm")
    String pcm = "src/test/resources/test.pcm";

    @Property(name = "play.ogg")
    String playOgg = "src/test/resources/test.ogg";

    @BeforeEach
    void setup() throws Exception {
        if (localPropertiesExists()) {
            PropsEntity.Util.bind(this);
        }
    }

    @Test
    void test1() throws Exception {
        Path out = Path.of("tmp", "out.pcm");
        if (!Files.exists(out.getParent())) {
            Files.createDirectories(out.getParent());
        }

        decode(Path.of(ogg), out);

        Debug.println(Checksum.getChecksum(out) + ", " + Checksum.getChecksum(Paths.get(pcm)));
        assertEquals(Checksum.getChecksum(out), Checksum.getChecksum(Paths.get(pcm)));
    }

    @Test
    @EnabledIfSystemProperty(named = "vavi.test", matches = "ide")
    void test2() throws Exception {
        Path out = Path.of("tmp", "out2.pcm");
        if (!Files.exists(out.getParent())) {
            Files.createDirectories(out.getParent());
        }

        decode(Path.of(playOgg), out);

        AudioFormat audioFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                44100,
                16,
                2,
                4,
                44100,
                false);
        AudioInputStream ais = new AudioInputStream(Files.newInputStream(out), audioFormat, Files.size(out));

        DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat, AudioSystem.NOT_SPECIFIED);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.addLineListener(event -> Debug.println(event.getType()));

        byte[] buf = new byte[8192];
        line.open(audioFormat, buf.length);
        volume(line, volume);
        line.start();
        int r;
        while (true) {
            r = ais.read(buf, 0, buf.length);
            if (r < 0) {
                break;
            }
            line.write(buf, 0, r);
        }
        line.drain();
        line.close();
    }

    // ----

    private static int convsize = 4096;

    void decode(Path ogg, Path wav) throws IOException {
        // sync and verify incoming physical bitstream
        SyncState oy;
        // take physical pages, weld into a logical stream of packets
        StreamState os;
        // one Ogg bitstream page.  Vorbis packets are inside
        Page og;
        // one raw packet of data for decode
        Packet op;

        // struct that stores all the static vorbis bitstream settings
        Info vi;
        // struct that stores all the bitstream user comments
        Comment vc;
        // central working state for the packet->PCM decoder
        DspState vd;
        // local working space for packet->PCM decode
        Block vb;

        oy = new SyncState();
        os = new StreamState();
        og = new Page();
        op = new Packet();

        vi = new Info();
        vc = new Comment();
        vd = new DspState();
        vb = new Block();

        int[] convBuffer = new int[convsize];
        byte[] buffer;
        int bytes;

        Debug.println("inputFile: " + ogg);
        InputStream inputStream = new BufferedInputStream(Files.newInputStream(ogg));
        OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(wav));

        buffer = new byte[4096];

        // Decode setup

        oy.init(); // Now we can read pages

        while (true) { // we repeat if the bitstream is chained
            boolean eos = false;

            // grab some data at the head of the stream.  We want the first page
            // (which is guaranteed to be small and only contain the Vorbis
            // stream initial header) We need the first page to get the stream
            // serialno.

            // submit a 4k block to libvorbis' Ogg layer
            bytes = inputStream.read(buffer);
            if (bytes == -1) {
                Debug.println(Level.FINE, "EOF");
                break;
            }
            oy.write(buffer, bytes);

            // Get the first page.
            int r = oy.pageOut(og);
            Debug.println(Level.FINE, "pageOut: " + r);
            if (r != 1) {
                // have we simply run out of data?  If so, we're done.
                if (bytes < 4096) {
                    break;
                }

                // error case.  Must not be Vorbis data
                throw new IllegalStateException("Input does not appear to be an Ogg bitstream.");
            }

            // Get the serial number and set up the rest of decode.
            // serialno first; use it to set up a logical stream
            os.init(og.getSerialNo());

            // extract the initial header from the first page and verify that the
            // Ogg bitstream is in fact Vorbis data

            // I handle the initial header first instead of just having the code
            // read all three Vorbis headers at once because reading the initial
            // header is an easy way to identify a Vorbis bitstream and it's
            // useful to see that functionality seperated out.

            vi.init();
            vc.init();
            if (os.pageIn(og) < 0) {
                // error; stream version mismatch perhaps
                throw new IllegalStateException("Error reading first page of Ogg bitstream data.");
            }

            if (os.packetOut(op) != 1) {
                // no page? must not be vorbis
                throw new IllegalStateException("Error reading initial header packet.");
            }

            if (vi.headerIn(vc, op) < 0) {
                // error case; not a vorbis header
                throw new IllegalStateException("This Ogg bitstream does not contain Vorbis audio data.");
            }

            // At this point, we're sure we're Vorbis.  We've set up the logical
            // (Ogg) bitstream decoder.  Get the comment and codebook headers and
            // set up the Vorbis decoder

            // The next two packets in order are the comment and codebook headers.
            // They're likely large and may span multiple pages.  Thus we reead
            // and submit data until we get our two pacakets, watching that no
            // pages are missing.  If a page is missing, error out; losing a
            // header page is the only place where missing data is fatal.

            int i = 0;
            while (i < 2) {
                while (i < 2) {
                    int result = oy.pageOut(og);
                    if (result == 0) {
                        break; // Need more data
                    }
                    // Don't complain about missing or corrupt data yet.  We'll
                    // catch it at the packet output phase
                    if (result == 1) {
                        // we can ignore any errors here
                        // as they'll also become apparent
                        // at packetout
                        os.pageIn(og);
                        while (i < 2) {
                            result = os.packetOut(op);
                            if (result == 0) {
                                break;
                            }
                            if (result < 0) {
                                // Uh oh; data at some point was corrupted or missing!
                                // We can't tolerate that in a header. Die.
                                throw new IllegalStateException("Corrupt secondary header. Exiting.");
                            }
                            vi.headerIn(vc, op);
                            i++;
                        }
                    }
                }
                // no harm in not checking before adding more
                bytes = inputStream.read(buffer);
                if (bytes == 0 && i < 2) {
                    throw new IllegalStateException("End of file before finding all Vorbis headers!");
                }
                if (bytes != -1) {
                    oy.write(buffer, bytes);
                }
            }

            // Throw the comments plus a few lines about the bitstream we're decoding
            {
                String[] astrComments = vc.getUserComments();
                for (i = 0; i < astrComments.length; i++) {
                    Debug.println(astrComments[i]);
                }
                Debug.print("\nBitstream is " + vi.getChannels() + " channel, " + vi.getRate() + " Hz\n" +
                        "Encoded by: " + vc.getVendor());
            }

            int nChannels = vi.getChannels();
            convsize = 4096 / nChannels;

            // OK, got and parsed all three headers. Initialize the Vorbis
            // packet->PCM decoder.
            vd.initSynthesis(vi); // central decode state TODO
            // local state for most of the decode so multiple block decodes can
            // proceed in parallel.  We could init multiple vorbis_block structures
            // for vd here
            vb.init(vd);
            // The rest is just a straight decode loop until end of stream
            while (!eos) {
                while (!eos) {
                    int result = oy.pageOut(og);
                    if (result == 0) {
                        break; // need more data
                    }
                    if (result < 0) { // missing or corrupt data at this page position
                        Debug.print("Corrupt or missing data in bitstream; continuing...\n");
                    } else {
                        os.pageIn(og); // can safely ignore errors at this point
                        while (true) {
                            result = os.packetOut(op);

                            if (result == 0) {
                                break; // need more data
                            }
                            if (result < 0) {
                                // missing or corrupt data at this page position
                                // no reason to complain; already complained above
                            } else {
                                // we have a packet.  Decode it
                                float[][] pcm = new float[nChannels][];
                                int samples;

                                if (vb.synthesis(op) == 0) { // test for success!
                                    vd.blockIn(vb);
                                }

                                // pcm[] is a multichannel float vector.  In stereo, for
                                // example, pcm[0] is left, and pcm[1] is right.  samples is
                                // the size of each channel.  Convert the float values
                                // (-1.<=range<=1.) to whatever PCM format and write it out

                                while ((samples = vd.pcmOut(pcm)) > 0) {
                                    boolean clipflag = false;
                                    int bout = Math.min(samples, convsize);

                                    // convert floats to 16 bit signed ints (host order) and interleave
                                    for (i = 0; i < nChannels; i++) {
                                        int ptr = i;
                                        //float *mono = pcm[i];
                                        for (int j = 0; j < bout; j++) {
                                            int val = Math.round(pcm[i][j] * 32767.0F);
                                            // might as well guard against clipping
                                            if (val > 32767) {
                                                val = 32767;
                                                clipflag = true;
                                            }
                                            if (val < -32768) {
                                                val = -32768;
                                                clipflag = true;
                                            }
                                            convBuffer[ptr] = val;
                                            ptr += nChannels;
                                        }
                                    }

                                    if (clipflag) {
                                        Debug.print(Level.FINER, "Clipping in frame " + vd.getSequence() + "\n");
                                    }
                                    byte[] abBuffer = new byte[2 * nChannels * bout];
                                    int byteOffset = 0;
                                    boolean bigEndian = false;
                                    for (int nSample = 0; nSample < nChannels * bout; nSample++) {
                                        int sample = convBuffer[nSample];
                                        if (bigEndian) {
                                            abBuffer[byteOffset++] = (byte) (sample >> 8);
                                            abBuffer[byteOffset++] = (byte) (sample & 0xFF);
                                        } else {
                                            abBuffer[byteOffset++] = (byte) (sample & 0xFF);
                                            abBuffer[byteOffset++] = (byte) (sample >> 8);
                                        }
                                    }
                                    outputStream.write(abBuffer);

                                    // tell libvorbis how many samples we actually consumed
                                    vd.read(bout);
                                }
                            }
                        }
                        if (og.isEos()) {
                            eos = true;
                        }
                    }
                }
                if (!eos) {
                    bytes = inputStream.read(buffer);
                    if (bytes > 0) {
                        oy.write(buffer, bytes);
                    }
                    if (bytes == 0 || bytes == -1) {
                        eos = true;
                    }
                }
            }

            // clean up this logical bitstream; before exit we see if we're
            // followed by another [chained]

            os.clear();

            // ogg_page and ogg_packet structs always point to storage in
            // libvorbis. They're never freed or manipulated directly
            vb.clear();
            vd.clear();
            vc.clear();
            vi.clear(); // must be called last
        }

        // OK, clean up the framer
        oy.clear();

        outputStream.flush();
        outputStream.close();

        Debug.print("Done.\n");
    }

    public static void main(String[] args) throws IOException {
        Path wav = Path.of(args[0]);
        Path ogg = Path.of(args[1]);
        if (!Files.exists(ogg.getParent())) {
            Files.createDirectories(ogg.getParent());
        }
        VorbisDecoder app = new VorbisDecoder();
        app.decode(wav, ogg);
    }
}

/* VorbisDecoder.java */
