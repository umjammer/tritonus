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

package org.tritonus.midi.device.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

import org.tritonus.lowlevel.alsa.AlsaSeq;
import org.tritonus.lowlevel.alsa.AlsaSeqEvent;
import org.tritonus.lowlevel.alsa.AlsaSeqPortSubscribe;
import org.tritonus.lowlevel.alsa.AlsaSeqQueueInfo;
import org.tritonus.lowlevel.alsa.AlsaSeqQueueStatus;
import org.tritonus.lowlevel.alsa.AlsaSeqQueueTempo;
import org.tritonus.share.midi.MidiUtils;
import org.tritonus.share.midi.TSequencer;

import static java.lang.System.getLogger;
import static javax.sound.midi.Sequencer.SyncMode.MIDI_SYNC;
import static javax.sound.midi.Sequencer.SyncMode.MIDI_TIME_CODE;
import static javax.sound.midi.Sequencer.SyncMode.NO_SYNC;
import static org.tritonus.lowlevel.alsa.AlsaSeq.SND_SEQ_CLIENT_SYSTEM;
import static org.tritonus.lowlevel.alsa.AlsaSeq.SND_SEQ_EVENT_SETPOS_TICK;
import static org.tritonus.lowlevel.alsa.AlsaSeq.SND_SEQ_PORT_SYSTEM_TIMER;
import static org.tritonus.lowlevel.alsa.AlsaSeq.SND_SEQ_QUEUE_DIRECT;
import static org.tritonus.lowlevel.alsa.AlsaSeq.SND_SEQ_TIME_MODE_REL;
import static org.tritonus.lowlevel.alsa.AlsaSeq.SND_SEQ_TIME_STAMP_REAL;


public class AlsaSequencer extends TSequencer {

    // TODO derive from TPreloadingSequencer

    private static final Logger logger = getLogger("org.tritonus.TraceSequencer");

    /**
     * The synchronization modes the sequencer can sync to.
     */
    private static final SyncMode[] MASTER_SYNC_MODES = {SyncMode.INTERNAL_CLOCK};

    /**
     * The synchronization modes the sequencer can send.
     */
    private static final SyncMode[] SLAVE_SYNC_MODES = {NO_SYNC, MIDI_SYNC};

    /**
     * The ALSA event tag used for MIDI clock events
     */
    private static final int CLOCK_EVENT_TAG = 255;

    private AlsaSeq playbackAlsaSeq;
    private AlsaSeq recordingAlsaSeq;
    private int recordingPort;
    private int playbackPort;
    private int queue;
    private AlsaSeqQueueInfo queueInfo;
    private AlsaSeqQueueStatus queueStatus;
    private AlsaSeqQueueTempo queueTempo;
    private AlsaMidiIn playbackAlsaMidiIn;
    private AlsaMidiOut playbackAlsaMidiOut;
    private AlsaMidiIn recordingAlsaMidiIn;
    protected LoaderThread loaderThread;
    private Thread syncThread;
    private AlsaSeqEvent queueControlEvent;
    protected AlsaSeqEvent clockEvent;
    private boolean recording;
    private Track track;
    private AlsaSeqEvent allNotesOffEvent;
    private Sequencer.SyncMode oldSlaveSyncMode;
    private float cachedRealMPQ;

    public AlsaSequencer(MidiDevice.Info info) {
        super(info, List.of(MASTER_SYNC_MODES), List.of(SLAVE_SYNC_MODES));
        // TODO fetch from base class instead
        cachedRealMPQ = -1.0F;
    }

    protected int getPlaybackClient() {
        int client = getPlaybackAlsaSeq().getClientId();
        return client;
    }

    protected int getPlaybackPort() {
        return playbackPort;
    }

    protected int getRecordingClient() {
        int client = getRecordingAlsaSeq().getClientId();
        return client;
    }

    protected int getRecordingPort() {
        return recordingPort;
    }

    protected int getQueue() {
        return queue;
    }

    private AlsaSeqQueueStatus getQueueStatus() {
        return queueStatus;
    }

    private AlsaSeqQueueTempo getQueueTempo() {
        return queueTempo;
    }

    private AlsaSeq getPlaybackAlsaSeq() {
        return playbackAlsaSeq;
    }

    protected AlsaSeq getRecordingAlsaSeq() {
        return recordingAlsaSeq;
    }

