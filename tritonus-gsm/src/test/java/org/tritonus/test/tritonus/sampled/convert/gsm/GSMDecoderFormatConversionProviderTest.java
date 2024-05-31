package org.tritonus.test.tritonus.sampled.convert.gsm;

import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.spi.FormatConversionProvider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tritonus.sampled.convert.gsm.GSMDecoderFormatConversionProvider;

import static javax.sound.sampled.AudioFormat.Encoding.ALAW;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED;
import static javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED;
import static javax.sound.sampled.AudioFormat.Encoding.ULAW;
import static javax.sound.sampled.AudioSystem.NOT_SPECIFIED;


public class GSMDecoderFormatConversionProviderTest extends AbstractGsmFormatConversionProviderTest {

    @BeforeEach
    public void setUp() throws Exception {
        setFormatConversionProvider(new GSMDecoderFormatConversionProvider());
    }

    /**
     * Test for {@link FormatConversionProvider#getSourceEncodings()}.
     */
    @Test
    public void testGetSourceEncodings() {
        List<Encoding> expectedSupportedEncodings = List.of(TOAST_GSM_ENCODING, MS_GSM_ENCODING);
        List<Encoding> expectedUnsupportedEncodings = List.of(PCM_SIGNED, PCM_UNSIGNED, ALAW, ULAW);
        testGetEncodings(false, expectedSupportedEncodings, expectedUnsupportedEncodings);
    }

    /**
     * Test for {@link FormatConversionProvider#getTargetEncodings()}.
     */
    @Test
    public void testGetTargetEncodings() {
        List<Encoding> expectedSupportedEncodings = List.of(PCM_SIGNED);
        List<Encoding> expectedUnsupportedEncodings = List.of(
                PCM_UNSIGNED, TOAST_GSM_ENCODING, MS_GSM_ENCODING, ALAW, ULAW);
        testGetEncodings(true, expectedSupportedEncodings, expectedUnsupportedEncodings);
    }

    /**
     * Test for {@link FormatConversionProvider#isSourceEncodingSupported(Encoding)}.
     */
    @Test
    public void testIsSourceEncodingSupportedEncoding() {
        List<Encoding> expectedSupportedEncodings = List.of(TOAST_GSM_ENCODING, MS_GSM_ENCODING);
        List<Encoding> expectedUnsupportedEncodings = List.of(PCM_SIGNED, PCM_UNSIGNED, ALAW, ULAW);

        testIsEncodingSupportedEncoding(false, expectedSupportedEncodings, expectedUnsupportedEncodings);
    }

    /**
     * Test for {@link FormatConversionProvider#isSourceEncodingSupported(Encoding)}.
     */
    @Test
    public void testIsTargetEncodingSupportedEncoding() {
        List<Encoding> expectedSupportedEncodings = List.of(PCM_SIGNED);
        List<Encoding> expectedUnsupportedEncodings = List.of(
                TOAST_GSM_ENCODING, MS_GSM_ENCODING, PCM_UNSIGNED, ALAW, ULAW);

        testIsEncodingSupportedEncoding(true, expectedSupportedEncodings, expectedUnsupportedEncodings);
    }

