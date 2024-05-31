package org.tritonus.lowlevel.dsp.sources;

/**
 * Generator for sine waveform.
 *
 * @author Matthias Pfisterer
 * @see SawtoothGenerator
 * @see SquareGenerator
 * @see TriangleGenerator
 */
public class SineGenerator extends AbstractPeriodicGenerator {

    public SineGenerator(float sampleRate, int channelCount) {
        super(sampleRate, channelCount);
    }

    @Override
    protected float generateSample(float periodPosition) {
        // TODO * Math.PI ??
        return (float) Math.sin(2.0 * periodPosition);
    }
}