    private void updateQueueStatus() {
        // TODO error handling
//        getRecordingAlsaSeq().getQueueStatus(getQueue(), getQueueStatus());
        getPlaybackAlsaSeq().getQueueStatus(getQueue(), getQueueStatus());
    }

    @Override
    protected void openImpl() {
        recordingAlsaSeq = new AlsaSeq("Tritonus ALSA Sequencer (recording/synchronization)");
        recordingPort = getRecordingAlsaSeq().createPort("recording/synchronization port", AlsaSeq.SND_SEQ_PORT_CAP_WRITE | AlsaSeq.SND_SEQ_PORT_CAP_SUBS_WRITE | AlsaSeq.SND_SEQ_PORT_CAP_READ | AlsaSeq.SND_SEQ_PORT_CAP_SUBS_READ, 0, AlsaSeq.SND_SEQ_PORT_TYPE_APPLICATION, 0, 0, 0);

        playbackAlsaSeq = new AlsaSeq("Tritonus ALSA Sequencer (playback)");
        playbackPort = getPlaybackAlsaSeq().createPort("playback port", AlsaSeq.SND_SEQ_PORT_CAP_WRITE | AlsaSeq.SND_SEQ_PORT_CAP_SUBS_WRITE | AlsaSeq.SND_SEQ_PORT_CAP_READ | AlsaSeq.SND_SEQ_PORT_CAP_SUBS_READ, 0, AlsaSeq.SND_SEQ_PORT_TYPE_APPLICATION, 0, 0, 0);

        queue = getPlaybackAlsaSeq().allocQueue();
        queueInfo = new AlsaSeqQueueInfo();
        queueStatus = new AlsaSeqQueueStatus();
        queueTempo = new AlsaSeqQueueTempo();
        getPlaybackAlsaSeq().getQueueInfo(getQueue(), queueInfo);
        queueInfo.setLocked(false);
        getPlaybackAlsaSeq().setQueueInfo(getQueue(), queueInfo);
        playbackAlsaMidiOut = new AlsaMidiOut(getPlaybackAlsaSeq(), getPlaybackPort(), getQueue());
        playbackAlsaMidiOut.setHandleMetaMessages(true);
        getRecordingAlsaSeq().setQueueUsage(getQueue(), true);

        // this establishes the subscription, too
        AlsaMidiIn.AlsaMidiInListener playbackListener = new PlaybackAlsaMidiInListener();
        playbackAlsaMidiIn = new AlsaMidiIn(getPlaybackAlsaSeq(), getPlaybackPort(), getPlaybackClient(), getPlaybackPort(), playbackListener);
        // start the receiving thread
        playbackAlsaMidiIn.start();
        queueControlEvent = new AlsaSeqEvent();
        clockEvent = new AlsaSeqEvent();
        clockEvent.setCommon(
                AlsaSeq.SND_SEQ_EVENT_CLOCK, // type
                AlsaSeq.SND_SEQ_TIME_STAMP_TICK | AlsaSeq.SND_SEQ_TIME_MODE_ABS,
                CLOCK_EVENT_TAG, // tag
                getQueue(),
                0L, // timestamp; not yet known
                0,    // source client
                getRecordingPort(),  // source port
                AlsaSeq.SND_SEQ_ADDRESS_SUBSCRIBERS, // dest client
                AlsaSeq.SND_SEQ_ADDRESS_UNKNOWN); // dest port
        allNotesOffEvent = new AlsaSeqEvent();
        oldSlaveSyncMode = getSlaveSyncMode();
        if (cachedRealMPQ != -1.0F) {
            setTempoImpl(cachedRealMPQ);
            cachedRealMPQ = -1.0F;
        }
        loaderThread = new LoaderThread();
        loaderThread.start();
        // this is for sending clock events
//        syncThread = new MasterSynchronizer();
//        syncThread.start();
    }

    @Override
    protected void closeImpl() {
        playbackAlsaMidiIn.interrupt();
        playbackAlsaMidiIn = null;
        getQueueStatus().free();
        queueStatus = null;
        getQueueTempo().free();
        queueTempo = null;
        // TODO
//        m_aSequencer.releaseQueue(getQueue());
//        m_aSequencer.destroyPort(getPort());
        getRecordingAlsaSeq().close();
        recordingAlsaSeq = null;
        getPlaybackAlsaSeq().close();
        playbackAlsaSeq = null;
        queueControlEvent.free();
        queueControlEvent = null;
        clockEvent.free();
        clockEvent = null;
        allNotesOffEvent.free();
        allNotesOffEvent = null;
    }

