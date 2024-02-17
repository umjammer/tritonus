import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.FormatConversionProvider;


/*
 * FormatConversionProviderTest
 */
public class FormatConversionProviderTest {

    private static final float[] COMMON_SAMPLE_RATES = {
            8000.0F,
            11025.0F,
            16000.0F,
            22050.0F,
            32000.0F,
            44100.0F,
            48000.0F,
            96000.0F,
    };

    private static final int[] COMMON_SAMPLE_SIZES = {
            8, 16, 24, 32,
    };

    private static final int[] COMMON_CHANNELS = {
            1, 2,
    };

    public static void main(String[] args) throws Exception {
        String strProviderClassName = args[0];
        FormatConversionProvider provider = getProvider(strProviderClassName);

        outSeparator();
        out("FormatConversionProvider: " + provider.getClass().getName());
        outSeparator();

        Encoding[] aSourceEncodings = provider.getSourceEncodings();
        out("Source Encodings:");
        out(aSourceEncodings);
        outSeparator();

        Encoding[] aTargetEncodings = provider.getTargetEncodings();
        out("Target Encodings:");
        out(aTargetEncodings);
        outSeparator();

        for (Encoding aSourceEncoding : aSourceEncodings) {
            outTargetEncodingsForFormat(provider, aSourceEncoding);
        }

        // test getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream)
        for (Encoding sourceEncoding : aSourceEncodings) {
            AudioFormat[] aSourceFormats = createAudioFormatsForEncoding(sourceEncoding);
            for (AudioFormat sourceFormat : aSourceFormats) {
                for (Encoding targetEncoding : aTargetEncodings) {
                    boolean bSupported = provider.isConversionSupported(targetEncoding, sourceFormat);
                    out("conversion supported " + getAudioFormatString(sourceFormat) + " --> " + targetEncoding + ": " + bSupported);
                }
            }
        }
    }

    private static void outTargetEncodingsForFormat(
            FormatConversionProvider provider, Encoding sourceEncoding) {
        AudioFormat audioFormat = new AudioFormat(
                sourceEncoding,
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED,
                AudioSystem.NOT_SPECIFIED,
                false);
        Encoding[] aTargetEncodingsForFormat = provider.getTargetEncodings();
        out("Target Encodings for " + getAudioFormatString(audioFormat));
        out(aTargetEncodingsForFormat);
        outSeparator();
    }

    private static AudioFormat[] createAudioFormatsForEncoding(Encoding encoding) {
        List<AudioFormat> formats = new ArrayList<>();
        if (encoding == Encoding.PCM_SIGNED ||
                encoding == Encoding.PCM_UNSIGNED) {
            for (int commonSampleSize : COMMON_SAMPLE_SIZES) {
                createAudioFormatsForEncodingSub(formats, encoding,
                        commonSampleSize);
            }
        } else if (encoding == Encoding.ALAW ||
                encoding == Encoding.ULAW) {
            createAudioFormatsForEncodingSub(formats, encoding, 8);
        }
        return formats.toArray(new AudioFormat[0]);
    }

    private static void createAudioFormatsForEncodingSub(List<AudioFormat> formats,
                                                         Encoding encoding,
                                                         int nSampleSizeInBits) {
        for (float commonSampleRate : COMMON_SAMPLE_RATES) {
            for (int commonChannel : COMMON_CHANNELS) {
                for (int nEndianess = 0; nEndianess <= 1; nEndianess++) {
                    boolean bEndianess = (nEndianess == 0);
                    AudioFormat format = new AudioFormat(
                            encoding,
                            commonSampleRate,
                            nSampleSizeInBits,
                            commonChannel,
                            nSampleSizeInBits * commonChannel / 8,
                            commonSampleRate,
                            bEndianess);
                    formats.add(format);
                }
            }
        }
    }

    private static FormatConversionProvider getProvider(String strProviderClassName) throws Exception {
        Class<?> providerClass = Class.forName(strProviderClassName);
        FormatConversionProvider provider = (FormatConversionProvider) providerClass.getDeclaredConstructor().newInstance();
        return provider;
    }

    private static String getAudioFormatString(AudioFormat audioFormat) {
        return getAudioFormatStringImpl0(audioFormat);
    }

    private static String getAudioFormatStringImpl0(AudioFormat audioFormat) {
        String strBuf = audioFormat.getEncoding().toString() +
                ", " +
                audioFormat.getSampleRate() +
                " Hz , " +
                audioFormat.getSampleSizeInBits() +
                " bit , " +
                audioFormat.getChannels() +
                " ch, " +
                audioFormat.getFrameSize() +
                " byte, " +
                audioFormat.getFrameRate() +
                " Hz, " +
                (audioFormat.isBigEndian() ? "BE" : "le");
        return strBuf;
    }

    private static String getAudioFormatStringImpl1(AudioFormat audioFormat) {
        String strBuf = "enc: " +
                audioFormat.getEncoding().toString() +
                ", sr: " +
                audioFormat.getSampleRate() +
                ", ss: " +
                audioFormat.getSampleSizeInBits() +
                ", ch: " +
                audioFormat.getChannels() +
                ", fs: " +
                audioFormat.getFrameSize() +
                ", fr: " +
                audioFormat.getFrameRate() +
                (audioFormat.isBigEndian() ? ", BE" : ", le");
        return strBuf;
    }

    private static void out(Encoding[] aEncodings) {
        for (Encoding aEncoding : aEncodings) {
            out(aEncoding.toString());
        }
    }

    private static void outSeparator() {
        out("------------------------------------------------------------------------------");
    }

    private static void out(String strMessage) {
        System.out.println(strMessage);
    }
}


