/*
 *  Copyright (c) 1999 - 2003 by Matthias Pfisterer
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

package org.tritonus.share.midi;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.BitSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

import org.tritonus.share.ArraySet;

import static java.lang.System.getLogger;
import static javax.sound.midi.ShortMessage.CONTROL_CHANGE;


public abstract class TSequencer extends TMidiDevice implements Sequencer {

    private static final Logger logger= getLogger("org.tritonus.TraceSequencer");

    private static final float MPQ_BPM_FACTOR = 6.0E7F;
    // This is for use in Collection.toArray(Object[]).
    private static final SyncMode[] EMPTY_SYNCMODE_ARRAY = new SyncMode[0];

    private boolean running;

    /**
     * The Sequence to play or to record to.
     */
    private Sequence sequence;

    /**
     * The listeners that want to be notified of MetaMessages.
     */
    private final Set<MetaEventListener> metaListeners;

    /**
     * The listeners that want to be notified of control change events.
     * They are organized as follows: this array is indexed with
     * the number of the controller change events listeners are
     * interested in. If there is any interest, the array element
     * contains a reference to a Set containing the listeners.
     * These sets are allocated on demand.
     */
    private final Set<ControllerEventListener>[] controllerListeners;

    private float nominalTempoInMPQ;
    private float tempoFactor;
    private final Collection<SyncMode> masterSyncModes;
    private final Collection<SyncMode> slaveSyncModes;
    private SyncMode masterSyncMode;
    private SyncMode slaveSyncMode;
    private final BitSet muteBitSet;
    private final BitSet soloBitSet;

    /**
     * Contains the enabled state of the tracks.
     * This BitSet holds the pre-calculated effect of mute and
     * solo status.
     */
    private BitSet enabledBitSet;

    /**
     * Start of the loop in ticks.
     */
    private long loopStartPoint;

    /**
     * End of the loop in ticks.
     */
    private long loopEndPoint;

    /**
     * Loop count.
     */
    private int loopCount;

    /**
     *
     */
    @SuppressWarnings("unchecked")
    protected TSequencer(MidiDevice.Info info,
                         Collection<SyncMode> masterSyncModes,
                         Collection<SyncMode> slaveSyncModes) {
        super(info);
        running = false;
        sequence = null;
        metaListeners = new ArraySet<>();
        controllerListeners = (Set<ControllerEventListener>[]) new Set[128];
        setTempoFactor(1.0F);
        setTempoInMPQ(500000);
        // TODO make a copy
        this.masterSyncModes = masterSyncModes;
        this.slaveSyncModes = slaveSyncModes;
        if (getMasterSyncModes().length > 0) {
            masterSyncMode = getMasterSyncModes()[0];
        }
        if (getSlaveSyncModes().length > 0) {
            slaveSyncMode = getSlaveSyncModes()[0];
        }
        muteBitSet = new BitSet();
        soloBitSet = new BitSet();
        enabledBitSet = new BitSet();
        updateEnabled();
        setLoopStartPoint(0);
        setLoopEndPoint(-1);
        setLoopCount(0);
    }

    @Override
    public void setSequence(Sequence sequence) throws InvalidMidiDataException {
        // TODO what if playing is in progress?
        if (getSequence() != sequence) {
            this.sequence = sequence;
            setSequenceImpl();
            // Yes, resetting the tempo factor is required by the specification.
            // TODO can't find this any more in the spec.
            //
            // It is unclear whether this should be executed in any case
            // (even if in fact the sequence didn't change).
            setTempoFactor(1.0F);
        }
    }

    /**
     * Set Sequence.
     * Subclasses that need to be informed when a Sequence is set
     * should override this method. It is called by setSequence().
     * Subclasses can find out the new Sequence by calling getSequence().
     * <p>
     * TODO make abstract
     */
    protected void setSequenceImpl() {
    }

    @Override
    public void setSequence(InputStream inputStream) throws InvalidMidiDataException, IOException {
        Sequence sequence = MidiSystem.getSequence(inputStream);
        setSequence(sequence);
    }

    @Override
    public Sequence getSequence() {
        return sequence;
    }

    @Override
    public void setLoopStartPoint(long tick) {
        loopStartPoint = tick;
    }

    @Override
    public long getLoopStartPoint() {
        return loopStartPoint;
    }

    @Override
    public void setLoopEndPoint(long tick) {
        loopEndPoint = tick;
    }

    @Override
    public long getLoopEndPoint() {
        return loopEndPoint;
    }

    @Override
    public void setLoopCount(int loopCount) {
        this.loopCount = loopCount;
    }

    @Override
    public int getLoopCount() {
        return loopCount;
    }

    @Override
    public synchronized void start() {
        checkOpen();
        if (!isRunning()) {
            running = true;
            // TODO perhaps check if sequence present
            startImpl();
        }
    }

    /**
     * Subclasses have to override this method to be notified of
     * starting.
     */
    protected void startImpl() {
    }

    @Override
    public synchronized void stop() {
        checkOpen();
        if (isRunning()) {
            stopImpl();
            running = false;
        }
    }

    /**
     * Subclasses have to override this method to be notified of
     * stopping.
     */
    protected void stopImpl() {
    }

    @Override
    public synchronized boolean isRunning() {
        return running;
    }

    /**
     * Checks if the Sequencer is open.
     * This method is intended to be called by
     * {@link javax.sound.midi.Sequencer#start start},
     * {@link javax.sound.midi.Sequencer#stop stop},
     * {@link javax.sound.midi.Sequencer#startRecording startRecording}
     * and {@link javax.sound.midi.Sequencer#stop stopRecording}.
     *
     * @throws IllegalStateException if the <code>Sequencer</code> is not open
     */
    protected void checkOpen() {
        if (!isOpen()) {
            throw new IllegalStateException("Sequencer is not open");
        }
    }

    /**
     * Returns the resolution (ticks per quarter) of the current sequence.
     * If no sequence is set, a bogus default value != 0 is returned.
     */
    protected int getResolution() {
        Sequence sequence = getSequence();
        int resolution;
        if (sequence != null) {
            resolution = sequence.getResolution();
        } else {
            resolution = 1;
        }
        return resolution;
    }

    protected void setRealTempo() {
        float tempoFactor = getTempoFactor();
        if (tempoFactor == 0.0F) {
            tempoFactor = 0.01F;
        }
        float realTempo = getTempoInMPQ() / tempoFactor;

        logger.log(Level.TRACE, "real tempo: " + realTempo);

        setTempoImpl(realTempo);
    }

    @Override
    public float getTempoInBPM() {
        float bpm = MPQ_BPM_FACTOR / getTempoInMPQ();
        return bpm;
    }

    @Override
    public void setTempoInBPM(float fBPM) {
        float mpq = MPQ_BPM_FACTOR / fBPM;
        setTempoInMPQ(mpq);
    }

    @Override
    public float getTempoInMPQ() {
        return nominalTempoInMPQ;
    }

    /**
     * Sets the tempo.
     * Implementation classes are required to call this method for changing
     * the tempo in reaction to a tempo change event.
     */
    @Override
    public void setTempoInMPQ(float mpq) {
        nominalTempoInMPQ = mpq;
        setRealTempo();
    }

    @Override
    public void setTempoFactor(float factor) {
        tempoFactor = factor;
        setRealTempo();
    }

    @Override
    public float getTempoFactor() {
        return tempoFactor;
    }

    /**
     * Change the tempo of the native sequencer part.
     * This method has to be defined by subclasses according
     * to the native facilities they use for sequenceing.
     * The implementation should not take into account the
     * tempo factor. This is handled elsewhere.
     */
    protected abstract void setTempoImpl(float mpq);

    // NOTE: has to be redefined if recording is done natively
    @Override
    public long getTickLength() {
        long length = 0;
        if (getSequence() != null) {
            length = getSequence().getTickLength();
        }
        return length;
    }

    // NOTE: has to be redefined if recording is done natively
    @Override
    public long getMicrosecondLength() {
        long length = 0;
        if (getSequence() != null) {
            length = getSequence().getMicrosecondLength();
        }
        return length;
    }

    @Override
    public boolean addMetaEventListener(MetaEventListener listener) {
        synchronized (metaListeners) {
            return metaListeners.add(listener);
        }
    }

    @Override
    public void removeMetaEventListener(MetaEventListener listener) {
        synchronized (metaListeners) {
            metaListeners.remove(listener);
        }
    }

    protected Iterator<MetaEventListener> getMetaEventListeners() {
        synchronized (metaListeners) {
            return metaListeners.iterator();
        }
    }

    protected void sendMetaMessage(MetaMessage message) {
        Iterator<MetaEventListener> iterator = getMetaEventListeners();
        while (iterator.hasNext()) {
            MetaEventListener metaEventListener = iterator.next();
            MetaMessage copiedMessage = (MetaMessage) message.clone();
            metaEventListener.meta(copiedMessage);
        }
    }

    @Override
    public int[] addControllerEventListener(ControllerEventListener listener, int[] controllers) {
        synchronized (controllerListeners) {
            if (controllers == null) {
                // Add to all controllers. NOTE: this
                // is an implementation-specific
                // semantic!
                for (int i = 0; i < 128; i++) {
                    addControllerListener(i, listener);
                }
            } else {
                for (int controller : controllers) {
                    addControllerListener(controller, listener);
                }
            }
        }
        return getListenedControllers(listener);
    }

    private void addControllerListener(int i, ControllerEventListener listener) {
        if (controllerListeners[i] == null) {
            controllerListeners[i] = new ArraySet<>();
        }
        controllerListeners[i].add(listener);
    }

    @Override
    public int[] removeControllerEventListener(ControllerEventListener listener, int[] controllers) {
        synchronized (controllerListeners) {
            if (controllers == null) {
                // Remove from all controllers. Unlike
                // above, this is specified semantics.
                for (int i = 0; i < 128; i++) {
                    removeControllerListener(i, listener);
                }
            } else {
                for (int controller : controllers) {
                    removeControllerListener(controller, listener);
                }
            }
        }
        return getListenedControllers(listener);
    }

    private void removeControllerListener(int i, ControllerEventListener listener) {
        if (controllerListeners[i] != null) {
            controllerListeners[i].add(listener);
        }
    }

    private int[] getListenedControllers(ControllerEventListener listener) {
        int[] controllers = new int[128];
        int index = 0; // points to the next position to use.
        for (int controller = 0; controller < 128; controller++) {
            if (controllerListeners[controller] != null && controllerListeners[controller].contains(listener)) {
                controllers[index] = controller;
                index++;
            }
        }
        int[] resultControllers = new int[index];
        System.arraycopy(controllers, 0, resultControllers, 0, index);
        return resultControllers;
    }

    protected void sendControllerEvent(ShortMessage message) {
        int controller = message.getData1();
        if (controllerListeners[controller] != null) {
            for (ControllerEventListener controllerEventListener : controllerListeners[controller]) {
                ShortMessage copiedMessage = (ShortMessage) message.clone();
                controllerEventListener.controlChange(copiedMessage);
            }
        }
    }

    protected void notifyListeners(MidiMessage message) {
        if (message instanceof MetaMessage) {
            // IDEA: use extra thread for event delivery
            sendMetaMessage((MetaMessage) message);
        } else if (message instanceof ShortMessage && ((ShortMessage) message).getCommand() == CONTROL_CHANGE) {
            sendControllerEvent((ShortMessage) message);
        }
    }

    @Override
    public SyncMode getMasterSyncMode() {
        return masterSyncMode;
    }

    @Override
    public void setMasterSyncMode(SyncMode syncMode) {
        if (masterSyncModes.contains(syncMode)) {
            if (!getMasterSyncMode().equals(syncMode)) {
                masterSyncMode = syncMode;
                setMasterSyncModeImpl(syncMode);
            }
        } else {
            throw new IllegalArgumentException("sync mode not allowed: " + syncMode);
        }
    }

    /**
     * This method is guaranteed only to be called if the sync mode really changes.
     */
    protected void setMasterSyncModeImpl(SyncMode syncMode) {
        // DO NOTHING
    }

    @Override
    public SyncMode[] getMasterSyncModes() {
        SyncMode[] syncModes = masterSyncModes.toArray(EMPTY_SYNCMODE_ARRAY);
        return syncModes;
    }

    @Override
    public SyncMode getSlaveSyncMode() {
        return slaveSyncMode;
    }

    @Override
    public void setSlaveSyncMode(SyncMode syncMode) {
        if (slaveSyncModes.contains(syncMode)) {
            if (!getSlaveSyncMode().equals(syncMode)) {
                slaveSyncMode = syncMode;
                setSlaveSyncModeImpl(syncMode);
            }
        } else {
            throw new IllegalArgumentException("sync mode not allowed: " + syncMode);
        }
    }

    /**
     * This method is guaranteed only to be called if the sync mode really changes.
     */
    protected void setSlaveSyncModeImpl(SyncMode syncMode) {
        // DO NOTHING
    }

    @Override
    public SyncMode[] getSlaveSyncModes() {
        SyncMode[] syncModes = slaveSyncModes.toArray(EMPTY_SYNCMODE_ARRAY);
        return syncModes;
    }

    @Override
    public boolean getTrackSolo(int track) {
        boolean soloed = false;
        if (getSequence() != null) {
            if (track < getSequence().getTracks().length) {
                soloed = soloBitSet.get(track);
            }
        }
        return soloed;
    }

    @Override
    public void setTrackSolo(int track, boolean solo) {
        if (getSequence() != null) {
            if (track < getSequence().getTracks().length) {
                boolean oldState = soloBitSet.get(track);
                if (solo != oldState) {
                    if (solo) {
                        soloBitSet.set(track);
                    } else {
                        soloBitSet.clear(track);
                    }
                    updateEnabled();
                    setTrackSoloImpl(track, solo);
                }
            }
        }
    }

    protected void setTrackSoloImpl(int track, boolean solo) {
    }

    @Override
    public boolean getTrackMute(int track) {
        boolean muted = false;
        if (getSequence() != null) {
            if (track < getSequence().getTracks().length) {
                muted = muteBitSet.get(track);
            }
        }
        return muted;
    }

    @Override
    public void setTrackMute(int track, boolean mute) {
        if (getSequence() != null) {
            if (track < getSequence().getTracks().length) {
                boolean oldState = muteBitSet.get(track);
                if (mute != oldState) {
                    if (mute) {
                        muteBitSet.set(track);
                    } else {
                        muteBitSet.clear(track);
                    }
                    updateEnabled();
                    setTrackMuteImpl(track, mute);
                }
            }
        }
    }

    protected void setTrackMuteImpl(int track, boolean mute) {
    }

    private void updateEnabled() {
        BitSet oldEnabledBitSet = (BitSet) enabledBitSet.clone();
        boolean soloExists = !soloBitSet.isEmpty();
        if (soloExists) {
            enabledBitSet = (BitSet) soloBitSet.clone();
        } else {
            for (int i = 0; i < muteBitSet.size(); i++) {
                if (muteBitSet.get(i)) {
                    enabledBitSet.clear(i);
                } else {
                    enabledBitSet.set(i);
                }
            }
        }
        oldEnabledBitSet.xor(enabledBitSet);
        // oldEnabledBitSet now has a bit set if the status for
        // this bit changed.
        for (int i = 0; i < oldEnabledBitSet.size(); i++) {
            if (oldEnabledBitSet.get(i)) {
                setTrackEnabledImpl(i, enabledBitSet.get(i));
            }
        }
    }

    /**
     * Shows that a track state has changed.
     * This method is called for each track where the enabled
     * state (calculated from mute and solo) has changed.
     * The boolean value passed represents the new state.
     *
     * @param track   The track number for which the enabled status has changed.
     * @param enabled The new enabled state for this track.
     */
    protected void setTrackEnabledImpl(int track, boolean enabled) {
    }

    protected boolean isTrackEnabled(int track) {
        return enabledBitSet.get(track);
    }

    /**
     * Sets the preloading intervall.
     * This is the time span between preloading events to an internal
     * queue and playing them. This intervall should be kept constant
     * by the implementation. However, this cannot be guaranteed.
     */
    public void setLatency(int milliseconds) {
    }

    /**
     * Get the preloading intervall.
     *
     * @return the preloading intervall in milliseconds, or -1 if the sequencer
     * doesn't respond to changes in the <code>Sequence</code> at all.
     */
    public int getLatency() {
        return -1;
    }
}
