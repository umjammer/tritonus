/*
 *  Copyright (c) 1999 - 2001 by Matthias Pfisterer
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

package org.tritonus.lowlevel.alsa;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Iterator;

import static java.lang.System.getLogger;


public class AlsaSeq {

    private static final Logger logger = getLogger("org.tritonus.TraceAlsaSeq");

    // definition of sequencer event types

// TODO recheck numbers; enable automatic check

    // 0-4: system messages
    // event data type = snd_seq_result_t
    public static final int SND_SEQ_EVENT_SYSTEM = 0;
    public static final int SND_SEQ_EVENT_RESULT = 1;

    // 2-4: reserved

    // 5-9: note messages (channel specific)
    // event data type = snd_seq_ev_note
    public static final int SND_SEQ_EVENT_NOTE = 5;
    public static final int SND_SEQ_EVENT_NOTEON = 6;
    public static final int SND_SEQ_EVENT_NOTEOFF = 7;
    public static final int SND_SEQ_EVENT_KEYPRESS = 8;

    // 9-10: reserved

    // 10-19: control messages (channel specific)
    // event data type = snd_seq_ev_ctrl
    public static final int SND_SEQ_EVENT_CONTROLLER = 10;
    public static final int SND_SEQ_EVENT_PGMCHANGE = 11;
    public static final int SND_SEQ_EVENT_CHANPRESS = 12;
    public static final int SND_SEQ_EVENT_PITCHBEND = 13; // from -8192 to 8191
    /** 14 bit controller value */
    public static final int SND_SEQ_EVENT_CONTROL14 = 14;
    /** 14 bit NRPN */
    public static final int SND_SEQ_EVENT_NONREGPARAM = 15;
    /** 14 bit RPN */
    public static final int SND_SEQ_EVENT_REGPARAM = 16;

    // 18-19: reserved

    // 20-29: synchronisation messages
    // event data type = snd_seq_ev_ctrl
    /** Song Position Pointer with LSB and MSB values */
    public static final int SND_SEQ_EVENT_SONGPOS = 20;
    /** Song Select with song ID number */
    public static final int SND_SEQ_EVENT_SONGSEL = 21;
    /** midi time code quarter frame */
    public static final int SND_SEQ_EVENT_QFRAME = 22;
    /** SMF Time Signature event */
    public static final int SND_SEQ_EVENT_TIMESIGN = 23;
    /** SMF Key Signature event */
    public static final int SND_SEQ_EVENT_KEYSIGN = 24;
    // 25-29: reserved

    // 30-39: timer messages
    // event data type = snd_seq_ev_queue_control_t
    /** midi Real Time Start message */
    public static final int SND_SEQ_EVENT_START = 30;
    /** midi Real Time Continue message */
    public static final int SND_SEQ_EVENT_CONTINUE = 31;
    /** midi Real Time Stop message */
    public static final int SND_SEQ_EVENT_STOP = 32;
    /** set tick queue position */
    public static final int SND_SEQ_EVENT_SETPOS_TICK = 33;
    /** set realtime queue position */
    public static final int SND_SEQ_EVENT_SETPOS_TIME = 34;
    /** (SMF) Tempo event */
    public static final int SND_SEQ_EVENT_TEMPO = 35;
    /** midi Real Time Clock message */
    public static final int SND_SEQ_EVENT_CLOCK = 36;
    /** midi Real Time Tick message */
    public static final int SND_SEQ_EVENT_TICK = 37;
    // 38-39: sync
    public static final int SND_SEQ_EVENT_SYNC = 38;
    public static final int SND_SEQ_EVENT_SYNC_POS = 39;
    // 40-49: others
    // event data type = none
    /** tune request */
    public static final int SND_SEQ_EVENT_TUNE_REQUEST = 40;
    /** reset to power-on state */
    public static final int SND_SEQ_EVENT_RESET = 41;
    /** "active sensing" event */
    public static final int SND_SEQ_EVENT_SENSING = 42;

    // 43-49: reserved

    // 50-59: echo back, kernel private messages
    // event data type = any type
    /** echo event */
    public static final int SND_SEQ_EVENT_ECHO = 50;
    /** OSS raw event */
    public static final int SND_SEQ_EVENT_OSS = 51;

    // 52-59: reserved

    // 60-69: system status messages (broadcast for subscribers)
    // event data type = snd_seq_addr_t
    /** new client has connected */
    public static final int SND_SEQ_EVENT_CLIENT_START = 60;
    /** client has left the system */
    public static final int SND_SEQ_EVENT_CLIENT_EXIT = 61;
    /** client status/info has changed */
    public static final int SND_SEQ_EVENT_CLIENT_CHANGE = 62;
    /** new port was created */
    public static final int SND_SEQ_EVENT_PORT_START = 63;
    /** port was deleted from system */
    public static final int SND_SEQ_EVENT_PORT_EXIT = 64;
    /** port status/info has changed */
    public static final int SND_SEQ_EVENT_PORT_CHANGE = 65;
    /** read port is subscribed */
    public static final int SND_SEQ_EVENT_PORT_SUBSCRIBED = 66;
    /** read port is released */
    public static final int SND_SEQ_EVENT_PORT_UNSUBSCRIBED = 67;

    // 70-79: synthesizer events
    // event data type = snd_seq_eve_sample_control_t
    /** sample select */
    public static final int SND_SEQ_EVENT_SAMPLE = 70;
    /** sample cluster select */
    public static final int SND_SEQ_EVENT_SAMPLE_CLUSTER = 71;
    /** voice start */
    public static final int SND_SEQ_EVENT_SAMPLE_START = 72;
    /** voice stop */
    public static final int SND_SEQ_EVENT_SAMPLE_STOP = 73;
    /** playback frequency */
    public static final int SND_SEQ_EVENT_SAMPLE_FREQ = 74;
    /** volume and balance */
    public static final int SND_SEQ_EVENT_SAMPLE_VOLUME = 75;
    /** sample loop */
    public static final int SND_SEQ_EVENT_SAMPLE_LOOP = 76;
    /** sample position */
    public static final int SND_SEQ_EVENT_SAMPLE_POSITION = 77;
    /** private (hardware dependent) event */
    public static final int SND_SEQ_EVENT_SAMPLE_PRIVATE1 = 78;

    // 80-89: reserved

    // 90-99: user-defined events with fixed length
    // event data type = any
    public static final int SND_SEQ_EVENT_USR0 = 90;
    public static final int SND_SEQ_EVENT_USR1 = 91;
    public static final int SND_SEQ_EVENT_USR2 = 92;
    public static final int SND_SEQ_EVENT_USR3 = 93;
    public static final int SND_SEQ_EVENT_USR4 = 94;
    public static final int SND_SEQ_EVENT_USR5 = 95;
    public static final int SND_SEQ_EVENT_USR6 = 96;
    public static final int SND_SEQ_EVENT_USR7 = 97;
    public static final int SND_SEQ_EVENT_USR8 = 98;
    public static final int SND_SEQ_EVENT_USR9 = 99;

    // 100-129: instrument layer
    // variable length data can be passed directly to the driver
    /** begin of instrument management */
    public static final int SND_SEQ_EVENT_INSTR_BEGIN = 100;
    /** end of instrument management */
    public static final int SND_SEQ_EVENT_INSTR_END = 101;
    /** instrument interface info */
    public static final int SND_SEQ_EVENT_INSTR_INFO = 102;
    /** result */
    public static final int SND_SEQ_EVENT_INSTR_INFO_RESULT = 103;
    /** get format info */
    public static final int SND_SEQ_EVENT_INSTR_FINFO = 104;
    /** get format info */
    public static final int SND_SEQ_EVENT_INSTR_FINFO_RESULT = 105;
    /** reset instrument memory */
    public static final int SND_SEQ_EVENT_INSTR_RESET = 106;
    /** instrument interface status */
    public static final int SND_SEQ_EVENT_INSTR_STATUS = 107;
    /** result */
    public static final int SND_SEQ_EVENT_INSTR_STATUS_RESULT = 108;
    /** put instrument to port */
    public static final int SND_SEQ_EVENT_INSTR_PUT = 109;
    /** get instrument from port */
    public static final int SND_SEQ_EVENT_INSTR_GET = 110;
    /** result */
    public static final int SND_SEQ_EVENT_INSTR_GET_RESULT = 111;
    /** free instrument(s) */
    public static final int SND_SEQ_EVENT_INSTR_FREE = 112;
    /** instrument list */
    public static final int SND_SEQ_EVENT_INSTR_LIST = 113;
    /** result */
    public static final int SND_SEQ_EVENT_INSTR_LIST_RESULT = 114;
    /** cluster parameters */
    public static final int SND_SEQ_EVENT_INSTR_CLUSTER = 115;
    /** get cluster parameters */
    public static final int SND_SEQ_EVENT_INSTR_CLUSTER_GET = 116;
    /** result */
    public static final int SND_SEQ_EVENT_INSTR_CLUSTER_RESULT = 117;
    /** instrument change */
    public static final int SND_SEQ_EVENT_INSTR_CHANGE = 118;

    // 119-129: reserved

    // 130-139: variable length events
    // event data type = snd_seq_ev_ext
    // (SND_SEQ_EVENT_LENGTH_VARIABLE must be set)
    /** system exclusive data (variable length) */
    public static final int SND_SEQ_EVENT_SYSEX = 130;
    /** error event */
    public static final int SND_SEQ_EVENT_BOUNCE = 131;
    // 132-134: reserved
    public static final int SND_SEQ_EVENT_USR_VAR0 = 135;
    public static final int SND_SEQ_EVENT_USR_VAR1 = 136;
    public static final int SND_SEQ_EVENT_USR_VAR2 = 137;
    public static final int SND_SEQ_EVENT_USR_VAR3 = 138;
    public static final int SND_SEQ_EVENT_USR_VAR4 = 139;

    /** 255: special event */
    public static final int SND_SEQ_EVENT_NONE = 255;

    /** unknown source */
    public static final int SND_SEQ_ADDRESS_UNKNOWN = 253;
    /** send event to all subscribed ports */
    public static final int SND_SEQ_ADDRESS_SUBSCRIBERS = 254;
    /** send event to all queues/clients/ports/channels */
    public static final int SND_SEQ_ADDRESS_BROADCAST = 255;
    /** direct dispatch */
    public static final int SND_SEQ_QUEUE_DIRECT = 253;

    // event mode flag - NOTE: only 8 bits available!
    /** timestamp in clock ticks */
    public static final int SND_SEQ_TIME_STAMP_TICK = 0 << 0;
    /** timestamp in real time */
    public static final int SND_SEQ_TIME_STAMP_REAL = 1 << 0;
    public static final int SND_SEQ_TIME_STAMP_MASK = 1 << 0;

    /** absolute timestamp */
    public static final int SND_SEQ_TIME_MODE_ABS = 0 << 1;
    /** relative to current time */
    public static final int SND_SEQ_TIME_MODE_REL = 1 << 1;
    public static final int SND_SEQ_TIME_MODE_MASK = 1 << 1;

    /** fixed event size */
    public static final int SND_SEQ_EVENT_LENGTH_FIXED = 0 << 2;
    /** variable event size */
    public static final int SND_SEQ_EVENT_LENGTH_VARIABLE = 1 << 2;
    /** variable event size - user memory space */
    public static final int SND_SEQ_EVENT_LENGTH_VARUSR = 2 << 2;
    /** variable event size - IPC */
    public static final int SND_SEQ_EVENT_LENGTH_VARIPC = 3 << 2;
    public static final int SND_SEQ_EVENT_LENGTH_MASK = 3 << 2;

    /** normal priority */
    public static final int SND_SEQ_PRIORITY_NORMAL = 0 << 4;
    /** event should be processed before others */
    public static final int SND_SEQ_PRIORITY_HIGH = 1 << 4;
    public static final int SND_SEQ_PRIORITY_MASK = 1 << 4;

    // known client numbers
    public static final int SND_SEQ_CLIENT_SYSTEM = 0;
    /** dummy ports */
    public static final int SND_SEQ_CLIENT_DUMMY = 62;
    /** oss sequencer emulator */
    public static final int SND_SEQ_CLIENT_OSS = 63;

    // event filter flags
    /** accept broadcast messages */
    public static final int SND_SEQ_FILTER_BROADCAST = 1 << 0;
    /** accept multicast messages */
    public static final int SND_SEQ_FILTER_MULTICAST = 1 << 1;
    /** accept bounce event in error */
    public static final int SND_SEQ_FILTER_BOUNCE = 1 << 2;
    /** use event filter */
    public static final long SND_SEQ_FILTER_USE_EVENT = 1L << 31;

    // Flush mode flags
    /** Restrict by destination q:client:port */
    public static final int SND_SEQ_REMOVE_INPUT = 1 << 0;
    /** Restrict by channel */
    public static final int SND_SEQ_REMOVE_OUTPUT = 1 << 1;
    /** Restrict by destination q:client:port */
    public static final int SND_SEQ_REMOVE_DEST = 1 << 2;
    /** Restrict by channel */
    public static final int SND_SEQ_REMOVE_DEST_CHANNEL = 1 << 3;
    /** Restrict to before time */
    public static final int SND_SEQ_REMOVE_TIME_BEFORE = 1 << 4;
    /** Restrict to time or after */
    public static final int SND_SEQ_REMOVE_TIME_AFTER = 1 << 5;
    /** Restrict to time or after */
    public static final int SND_SEQ_REMOVE_TIME_TICK = 1 << 6;
    /** Restrict to event type */
    public static final int SND_SEQ_REMOVE_EVENT_TYPE = 1 << 7;
    /** Do not flush off events */
    public static final int SND_SEQ_REMOVE_IGNORE_OFF = 1 << 8;
    /** Restrict to events with given tag */
    public static final int SND_SEQ_REMOVE_TAG_MATCH = 1 << 9;

    // known port numbers
    public static final int SND_SEQ_PORT_SYSTEM_TIMER = 0;
    public static final int SND_SEQ_PORT_SYSTEM_ANNOUNCE = 1;

    // port capabilities (32 bits)
    /** readable from this port */
    public static final int SND_SEQ_PORT_CAP_READ = 1 << 0;
    /** writable to this port */
    public static final int SND_SEQ_PORT_CAP_WRITE = 1 << 1;

    public static final int SND_SEQ_PORT_CAP_SYNC_READ = 1 << 2;
    public static final int SND_SEQ_PORT_CAP_SYNC_WRITE = 1 << 3;

    public static final int SND_SEQ_PORT_CAP_DUPLEX = 1 << 4;

    /** allow read subscription */
    public static final int SND_SEQ_PORT_CAP_SUBS_READ = 1 << 5;
    /** allow write subscription */
    public static final int SND_SEQ_PORT_CAP_SUBS_WRITE = 1 << 6;
    /** routing not allowed */
    public static final int SND_SEQ_PORT_CAP_NO_EXPORT = 1 << 7;

    // port type
    /** hardware specific */
    public static final int SND_SEQ_PORT_TYPE_SPECIFIC = 1 << 0;
    /** generic MIDI device */
    public static final int SND_SEQ_PORT_TYPE_MIDI_GENERIC = 1 << 1;
    /** General MIDI compatible device */
    public static final int SND_SEQ_PORT_TYPE_MIDI_GM = 1 << 2;
    /** GS compatible device */
    public static final int SND_SEQ_PORT_TYPE_MIDI_GS = 1 << 3;
    /** XG compatible device */
    public static final int SND_SEQ_PORT_TYPE_MIDI_XG = 1 << 4;
    /** MT-32 compatible device */
    public static final int SND_SEQ_PORT_TYPE_MIDI_MT32 = 1 << 5;

    // other standards...

    /** Synth device */
    public static final int SND_SEQ_PORT_TYPE_SYNTH = 1 << 10;
    /** Sampling device (support sample download) */
    public static final int SND_SEQ_PORT_TYPE_DIRECT_SAMPLE = 1 << 11;
    /** Sampling device (sample can be downloaded at any time) */
    public static final int SND_SEQ_PORT_TYPE_SAMPLE = 1 << 12;
    // ...
    /** application (sequencer/editor) */
    public static final int SND_SEQ_PORT_TYPE_APPLICATION = 1 << 20;

    // standard group names
    public static final String SND_SEQ_GROUP_SYSTEM = "system";
    public static final String SND_SEQ_GROUP_DEVICE = "device";
    public static final String SND_SEQ_GROUP_APPLICATION = "application";

    // misc. conditioning flags
    public static final int SND_SEQ_PORT_FLG_GIVEN_PORT = 1 << 0;

    // queue flags
    /** sync enabled */
    public static final int SND_SEQ_QUEUE_FLG_SYNC = 1 << 0;

    /** queue status flag */
    public static final int SND_SEQ_QUEUE_FLG_SYNC_LOST = 1;

    // synchronization types

    // mode
    public static final int SND_SEQ_SYNC_TICK = 0x80;
    public static final int SND_SEQ_SYNC_TIME = 0x40;
    public static final int SND_SEQ_SYNC_MODE = 0xc0; // mask

    // private format
    public static final int SND_SEQ_SYNC_FMT_PRIVATE_CLOCK = SND_SEQ_SYNC_TICK | 0;
    public static final int SND_SEQ_SYNC_FMT_PRIVATE_TIME = SND_SEQ_SYNC_TIME | 0;

    // pre-defined format
    public static final int SND_SEQ_SYNC_FMT_MIDI_CLOCK = SND_SEQ_SYNC_TICK | 1;
    public static final int SND_SEQ_SYNC_FMT_MTC = SND_SEQ_SYNC_TIME | 1;
    public static final int SND_SEQ_SYNC_FMT_DTL = SND_SEQ_SYNC_TIME | 2;
    public static final int SND_SEQ_SYNC_FMT_SMPTE = SND_SEQ_SYNC_TIME | 3;
    public static final int SND_SEQ_SYNC_FMT_MIDI_TICK = SND_SEQ_SYNC_TIME | 4;

    // time format
    public static final int SND_SEQ_SYNC_FPS_24 = 0;
    public static final int SND_SEQ_SYNC_FPS_25 = 1;
    public static final int SND_SEQ_SYNC_FPS_30_DP = 2;
    public static final int SND_SEQ_SYNC_FPS_30_NDP = 3;

    // sequencer timer sources

    /** ALSA timer */
    public static final int SND_SEQ_TIMER_ALSA = 0;
    /** Midi Clock (CLOCK event) */
    public static final int SND_SEQ_TIMER_MIDI_CLOCK = 1;
    /** Midi Timer Tick (TICK event) */
    public static final int SND_SEQ_TIMER_MIDI_TICK = 2;

    // type of query subscription
    public static final int SND_SEQ_QUERY_SUBS_READ = 0;
    public static final int SND_SEQ_QUERY_SUBS_WRITE = 1;

    // instrument types

    /** instrument data */
    public static final int SND_SEQ_INSTR_ATYPE_DATA = 0;
    /** instrument alias */
    public static final int SND_SEQ_INSTR_ATYPE_ALIAS = 1;

    // instrument ASCII identifiers
    public static final String SND_SEQ_INSTR_ID_DLS1 = "DLS1";
    public static final String SND_SEQ_INSTR_ID_DLS2 = "DLS2";
    public static final String SND_SEQ_INSTR_ID_SIMPLE = "Simple Wave";
    public static final String SND_SEQ_INSTR_ID_SOUNDFONT = "SoundFont";
    public static final String SND_SEQ_INSTR_ID_GUS_PATCH = "GUS Patch";
    public static final String SND_SEQ_INSTR_ID_INTERWAVE = "InterWave FFFF";
    public static final String SND_SEQ_INSTR_ID_OPL2_3 = "OPL2/3 FM";
    public static final String SND_SEQ_INSTR_ID_OPL4 = "OPL4";

    // instrument types
    /** MIDI DLS v1 */
    public static final int SND_SEQ_INSTR_TYPE0_DLS1 = 1 << 0;
    /** MIDI DLS v2 */
    public static final int SND_SEQ_INSTR_TYPE0_DLS2 = 1 << 1;
    /** Simple Wave */
    public static final int SND_SEQ_INSTR_TYPE1_SIMPLE = 1 << 0;
    /** EMU SoundFont */
    public static final int SND_SEQ_INSTR_TYPE1_SOUNDFONT = 1 << 1;
    /** Gravis UltraSound Patch */
    public static final int SND_SEQ_INSTR_TYPE1_GUS_PATCH = 1 << 2;
    /** InterWave FFFF */
    public static final int SND_SEQ_INSTR_TYPE1_INTERWAVE = 1 << 3;
    /** Yamaha OPL2/3 FM */
    public static final int SND_SEQ_INSTR_TYPE2_OPL2_3 = 1 << 0;
    /** Yamaha OPL4 */
    public static final int SND_SEQ_INSTR_TYPE2_OPL4 = 1 << 1;

    // put commands
    public static final int SND_SEQ_INSTR_PUT_CMD_CREATE = 0;
    public static final int SND_SEQ_INSTR_PUT_CMD_REPLACE = 1;
    public static final int SND_SEQ_INSTR_PUT_CMD_MODIFY = 2;
    public static final int SND_SEQ_INSTR_PUT_CMD_ADD = 3;
    public static final int SND_SEQ_INSTR_PUT_CMD_REMOVE = 4;

    // get commands
    public static final int SND_SEQ_INSTR_GET_CMD_FULL = 0;
    public static final int SND_SEQ_INSTR_GET_CMD_PARTIAL = 1;

    // query flags
    public static final int SND_SEQ_INSTR_QUERY_FOLLOW_ALIAS = 1 << 0;

    // free commands
    public static final int SND_SEQ_INSTR_FREE_CMD_ALL = 0;
    public static final int SND_SEQ_INSTR_FREE_CMD_PRIVATE = 1;
    public static final int SND_SEQ_INSTR_FREE_CMD_CLUSTER = 2;
    public static final int SND_SEQ_INSTR_FREE_CMD_SINGLE = 3;

    static {
        Alsa.loadNativeLibrary();
    }

    /**
     * This holds a pointer for the native code - do not touch!
     */
    @SuppressWarnings("unused")
    private long nativeHandle;

    public AlsaSeq() {
        super();
        logger.log(Level.TRACE, "begin");

        int success = open();
        if (success < 0) {
            throw new RuntimeException("open failed");
        }

        logger.log(Level.TRACE, "end");
    }

    public AlsaSeq(String clientName) {
        this();
        logger.log(Level.TRACE, "begin");

        setClientName(clientName);

        logger.log(Level.TRACE, "end");
    }

    /**
     * Opens the sequencer.
     * This method is intended to be called by the constructor.
     * Calls snd_seq_open().
     */
    private native int open();

    /**
     * Closes the sequencer.
     * Calls snd_seq_close().
     */
    public native void close();

    public native String getName();

    public native int getType();

    public native int setNonblock(boolean nonblock);

    public native int getClientId();

    public native int getOutputBufferSize();

    public native int getInputBufferSize();

    public native int setOutputBufferSize(int size);

    public native int setInputBufferSize(int size);

    public native int getSystemInfo(AlsaSeqSystemInfo systemInfo);

    public int getClientInfo(AlsaSeqClientInfo clientInfo) {
        return getClientInfo(-1, clientInfo);
    }

    public native int getClientInfo(int client, AlsaSeqClientInfo clientInfo);

    public native int setClientInfo(AlsaSeqClientInfo clientInfo);

    /**
     * Gets information about the next client.
     * Calls snd_seq_query_next_client().
     * and puts the returned values
     * into the passed arrays.
     * <p>
     * client has to be -1 to start, or a client id returned by
     * a previous call to this method.
     * <p>
     * values[0] client id
     * <p>
     * Returns 0 if successful.
     */
    public native int getNextClient(int client, int[] values);

    public void setClientName(String name) {
        logger.log(Level.TRACE, "begin");

        AlsaSeqClientInfo clientInfo = new AlsaSeqClientInfo();
        // TODO error check
        getClientInfo(clientInfo);
        clientInfo.setName(name);
        setClientInfo(clientInfo);

        logger.log(Level.TRACE, "end");
    }

    public int getPortInfo(int port, AlsaSeqPortInfo portInfo) {
        return getPortInfo(-1, port, portInfo);
    }

    public native int getPortInfo(int client, int port, AlsaSeqPortInfo portInfo);

    /**
     * Gets the next port.
     * Calls snd_seq_query_next_port().
     * and put the returned values
     * into the passed arrays.
     * <p>
     * client has to be a valid client.
     * port has to be -1 to start, or a port returned by
     * a previous call to this method.
     * <p>
     * values[0] client
     * values[1] port
     * <p>
     * Returns 0 if successful.
     */
    public native int getNextPort(int client, int port, int[] values);

    // TODO use structure
    public native int createPort(String name, int capabilities, int groupPermissions, int type, int midiChannels, int midiVoices, int synthVoices);

    /**
     * Allocates (reserves) a sequencing queue.
     * Calls snd_seq_alloc_queue().
     *
     * @return the queue number (>= 0), if successful. A negative
     * value otherwise.
     */
    public native int allocQueue();

    /**
     * Frees a sequencing queue.
     * Calls snd_seq_free_queue().
     *
     * @param queue a queue number that has previously been
     *               allocated with allocQueue().
     * @return 0 if successful. A negative
     * value otherwise.
     */
    public native int freeQueue(int queue);

    /**
     * Get the queue usage flag.
     * Calls snd_seq_get_queue_usage().
     *
     * @param queue a queue number that has previously been
     *               allocated with allocQueue().
     * @return true if the client is allowed to access the queue.
     * false otherwise.
     */
    public native boolean getQueueUsage(int queue);

    /**
     * Set the queue usage flag.
     * Calls snd_seq_set_queue_usage().
     *
     * @param queue        a queue number that has previously been
     *                      allocated with allocQueue().
     * @param usageAllowed true to allow the client access to this
     *                      queue. false to deny it.
     * @return 0 if successful. A negative
     * value otherwise.
     */
    public native int setQueueUsage(int queue, boolean usageAllowed);

    /**
     * Get the queue information.
     * This method fills a QueueInfo instance with information
     * from the given queue. Internally, snd_seq_get_queue_info()
     * is called.
     *
     * @return returns 0 on success, otherwise a negative value.
     */
    public native int getQueueInfo(int queue, AlsaSeqQueueInfo queueInfo);

    /**
     * Set the queue information.
     * This method sets the information for the given queue from
     * the QueueInfo instance. Internally, snd_seq_set_queue_info()
     * is called.
     *
     * @return returns 0 on success, otherwise a negative value.
     */
    public native int setQueueInfo(int queue, AlsaSeqQueueInfo queueInfo);

    /**
     * Get the queue status.
     * This method fills a QueueStatus instance with information
     * from the given queue. Internally, snd_seq_get_queue_status()
     * is called.
     *
     * @return returns 0 on success, otherwise a negative value.
     */
    public native int getQueueStatus(int queue, AlsaSeqQueueStatus queueStatus);

    /**
     * Get the queue tempo.
     * This method fills a QueueTempo instance with information
     * from the given queue. Internally, snd_seq_get_queue_tempo()
     * is called.
     *
     * @return returns 0 on success, otherwise a negative value.
     */
    public native int getQueueTempo(int queue, AlsaSeqQueueTempo queueTempo);

    /**
     * Set the queue tempo.
     * This method sets the information for the given queue from
     * the QueueTempo instance. Internally, snd_seq_set_queue_tempo()
     * is called.
     *
     * @return returns 0 on success, otherwise a negative value.
     */
    public native int setQueueTempo(int queue, AlsaSeqQueueTempo queueTempo);

    /**
     * Get the queue timer.
     * This method fills a QueueTimer instance with information
     * from the given queue. Internally, snd_seq_get_queue_timer()
     * is called.
     *
     * @return returns 0 on success, otherwise a negative value.
     */
    public native int getQueueTimer(int queue, AlsaSeqQueueTimer queueTimer);

    /**
     * Set the queue timer.
     * This method sets the timer for the given queue from
     * the QueueTimer instance. Internally, snd_seq_set_queue_timer()
     * is called.
     *
     * @return returns 0 on success, otherwise a negative value.
     */
    public native int setQueueTimer(int queue, AlsaSeqQueueTimer queueTimer);

    public native int getPortSubscription(AlsaSeqPortSubscribe portSubscribe);

    public native int subscribePort(AlsaSeqPortSubscribe portSubscribe);

    public native int unsubscribePort(AlsaSeqPortSubscribe portSubscribe);

    private static native void setTrace(boolean trace);

    public Iterator<AlsaSeqClientInfo> getClientInfos() {
        return new ClientInfoIterator();
    }

    public Iterator<AlsaSeqPortInfo> getPortInfos(int client) {
        return new PortInfoIterator(client);
    }

    //
    // Events
    //

    public native int eventOutput(AlsaSeqEvent event);

    public native int eventOutputBuffer(AlsaSeqEvent event);

    public native int eventOutputDirect(AlsaSeqEvent event);

    public native int eventInput(AlsaSeqEvent event);

    public native int eventInputPending(int fetchSequencer);

    public native int drainOutput();

    public native int eventOutputPending();

    public native int extractOutput(AlsaSeqEvent event);

    public native int dropOutput();

    public native int dropOutputBuffer();

    public native int dropInput();

    public native int dropInputBuffer();

    private class ClientInfoIterator implements Iterator<AlsaSeqClientInfo> {

        private int client;
        private AlsaSeqClientInfo clientInfo;

        public ClientInfoIterator() {
            client = -1;
            clientInfo = createNextClientInfo();
        }

        @Override
        public boolean hasNext() {
//logger.log(Level.TRACE, "clientInfo: " + clientInfo);
            return clientInfo != null;
        }

        @Override
        public AlsaSeqClientInfo next() {
            AlsaSeqClientInfo next = clientInfo;
            clientInfo = createNextClientInfo();
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private AlsaSeqClientInfo createNextClientInfo() {
            logger.log(Level.TRACE, "begin");

            AlsaSeqClientInfo clientInfo = null;
            int[] values = new int[1];
            int success = getNextClient(client, values);
            logger.log(Level.TRACE, "succ: " + success);

            if (success == 0) {
//logger.log(Level.TRACE, "getNextClientInfo successful");
                client = values[0];
                clientInfo = new AlsaSeqClientInfo();
                // TODO error check
                getClientInfo(client, clientInfo);
            }

            logger.log(Level.TRACE, "end");

            return clientInfo;
        }
    }

    private class PortInfoIterator implements Iterator<AlsaSeqPortInfo> {

        private final int client;
        private int port;
        private AlsaSeqPortInfo portInfo;

        public PortInfoIterator(int client) {
            logger.log(Level.TRACE, "begin");

            this.client = client;
            port = -1;
            portInfo = createNextPortInfo();

            logger.log(Level.TRACE, "end");
        }

        @Override
        public boolean hasNext() {
            return portInfo != null;
        }

        @Override
        public AlsaSeqPortInfo next() {
            AlsaSeqPortInfo next = portInfo;
            portInfo = createNextPortInfo();
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private AlsaSeqPortInfo createNextPortInfo() {
            logger.log(Level.TRACE, "begin");

            AlsaSeqPortInfo portInfo = null;
            int[] values = new int[2];
            int success = getNextPort(client, port, values);
            logger.log(Level.TRACE, "getNextPort() returns: " + success);

            if (success == 0) {
                port = values[1];
                portInfo = new AlsaSeqPortInfo();
                // TODO error check
                getPortInfo(client, port, portInfo);
            }

            logger.log(Level.TRACE, "end");

            return portInfo;
        }
    }
}