    /**
     * Test for {@link FormatConversionProvider#getTargetEncodings(AudioFormat)}.
     */
    @Test
    public void testGetTargetEncodingsAudioFormat() {
        testGetTargetEncodingsAudioFormat(
                new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, ALL, 1, ALL, 50.0F, true),
                List.of(PCM_SIGNED),
                List.of(PCM_UNSIGNED, ALAW, ULAW, TOAST_GSM_ENCODING, MS_GSM_ENCODING));
        testGetTargetEncodingsAudioFormat(
                new AudioFormat(MS_GSM_ENCODING, 8000.0F, ALL, 1, ALL, 25.0F, true),
                List.of(PCM_SIGNED),
                List.of(PCM_UNSIGNED, ALAW, ULAW, TOAST_GSM_ENCODING, MS_GSM_ENCODING));
    }

    /**
     * Test for {@link FormatConversionProvider#isConversionSupported(Encoding, AudioFormat)}.
     */
    @Test
    public void testIsConversionSupportedEncodingAudioFormat() {
        testIsConversionSupportedEncodingAudioFormat(
                new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, true),
                EMPTY_ENCODING_LIST,
                List.of(TOAST_GSM_ENCODING, MS_GSM_ENCODING, PCM_SIGNED, PCM_UNSIGNED, ALAW, ULAW));
        testIsConversionSupportedEncodingAudioFormat(
                new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, 16, 1, 33, 50.0F, true),
                List.of(PCM_SIGNED),
                List.of(TOAST_GSM_ENCODING, MS_GSM_ENCODING, PCM_UNSIGNED, ALAW, ULAW));
        testIsConversionSupportedEncodingAudioFormat(
                new AudioFormat(MS_GSM_ENCODING, 8000.0F, 16, 1, 65, 25.0F, true),
                List.of(PCM_SIGNED),
                List.of(TOAST_GSM_ENCODING, MS_GSM_ENCODING, PCM_UNSIGNED, ALAW, ULAW));
    }

    /**
     * Test for {@link FormatConversionProvider#getTargetFormats(Encoding, AudioFormat)}.
     */
    @Test
    public void testGetTargetFormatsEncodingAudioFormat() {
        testGetTargetFormatsEncodingAudioFormat(PCM_SIGNED,
                new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, NOT_SPECIFIED, 1, 33, 50.0F, true),
                List.of(new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, true),
                        new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, true)),
                List.of(new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, NOT_SPECIFIED, 1, 33, 50.0F, false),
                        new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, NOT_SPECIFIED, 1, 33, 50.0F, true),
                        new AudioFormat(MS_GSM_ENCODING, 8000.0F, NOT_SPECIFIED, 1, ALL, ALL, false),
                        new AudioFormat(MS_GSM_ENCODING, 8000.0F, NOT_SPECIFIED, 1, ALL, ALL, true))
        );
        testGetTargetFormatsEncodingAudioFormat(PCM_SIGNED,
                new AudioFormat(MS_GSM_ENCODING, 8000.0F, NOT_SPECIFIED, 1, 65, 25.0F, true),
                List.of(new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, true),
                        new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, true)),
                List.of(new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, NOT_SPECIFIED, 1, 33, 50.0F, false),
                        new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, NOT_SPECIFIED, 1, 33, 50.0F, true),
                        new AudioFormat(MS_GSM_ENCODING, 8000.0F, NOT_SPECIFIED, 1, ALL, ALL, false),
                        new AudioFormat(MS_GSM_ENCODING, 8000.0F, NOT_SPECIFIED, 1, ALL, ALL, true))
        );
    }

    /**
     * Test for {@link FormatConversionProvider#isConversionSupported(AudioFormat, AudioFormat)}.
     */
    @Test
    public void testIsConversionSupportedAudioFormatAudioFormat() {
        testIsConversionSupportedAudioFormatAudioFormat(
                new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, 16, 1, 33, 50.0F, true),
                List.of(new AudioFormat(PCM_SIGNED, _8KHZ, 16, 1, 2, _8KHZ, true),
                        new AudioFormat(PCM_SIGNED, _8KHZ, 16, 1, 2, _8KHZ, false)),
                List.of(new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, ALL, 1, 33, 50.0F, true),
                        new AudioFormat(MS_GSM_ENCODING, 8000.0F, ALL, 1, 33, 50.0F, true),
                        new AudioFormat(PCM_UNSIGNED, _8KHZ, 16, 1, 2, _8KHZ, true),
                        new AudioFormat(ALAW, _8KHZ, 16, 1, 2, _8KHZ, true),
                        new AudioFormat(ULAW, _8KHZ, 16, 1, 2, _8KHZ, true))
        );
        testIsConversionSupportedAudioFormatAudioFormat(
                new AudioFormat(MS_GSM_ENCODING, 8000.0F, 16, 1, 65, 25.0F, true),
                List.of(new AudioFormat(PCM_SIGNED, _8KHZ, 16, 1, 2, _8KHZ, true),
                        new AudioFormat(PCM_SIGNED, _8KHZ, 16, 1, 2, _8KHZ, false)),
                List.of(new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, ALL, 1, 33, 50.0F, true),
                        new AudioFormat(MS_GSM_ENCODING, 8000.0F, ALL, 1, 33, 50.0F, true),
                        new AudioFormat(PCM_UNSIGNED, _8KHZ, 16, 1, 2, _8KHZ, true),
                        new AudioFormat(ALAW, _8KHZ, 16, 1, 2, _8KHZ, true),
                        new AudioFormat(ULAW, _8KHZ, 16, 1, 2, _8KHZ, true))
        );
        testIsConversionSupportedAudioFormatAudioFormat(
                new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, true),
                EMPTY_AUDIOFORMAT_LIST,
                List.of(new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, ALL, 1, 33, 50.0F, true),
                        new AudioFormat(MS_GSM_ENCODING, 8000.0F, ALL, 1, 33, 50.0F, true),
                        new AudioFormat(PCM_SIGNED, _8KHZ, 16, 1, 2, _8KHZ, true),
                        new AudioFormat(PCM_UNSIGNED, _8KHZ, 16, 1, 2, _8KHZ, true),
                        new AudioFormat(ALAW, _8KHZ, 16, 1, 2, _8KHZ, true),
                        new AudioFormat(ULAW, _8KHZ, 16, 1, 2, _8KHZ, true))
        );
    }

    /**
     * Test for {@link FormatConversionProvider#getAudioInputStream(Encoding, AudioInputStream)}.
     */
    @Test
    public void testGetAudioInputStreamEncodingAudioInputStream() {
        testGetAudioInputStreamEncoding(
                PCM_SIGNED,
                new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, ALL, 1, 33, 50, true),
                NOT_SPECIFIED,
                new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, false),
                NOT_SPECIFIED);
        testGetAudioInputStreamEncoding(
                PCM_SIGNED,
                new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, ALL, 1, 33, 50, true),
                134,
                new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, false),
                160 * 134);
        testGetAudioInputStreamEncoding(
                PCM_SIGNED,
                new AudioFormat(MS_GSM_ENCODING, 8000.0F, ALL, 1, 65, 25, true),
                NOT_SPECIFIED,
                new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, false),
                NOT_SPECIFIED);
        testGetAudioInputStreamEncoding(
                PCM_SIGNED,
                new AudioFormat(MS_GSM_ENCODING, 8000.0F, ALL, 1, 65, 25, true),
                134,
                new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, false),
                320 * 134);
    }

    /**
     * Test for {@link FormatConversionProvider#getAudioInputStream(AudioFormat, AudioInputStream)}.
     */
    @Test
    public void testGetAudioInputStreamAudioFormatAudioInputStream() {
        testGetAudioInputStreamAudioFormat(
                new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, false),
                new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, ALL, 1, 33, 50, true),
                NOT_SPECIFIED,
                new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, false),
                NOT_SPECIFIED);
        testGetAudioInputStreamAudioFormat(
                new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, false),
                new AudioFormat(TOAST_GSM_ENCODING, 8000.0F, ALL, 1, 33, 50, true),
                134,
                new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, false),
                160 * 134);
        testGetAudioInputStreamAudioFormat(
                new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, false),
                new AudioFormat(MS_GSM_ENCODING, 8000.0F, ALL, 1, 65, 25, true),
                NOT_SPECIFIED,
                new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, false),
                NOT_SPECIFIED);
        testGetAudioInputStreamAudioFormat(
                new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, false),
                new AudioFormat(MS_GSM_ENCODING, 8000.0F, ALL, 1, 65, 25, true),
                134,
                new AudioFormat(PCM_SIGNED, 8000.0F, 16, 1, 2, 8000.0F, false),
                320 * 134);
    }
}
