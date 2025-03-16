/*
 *  Copyright (c) 2000 - 2003 by Matthias Pfisterer
 *  Copyright (c) 2003 by Gabriele Mondada
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

package org.tritonus.midi.device.java;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

import org.tritonus.share.midi.MidiUtils;
import org.tritonus.share.midi.TSequencer;

import static java.lang.System.getLogger;


/**
 * Sequencer implementation in pure Java.
 */
public class JavaSequencer extends TSequencer implements Runnable {

    private static final Logger logger = getLogger("org.tritonus.TraceSequencer");

    private static final SyncMode[] MASTER_SYNC_MODES = {SyncMode.INTERNAL_CLOCK};
    private static final SyncMode[] SLAVE_SYNC_MODES = {SyncMode.NO_SYNC};

    // internal states
    /** not running */
    private static final int STATE_STOPPED = 0;
    /** starting, awake thread */
    private static final int STATE_STARTING = 1;
    /** running */
    private static final int STATE_STARTED = 2;
    /** stopping */
    private static final int STATE_STOPPING = 3;
    /** closing, terminate thread */
    private static final int STATE_CLOSING = 4;

    private Thread thread;
    private long microSecondsPerTick;

    private int[] trackPositions;
    private long tickPosition;
    private long startTime;

    /**
     * Internal state of the sequencer.
     * As values, the symbolic constants STATE_*
     * are used.
     */
    private int phase;
    private boolean tempoChanged;

    /**
     * The clock to use as time base for this sequencer.
     * This is commonly initialized in the constructor,
     * but can also be set with {@link #setClock}.
     */
    private Clock clock;

    /**
     * How long to sleep in the main loop.
     * The value is initialized in the constructor by reading a
     * system property.
     */
    private final long sleepInterval;

    public JavaSequencer(MidiDevice.Info info) {
        super(info, List.of(MASTER_SYNC_MODES), List.of(SLAVE_SYNC_MODES));
        logger.log(Level.TRACE, "begin");

        String version = System.getProperty("java.version");
        if (version.contains("1.4.2")) {
            setClock(new SunMiscPerfClock());
        } else {
            setClock(new SystemCurrentTimeMillisClock());
        }
        String os = System.getProperty("os.name");
        if (os.equals("Linux")) {
            sleepInterval = 0;
        } else {
            sleepInterval = 1;
        }

        logger.log(Level.TRACE, "end");
    }

    @Override
    protected void openImpl() {
        logger.log(Level.TRACE, "begin");

        phase = STATE_STOPPED;
        thread = new Thread(this);
        thread.setPriority(Thread.MAX_PRIORITY);

        logger.log(Level.TRACE, "starting thread");

        thread.start();

        logger.log(Level.TRACE, "end");
    }

    @Override
    protected void closeImpl() {
        logger.log(Level.TRACE, "begin");

        stop();
        // terminate the thread
        synchronized (this) {
            phase = STATE_CLOSING; // ask end of thread
            this.notifyAll();
        }
        // now the thread should terminate
        thread = null;

        logger.log(Level.TRACE, "end");
    }

