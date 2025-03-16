package org.tritonus.lowlevel.dsp.sources;

/**
 * Generator for square waveform.
 *
 * @author Matthias Pfisterer
 * @see SawtoothGenerator
 * @see SineGenerator
 * @see TriangleGenerator
 */
public class SquareGenerator extends AbstractPeriodicGenerator {

    public SquareGenerator(float sampleRate, int channelCount) {
        super(sampleRate, channelCount);
    }

    @Override
    protected float generateSample(float periodPosition) {
        return (periodPosition < 0.5f) ? 1.0f : -1.0f;
    }
}
