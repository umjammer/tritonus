/*
 *  Copyright (c) 1999, 2000 by Matthias Pfisterer
 *
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.tritonus.share.sampled.convert;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.spi.FormatConversionProvider;

import org.tritonus.share.sampled.AudioFormats;

import static java.lang.System.getLogger;


/**
 * Base class for all conversion providers of Tritonus.
 *
 * @author Matthias Pfisterer
 */
public abstract class TFormatConversionProvider extends FormatConversionProvider {

    private static final Logger logger= getLogger("org.tritonus.TraceAudioConverter");
    
    protected static final AudioFormat.Encoding[] EMPTY_ENCODING_ARRAY = new AudioFormat.Encoding[0];
    protected static final AudioFormat[] EMPTY_FORMAT_ARRAY = new AudioFormat[0];

    // $$fb2000-10-04: use AudioSystem.NOT_SPECIFIED for all fields.
    @Override
    public AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream audioInputStream) {
        AudioFormat sourceFormat = audioInputStream.getFormat();
        AudioFormat targetFormat = new AudioFormat(
                targetEncoding,
                AudioSystem.NOT_SPECIFIED, // sample rate
                AudioSystem.NOT_SPECIFIED, // sample size in bits
                AudioSystem.NOT_SPECIFIED, // channels
                AudioSystem.NOT_SPECIFIED, // frame size
                AudioSystem.NOT_SPECIFIED, // frame rate
                sourceFormat.isBigEndian()); // big endian
        logger.log(Level.TRACE, "begin");
        logger.log(Level.TRACE, "trying to convert to " + targetFormat);
        return getAudioInputStream(targetFormat, audioInputStream);
    }

    /**
     * WARNING: this method uses <code>getTargetFormats(AudioFormat.Encoding, AudioFormat)</code>
     * which may create infinite loops if the latter is overwritten.
     * <p>
     * This method is overwritten here to make use of org.tritonus.share.sampled.AudioFormats.matches
     * and is considered temporary until AudioFormat#matches is corrected in the JavaSound API.
     * <p>
     * $$mp: if we decide to use getMatchingFormat(), this method should be
     * implemented by simply calling getMatchingFormat() and comparing the
     * result against null.
     */
    @Override
    public boolean isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat) {
        logger.log(Level.TRACE, "begin");
        logger.log(Level.TRACE, "class: " + getClass().getName());
        logger.log(Level.TRACE, "checking if conversion possible");
        logger.log(Level.TRACE, "from: " + sourceFormat);
        logger.log(Level.TRACE, "to: " + targetFormat);
        AudioFormat[] targetFormats = getTargetFormats(targetFormat.getEncoding(), sourceFormat);
        for (AudioFormat _targetFormat : targetFormats) {
            logger.log(Level.TRACE, "checking against possible target format: " + _targetFormat);

            if (_targetFormat != null && AudioFormats.matches(_targetFormat, targetFormat)) {
                logger.log(Level.TRACE, "<result=true");

                return true;
            }
        }
        logger.log(Level.TRACE, "<result=false");

        return false;
    }

    /**
     * WARNING: this method uses <code>getTargetFormats(AudioFormat.Encoding, AudioFormat)</code>
     * which may create infinite loops if the latter is overwritten.
     * <p>
     * This method is overwritten here to make use of org.tritonus.share.sampled.AudioFormats.matches
     * and is considered temporary until AudioFormat.matches is corrected in the JavaSound API.
     */
    public AudioFormat getMatchingFormat(AudioFormat targetFormat, AudioFormat sourceFormat) {
        logger.log(Level.TRACE, "begin");
        logger.log(Level.TRACE, "class: " + getClass().getName());
        logger.log(Level.TRACE, "checking if conversion possible");
        logger.log(Level.TRACE, "from: " + sourceFormat);
        logger.log(Level.TRACE, "to: " + targetFormat);
        AudioFormat[] targetFormats = getTargetFormats(targetFormat.getEncoding(), sourceFormat);
        for (AudioFormat aTargetFormat : targetFormats) {
            logger.log(Level.TRACE, "checking against possible target format: " + aTargetFormat);

            if (aTargetFormat != null && AudioFormats.matches(aTargetFormat, targetFormat)) {
                logger.log(Level.TRACE, "<result=true");

                return aTargetFormat;
            }
        }
        logger.log(Level.TRACE, "<result=false");

        return null;
    }
}