    @Override
    protected void startImpl() {
        logger.log(Level.TRACE, "begin");

        synchronized (this) {
            if (phase == STATE_STOPPED) {
                // unlock thread waiting for start
                phase = STATE_STARTING;  // ask for start
                this.notifyAll();
                // wait until startTime is set
                while (phase == STATE_STARTING) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        logger.log(Level.ERROR, e.getMessage(), e);
                    }
                }
            }
        }

        logger.log(Level.TRACE, "end");
    }

    @Override
    protected void stopImpl() {
        logger.log(Level.TRACE, "begin");

        synchronized (this) {
            // condition true if called from own run() method
            if (Thread.currentThread() == thread) {
                if (phase != STATE_STOPPED) {
                    phase = STATE_STOPPED;
                    notifyAll();
                }
            } else {
                if (phase == STATE_STARTED) {
                    phase = STATE_STOPPING; // ask for stop
                    while (phase == STATE_STOPPING) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            logger.log(Level.ERROR, e.getMessage(), e);
                        }
                    }
                }
            }
        }

        logger.log(Level.TRACE, "JavaSequencer.stopImpl(): end");
    }

    @Override
    public void run() {
        logger.log(Level.TRACE, "JavaSequencer.run(): begin");

        while (true) {
            synchronized (this) {
                while (phase == STATE_STOPPED) {
                    logger.log(Level.TRACE, "JavaSequencer.run(): waiting to become running");

                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        logger.log(Level.ERROR, e.getMessage(), e);
                    }
                }
                if (phase == STATE_CLOSING) {
                    logger.log(Level.TRACE, "JavaSequencer.run(): end");

                    return;
                }
                logger.log(Level.TRACE, "JavaSequencer.run(): now running");

                // NOTE: all time calculations are done in microseconds
                startTime = getTimeInMicroseconds() - tickPosition * microSecondsPerTick;
                phase = STATE_STARTED;
                this.notifyAll();
            }
            Sequence sequence = getSequence();
            if (sequence == null) {
                stop();
                continue;
            }
            Track[] tracks = sequence.getTracks();
            // this is used to get a useful time value for the end of track message
//            long highestTime = 0;
            while (phase == STATE_STARTED) {
                // searching for the next event
                boolean trackPresent = false;
                long bestTick = Long.MAX_VALUE;
                int bestTrack = -1;
                for (int track = 0; track < tracks.length; track++) {
//logger.log(Level.TRACE, "track " + track);
//                    Track track = tracks[track];
                    if (trackPositions[track] < tracks[track].size() && isTrackEnabled(track)) {
                        trackPresent = true;
                        MidiEvent event = tracks[track].get(trackPositions[track]);
                        long tick = event.getTick();
                        if (tick < bestTick) {
                            bestTick = tick;
                            bestTrack = track;
                        }
                    }
                }
                if (!trackPresent) {
                    MetaMessage metaMessage = new MetaMessage();
                    try {
                        metaMessage.setMessage(0x2F, new byte[0], 0);
                    } catch (InvalidMidiDataException e) {
                        logger.log(Level.ERROR, e.getMessage(), e);
                    }
                    logger.log(Level.TRACE, "sending End of Track message with tick " + (tickPosition + 1));

                    // TODO calulate us
                    deliverEvent(metaMessage, tickPosition + 1);
                    stop();
                    break;
                }
                MidiEvent event = tracks[bestTrack].get(trackPositions[bestTrack]);
                MidiMessage message = event.getMessage();
                long tick = event.getTick();
                if (message instanceof MetaMessage && ((MetaMessage) message).getType() == 0x2F) {
                    logger.log(Level.TRACE, "ignoring End of Track message with tick " + tick);

                    trackPositions[bestTrack]++;
                    synchronized (this) {
                        tickPosition = tick;
                    }
                } else {
                    if (deliverEvent(message, tick)) {
                        trackPositions[bestTrack]++;
                        synchronized (this) {
                            tickPosition = tick;
                        }
                    } else {
                        // be sure that the current position is before the next event
                        synchronized (this) {
                            tickPosition = Math.min(tick, (getTimeInMicroseconds() - startTime) / microSecondsPerTick);
                        }
                    }
                }
            }

            stop();
        }
    }

    /**
     * Deliver a message at a certain time.
     *
     * @param scheduledTick when to deliver the message in ticks
     * @return true if the event was sent, false otherwise
     */
    private boolean deliverEvent(MidiMessage message, long scheduledTick) {
        logger.log(Level.TRACE, "JavaSequencer.deliverEvent(): begin");

        long scheduledTime;
        synchronized (this) {
            scheduledTime = scheduledTick * microSecondsPerTick + startTime;
        }

        // wait for scheduled time
        while (getTimeInMicroseconds() < scheduledTime) {
            if (phase != STATE_STARTED)
                return false;
            if (tempoChanged) {
                synchronized (this) {
                    scheduledTime = scheduledTick * microSecondsPerTick + startTime;
                    tempoChanged = false;
                }
            }
            try {
                Thread.sleep(sleepInterval);
            } catch (InterruptedException e) {
                logger.log(Level.ERROR, e.getMessage(), e);
            }
        }

        // send midi message
        if (message instanceof MetaMessage metaMessage) {
            if (metaMessage.getType() == 0x51) { // set tempo
                byte[] data = metaMessage.getData();
                int tempo = MidiUtils.getUnsignedInteger(data[0]) * 65536 +
                        MidiUtils.getUnsignedInteger(data[1]) * 256 +
                        MidiUtils.getUnsignedInteger(data[2]);
//logger.log(Level.TRACE, "tempo (us/quarter note): " + tempo);
                setTempoInMPQ(tempo); // TODO setTempoInMPQ() seems to be not thread-safe
            }
        }

        logger.log(Level.TRACE, "sending message: " + message + " at: " + scheduledTime);

//        sendImpl(message, event.getTick());
        sendImpl(message, -1); // TODO sendImpl() seems to be not thread-safe
        notifyListeners(message); // TODO notifyListeners() seems to be not thread-safe

        logger.log(Level.TRACE, "end");

        return true; // success
    }

    @Override
    protected void setMasterSyncModeImpl(SyncMode syncMode) {
        // DO NOTHING
    }

    @Override
    protected void setSlaveSyncModeImpl(SyncMode syncMode) {
        // DO NOTHING
    }

    @Override
    public void setSequence(Sequence sequence) throws InvalidMidiDataException {
        boolean wasRunning = isRunning();
        if (wasRunning) {
            stop();
        }
        super.setSequence(sequence);
        tickPosition = 0;
        trackPositions = new int[sequence.getTracks().length];
        Arrays.fill(trackPositions, 0);
        if (wasRunning) {
            start();
        }
    }

    @Override
    public void setMicrosecondPosition(long position) {
        setTickPosition(position / microSecondsPerTick);
    }

    @Override
    public void setTickPosition(long position) {
        if (getSequence() == null || trackPositions == null) {
            return;
        }
        boolean wasRunning = isRunning();
        if (wasRunning)
            stop();
        tickPosition = Math.min(position, getSequence().getTickLength());
        for (int i = 0; i < trackPositions.length; i++) {
            trackPositions[i] = getTrackPosition(getSequence().getTracks()[i], position);
        }
        if (wasRunning)
            start();
    }

    @Override
    public synchronized long getTickPosition() {
        if (phase == STATE_STARTED) {
            return Math.max(tickPosition, (getTimeInMicroseconds() - startTime) / microSecondsPerTick);
        } else {
            return tickPosition;
        }
    }

    @Override
    public void recordDisable(Track track) {
    }

    public void recordEnable(Track track) {
    }

    @Override
    public void recordEnable(Track track, int channel) {
    }

    @Override
    public boolean isRecording() {
        return false;
    }

    @Override
    public void stopRecording() {
        checkOpen();
    }

    @Override
    public void startRecording() {
        checkOpen();
    }

    @Override
    protected synchronized void setTempoImpl(float mpq) {
        logger.log(Level.TRACE, "begin");

        int resolution = getResolution();
        long currentTime = getTimeInMicroseconds();
        long currentTickPosition = 0;
        if (microSecondsPerTick != 0)
            currentTickPosition = (currentTime - startTime) / microSecondsPerTick;
        microSecondsPerTick = (long) mpq / resolution;
        startTime = currentTime - currentTickPosition * microSecondsPerTick;
        tempoChanged = true;
        // TODO update microSecondsPerTick and startTime only after the next event
        //      because the the event now waiting for its schedule is not updated

        logger.log(Level.TRACE, "end");
    }

    /**
     * Obtain the index of the event with the closest tick value.
     */
    private int getTrackPosition(Track track, long tickPosition) {
        // check params
        if (track.size() == 0 || tickPosition <= track.get(0).getTick())
            return 0;
        if (tickPosition > track.get(track.size() - 1).getTick())
            return track.size(); // index out of track

        // quick search
        int idx1 = 0;
        int idx2 = track.size() - 1;
        for (;;) {
            if ((idx2 - idx1) == 1)
                return idx1;
            int idx3 = (int) (((long) idx1 + (long) idx2) / 2L);
            if (tickPosition > track.get(idx3).getTick())
                idx1 = idx3;
            else
                idx2 = idx3;
        }
    }

    /**
     * Retrieve system time in microseconds.
     * This method uses the clock as set with {@link #setClock}.
     *
     * @return the system time in microseconds
     */
    protected long getTimeInMicroseconds() {
        // temporary hack
        if (getClock() == null) {
            return 0;
        }
        // end hack
        return getClock().getMicroseconds();
    }

    /**
     * Set the clock this sequencer should use.
     *
     * @param clock the Clock to be used
     * @throws IllegalStateException if the sequencer is not closed
     */
    public void setClock(Clock clock) {
        if (isOpen()) {
            throw new IllegalStateException("closed state required to set the clock");
        }
        this.clock = clock;
    }

    /**
     * Obtain the clock used by this sequencer.
     *
     * @return the clock currently set for this sequencer
     */
    public Clock getClock() {
        return clock;
    }

    /**
     * Interface for sequencer clocks.
     */
    public interface Clock {

        long getMicroseconds();
    }
}