    @Override
    protected void startImpl() {
        if (getTickPosition() == 0) {
            startQueue();
        } else {
            continueQueue();
        }
        synchronized (loaderThread) {
            logger.log(Level.TRACE, "notifying loader thread");

            loaderThread.notify();
        }
        // TODO should depend on sync mode
//       synchronized (syncThread) {
//logger.log(Level.TRACE, "AlsaSequencer.startImpl(): notifying synchronizer thread");
//           syncThread.notify();
//        }
        if (!getSlaveSyncMode().equals(NO_SYNC)) {
            sendStartEvent();
        }
    }

    @Override
    protected void stopImpl() {
        stopQueue();
        sendAllNotesOff();
        // should be in base class?
        stopRecording();
        if (!getSlaveSyncMode().equals(NO_SYNC)) {
            sendStopEvent();
        }
    }

    @Override
    protected void setSequenceImpl() {
        if (loaderThread != null) {
            loaderThread.setLoading(getSequence() != null);
        }
    }

    /**
     * TODO can be implemented just with a flag?
     */
    @Override
    public boolean isRunning() {
        boolean running = false;
        if (isOpen()) {
            updateQueueStatus();
            int status = getQueueStatus().getStatus();
            logger.log(Level.TRACE, "queue status: " + status);

            running = (status != 0);
        }
        return running;
    }

    @Override
    public void startRecording() {
        checkOpen(); // may throw IllegalStateException
        recording = true;
        start();
    }

    @Override
    public void stopRecording() {
        checkOpen(); // may throw IllegalStateException
        recording = false;
    }

    @Override
    public boolean isRecording() {
        return recording;
    }

    // name should be: enableRecording
    @Override
    public void recordEnable(Track track, int channel) {
        // TODO hacky
        this.track = track;
    }

    // name should be: disableRecording
    @Override
    public void recordDisable(Track track) {
        // TODO
    }

    @Override
    protected void setTempoImpl(float mpq) {
        if (isOpen()) {
            logger.log(Level.TRACE, "setting tempo to " + (int) mpq);

            getQueueTempo().setTempo((int) mpq);
            getQueueTempo().setPpq(getResolution());
            getPlaybackAlsaSeq().setQueueTempo(getQueue(), getQueueTempo());
        } else {
            logger.log(Level.TRACE, "ignoring because sequencer is not open");

            cachedRealMPQ = mpq;
        }
    }

    @Override
    public long getTickPosition() {
        long position;
        if (isOpen()) {
            updateQueueStatus();
            position = getQueueStatus().getTickTime();
        } else {
            logger.log(Level.TRACE, "sequencer not open, returning 0");

            position = 0;
        }
        return position;
    }

    @Override
    public void setTickPosition(long tick) {
        if (isOpen()) {
            int sourcePort = getRecordingPort();
            int queue = getQueue();
            sendQueueControlEvent(
                    SND_SEQ_EVENT_SETPOS_TICK,
                    SND_SEQ_TIME_STAMP_REAL | SND_SEQ_TIME_MODE_REL, 0, SND_SEQ_QUEUE_DIRECT, 0L,
                    sourcePort, SND_SEQ_CLIENT_SYSTEM, SND_SEQ_PORT_SYSTEM_TIMER,
                    queue, 0, tick);
        } else {
            logger.log(Level.TRACE, "ignored because sequencer is not open");
        }
    }

    @Override
    public long getMicrosecondPosition() {
        long position;
        if (isOpen()) {
            updateQueueStatus();
            long nanoSeconds = getQueueStatus().getRealTime();
            position = nanoSeconds / 1000;
        } else {
            logger.log(Level.TRACE, "sequencer not open, returning 0");

            position = 0;
        }
        return position;
    }

