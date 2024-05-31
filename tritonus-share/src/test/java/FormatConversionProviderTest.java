import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.spi.FormatConversionProvider;

import static javax.sound.sampled.AudioSystem.NOT_SPECIFIED;


/**
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
        String providerClassName = args[0];
        FormatConversionProvider provider = getProvider(providerClassName);

        outSeparator();
        out("FormatConversionProvider: " + provider.getClass().getName());
        outSeparator();

        Encoding[] sourceEncodings = provider.getSourceEncodings();
        out("Source Encodings:");
        out(sourceEncodings);
        outSeparator();

        Encoding[] targetEncodings = provider.getTargetEncodings();
        out("Target Encodings:");
        out(targetEncodings);
        outSeparator();

        for (Encoding sourceEncoding : sourceEncodings) {
            outTargetEncodingsForFormat(provider, sourceEncoding);
        }

        // test getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream sourceStream)
        for (Encoding sourceEncoding : sourceEncodings) {
            AudioFormat[] sourceFormats = createAudioFormatsForEncoding(sourceEncoding);
            for (AudioFormat sourceFormat : sourceFormats) {
                for (Encoding targetEncoding : targetEncodings) {
                    boolean supported = provider.isConversionSupported(targetEncoding, sourceFormat);
                    out("conversion supported " + getAudioFormatString(sourceFormat) + " --> " + targetEncoding + ": " + supported);
                }
            }
        }
    }

    private static void outTargetEncodingsForFormat(FormatConversionProvider provider, Encoding sourceEncoding) {
        AudioFormat audioFormat = new AudioFormat(
                sourceEncoding, NOT_SPECIFIED, NOT_SPECIFIED, NOT_SPECIFIED, NOT_SPECIFIED, NOT_SPECIFIED, false);
        Encoding[] targetEncodingsForFormat = provider.getTargetEncodings();
        out("Target Encodings for " + getAudioFormatString(audioFormat));
        out(targetEncodingsForFormat);
        outSeparator();
    }

    private static AudioFormat[] createAudioFormatsForEncoding(Encoding encoding) {
        List<AudioFormat> formats = new ArrayList<>();
        if (encoding == Encoding.PCM_SIGNED || encoding == Encoding.PCM_UNSIGNED) {
            for (int commonSampleSize : COMMON_SAMPLE_SIZES) {
                createAudioFormatsForEncodingSub(formats, encoding, commonSampleSize);
            }
        } else if (encoding == Encoding.ALAW || encoding == Encoding.ULAW) {
            createAudioFormatsForEncodingSub(formats, encoding, 8);
        }
        return formats.toArray(new AudioFormat[0]);
    }

    private static void createAudioFormatsForEncodingSub(List<AudioFormat> formats,
                                                         Encoding encoding,
                                                         int sampleSizeInBits) {
        for (float commonSampleRate : COMMON_SAMPLE_RATES) {
            for (int commonChannel : COMMON_CHANNELS) {
                for (int _endianess = 0; _endianess <= 1; _endianess++) {
                    boolean endianess = (_endianess == 0);
                    AudioFormat format = new AudioFormat(
                            encoding,
                            commonSampleRate,
                            sampleSizeInBits,
                            commonChannel,
                            sampleSizeInBits * commonChannel / 8,
                            commonSampleRate,
                            endianess);
                    formats.add(format);
                }
            }
        }
    }

    private static FormatConversionProvider getProvider(String providerClassName) throws Exception {
        Class<?> providerClass = Class.forName(providerClassName);
        var provider = (FormatConversionProvider) providerClass.getDeclaredConstructor().newInstance();
        return provider;
    }

    private static String getAudioFormatString(AudioFormat audioFormat) {
        return getAudioFormatStringImpl0(audioFormat);
    }

    private static String getAudioFormatStringImpl0(AudioFormat audioFormat) {
        String buf = audioFormat.getEncoding().toString() +
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
        return buf;
    }

    private static String getAudioFormatStringImpl1(AudioFormat audioFormat) {
        String buf = "enc: " +
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
        return buf;
    }

    private static void out(Encoding[] encodings) {
        for (Encoding aEncoding : encodings) {
            out(aEncoding.toString());
        }
    }

    private static void outSeparator() {
        out("------------------------------------------------------------------------------");
    }

    private static void out(String message) {
        System.out.println(message);
    }
}
