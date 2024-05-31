package org.tritonus.dsp.processor;

import org.tritonus.dsp.interfaces.FloatSampleProcessor;
import org.tritonus.lowlevel.dsp.FIR;
import org.tritonus.lowlevel.dsp.FIRDirectFormFilterDescription;
import org.tritonus.share.sampled.FloatSampleBuffer;


public class FIRFilterProcessor implements FloatSampleProcessor {

    private final FIR[] filters;

    public FIRFilterProcessor(int channels, FIRDirectFormFilterDescription filterDescription) {
        filters = new FIR[channels];
        for (int channel = 0; channel < channels; channel++) {
            filters[channel] = new FIR(filterDescription);
        }
    }

    public void setFilterDescription(FIRDirectFormFilterDescription filterDescription) {
        for (FIR filter : filters) {
            filter.setFilterDescription(filterDescription);
        }
    }

    private int getChannelCount() {
        return filters.length;
    }

    @Override
    public void process(FloatSampleBuffer buffer) {
        if (getChannelCount() != buffer.getChannelCount()) {
            throw new IllegalArgumentException(
                    "number of channels of FloatSampleBuffer not equal to number of channels of this filter processor");
        }
        for (int channel = 0; channel < buffer.getChannelCount(); channel++) {
            float[] b = buffer.getChannel(channel);
            for (int sample = 0; sample < buffer.getSampleCount(); sample++) {
                b[sample] = filters[channel].process(b[sample]);
            }
        }
    }
}
