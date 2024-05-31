package org.tritonus.lowlevel.dsp.sources;

/**
 * Generator for triangle waveform.
 *
 * @author Matthias Pfisterer
 * @see SawtoothGenerator
 * @see SineGenerator
 * @see SquareGenerator
 */
public class TriangleGenerator extends AbstractPeriodicGenerator {

    public TriangleGenerator(float sampleRate, int channelCount) {
        super(sampleRate, channelCount);
    }

    @Override
    protected float generateSample(float periodPosition) {
        float value;
        if (periodPosition < 0.25f) {
            value = 4.0f * periodPosition;
        } else if (periodPosition < 0.75f) {
            value = -4.0f * (periodPosition - 0.5f);
        } else {
            value = 4.0f * (periodPosition - 1.0f);
        }
        return value;
    }
}
