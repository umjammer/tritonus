/*
 *  Copyright (c) 2002 by Matthias Pfisterer
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
 */

package org.tritonus.saol.engine;


public abstract class AbstractInstrument implements Output {

    private Output outputPort;
    private int startTime;
    private int endTime;

    protected AbstractInstrument() {
    }

    // should be a constructor argument, but is not to simplify instantiation and inheritance
    public void setOutput(Output output) {
        outputPort = output;
    }

    // should be a constructor argument, but is not to simplify instantiation and inheritance
    public void setStartAndEndTime(int startTime, int endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void doIPass(RTSystem rtSystem) {
    }

    public void doKPass(RTSystem rtSystem) {
    }

    public void doAPass(RTSystem rtSystem) {
    }

    /**
     * Gives the width of the output port.
     *
     * @return width of the output port
     * (number of channels)
     */
    @Override
    public int getWidth() {
        return outputPort.getWidth();
    }

    /**
     * Initiate the output port of the instrument.
     * Sets the values of all samples to 0.0.
     * This method must be called in an a-cycle before
     * this instrument's a-cycle code is executed.
     */
    @Override
    public void clear() {
        outputPort.clear();
    }

    /**
     * Add the sample value of one instrument.
     * This method can be called by instrument's a-cycle
     * code to output the sample value the instrument has
     * calculated for this a-cycle.
     */
    @Override
    public void output(float sample) {
        outputPort.output(sample);
    }

    /**
     * Add sample values of one instrument.
     * This method can be called by instrument's a-cycle
     * code to output the sample value the instrument has
     * calculated for this a-cycle.
     * The current hacky version allows only for mono samples.
     */
    @Override
    public void output(float[] samples) {
        outputPort.output(samples);
    }
}
