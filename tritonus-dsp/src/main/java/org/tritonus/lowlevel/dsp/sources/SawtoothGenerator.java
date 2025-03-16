package org.tritonus.lowlevel.dsp.sources;

/**
 * Generator for sawtooth waveform.
 *
 * @author Matthias Pfisterer
 * @see SineGenerator
 * @see SquareGenerator
 * @see TriangleGenerator
 */
public class SawtoothGenerator extends AbstractPeriodicGenerator {

    public SawtoothGenerator(float sampleRate, int channelCount) {
        super(sampleRate, channelCount);
    }

    @Override
    protected float generateSample(float periodPosition) {
        float value;
        if (periodPosition < 0.5f) {
            value = 2.0f * periodPosition;
        } else {
            value = 2.0f * (periodPosition - 1.0f);
        }
        return value;
    }
}
