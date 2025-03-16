/*
 *  Copyright (c) 2001 by Matthias Pfisterer
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

package org.tritonus.midi.device.alsa;

import org.tritonus.lowlevel.alsa.AlsaSeq;


/**
 * A representation of a physical MIDI port based on the ALSA sequencer.
 */
public class AlsaQueueHolder {

    /**
     * The object interfacing to the ALSA sequencer.
     */
    private final AlsaSeq sequencer;

    /**
     * ALSA queue number.
     */
    private final int queue;

    /**
     *
     */
    public AlsaQueueHolder(AlsaSeq sequencer) {
        this.sequencer = sequencer;
        queue = this.sequencer.allocQueue();
        if (queue < 0) {
            throw new RuntimeException("can't get ALSA sequencer queue");
        }
    }

    /**
     * Returns the allocated queue
     *
     * @return the queue number.
     */
    public int getQueue() {
        return queue;
    }

    /**
     * Frees the queue.
     */
    public void close() {
        sequencer.freeQueue(getQueue());
    }
}