    @Override
    public void setMicrosecondPosition(long microseconds) {
        if (isOpen()) {
            long nanoSeconds = microseconds * 1000;
            int sourcePort = getRecordingPort();
            int queue = getQueue();
            long time = nanoSeconds;
            sendQueueControlEvent(
                    AlsaSeq.SND_SEQ_EVENT_SETPOS_TIME,
                    SND_SEQ_TIME_STAMP_REAL | SND_SEQ_TIME_MODE_REL, 0, SND_SEQ_QUEUE_DIRECT, 0L,
                    sourcePort, SND_SEQ_CLIENT_SYSTEM, SND_SEQ_PORT_SYSTEM_TIMER,
                    queue, 0, time);
        } else {
            logger.log(Level.TRACE, "ignoring because sequencer is not open");
        }
    }

    @Override
    protected void setMasterSyncModeImpl(SyncMode syncMode) {
        // TODO
    }

    @Override
    protected void setSlaveSyncModeImpl(SyncMode syncMode) {
        if (isRunning()) {
            if (oldSlaveSyncMode.equals(NO_SYNC) && (syncMode.equals(MIDI_SYNC) || syncMode.equals(MIDI_TIME_CODE))) {
                sendStartEvent();
                // TODO notify sync thread
            } else if ((oldSlaveSyncMode.equals(MIDI_SYNC) || oldSlaveSyncMode.equals(MIDI_TIME_CODE)) && syncMode.equals(NO_SYNC)) {
                sendStopEvent();
                // TODO remove enqueued messages from queue (and buffer).
                //  perhaps do this by putting the code to do so after the main loop of the sync thread.
            }
        }
    }

    /**
     * If the enabled state changes to true, the events
     * between the current playback position and the current
     * loading position are enqueued.
     * If the enabled state of a track changed to false,
     * the events belonging to this track are removed from
     * the queue, besides the 'off'-events.
     */
    @Override
    protected void setTrackEnabledImpl(int track, boolean enabled) {
        if (enabled) {
            // TODO reload events
        } else {
            // TODO remove events
        }
    }

    /**
     * This method has to be synchronized because it is called
     * from sendMessageTick() as well as from loadSequenceToNative().
     */
    protected synchronized void enqueueMessage(MidiMessage message, long tick) {
        playbackAlsaMidiOut.enqueueMessage(message, tick);
    }

    /**
     * Put a message into the queue.
     * This is Claus-Dieter's special method: it puts the message to
     * the ALSA queue for delivery at the specified time.
     * The time has to be given in ticks according to the resolution
     * of the currently active Sequence. For this method to work,
     * the Sequencer has to be started. The message is delivered
     * the same way as messages from a Sequence, i.e. to all
     * registered Transmitters. If the current queue position (as
     * returned by getTickPosition()) is
     * already behind the desired schedule time, the message is
     * ignored.
     *
     * @param message the MidiMessage to put into the queue.
     * @param tick   the desired schedule time in ticks.
     */
    public void sendMessageTick(MidiMessage message, long tick) {
        enqueueMessage(message, tick);
    }

    private void startQueue() {
        controlQueue(AlsaSeq.SND_SEQ_EVENT_START);
    }

    private void continueQueue() {
        controlQueue(AlsaSeq.SND_SEQ_EVENT_CONTINUE);
    }

    private void stopQueue() {
        controlQueue(AlsaSeq.SND_SEQ_EVENT_STOP);
    }

    private void controlQueue(int type) {
        int sourcePort = getPlaybackPort();
        int queue = getQueue();
        sendQueueControlEvent(
                type,
                SND_SEQ_TIME_STAMP_REAL | SND_SEQ_TIME_MODE_REL,
                0,
                SND_SEQ_QUEUE_DIRECT,
                0L,
                sourcePort,
                SND_SEQ_CLIENT_SYSTEM,
                SND_SEQ_PORT_SYSTEM_TIMER,
                queue, 0, 0);
    }

    /**
     * Send a real time START  event to the subscribers immediately.
     */
    private void sendStartEvent() {
        sendRealtimeEvent(AlsaSeq.SND_SEQ_EVENT_START);
    }

    /**
     * Send a real time STOP  event to the subscribers immediately.
     */
    private void sendStopEvent() {
        sendRealtimeEvent(AlsaSeq.SND_SEQ_EVENT_STOP);
    }

