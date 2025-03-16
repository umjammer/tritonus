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

package org.tritonus.saol.compiler;


public class SAOLGlobals {

    private static final int DEFAULT_ARATE = 32000;
    private static final int DEFAULT_KRATE = 100;
    private static final int DEFAULT_INCHANNELS = 0; // ?? TODO
    private static final int DEFAULT_OUTCHANNELS = 1;
    private static final int DEFAULT_INTERP = 0;

    private int aRate;
    private int kRate;
    private int inChannels;
    private int outChannels;
    private int interP;

    public SAOLGlobals() {
        this(DEFAULT_ARATE,
                DEFAULT_KRATE,
                DEFAULT_INCHANNELS,
                DEFAULT_OUTCHANNELS,
                DEFAULT_INTERP);
    }

    private SAOLGlobals(int defaultARate,
                        int defaultKRate,
                        int defaultInChannels,
                        int defaultOutChannels,
                        int defaultInterP) {
        aRate = defaultARate;
        kRate = defaultKRate;
        inChannels = defaultInChannels;
        outChannels = defaultOutChannels;
        interP = defaultInterP;
    }

    public void setARate(int aRate) {
        this.aRate = aRate;
    }

    public int getARate() {
        return aRate;
    }

    public void setKRate(int kRate) {
        this.kRate = kRate;
    }

    public int getKRate() {
        return kRate;
    }

    public void setInChannels(int inChannels) {
        this.inChannels = inChannels;
    }

    public int getInChannels() {
        return inChannels;
    }

    public void setOutChannels(int outChannels) {
        this.outChannels = outChannels;
    }

    public int getOutChannels() {
        return outChannels;
    }

    public void setInterP(int interP) {
        this.interP = interP;
    }

    public int getInterP() {
        return interP;
    }
}
