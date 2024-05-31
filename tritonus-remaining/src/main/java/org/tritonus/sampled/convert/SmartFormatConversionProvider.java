/*
 *  Copyright (c) 2000 - 2004 by Matthias Pfisterer
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

package org.tritonus.sampled.convert;

import java.util.HashSet;
import java.util.Set;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.tritonus.share.sampled.convert.TFormatConversionProvider;


/**
 * "Smart" formatConversionProvider.
 * <p>
 * This FormatConversionProvider tries to find combinations of other
 * FormatConversionProviders so that the chain of these providers fulfill the request for a
 * format conversion given to this provider.
 * <p>
 * Name suggested by Florian: MetaFormatConversionProvider
 * Additional explanation:
 * > I took a quick look at the SmartConverter, why are you doing
 * > that with the threads? A new thread isn't used in recursion?
 * > And otherwise you could do this with synchronized or a real lock?
 * <p>
 * You are in recursion regarding the same converter search
 * in the same thread; I'm currently taking advantage of this feature. It can
 * but the application program from several threads at the same time
 * Request converter. These calls all go into the same SmartF.C.P.
 * Object (there is only one). The methods of the converter (this applies to
 * All of them must therefore be reentrant. The alternative would be a global one
 * Lock. But I don't think that's acceptable. With my sound machine
 * for example, that would lead to problems: it is necessary that?
 * Playing multiple channels on the fly is converted. A global lock
 * would lead to delays in playback. The "simple" version of the
 * Recursion detection (without taking threads into account) only needs one
 * simple flag that is set when no more recursion takes place
 * should. This flag is used in my implementation with the hash table
 * realized; it simulates a thread-local behavior of this flag.
 * Understood? I realize you need two knots in your brain to do that
 * understand...
 *
 * @author Matthias Pfisterer
 */
public class SmartFormatConversionProvider extends TFormatConversionProvider {

    /**
     * Stores the threads currently blocked.
     * To avoid recursion, this class stores which threads have already "passed"
     * methods of this class once. On entry of a method prone to recursion, it is
     * checked if the current thread is in the set. If so, this indicates a recursion
     * and the method will return immediately. If not, the current thread is entered
     * this data structure, so that further invocations can detect a recursion. On
     * exit of this method, it is removed from this data structure to indicate it is
     * "free".
     */
    private final Set<Thread> blockedThreads;

    public SmartFormatConversionProvider() {
        blockedThreads = new HashSet<>();
    }

    // TODO can use AudioSystem to return all source encodings? (don't forget to block!)
    @Override
    public AudioFormat.Encoding[] getSourceEncodings() {
        return EMPTY_ENCODING_ARRAY;
    }

    // TODO can use AudioSystem to return all target encodings? (don't forget to block!)
    @Override
    public AudioFormat.Encoding[] getTargetEncodings() {
        return EMPTY_ENCODING_ARRAY;
    }

    @Override
    public AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat) { // TODO
        return null;
    }

    @Override
    public boolean isConversionSupported(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat) {
        return false;
    }

    @Override
    public AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat) {
        return null;
    }

    @Override
    public boolean isConversionSupported(AudioFormat targetFormat, AudioFormat sourceFormat) {
        if (isCurrentThreadBlocked()) {
            return false;
        }
        AudioFormat[] intermediateFormats = getIntermediateFormats(sourceFormat, targetFormat);
        return intermediateFormats != null;
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream audioInputStream) {
        return null;
    }

    @Override
    public AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream audioInputStream) {
        return null;
    }

    /**
     * Search for converter chain.
     *
     * @return an array of intermediate formats (possibly of length 0 if it's possible
     * to do the conversion in one step) or null if the conversion is not
     * possible.
     */
    private AudioFormat[] getIntermediateFormats(AudioFormat sourceFormat, AudioFormat targetFormat) {
        AudioFormat.Encoding sourceEncoding = sourceFormat.getEncoding();
        AudioFormat.Encoding targetEncoding = targetFormat.getEncoding();
        blockCurrentThread();
        boolean directConversionPossible = AudioSystem.isConversionSupported(targetFormat, sourceFormat);
        unblockCurrentThread();
        if (directConversionPossible) {
            return EMPTY_FORMAT_ARRAY;
        } else if (isPCM(sourceEncoding) && isPCM(targetEncoding)) {
            // The SR converter is not yet implemented. The PCM2PCM converter
            // should handle all other cases.
            return null;
            // eventually block
        } else if (!isPCM(sourceEncoding)) {
            AudioFormat intermediateFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    sourceFormat.getSampleSizeInBits(),
                    sourceFormat.getChannels(),
                    AudioSystem.NOT_SPECIFIED,
                    sourceFormat.getSampleRate(),
                    true);
            blockCurrentThread();
            AudioFormat[] preIntermediateFormats = getIntermediateFormats(sourceFormat, intermediateFormat);
            unblockCurrentThread();
            AudioFormat[] postIntermediateFormats = getIntermediateFormats(intermediateFormat, targetFormat);
            if (preIntermediateFormats != null && postIntermediateFormats != null) {
                AudioFormat[] intermediateFormats = new AudioFormat[preIntermediateFormats.length + 1 + postIntermediateFormats.length];
                System.arraycopy(preIntermediateFormats, 0, intermediateFormats, 0, preIntermediateFormats.length);
                intermediateFormats[preIntermediateFormats.length] = intermediateFormat;
                System.arraycopy(postIntermediateFormats, 0, intermediateFormats, preIntermediateFormats.length, postIntermediateFormats.length);
                return intermediateFormats;
            } else {
                return null;
            }
        } else if (!isPCM(targetEncoding)) {
            AudioFormat intermediateFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    targetFormat.getSampleRate(),
                    targetFormat.getSampleSizeInBits(),
                    targetFormat.getChannels(),
                    AudioSystem.NOT_SPECIFIED,
                    targetFormat.getSampleRate(),
                    true);
            AudioFormat[] preIntermediateFormats = getIntermediateFormats(sourceFormat, intermediateFormat);
            blockCurrentThread();
            AudioFormat[] postIntermediateFormats = getIntermediateFormats(intermediateFormat, targetFormat);
            unblockCurrentThread();
            if (preIntermediateFormats != null && postIntermediateFormats != null) {
                AudioFormat[] intermediateFormats = new AudioFormat[preIntermediateFormats.length + 1 + postIntermediateFormats.length];
                System.arraycopy(preIntermediateFormats, 0, intermediateFormats, 0, preIntermediateFormats.length);
                intermediateFormats[preIntermediateFormats.length] = intermediateFormat;
                System.arraycopy(postIntermediateFormats, 0, intermediateFormats, preIntermediateFormats.length, postIntermediateFormats.length);
                return intermediateFormats;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    // General helper methods.

    private static boolean isPCM(AudioFormat.Encoding encoding) {
        return encoding.equals(AudioFormat.Encoding.PCM_SIGNED) || encoding.equals(Encoding.PCM_UNSIGNED);
    }

    protected static boolean isSignedPCM(AudioFormat.Encoding encoding) {
        return encoding.equals(AudioFormat.Encoding.PCM_SIGNED);
    }

    // Methods for recursion detection/blocking.

    private boolean isCurrentThreadBlocked() {
        return blockedThreads.contains(Thread.currentThread());
    }

    private void blockCurrentThread() {
        blockedThreads.add(Thread.currentThread());
    }

    private void unblockCurrentThread() {
        blockedThreads.remove(Thread.currentThread());
    }
}