    private void sendRealtimeEvent(int type) {
        sendQueueControlEvent(
                type,
                SND_SEQ_TIME_STAMP_REAL | SND_SEQ_TIME_MODE_REL,
                0, // tag
                SND_SEQ_QUEUE_DIRECT, // queue
                0L, // time
                getPlaybackPort(), // source
                AlsaSeq.SND_SEQ_ADDRESS_SUBSCRIBERS, // dest client
                AlsaSeq.SND_SEQ_ADDRESS_UNKNOWN, // dest port
                0, 0, 0);
    }

    // NOTE: also used for setting position and start/stop RT
    private void sendQueueControlEvent(
            int type, int flags, int tag, int queue, long time, int sourcePort, int destClient, int destPort,
            int controlQueue, int controlValue, long controlTime) {
        queueControlEvent.setCommon(type, flags, tag, queue, time, 0, sourcePort, destClient, destPort);
        queueControlEvent.setQueueControl(controlQueue, controlValue, controlTime);
        getPlaybackAlsaSeq().eventOutputDirect(queueControlEvent);
    }

    private void sendAllNotesOffEvent(int channel) {
        int sourcePort = getPlaybackPort();
        allNotesOffEvent.setCommon(
                AlsaSeq.SND_SEQ_EVENT_CONTROLLER,
                SND_SEQ_TIME_STAMP_REAL | SND_SEQ_TIME_MODE_REL,
                0, // tag
                SND_SEQ_QUEUE_DIRECT, // queue
                0L, // time
                0, sourcePort, // source
                AlsaSeq.SND_SEQ_ADDRESS_SUBSCRIBERS, // dest client
                AlsaSeq.SND_SEQ_ADDRESS_UNKNOWN); // dest port
        allNotesOffEvent.setControl(channel, 0x78, 0);
        getPlaybackAlsaSeq().eventOutputDirect(allNotesOffEvent);
    }

    private void sendAllNotesOff() {
        // TODO check if [0..15] or [1..16]
        for (int channel = 0; channel < 16; channel++) {
            sendAllNotesOffEvent(channel);
        }
    }

    /**
     * Receive a correctely timestamped event.
     * This method expects that the timestamp is in ticks,
     * appropriate for the Sequence currently running.
     */
    protected void receiveTimestamped(MidiMessage message, long timestamp) {
        if (isRecording()) {
            // TODO this is hacky; should implement correct track mapping
            Track track = this.track;
            MidiEvent event = new MidiEvent(message, timestamp);
            track.add(event);
        }
        // TODO entering an event into the sequence
    }

    /**
     * Receive an event from a Receiver.
     * This method is called by AlsaSequencer.AlsaSequencerReceiver
     * on receipt of a MidiMessage.
     */
    @Override
    protected void receive(MidiMessage message, long timeStamp) {
        timeStamp = getTickPosition();
        receiveTimestamped(message, timeStamp);
    }

    //

    @Override
    public Receiver getReceiver() throws MidiUnavailableException {
        return new AlsaSequencerReceiver();
    }

    @Override
    public Transmitter getTransmitter() throws MidiUnavailableException {
        return new AlsaSequencerTransmitter();
    }

    // inner classes

    /* private */ public class PlaybackAlsaMidiInListener implements AlsaMidiIn.AlsaMidiInListener {

        @Override
        public void dequeueEvent(MidiMessage message, long timestamp) {
            logger.log(Level.TRACE, "message: " + message);

            if (message instanceof MetaMessage metaMessage) {
                byte[] data = metaMessage.getData();
                switch (metaMessage.getType()) {
                case 6: // marker
                    String markerText = new String(data);
                    if (markerText.equals("loopend")) {
                        setTickPosition(getLoopStartPoint());
                        loaderThread.setStartPosition(getLoopStartPoint());
                        loaderThread.setLoading(true);
                    }
                    break;

                case 0x51: // set tempo
                    int tempo = MidiUtils.getUnsignedInteger(data[0]) * 65536 +
                            MidiUtils.getUnsignedInteger(data[1]) * 256 +
                            MidiUtils.getUnsignedInteger(data[2]);
                    setTempoInMPQ(tempo);
                    break;
                }
            }
            // passes events to the receivers
            sendImpl(message, -1L);
            // calls control and meta listeners
            notifyListeners(message);
        }
    }

    /* private */ public class RecordingAlsaMidiInListener implements AlsaMidiIn.AlsaMidiInListener {

        @Override
        public void dequeueEvent(MidiMessage message, long timestamp) {
            logger.log(Level.TRACE, "message: " + message);

            AlsaSequencer.this.receiveTimestamped(message, timestamp);
        }
    }

    /* private */ public class AlsaSequencerReceiver extends TReceiver implements AlsaReceiver {

        /**
         * Subscribe to the passed port.
         * This establishes a subscription in the ALSA sequencer
         * so that the device this Receiver belongs to receives
         * event from the client:port passed as parameters.
         *
         * @return true if subscription was established,
         * false otherwise
         */
        @Override
        public boolean subscribeTo(int client, int port) {
            try {
                AlsaSeqPortSubscribe portSubscribe = new AlsaSeqPortSubscribe();
                portSubscribe.setSender(client, port);
                portSubscribe.setDest(AlsaSequencer.this.getRecordingClient(), AlsaSequencer.this.getRecordingPort());
                portSubscribe.setQueue(AlsaSequencer.this.getQueue());
                portSubscribe.setExclusive(false);
                portSubscribe.setTimeUpdate(true);
                portSubscribe.setTimeReal(false);
                AlsaSequencer.this.getRecordingAlsaSeq().subscribePort(portSubscribe);
                portSubscribe.free();
                return true;
            } catch (RuntimeException e) {
                return false;
            }
        }
    }

    /* private */ public class AlsaSequencerTransmitter extends TTransmitter {

        private boolean receiverSubscribed;

        public AlsaSequencerTransmitter() {
            super();
            receiverSubscribed = false;
        }

        /**
         * Try to establish a subscription of the Receiver
         * to the ALSA sequencer client of the device this
         * Transmitter belongs to.
         */
        @Override
        public void setReceiver(Receiver receiver) {
            super.setReceiver(receiver);
            if (receiver instanceof AlsaReceiver) {
                //logger.log(Level.TRACE, "trying to establish subscription");
                receiverSubscribed = ((AlsaReceiver) receiver).subscribeTo(getPlaybackClient(), getPlaybackPort());
                // TODO similar subscription for the sequencer's own midi in listener!!
                // this is necessary because sync messages are sent via the recording port
                receiverSubscribed = ((AlsaReceiver) receiver).subscribeTo(getRecordingClient(), getRecordingPort());
                //logger.log(Level.TRACE, "subscription established: " + receiverSubscribed);
            }
        }

        /**
         * Send message via Java methods only if no
         * subscription was established. If there is a
         * subscription, the message is routed inside
         * the ALSA sequencer.
         */
        @Override
        public void send(MidiMessage message, long timeStamp) {
            if (!receiverSubscribed) {
                super.send(message, timeStamp);
            }
        }

        @Override
        public void close() {
            super.close();
            // TODO remove subscription
        }
    }

    /**
     * Pre-loading events to the sequencer queue.
     */
    /* private */ public class LoaderThread extends Thread {

        /**
         * Current position of loading in Ticks.  This is used to get
         * a useful tick value for the end of track message.
         */
        private long loadingPosition;

        /**
         * Position to start loading in Ticks.
         * This is used for Sequencer.seq[Tick|]Position().
         */
        private long startPosition;

        /**
         * Loading activity.  This flag shows if the LoaderThread is
         * currently loading events to the native queue. Loading may
         * be temporarily stopped if a position change in the
         * sequencer is executed, especially in case of a position
         * change for looping.  This variable is only valid if
         * isRunning() is true. If the sequencer is stopped, it has
         * no significance (since loading is stopped anyway).
         */
        private boolean loading;

        private Track[] tracks;
        private int[] trackPositions;

        public LoaderThread() {
            // TODO monitor changes in the number of tracks
            loadingPosition = 0;
            initTracks();
            // If no sequence is set, we remain idle. We only start loading
            // if there is something to load.
            // If sequence has been set before open(), we set loading here
            // (LoaderThread is created in openImpl()).
            // If sequence is set after open, we do not set loading here,
            // but in setSequenceImpl().
            setLoading(getSequence() != null);
        }

        private void initTracks() {
            Sequence sequence = getSequence();
            // TODO reallocate if number of tracks has been changed.
            if (tracks == null && sequence != null) {
                tracks = sequence.getTracks();
                trackPositions = new int[tracks.length];
            }
        }

        public void setLoading(boolean loading) {
            logger.log(Level.TRACE, "new value: " + loading);

            this.loading = loading;
            synchronized (this) {
                this.notify();
            }
        }

        private boolean isLoading() {
            return loading;
        }

        public void setStartPosition(long ticks) {
            // only to make sure...
            setLoading(false);
            startPosition = ticks;
        }

        @Override
        public void run() {
            while (isOpen()) {
                do {
                    synchronized (this) {
                        try {
                            this.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
                while (!(isRunning() && isLoading()));
                if (isOpen()) {
                    loadSequenceToNative();
                }
            }
        }

        private void loadSequenceToNative() {
            initTracks();
            // For non-0 start positions, this works in conjunction with the
            // 'continue' clause below. Not very efficient...
            // setStartPostion() shoult adapt trackPositions[].
            for (int i = 0; i < tracks.length; i++) {
                trackPositions[i] = 0;
            }
            while (isRunning() && isLoading()) {
                boolean trackPresent = false;
                long bestTick = Long.MAX_VALUE;
                int bestTrack = -1;
                for (int track = 0; track < tracks.length; track++) {
                    if (trackPositions[track] < tracks[track].size()) {
                        trackPresent = true;
                        MidiEvent event = tracks[track].get(trackPositions[track]);
                        long tick = event.getTick();
                        if (tick < startPosition) {
                            // consider next event
                            continue;
                        }
                        if (tick < bestTick) {
                            bestTick = tick;
                            bestTrack = track;
                        }
                    }
                }
                if (!trackPresent) {
                    // No more events; send
                    // end-of-track event.
                    MetaMessage metaMessage = new MetaMessage();
                    try {
                        metaMessage.setMessage(0x2F, new byte[0], 0);
                    } catch (InvalidMidiDataException e) {
                    }
                    enqueueMessage(metaMessage, loadingPosition + 1);
                    // leave the while (isRunning() && isLoading())-loop
                    setLoading(false);
                }
                // The normal case: deliver the event
                // found to be the next.
                MidiEvent event = tracks[bestTrack].get(trackPositions[bestTrack]);
                trackPositions[bestTrack]++;
                long tick = event.getTick();
                loadingPosition = Math.max(loadingPosition, tick);
                MidiMessage message = event.getMessage();
                processMessage(message, tick);
            }
        }

        private void processMessage(MidiMessage message, long tick) {
            boolean messageConsumed = false;
            if (message instanceof MetaMessage metaMessage) {
                int type = metaMessage.getType();
                if (type == 0x2F) { // E.O.T.
                    messageConsumed = true;
                    logger.log(Level.TRACE, "ignoring End of Track message with tick " + tick);

                } else if (type == 6) { // marker
                    String markerText = new String(metaMessage.getData());
                    if (markerText.equals("loopstart")) {
                        setLoopStartPoint(tick);
                        messageConsumed = true;
                    } else if (markerText.equals("loopend")) {
                        setLoopEndPoint(tick);
                        setLoopCount(-1 /* TODO Sequencer.LOOP_CONTINUOUSLY */);
                        // This one needs to be enqueued, because we do
                        // a setPosition() once it is delivered. */
                        messageConsumed = false;
                        setLoading(false);
                    }
                }
            }
            if (!messageConsumed) {
                logger.log(Level.TRACE, "enqueueing event with tick " + tick);

                enqueueMessage(message, tick);
            }
        }
    }

    // TODO start/stop; on/off
    /* private */ public class MasterSynchronizer extends Thread {

        @Override
        public void run() {
            while (isOpen()) {
                do {
                    synchronized (this) {
                        try {
                            this.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
                while (!isRunning());
                double tickMin = getTickPosition();
                double tickMax = getSequence().getTickLength();
                double tickStep = getSequence().getResolution() / 24.0;
                logger.log(Level.TRACE, "tick step: " + tickStep);

                double tick = tickMin;
                // TODO ... && getS.Mode().equals(...)
                while (tick < tickMax && isRunning()) {
                    long tickL = Math.round(tick);
                    logger.log(Level.TRACE, "sending clock event with tick " + tickL);

                    clockEvent.setTimestamp(tickL);
                    getRecordingAlsaSeq().eventOutput(clockEvent);
                    getRecordingAlsaSeq().drainOutput();
                    tick += tickStep;
                }
            }
        }
    }
}
