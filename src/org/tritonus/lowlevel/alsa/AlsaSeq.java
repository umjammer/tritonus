/*
 *	AlsaSeq.java
 */

/*
 *  Copyright (c) 1999 - 2001 by Matthias Pfisterer <Matthias.Pfisterer@gmx.de>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */


package	org.tritonus.lowlevel.alsa;


import	java.lang.UnsupportedOperationException;

import	java.util.Iterator;

import	javax.sound.midi.MidiEvent;
import	javax.sound.midi.MidiMessage;
import	javax.sound.midi.ShortMessage;
import	javax.sound.midi.SysexMessage;
import	javax.sound.midi.MetaMessage;
import	javax.sound.midi.InvalidMidiDataException;

import	org.tritonus.share.TDebug;


public class AlsaSeq
{
/*                                   	*/
/* definition of sequencer event types 	*/
/*                                   	*/

/* 0-4: system messages
 * event data type = snd_seq_result_t
 */
	public static final int	SND_SEQ_EVENT_SYSTEM =		0;
	public static final int	SND_SEQ_EVENT_RESULT =		1;
/* 2-4: reserved */

/* 5-9: note messages (channel specific)
 * event data type = snd_seq_ev_note
 */
	public static final int	SND_SEQ_EVENT_NOTE =		5;
	public static final int	SND_SEQ_EVENT_NOTEON =		6;
	public static final int	SND_SEQ_EVENT_NOTEOFF =		7;
	public static final int	SND_SEQ_EVENT_KEYPRESS =	8;
/* 9-10: reserved */

/* 10-19: control messages (channel specific)
 * event data type = snd_seq_ev_ctrl
 */
	public static final int	SND_SEQ_EVENT_CONTROLLER =	10;
	public static final int	SND_SEQ_EVENT_PGMCHANGE =	11;
	public static final int	SND_SEQ_EVENT_CHANPRESS =	12;
	public static final int	SND_SEQ_EVENT_PITCHBEND =	13;	/* from -8192 to 8191 */
	public static final int	SND_SEQ_EVENT_CONTROL14 =	14;	/* 14 bit controller value */
	public static final int	SND_SEQ_EVENT_NONREGPARAM =	15;	/* 14 bit NRPN */
	public static final int	SND_SEQ_EVENT_REGPARAM =	16;	/* 14 bit RPN */
/* 18-19: reserved */

/* 20-29: synchronisation messages
 * event data type = snd_seq_ev_ctrl
 */
	public static final int	SND_SEQ_EVENT_SONGPOS =		20;	/* Song Position Pointer with LSB and MSB values */
	public static final int	SND_SEQ_EVENT_SONGSEL =		21;	/* Song Select with song ID number */
	public static final int	SND_SEQ_EVENT_QFRAME =		22;	/* midi time code quarter frame */
	public static final int	SND_SEQ_EVENT_TIMESIGN =	23;	/* SMF Time Signature event */
	public static final int	SND_SEQ_EVENT_KEYSIGN =		24;	/* SMF Key Signature event */
/* 25-29: reserved */
	        
/* 30-39: timer messages
 * event data type = snd_seq_ev_queue_control_t
 */
	public static final int	SND_SEQ_EVENT_START =		30;	/* midi Real Time Start message */
	public static final int	SND_SEQ_EVENT_CONTINUE =	31;	/* midi Real Time Continue message */
	public static final int	SND_SEQ_EVENT_STOP =		32;	/* midi Real Time Stop message */	
	public static final int	SND_SEQ_EVENT_SETPOS_TICK =	33;	/* set tick queue position */
	public static final int	SND_SEQ_EVENT_SETPOS_TIME =	34;	/* set realtime queue position */
	public static final int	SND_SEQ_EVENT_TEMPO =		35;	/* (SMF) Tempo event */
	public static final int	SND_SEQ_EVENT_CLOCK =		36;	/* midi Real Time Clock message */
	public static final int	SND_SEQ_EVENT_TICK =		37;	/* midi Real Time Tick message */
/* 38-39: sync */
	public static final int	SND_SEQ_EVENT_SYNC =		38;
	public static final int	SND_SEQ_EVENT_SYNC_POS =	39;
/* 40-49: others
 * event data type = none
 */
	public static final int	SND_SEQ_EVENT_TUNE_REQUEST =	40;	/* tune request */
	public static final int	SND_SEQ_EVENT_RESET =		41;	/* reset to power-on state */
	public static final int	SND_SEQ_EVENT_SENSING =		42;	/* "active sensing" event */
/* 43-49: reserved */

/* 50-59: echo back, kernel private messages
 * event data type = any type
 */
	public static final int	SND_SEQ_EVENT_ECHO =		50;	/* echo event */
	public static final int	SND_SEQ_EVENT_OSS =		51;	/* OSS raw event */
/* 52-59: reserved */

/* 60-69: system status messages (broadcast for subscribers)
 * event data type = snd_seq_addr_t
 */
	public static final int	SND_SEQ_EVENT_CLIENT_START =	60;	/* new client has connected */
	public static final int	SND_SEQ_EVENT_CLIENT_EXIT =	61;	/* client has left the system */
	public static final int	SND_SEQ_EVENT_CLIENT_CHANGE =	62;	/* client status/info has changed */
	public static final int	SND_SEQ_EVENT_PORT_START =	63;	/* new port was created */
	public static final int	SND_SEQ_EVENT_PORT_EXIT =	64;	/* port was deleted from system */
	public static final int	SND_SEQ_EVENT_PORT_CHANGE =	65;	/* port status/info has changed */
	public static final int	SND_SEQ_EVENT_PORT_SUBSCRIBED =	66;	/* read port is subscribed */
	public static final int	SND_SEQ_EVENT_PORT_USED =	67;	/* write port is subscribed */
	public static final int	SND_SEQ_EVENT_PORT_UNSUBSCRIBED =	68;	/* read port is released */
	public static final int	SND_SEQ_EVENT_PORT_UNUSED =	69;	/* write port is released */

/* 70-79: synthesizer events
 * event data type = snd_seq_eve_sample_control_t
 */
	public static final int	SND_SEQ_EVENT_SAMPLE =		70;	/* sample select */
	public static final int	SND_SEQ_EVENT_SAMPLE_CLUSTER =	71;	/* sample cluster select */
	public static final int	SND_SEQ_EVENT_SAMPLE_START =	72;	/* voice start */
	public static final int	SND_SEQ_EVENT_SAMPLE_STOP =	73;	/* voice stop */
	public static final int	SND_SEQ_EVENT_SAMPLE_FREQ =	74;	/* playback frequency */
	public static final int	SND_SEQ_EVENT_SAMPLE_VOLUME =	75;	/* volume and balance */
	public static final int	SND_SEQ_EVENT_SAMPLE_LOOP =	76;	/* sample loop */
	public static final int	SND_SEQ_EVENT_SAMPLE_POSITION =	77;	/* sample position */
	public static final int	SND_SEQ_EVENT_SAMPLE_PRIVATE1 =	78;	/* private (hardware dependent) event */

/* 80-89: reserved */

/* 90-99: user-defined events with fixed length
 * event data type = any
 */
	public static final int	SND_SEQ_EVENT_USR0 =		90;
	public static final int	SND_SEQ_EVENT_USR1 =		91;
	public static final int	SND_SEQ_EVENT_USR2 =		92;
	public static final int	SND_SEQ_EVENT_USR3 =		93;
	public static final int	SND_SEQ_EVENT_USR4 =		94;
	public static final int	SND_SEQ_EVENT_USR5 =		95;
	public static final int	SND_SEQ_EVENT_USR6 =		96;
	public static final int	SND_SEQ_EVENT_USR7 =		97;
	public static final int	SND_SEQ_EVENT_USR8 =		98;
	public static final int	SND_SEQ_EVENT_USR9 =		99;

/* 100-129: instrument layer
 * variable length data can be passed directly to the driver
 */
	public static final int	SND_SEQ_EVENT_INSTR_BEGIN =	100;	/* begin of instrument management */
	public static final int	SND_SEQ_EVENT_INSTR_END =	101;	/* end of instrument management */
	public static final int	SND_SEQ_EVENT_INSTR_INFO =	102;	/* instrument interface info */
	public static final int	SND_SEQ_EVENT_INSTR_INFO_RESULT = 103;	/* result */
	public static final int	SND_SEQ_EVENT_INSTR_FINFO =	104;	/* get format info */
	public static final int	SND_SEQ_EVENT_INSTR_FINFO_RESULT = 105;	/* get format info */
	public static final int	SND_SEQ_EVENT_INSTR_RESET =	106;	/* reset instrument memory */
	public static final int	SND_SEQ_EVENT_INSTR_STATUS =	107;	/* instrument interface status */
	public static final int	SND_SEQ_EVENT_INSTR_STATUS_RESULT = 108;	/* result */
	public static final int	SND_SEQ_EVENT_INSTR_PUT =	109;	/* put instrument to port */
	public static final int	SND_SEQ_EVENT_INSTR_GET =	110;	/* get instrument from port */
	public static final int	SND_SEQ_EVENT_INSTR_GET_RESULT =	111;	/* result */
	public static final int	SND_SEQ_EVENT_INSTR_FREE =	112;	/* free instrument(s) */
	public static final int	SND_SEQ_EVENT_INSTR_LIST =	113;	/* instrument list */
	public static final int	SND_SEQ_EVENT_INSTR_LIST_RESULT = 114;	/* result */
	public static final int	SND_SEQ_EVENT_INSTR_CLUSTER =	115;	/* cluster parameters */
	public static final int	SND_SEQ_EVENT_INSTR_CLUSTER_GET =	116;	/* get cluster parameters */
	public static final int	SND_SEQ_EVENT_INSTR_CLUSTER_RESULT = 117;	/* result */
	public static final int	SND_SEQ_EVENT_INSTR_CHANGE =	118;	/* instrument change */
/* 119-129: reserved */

/* 130-139: variable length events
 * event data type = snd_seq_ev_ext
 * (SND_SEQ_EVENT_LENGTH_VARIABLE must be set)
 */
	public static final int	SND_SEQ_EVENT_SYSEX =		130;	/* system exclusive data (variable length) */
	public static final int	SND_SEQ_EVENT_BOUNCE =		131;	/* error event */
/* 132-134: reserved */
	public static final int	SND_SEQ_EVENT_USR_VAR0 =	135;
	public static final int	SND_SEQ_EVENT_USR_VAR1 =	136;
	public static final int	SND_SEQ_EVENT_USR_VAR2 =	137;
	public static final int	SND_SEQ_EVENT_USR_VAR3 =	138;
	public static final int	SND_SEQ_EVENT_USR_VAR4 =	139;

/* 140-149: IPC shared memory events (*NOT SUPPORTED YET*)
 * event data type = snd_seq_ev_ipcshm
 * (SND_SEQ_EVENT_LENGTH_VARIPC must be set)
 */
	public static final int	SND_SEQ_EVENT_IPCSHM =		140;
/* 141-144: reserved */
	public static final int	SND_SEQ_EVENT_USR_VARIPC0 =	145;
	public static final int	SND_SEQ_EVENT_USR_VARIPC1 =	146;
	public static final int	SND_SEQ_EVENT_USR_VARIPC2 =	147;
	public static final int	SND_SEQ_EVENT_USR_VARIPC3 =	148;
	public static final int	SND_SEQ_EVENT_USR_VARIPC4 =	149;

/* 150-191: reserved */

/* 192-254: hardware specific events */

/* 255: special event */
	public static final int	SND_SEQ_EVENT_NONE =		255;


	public static final int	SND_SEQ_ADDRESS_UNKNOWN	 =	253;	/* unknown source */
	public static final int	SND_SEQ_ADDRESS_SUBSCRIBERS =	254;	/* send event to all subscribed ports */
	public static final int	SND_SEQ_ADDRESS_BROADCAST =	255;	/* send event to all queues/clients/ports/channels */
	public static final int	SND_SEQ_QUEUE_DIRECT =		253;	/* direct dispatch */

	/* event mode flag - NOTE: only 8 bits available! */
	public static final int	SND_SEQ_TIME_STAMP_TICK	 =	(0<<0); /* timestamp in clock ticks */
	public static final int	SND_SEQ_TIME_STAMP_REAL	 =	(1<<0); /* timestamp in real time */
	public static final int	SND_SEQ_TIME_STAMP_MASK	 =	(1<<0);

	public static final int	SND_SEQ_TIME_MODE_ABS =		(0<<1);	/* absolute timestamp */
	public static final int	SND_SEQ_TIME_MODE_REL =		(1<<1);	/* relative to current time */
	public static final int	SND_SEQ_TIME_MODE_MASK =	(1<<1);

	public static final int	SND_SEQ_EVENT_LENGTH_FIXED =	(0<<2);	/* fixed event size */
	public static final int	SND_SEQ_EVENT_LENGTH_VARIABLE =	(1<<2);	/* variable event size */
	public static final int	SND_SEQ_EVENT_LENGTH_VARUSR =	(2<<2);	/* variable event size - user memory space */
	public static final int	SND_SEQ_EVENT_LENGTH_VARIPC =	(3<<2);	/* variable event size - IPC */
	public static final int	SND_SEQ_EVENT_LENGTH_MASK =	(3<<2);

	public static final int	SND_SEQ_PRIORITY_NORMAL	 =	(0<<4);	/* normal priority */
	public static final int	SND_SEQ_PRIORITY_HIGH =		(1<<4);	/* event should be processed before others */
	public static final int	SND_SEQ_PRIORITY_MASK =		(1<<4);





	/* known client numbers */
	public static final int	SND_SEQ_CLIENT_SYSTEM =		0;
	public static final int	SND_SEQ_CLIENT_DUMMY =		62;	/* dummy ports */
	public static final int	SND_SEQ_CLIENT_OSS =		63;	/* oss sequencer emulator */

	/* event filter flags */
	public static final int	SND_SEQ_FILTER_BROADCAST =	(1<<0);	/* accept broadcast messages */
	public static final int	SND_SEQ_FILTER_MULTICAST =	(1<<1);	/* accept multicast messages */
	public static final int	SND_SEQ_FILTER_BOUNCE =		(1<<2);	/* accept bounce event in error */
	public static final long	SND_SEQ_FILTER_USE_EVENT =	(1L<<31);	/* use event filter */


/* Flush mode flags */
	public static final int	SND_SEQ_REMOVE_DEST =		(1<<0);	/* Restrict by destination q:client:port */
	public static final int	SND_SEQ_REMOVE_DEST_CHANNEL =	(1<<1);	/* Restrict by channel */
	public static final int	SND_SEQ_REMOVE_TIME_BEFORE =	(1<<2);	/* Restrict to before time */
	public static final int	SND_SEQ_REMOVE_TIME_AFTER =	(1<<3);	/* Restrict to time or after */
	public static final int	SND_SEQ_REMOVE_EVENT_TYPE =	(1<<4);	/* Restrict to event type */
	public static final int	SND_SEQ_REMOVE_IGNORE_OFF = 	(1<<5);	/* Do not flush off events */
	public static final int	SND_SEQ_REMOVE_TAG_MATCH = 	(1<<6);	/* Restrict to events with given tag */

	/* known port numbers */
	public static final int SND_SEQ_PORT_SYSTEM_TIMER =	0;
	public static final int SND_SEQ_PORT_SYSTEM_ANNOUNCE =	1;

	/* port capabilities (32 bits) */
	public static final int	SND_SEQ_PORT_CAP_READ =		(1<<0);	/* readable from this port */
	public static final int	SND_SEQ_PORT_CAP_WRITE =	(1<<1);	/* writable to this port */

	public static final int	SND_SEQ_PORT_CAP_SYNC_READ =	(1<<2);
	public static final int	SND_SEQ_PORT_CAP_SYNC_WRITE =	(1<<3);

	public static final int	SND_SEQ_PORT_CAP_DUPLEX =	(1<<4);

	public static final int	SND_SEQ_PORT_CAP_SUBS_READ =	(1<<5);	/* allow read subscription */
	public static final int	SND_SEQ_PORT_CAP_SUBS_WRITE =	(1<<6);	/* allow write subscription */
	public static final int	SND_SEQ_PORT_CAP_NO_EXPORT =	(1<<7);	/* routing not allowed */

	/* port type */
	public static final int	SND_SEQ_PORT_TYPE_SPECIFIC =	(1<<0);	/* hardware specific */
	public static final int	SND_SEQ_PORT_TYPE_MIDI_GENERIC =(1<<1);	/* generic MIDI device */
	public static final int	SND_SEQ_PORT_TYPE_MIDI_GM =	(1<<2);	/* General MIDI compatible device */
	public static final int	SND_SEQ_PORT_TYPE_MIDI_GS =	(1<<3);	/* GS compatible device */
	public static final int	SND_SEQ_PORT_TYPE_MIDI_XG =	(1<<4);	/* XG compatible device */
	public static final int	SND_SEQ_PORT_TYPE_MIDI_MT32 =	(1<<5);	/* MT-32 compatible device */

/* other standards...*/
	public static final int	SND_SEQ_PORT_TYPE_SYNTH =	(1<<10);	/* Synth device */
	public static final int	SND_SEQ_PORT_TYPE_DIRECT_SAMPLE =(1<<11);	/* Sampling device (support sample download) */
	public static final int	SND_SEQ_PORT_TYPE_SAMPLE =	(1<<12);	/* Sampling device (sample can be downloaded at any time) */
/*...*/
	public static final int	SND_SEQ_PORT_TYPE_APPLICATION =	(1<<20);	/* application (sequencer/editor) */

/* standard group names */
	public static final String SND_SEQ_GROUP_SYSTEM =	"system";
	public static final String SND_SEQ_GROUP_DEVICE =	"device";
	public static final String SND_SEQ_GROUP_APPLICATION =	"application";

/* misc. conditioning flags */
	public static final int SND_SEQ_PORT_FLG_GIVEN_PORT =	(1<<0);

/* queue flags */
	public static final int SND_SEQ_QUEUE_FLG_SYNC =	(1<<0);	/* sync enabled */

/* queue status flag */
	public static final int SND_SEQ_QUEUE_FLG_SYNC_LOST =	1;

/* synchronization types */
/* mode */
	public static final int SND_SEQ_SYNC_TICK =		0x80;
	public static final int SND_SEQ_SYNC_TIME =		0x40;
	public static final int SND_SEQ_SYNC_MODE =		0xc0;		/* mask */
/* private format */
	public static final int SND_SEQ_SYNC_FMT_PRIVATE_CLOCK = (SND_SEQ_SYNC_TICK|0);
	public static final int SND_SEQ_SYNC_FMT_PRIVATE_TIME =	(SND_SEQ_SYNC_TIME|0);
/* pre-defined format */
	public static final int SND_SEQ_SYNC_FMT_MIDI_CLOCK =	(SND_SEQ_SYNC_TICK|1);
	public static final int SND_SEQ_SYNC_FMT_MTC =		(SND_SEQ_SYNC_TIME|1);
	public static final int SND_SEQ_SYNC_FMT_DTL =		(SND_SEQ_SYNC_TIME|2);
	public static final int SND_SEQ_SYNC_FMT_SMPTE =	(SND_SEQ_SYNC_TIME|3);
	public static final int SND_SEQ_SYNC_FMT_MIDI_TICK =	(SND_SEQ_SYNC_TIME|4);
/* time format */
	public static final int SND_SEQ_SYNC_FPS_24 =		0;
	public static final int SND_SEQ_SYNC_FPS_25 =		1;
	public static final int SND_SEQ_SYNC_FPS_30_DP =	2;
	public static final int SND_SEQ_SYNC_FPS_30_NDP =	3;

/* sequencer timer sources */
	public static final int SND_SEQ_TIMER_ALSA =		0;	/* ALSA timer */
	public static final int SND_SEQ_TIMER_MIDI_CLOCK =	1;	/* Midi Clock (CLOCK event) */
	public static final int SND_SEQ_TIMER_MIDI_TICK =	2;	/* Midi Timer Tick (TICK event) */

/* type of query subscription */
	public static final int SND_SEQ_QUERY_SUBS_READ =	0;
	public static final int SND_SEQ_QUERY_SUBS_WRITE =	1;

/* instrument types */
	public static final int SND_SEQ_INSTR_ATYPE_DATA =	0;	/* instrument data */
	public static final int SND_SEQ_INSTR_ATYPE_ALIAS =	1;	/* instrument alias */

/* instrument ASCII identifiers */
	public static final String SND_SEQ_INSTR_ID_DLS1 =	"DLS1";
	public static final String SND_SEQ_INSTR_ID_DLS2 =	"DLS2";
	public static final String SND_SEQ_INSTR_ID_SIMPLE =	"Simple Wave";
	public static final String SND_SEQ_INSTR_ID_SOUNDFONT =	"SoundFont";
	public static final String SND_SEQ_INSTR_ID_GUS_PATCH =	"GUS Patch";
	public static final String SND_SEQ_INSTR_ID_INTERWAVE =	"InterWave FFFF";
	public static final String SND_SEQ_INSTR_ID_OPL2_3 =	"OPL2/3 FM";
	public static final String SND_SEQ_INSTR_ID_OPL4 =	"OPL4";

/* instrument types */
	public static final int SND_SEQ_INSTR_TYPE0_DLS1 =	(1<<0);	/* MIDI DLS v1 */
	public static final int SND_SEQ_INSTR_TYPE0_DLS2 =	(1<<1);	/* MIDI DLS v2 */
	public static final int SND_SEQ_INSTR_TYPE1_SIMPLE =	(1<<0);	/* Simple Wave */
	public static final int SND_SEQ_INSTR_TYPE1_SOUNDFONT =	(1<<1);	/* EMU SoundFont */
	public static final int SND_SEQ_INSTR_TYPE1_GUS_PATCH =	(1<<2);	/* Gravis UltraSound Patch */
	public static final int SND_SEQ_INSTR_TYPE1_INTERWAVE =	(1<<3);	/* InterWave FFFF */
	public static final int SND_SEQ_INSTR_TYPE2_OPL2_3 =	(1<<0);	/* Yamaha OPL2/3 FM */
	public static final int SND_SEQ_INSTR_TYPE2_OPL4 =	(1<<1);	/* Yamaha OPL4 */

/* put commands */
	public static final int SND_SEQ_INSTR_PUT_CMD_CREATE =	0;
	public static final int SND_SEQ_INSTR_PUT_CMD_REPLACE =	1;
	public static final int SND_SEQ_INSTR_PUT_CMD_MODIFY =	2;
	public static final int SND_SEQ_INSTR_PUT_CMD_ADD =	3;
	public static final int SND_SEQ_INSTR_PUT_CMD_REMOVE =	4;

/* get commands */
	public static final int SND_SEQ_INSTR_GET_CMD_FULL =	0;
	public static final int SND_SEQ_INSTR_GET_CMD_PARTIAL =	1;

/* query flags */
	public static final int SND_SEQ_INSTR_QUERY_FOLLOW_ALIAS = (1<<0);

/* free commands */
	public static final int SND_SEQ_INSTR_FREE_CMD_ALL =		0;
	public static final int SND_SEQ_INSTR_FREE_CMD_PRIVATE =	1;
	public static final int SND_SEQ_INSTR_FREE_CMD_CLUSTER =	2;
	public static final int SND_SEQ_INSTR_FREE_CMD_SINGLE =		3;



	static
	{
		Alsa.loadNativeLibrary();
		if (TDebug.TraceAlsaSeqNative)
		{
			setTrace(true);
		}
	}



	/*
	 *	This holds a pointer for the native code - do not touch!
	 */
	private long		m_lNativeHandle;

	// TODO: hack
	private Event		m_event = new Event();


	public AlsaSeq()
	{
		super();
		if (TDebug.TraceAlsaSeq) { TDebug.out("AlsaSeq.<init>(): begin"); }
		int	nSuccess = open();
		if (nSuccess < 0)
		{
			throw new RuntimeException("open failed");
		}
		if (TDebug.TraceAlsaSeq) { TDebug.out("AlsaSeq.<init>(): end"); }
	}



	public AlsaSeq(String strClientName)
	{
		this();
		if (TDebug.TraceAlsaSeq) { TDebug.out("AlsaSeq.<init>(String): begin"); }
		setClientName(strClientName);
		if (TDebug.TraceAlsaSeq) { TDebug.out("AlsaSeq.<init>(String): end"); }
	}



	/**	Opens the sequencer.
	 *	Calls snd_seq_open().
	 */
	public native int open();

	/**	Closes the sequencer.
	 *	Calls snd_seq_close().
	 */
	public native void close();

	public native String getName();

	public native int getType();
	
	public native int setNonblock(boolean bNonblock);
	
	public native int getClientId();

	public native int getOutputBufferSize();

	public native int getInputBufferSize();

	public native int setOutputBufferSize(int nSize);

	public native int setInputBufferSize(int nSize);




	public native int getSystemInfo(SystemInfo systemInfo);



	public int getClientInfo(ClientInfo clientInfo)
	{
		return getClientInfo(-1, clientInfo);
	}

	public native int getClientInfo(int nClient, ClientInfo clientInfo);

	public native int setClientInfo(ClientInfo clientInfo);



	/**	Gets information about the next client.
	 *	Calls snd_seq_query_next_client().
	 *	and puts the returned values
	 *	into the passed arrays.
	 *
	 *	nClient has to be -1 to start, or a client id returned by
	 *	a previous call to this method.
	 *
	 *	anValues[0]	client id
	 *
	 *	Returns 0 if successful.
	 */
	public native int getNextClient(int nClient, int[] anValues);



	public void setClientName(String strName)
	{
		if (TDebug.TraceAlsaSeq) { TDebug.out("AlsaSeq.setClientName(): begin"); }
		ClientInfo	clientInfo = new ClientInfo();
		// TODO: error check
		getClientInfo(clientInfo);
		clientInfo.setName(strName);
		setClientInfo(clientInfo);
		if (TDebug.TraceAlsaSeq) { TDebug.out("AlsaSeq.setClientName(): end"); }
	}


	public int getPortInfo(int nPort, PortInfo portInfo)
	{
		return getPortInfo(-1, nPort, portInfo);
	}

	public native int getPortInfo(int nClient, int nPort, PortInfo portInfo);



	/**	Gets the next port.
	 *	Calls snd_seq_query_next_port().
	 *	and put the returned values
	 *	into the passed arrays.
	 *
	 *	nClient has to be a valid client.
	 *	nPort has to be -1 to start, or a port returned by
	 *	a previous call to this method.
	 *
	 *	anValues[0]	client
	 *	anValues[1]	port
	 *
	 *	Returns 0 if successful.
	 */
	public native int getNextPort(int nClient, int nPort, int[] anValues);



	// TODO: use structure
	public native int createPort(String strName, int nCapabilities, int nGroupPermissions, int nType, int nMidiChannels, int nMidiVoices, int nSynthVoices);

	/**	Allocates (reserves) a sequencing queue.
		Calls snd_seq_alloc_queue().
		@return the queue number (>= 0), if successful. A negative
		value otherwise.
	*/
	public native int allocQueue();


	/**	Frees a sequencing queue.
		Calls snd_seq_free_queue().

		@param nQueue a queue number that has previously been
		allocated with allocQueue().

		@return 0 if successful. A negative
		value otherwise.
	*/
	public native int freeQueue(int nQueue);



	/**	Get the queue information.
		This method fills a QueueInfo instance with information
		from the given queue. Internally, snd_seq_get_queue_info()
		is called.

		@return returns 0 on success, otherwise a negative value.
	*/
	public native int getQueueInfo(int nQueue, QueueInfo queueInfo);



	/**	Set the queue information.
		This method sets the information for the given queue from
		the QueueInfo instance. Internally, snd_seq_set_queue_info()
		is called.

		@return returns 0 on success, otherwise a negative value.
	*/
	public native int setQueueInfo(int nQueue, QueueInfo queueInfo);


	/**	Get the queue status.
		This method fills a QueueStatus instance with information
		from the given queue. Internally, snd_seq_get_queue_status()
		is called.

		@return returns 0 on success, otherwise a negative value.
	*/
	public native int getQueueStatus(int nQueue, QueueStatus queueStatus);


	/**	Get the queue tempo.
		This method fills a QueueTempo instance with information
		from the given queue. Internally, snd_seq_get_queue_tempo()
		is called.

		@return returns 0 on success, otherwise a negative value.
	*/
	public native int getQueueTempo(int nQueue, QueueTempo queueTempo);


	/**	Set the queue tempo.
		This method sets the information for the given queue from
		the QueueTempo instance. Internally, snd_seq_set_queue_tempo()
		is called.

		@return returns 0 on success, otherwise a negative value.
	*/
	public native int setQueueTempo(int nQueue, QueueTempo queueTempo);


	/**	Get the queue timer.
		This method fills a QueueTimer instance with information
		from the given queue. Internally, snd_seq_get_queue_timer()
		is called.

		@return returns 0 on success, otherwise a negative value.
	*/
	public native int getQueueTimer(int nQueue, QueueTimer queueTimer);


	/**	Set the queue timer.
		This method sets the timer for the given queue from
		the QueueTimer instance. Internally, snd_seq_set_queue_timer()
		is called.

		@return returns 0 on success, otherwise a negative value.
	*/
	public native int setQueueTimer(int nQueue, QueueTimer queueTimer);



	public native void subscribePort(
		int nSenderClient, int nSenderPort,
		int nDestClient, int nDestPort,
		int nQueue, boolean bExclusive, boolean bRealtime, boolean bConvertTime,
		int nMidiChannels, int nMidiVoices, int nSynthVoices);


	public void sendNoteEvent(
		int nType, int nFlags, int nTag, int nQueue, long lTime,
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nNote, int nVelocity, int nOffVelocity, int nDuration)
	{
		m_event.setCommon(nType, nFlags, nTag, nQueue, lTime,
		0, nSourcePort, nDestClient, nDestPort);
		m_event.setNote(nChannel, nNote, nVelocity, nOffVelocity, nDuration);
		eventOutput(m_event);
		drainOutput();
	}




	public void sendControlEvent(
		int nType, int nFlags, int nTag, int nQueue, long lTime,
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nParam, int nValue)
	{
		m_event.setCommon(nType, nFlags, nTag, nQueue, lTime,
		0, nSourcePort, nDestClient, nDestPort);
		m_event.setControl(nChannel, nParam, nValue);
		eventOutput(m_event);
		drainOutput();
	}




	public void sendQueueControlEvent(
		int nType, int nFlags, int nTag, int nQueue, long lTime,
		int nSourcePort, int nDestClient, int nDestPort,
		int nControlQueue, int nControlValue, long lControlTime)
	{
		m_event.setCommon(nType, nFlags, nTag, nQueue, lTime,
		0, nSourcePort, nDestClient, nDestPort);
		m_event.setQueueControl(nControlQueue, nControlValue, lControlTime);
		eventOutput(m_event);
		drainOutput();
	}


	/**
	 *	Transmits arbitrary data in an ALSA event.
	 *	In getEvent(), a reference to a byte array is returned
	 *	in the passed object array.
	 */
	public void sendVarEvent(
		int nType, int nFlags, int nTag, int nQueue, long lTime,
		int nSourcePort, int nDestClient, int nDestPort,
		byte[] abData, int nOffset, int nLength)
	{
		m_event.setCommon(nType, nFlags, nTag, nQueue, lTime,
		0, nSourcePort, nDestClient, nDestPort);
		m_event.setVar(abData, nOffset, nLength);
		eventOutput(m_event);
		drainOutput();
	}



	/**	Wait for an event.
	 *	Calls snd_seq_event_input().
	 *	and put the data elements of the returned event
	 *	into the passed arrays.
	 *	anValues[0]	type
	 *	anValues[1]	flags
	 *	anValues[2]	tag
	 *
	 *	anValues[3]	queue
	 *
	 *	anValues[4]	source client
	 *	anValues[5]	source port
	 *
	 *	anValues[6]	dest client
	 *	anValues[7]	dest port
	 *
	 *	The values starting with index 8 depend on the type of event.
	 *
	 *	SND_SEQ_EVENT_NOTE,
	 *	SND_SEQ_EVENT_NOTEON,
	 *	SND_SEQ_EVENT_NOTEOFF:
	 *	anValues[08]	channel
	 *	anValues[09]	note
	 *	anValues[10]	velocity
	 *	anValues[11]	off_velocity
	 *	anValues[12]	duration
	 *
	 *	SND_SEQ_EVENT_KEYPRESS,
	 *	SND_SEQ_EVENT_CONTROLLER,
	 *	SND_SEQ_EVENT_PGMCHANGE,
	 *	SND_SEQ_EVENT_CHANPRESS,
	 *	SND_SEQ_EVENT_PITCHBEND,
	 *	SND_SEQ_EVENT_CONTROL14, ??
	 *	SND_SEQ_EVENT_NONREGPARAM, ??
	 *	SND_SEQ_EVENT_REGPARAM: ??
	 *	anValues[08]	channel
	 *	anValues[09]	param
	 *	anValues[10]	values
	 *
	 *
	 *	returns true if an event was received. Above values are
	 *	only valid if this is true!
	 *
	 *	Throws RuntimeExceptions in certain cases.
	 */
	public native boolean getEvent(int[] anValues, long[] alValues, Object[] aValues);



	private static native void setTrace(boolean bTrace);




	public Iterator getClientInfos()
	{
		return new ClientInfoIterator();
	}



	// TODO: perhaps remove
	public Iterator getPortInfos(int nClient)
	{
		return new PortInfoIterator(nClient);
	}



	public void subscribePort(
		int nSenderClient, int nSenderPort,
		int nDestClient, int nDestPort)
	{
		subscribePort(
			nSenderClient, nSenderPort,
			nDestClient, nDestPort,
			0, false, false, false);
	}



	public void subscribePort(
		int nSenderClient, int nSenderPort,
		int nDestClient, int nDestPort,
		int nQueue, boolean bExclusive, boolean bRealtime, boolean bConvertTime)
	{
		subscribePort(
			nSenderClient, nSenderPort,
			nDestClient, nDestPort,
			nQueue, bExclusive, bRealtime, bConvertTime,
			0, 0, 0);
	}



	////////////////////////////////////////////////////////////////
	//
	//	Events
	//
	////////////////////////////////////////////////////////////////


	public native int eventOutput(Event event);
	public native int eventOutputBuffer(Event event);
	public native int eventOutputDirect(Event event);
	public native int eventInput(Event event);
	public native int eventInputPending(int nFetchSequencer);
	public native int drainOutput();
	public native int eventOutputPending();
	public native int extractOutput(Event event);
	public native int dropOutput();
	public native int dropOutputBuffer();
	public native int dropInput();
	public native int dropInputBuffer();



	public void startQueue(int nSourcePort, int nQueue)
	{
		controlQueue(SND_SEQ_EVENT_START, nSourcePort, nQueue);
	}



	public void stopQueue(int nSourcePort, int nQueue)
	{
		controlQueue(SND_SEQ_EVENT_STOP, nSourcePort, nQueue);
	}



	/**	Set the playback position of a sequencer queue.
	 *
	 *	@param lTick the desired playback position in ticks
	 */
	public void setQueuePositionTick(int nSourcePort, int nQueue,
					 long lTick)
	{
		controlQueue(SND_SEQ_EVENT_SETPOS_TICK, nSourcePort, nQueue,
			     lTick);
	}



	/**	Set the playback position of a sequencer queue.
	 *
	 *	@param lTick the desired playback position in nanoseconds
	 */
	public void setQueuePositionTime(int nSourcePort, int nQueue,
					 long lTime)
	{
		controlQueue(SND_SEQ_EVENT_SETPOS_TIME, nSourcePort, nQueue,
			     lTime);
	}



	public void controlQueue(int nType, int nSourcePort, int nQueue)
	{
		sendQueueControlEvent(
			nType, SND_SEQ_TIME_STAMP_REAL | SND_SEQ_TIME_MODE_REL, 0, SND_SEQ_QUEUE_DIRECT, 0L,
			nSourcePort, SND_SEQ_CLIENT_SYSTEM, SND_SEQ_PORT_SYSTEM_TIMER,
			nQueue, 0, 0);
	}


	public void controlQueue(int nType, int nSourcePort, int nQueue,
				 long lTime)
	{
		sendQueueControlEvent(
			nType, SND_SEQ_TIME_STAMP_REAL | SND_SEQ_TIME_MODE_REL, 0, SND_SEQ_QUEUE_DIRECT, 0L,
			nSourcePort, SND_SEQ_CLIENT_SYSTEM, SND_SEQ_PORT_SYSTEM_TIMER,
			nQueue, 0, lTime);
	}



	/////////////////////////// sending MIDI messages


	public void sendNoteOffEventImmediately(
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nNote, int nVelocity)
	{
		sendNoteEventImmediately(
			SND_SEQ_EVENT_NOTEOFF,
			nSourcePort,
			nDestClient, nDestPort, nChannel,
			nNote, nVelocity);
	}



	public void sendNoteOffEventTicked(
		int nQueue, long lTick,
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nNote, int nVelocity)
	{
		sendNoteEventTicked(
			SND_SEQ_EVENT_NOTEOFF, nQueue, lTick,
			nSourcePort, nDestClient, nDestPort,
			nChannel, nNote, nVelocity);
	}



	public void sendNoteOffEventSubscribersTicked(
		int nQueue, long lTick,
		int nSourcePort,
		int nChannel, int nNote, int nVelocity)
	{
		sendNoteEventSubscribersTicked(
			SND_SEQ_EVENT_NOTEOFF, nQueue, lTick,
			nSourcePort,
			nChannel, nNote, nVelocity);
	}



	public void sendNoteOffEventSubscribersImmediately(
		int nSourcePort,
		int nChannel, int nNote, int nVelocity)
	{
		sendNoteEventSubscribersImmediately(
			SND_SEQ_EVENT_NOTEOFF,
			nSourcePort,
			nChannel, nNote, nVelocity);
	}



	public void sendNoteOnEventImmediately(
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nNote, int nVelocity)
	{
		sendNoteEventImmediately(
			SND_SEQ_EVENT_NOTEON,
			nSourcePort,
			nDestClient, nDestPort, nChannel,
			nNote, nVelocity);
	}



	public void sendNoteOnEventTicked(
		int nQueue, long lTime,
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nNote, int nVelocity)
	{
		sendNoteEventTicked(
			SND_SEQ_EVENT_NOTEON, nQueue, lTime,
			nSourcePort, nDestClient, nDestPort,
			nChannel, nNote, nVelocity);
	}



	public void sendNoteOnEventSubscribersTicked(
		int nQueue, long lTime,
		int nSourcePort,
		int nChannel, int nNote, int nVelocity)
	{
		sendNoteEventSubscribersTicked(
			SND_SEQ_EVENT_NOTEON, nQueue, lTime,
			nSourcePort,
			nChannel, nNote, nVelocity);
	}



	public void sendNoteOnEventSubscribersImmediately(
		int nSourcePort,
		int nChannel, int nNote, int nVelocity)
	{
		sendNoteEventSubscribersImmediately(
			SND_SEQ_EVENT_NOTEON,
			nSourcePort,
			nChannel, nNote, nVelocity);
	}



	// sendNoteEventTimed: real-time timestamp?


	public void sendNoteEventImmediately(
		int nType,
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nNote, int nVelocity)
	{
		sendNoteEvent(
			nType, SND_SEQ_TIME_STAMP_REAL | SND_SEQ_TIME_MODE_REL, 0, SND_SEQ_QUEUE_DIRECT, 0L,
			nSourcePort, nDestClient, nDestPort,
			nChannel, nNote, nVelocity, 0, 0);
	}



	public void sendNoteEventTicked(
		int nType, int nQueue, long lTick,
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nNote, int nVelocity)
	{
		sendNoteEvent(
			nType, SND_SEQ_TIME_STAMP_TICK | SND_SEQ_TIME_MODE_ABS, 0, nQueue, lTick,
			nSourcePort, nDestClient, nDestPort,
			nChannel, nNote, nVelocity, 0, 0);
	}



	public void sendNoteEventSubscribersTicked(
		int nType, int nQueue, long lTick,
		int nSourcePort,
		int nChannel, int nNote, int nVelocity)
	{
		sendNoteEvent(
			nType, SND_SEQ_TIME_STAMP_TICK | SND_SEQ_TIME_MODE_ABS, 0, nQueue, lTick,
			nSourcePort, SND_SEQ_ADDRESS_SUBSCRIBERS, SND_SEQ_ADDRESS_UNKNOWN,
			nChannel, nNote, nVelocity, 0, 0);
	}



	public void sendNoteEventSubscribersImmediately(
		int nType,
		int nSourcePort,
		int nChannel, int nNote, int nVelocity)
	{
		sendNoteEvent(
			nType, SND_SEQ_TIME_STAMP_REAL | SND_SEQ_TIME_MODE_REL, 0, SND_SEQ_QUEUE_DIRECT, 0L,
			nSourcePort, SND_SEQ_ADDRESS_SUBSCRIBERS, SND_SEQ_ADDRESS_UNKNOWN,
			nChannel, nNote, nVelocity, 0, 0);
	}



	public void sendKeyPressureEventImmediately(
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nNote, int nPressure)
	{
		sendControlEventImmediately(
			SND_SEQ_EVENT_KEYPRESS,
			nSourcePort, nDestClient, nDestPort,
			nChannel, nNote, nPressure);
	}



	public void sendKeyPressureEventTicked(
		int nQueue, long lTime,
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nNote, int nPressure)
	{
		sendControlEventTicked(
			SND_SEQ_EVENT_KEYPRESS, nQueue, lTime,
			nSourcePort, nDestClient, nDestPort,
			nChannel, nNote, nPressure);
	}



	public void sendKeyPressureEventSubscribersTicked(
		int nQueue, long lTime,
		int nSourcePort,
		int nChannel, int nNote, int nPressure)
	{
		sendControlEventSubscribersTicked(
			SND_SEQ_EVENT_KEYPRESS, nQueue, lTime,
			nSourcePort,
			nChannel, nNote, nPressure);
	}



	public void sendKeyPressureEventSubscribersImmediately(
		int nSourcePort,
		int nChannel, int nNote, int nPressure)
	{
		sendControlEventSubscribersImmediately(
			SND_SEQ_EVENT_KEYPRESS,
			nSourcePort,
			nChannel, nNote, nPressure);
	}



	public void sendControlChangeEventImmediately(
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nControl, int nValue)
	{
		sendControlEventImmediately(
			SND_SEQ_EVENT_CONTROLLER,
			nSourcePort, nDestClient, nDestPort,
			nChannel, nControl, nValue);
	}



	public void sendControlChangeEventTicked(
		int nQueue, long lTime,
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nControl, int nValue)
	{
		sendControlEventTicked(
			SND_SEQ_EVENT_CONTROLLER, nQueue, lTime,
			nSourcePort, nDestClient, nDestPort,
			nChannel, nControl, nValue);
	}



	public void sendControlChangeEventSubscribersTicked(
		int nQueue, long lTime,
		int nSourcePort,
		int nChannel, int nControl, int nValue)
	{
		sendControlEventSubscribersTicked(
			SND_SEQ_EVENT_CONTROLLER, nQueue, lTime,
			nSourcePort,
			nChannel, nControl, nValue);
	}



	public void sendControlChangeEventSubscribersImmediately(
		int nSourcePort,
		int nChannel, int nControl, int nValue)
	{
		sendControlEventSubscribersImmediately(
			SND_SEQ_EVENT_CONTROLLER,
			nSourcePort,
			nChannel, nControl, nValue);
	}



	public void sendProgramChangeEventImmediately(
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nProgram)
	{
		sendControlEventImmediately(
			SND_SEQ_EVENT_PGMCHANGE,
			nSourcePort, nDestClient, nDestPort,
			nChannel, 0, nProgram);
	}



	public void sendProgramChangeEventTicked(
		int nQueue, long lTime,
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nProgram)
	{
		sendControlEventTicked(
			SND_SEQ_EVENT_PGMCHANGE,nQueue, lTime,
			nSourcePort, nDestClient, nDestPort,
			nChannel, 0, nProgram);
	}



	public void sendProgramChangeEventSubscribersTicked(
		int nQueue, long lTime,
		int nSourcePort,
		int nChannel, int nProgram)
	{
		sendControlEventSubscribersTicked(
			SND_SEQ_EVENT_PGMCHANGE,nQueue, lTime,
			nSourcePort,
			nChannel, 0, nProgram);
	}



	public void sendProgramChangeEventSubscribersImmediately(
		int nSourcePort,
		int nChannel, int nProgram)
	{
		sendControlEventSubscribersImmediately(
			SND_SEQ_EVENT_PGMCHANGE,
			nSourcePort,
			nChannel, 0, nProgram);
	}



	/////////////////// channel pressure /////////////////



	public void sendChannelPressureEventImmediately(
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nPressure)
	{
		sendControlEventImmediately(
			SND_SEQ_EVENT_CHANPRESS,
			nSourcePort, nDestClient, nDestPort,
			nChannel, 0, nPressure);
	}



	public void sendChannelPressureEventTicked(
		int nQueue, long lTime,
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nPressure)
	{
		sendControlEventTicked(
			SND_SEQ_EVENT_CHANPRESS, nQueue, lTime,
			nSourcePort, nDestClient, nDestPort,
			nChannel, 0, nPressure);
	}



	public void sendChannelPressureEventSubscribersTicked(
		int nQueue, long lTime,
		int nSourcePort,
		int nChannel, int nPressure)
	{
		sendControlEventSubscribersTicked(
			SND_SEQ_EVENT_CHANPRESS, nQueue, lTime,
			nSourcePort,
			nChannel, 0, nPressure);
	}



	public void sendChannelPressureEventSubscribersImmediately(
		int nSourcePort,
		int nChannel, int nPressure)
	{
		sendControlEventSubscribersImmediately(
			SND_SEQ_EVENT_CHANPRESS,
			nSourcePort,
			nChannel, 0, nPressure);
	}



	////////////////// pitch bend /////////////////////



	public void sendPitchBendEventImmediately(
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nPitch)
	{
		sendControlEventImmediately(
			SND_SEQ_EVENT_PITCHBEND,
			nSourcePort, nDestClient, nDestPort,
			nChannel, 0, nPitch);
	}



	public void sendPitchBendEventTicked(
		int nQueue, long lTime,
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nPitch)
	{
		sendControlEventTicked(
			SND_SEQ_EVENT_PITCHBEND, nQueue, lTime,
			nSourcePort, nDestClient, nDestPort,
			nChannel, 0, nPitch);
	}



	public void sendPitchBendEventSubscribersTicked(
		int nQueue, long lTime,
		int nSourcePort,
		int nChannel, int nPitch)
	{
		sendControlEventSubscribersTicked(
			SND_SEQ_EVENT_PITCHBEND, nQueue, lTime,
			nSourcePort,
			nChannel, 0, nPitch);
	}



	public void sendPitchBendEventSubscribersImmediately(
		int nSourcePort,
		int nChannel, int nPitch)
	{
		sendControlEventSubscribersImmediately(
			SND_SEQ_EVENT_PITCHBEND,
			nSourcePort,
			nChannel, 0, nPitch);
	}



	/////////////////////// common procedures



	public void sendControlEventImmediately(
		int nType,
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nParam, int nValue)
	{
		sendControlEvent(
			nType, SND_SEQ_TIME_STAMP_REAL | SND_SEQ_TIME_MODE_REL, 0, SND_SEQ_QUEUE_DIRECT, 0L,
			nSourcePort, nDestClient, nDestPort,
			nChannel, nParam, nValue);
	}



	public void sendControlEventTicked(
		int nType, int nQueue, long lTime,
		int nSourcePort, int nDestClient, int nDestPort,
		int nChannel, int nParam, int nValue)
	{
		sendControlEvent(
			nType, SND_SEQ_TIME_STAMP_TICK | SND_SEQ_TIME_MODE_ABS, 0, nQueue, lTime,
			nSourcePort, nDestClient, nDestPort,
			nChannel, nParam, nValue);
	}



	public void sendControlEventSubscribersTicked(
		int nType, int nQueue, long lTime,
		int nSourcePort,
		int nChannel, int nParam, int nValue)
	{
		sendControlEvent(
			nType, SND_SEQ_TIME_STAMP_TICK | SND_SEQ_TIME_MODE_ABS, 0, nQueue, lTime,
			nSourcePort, SND_SEQ_ADDRESS_SUBSCRIBERS, SND_SEQ_ADDRESS_UNKNOWN,
			nChannel, nParam, nValue);
	}



	public void sendControlEventSubscribersImmediately(
		int nType,
		int nSourcePort,
		int nChannel, int nParam, int nValue)
	{
		sendControlEvent(
			nType, SND_SEQ_TIME_STAMP_REAL | SND_SEQ_TIME_MODE_REL, 0, SND_SEQ_QUEUE_DIRECT, 0L,
			nSourcePort, SND_SEQ_ADDRESS_SUBSCRIBERS, SND_SEQ_ADDRESS_UNKNOWN,
			nChannel, nParam, nValue);
	}



	/*
	 *	nTempo is in us/beat
	 */
	// TODO: check if used
	public void sendTempoEventSubscribersTicked(
		int nQueue, long lTime,
		int nSourcePort,
		int nTempo)
	{
		sendQueueControlEvent(
			AlsaSeq.SND_SEQ_EVENT_TEMPO, SND_SEQ_TIME_STAMP_TICK | SND_SEQ_TIME_MODE_ABS, 0, nQueue, lTime,
			nSourcePort, SND_SEQ_ADDRESS_SUBSCRIBERS, SND_SEQ_ADDRESS_UNKNOWN,
			nQueue, nTempo, 0);
	}


	public void sendVarEventSubscribersTicked(
		int nType, int nQueue, long lTime,
		int nSourcePort,
		byte[] abData, int nOffset, int nLength)
	{
		sendVarEvent(
			nType, SND_SEQ_TIME_STAMP_TICK | SND_SEQ_TIME_MODE_ABS | SND_SEQ_EVENT_LENGTH_VARIABLE, 0, nQueue, lTime,
			nSourcePort, SND_SEQ_ADDRESS_SUBSCRIBERS, SND_SEQ_ADDRESS_UNKNOWN,
			abData, nOffset, nLength);
	}



	public void sendVarEventSubscribersImmediately(
		int nType,
		int nSourcePort,
		byte[] abData, int nOffset, int nLength)
	{
		sendVarEvent(
			nType, SND_SEQ_TIME_STAMP_REAL | SND_SEQ_TIME_MODE_REL, 0, SND_SEQ_QUEUE_DIRECT, 0L,
			nSourcePort, SND_SEQ_ADDRESS_SUBSCRIBERS, SND_SEQ_ADDRESS_UNKNOWN,
			abData, nOffset, nLength);
	}



	///////////////// receiving of events ///////////////////////



	public MidiEvent getEvent()
	{
		int[]	anValues = new int[13];
		long[]	alValues = new long[1];
		Object[]	aValues = new Object[1];
		if (TDebug.TraceAlsaSeq) { TDebug.out("AlsaSeq.getEvent(): before getEvent(int[], long[])"); }
		while (true)
		{
			boolean	bEventPresent = getEvent(anValues, alValues, aValues);
			if (bEventPresent)
			{
				break;
			}
			/*
			 *	Sleep for 1 ms to enable scheduling.
			 */
		try
		{
			Thread.sleep(1);
		}
		catch (InterruptedException e)
		{
			if (TDebug.TraceAllExceptions) { TDebug.out(e); }
		}
		}
	// TDebug.out("after getEvent()");
	MidiMessage	message = null;
	int	nType = anValues[0];
	switch (nType)
	{
	case SND_SEQ_EVENT_NOTEON:
	case SND_SEQ_EVENT_NOTEOFF:
	{
		if (TDebug.TraceAlsaSeq)
		{
			TDebug.out("AlsaSeq.getEvent(): note event");
		}
		ShortMessage	shortMessage = new ShortMessage();
		int	nCommand = (nType == SND_SEQ_EVENT_NOTEON) ? 0x90 : 0x80;
		int	nChannel = anValues[8] & 0xF;
		int	nKey = anValues[9] & 0x7F;
		int	nVelocity = anValues[10] & 0x7F;
		try
		{
			shortMessage.setMessage(nCommand, nChannel, nKey, nVelocity);
		}
		catch (InvalidMidiDataException e)
		{
			if (TDebug.TraceAlsaSeq || TDebug.TraceAllExceptions)
			{
				TDebug.out(e);
			}
		}
		message = shortMessage;
		break;
	}

	case SND_SEQ_EVENT_KEYPRESS:
	{
		if (TDebug.TraceAlsaSeq)
		{
			TDebug.out("AlsaSeq.getEvent(): key pressure event");
		}
		ShortMessage	shortMessage = new ShortMessage();
		int	nCommand = 0xA0;
		int	nChannel = anValues[8] & 0xF;
		int	nKey = anValues[9] & 0x7F;
		int	nValue = anValues[10] & 0x7F;
		try
		{
			shortMessage.setMessage(nCommand, nChannel, nKey, nValue);
		}
		catch (InvalidMidiDataException e)
		{
			if (TDebug.TraceAlsaSeq || TDebug.TraceAllExceptions)
			{
				TDebug.out(e);
			}
		}
		message = shortMessage;
		break;
	}

	case SND_SEQ_EVENT_CONTROLLER:
	{
		if (TDebug.TraceAlsaSeq)
		{
			TDebug.out("AlsaSeq.getEvent(): controller event");
		}
		ShortMessage	shortMessage = new ShortMessage();
		int	nCommand = 0xB0;
		int	nChannel = anValues[8] & 0xF;
		int	nControl = anValues[9] & 0x7F;
		int	nValue = anValues[10] & 0x7F;
		try
		{

			shortMessage.setMessage(nCommand, nChannel, nControl, nValue);
		}
		catch (InvalidMidiDataException e)
		{
			if (TDebug.TraceAlsaSeq || TDebug.TraceAllExceptions)
			{
				TDebug.out(e);
			}
		}
		message = shortMessage;
		break;
	}

	case SND_SEQ_EVENT_PGMCHANGE:
	{
		if (TDebug.TraceAlsaSeq)
		{
			TDebug.out("AlsaSeq.getEvent(): program change event");
		}
		ShortMessage	shortMessage = new ShortMessage();
		int	nCommand = 0xC0;
		int	nChannel = anValues[8] & 0xF;
		int	nProgram = anValues[10] & 0x7F;
		try
		{
			shortMessage.setMessage(nCommand, nChannel, nProgram, 0);
		}
		catch (InvalidMidiDataException e)
		{
			if (TDebug.TraceAllExceptions)
			{
				TDebug.out(e);
			}
		}
		message = shortMessage;
		break;
	}

	case SND_SEQ_EVENT_CHANPRESS:
	{
		if (TDebug.TraceAlsaSeq)
		{
			TDebug.out("AlsaSeq.getEvent(): channel pressure event");
		}
		ShortMessage	shortMessage = new ShortMessage();
		int	nCommand = 0xD0;
		int	nChannel = anValues[8] & 0xF;
		int	nPressure = anValues[10] & 0x7F;
		try
		{
			// TODO: dirty: assuming time in ticks
			shortMessage.setMessage(nCommand, nChannel, nPressure, 0);
		}
		catch (InvalidMidiDataException e)
		{
			if (TDebug.TraceAlsaSeq || TDebug.TraceAllExceptions)
			{
				TDebug.out(e);
			}
		}
		message = shortMessage;
		break;
	}

	case SND_SEQ_EVENT_PITCHBEND:
	{
		if (TDebug.TraceAlsaSeq)
		{
			TDebug.out("AlsaSeq.getEvent(): pitchbend event");
		}
		ShortMessage	shortMessage = new ShortMessage();
		int	nCommand = 0xE0;
		int	nChannel = anValues[8] & 0xF;
		int	nValueLow = anValues[10] & 0x7F;
		int	nValueHigh = (anValues[10] >> 7) & 0x7F;
		try
		{
			shortMessage.setMessage(nCommand, nChannel, nValueLow, nValueHigh);
		}
		catch (InvalidMidiDataException e)
		{
			if (TDebug.TraceAlsaSeq || TDebug.TraceAllExceptions)
			{
				TDebug.out(e);
			}
		}
		message = shortMessage;
		break;
	}

	case SND_SEQ_EVENT_USR_VAR4:
	{
		if (TDebug.TraceAlsaSeq) { TDebug.out("AlsaSeq.getEvent(): meta event"); }
		MetaMessage	metaMessage = new MetaMessage();
		byte[]	abTransferData = (byte[]) aValues[0];
		int	nMetaType = abTransferData[0];
		byte[]	abData = new byte[abTransferData.length - 1];
		System.arraycopy(abTransferData, 1, abData, 0, abTransferData.length - 1);
		try
		{
			metaMessage.setMessage(nMetaType, abData, abData.length);
		}
		catch (InvalidMidiDataException e)
		{
			if (TDebug.TraceAlsaSeq || TDebug.TraceAllExceptions) { TDebug.out(e); }
		}
		message = metaMessage;
		break;
	}

	case SND_SEQ_EVENT_SYSEX:
	{
		if (TDebug.TraceAlsaSeq)
		{
			TDebug.out("AlsaSeq.getEvent(): sysex event");
		}
		SysexMessage	sysexMessage = new SysexMessage();
		byte[]	abCompleteData = (byte[]) aValues[0];
		int	nStatus = 0;
		int	nDataStart = 0;
		if (abCompleteData[0] == 0xF0)
		{
			nStatus = 0xF0;
			nDataStart = 1;
		}
		else
		{
			nStatus = 0xF7;
		}
		byte[]	abData = new byte[abCompleteData.length - nDataStart];
		System.arraycopy(abCompleteData, nDataStart, abData, 0, abCompleteData.length - 1);
		try
		{
			sysexMessage.setMessage(nStatus, abData, abData.length);
		}
		catch (InvalidMidiDataException e)
		{
			if (TDebug.TraceAlsaSeq || TDebug.TraceAllExceptions)
			{
				TDebug.out(e);
			}
		}
		message = sysexMessage;
		break;
	}

	default:
		if (TDebug.TraceAlsaSeq)
		{
			TDebug.out("AlsaSeq.getEvent(): unknown event");
		}

	}
	if (message != null)
	{
		/*
		  If the timestamp is in ticks, ticks in the MidiEvent
		  gets this value.
		  Otherwise, if the timestamp is in realtime (ns),
		  we put us in the tick value.
		*/
		long	lTick = 0L;
		if ((anValues[1] & SND_SEQ_TIME_STAMP_MASK) == SND_SEQ_TIME_STAMP_TICK)
		{
			lTick = alValues[0];
		}
		else
		{
			// ns -> us
			lTick = alValues[0] / 1000;
		}
		MidiEvent	event = new MidiEvent(message, lTick);
		return event;
	}
	else
	{
		return null;
	}
	}



	///////////////////////////////////////////////////////////




	private void outputCommonFields(
		int nType, int nFlags, int nTag, int nQueue, long lTime,
		int nSourcePort, int nDestClient, int nDestPort)
	{
		TDebug.out("AlsaSeq.sendNoteEvent():");
		TDebug.out("\t nType:" + nType);
		TDebug.out("\t nFlags:" + nFlags);
		TDebug.out("\t nTag:" + nTag);
		TDebug.out("\t nQueue:" + nQueue);
		TDebug.out("\t lTime:" + lTime);
		TDebug.out("\t nSourcePort:" + nSourcePort);
		TDebug.out("\t nDestClient:" + nDestClient);
		TDebug.out("\t nDestPort:" + nDestPort);
	}


	private void outputNoteFields(
		int nChannel, int nNote, int nVelocity, int nOffVelocity, int nDuration)
	{
		TDebug.out("\t nChannel:" + nChannel);
		TDebug.out("\t nNote:" + nNote);
		TDebug.out("\t nVelocity:" + nVelocity);
		TDebug.out("\t nOffVelocity:" + nOffVelocity);
		TDebug.out("\t nDuration:" + nDuration);
	}



	///////////////////////////////////////////////////////////


	/**	Event for the sequencer.
	 *	This class encapsulates an instance of
	 *	snd_seq_event_t.
	 */
	public static class Event
	{
		/**
		 *	Holds the pointer to snd_seq_event_t
		 *	for the native code.
		 *	This must be long to be 64bit-clean.
		 */
		/*private*/ long	m_lNativeHandle;



		public Event()
		{
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.Event.<init>(): begin"); }
			int	nReturn = malloc();
			if (nReturn < 0)
			{
				throw new RuntimeException("malloc of event failed");
			}
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.Event.<init>(): end"); }
		}



		public void finalize()
		{
			// TODO: call free()
			// call super.finalize() first or last?
			// and introduce a flag if free() has already been called?
		}



		private native int malloc();
		// TODO: this relies on snd_seq_event_free(), which is currently a no-op
		public native void free();

		// TODO: implement natively
		public native int getLength();

		public native int getType();
		public native int getFlags();
		public native int getTag();
		public native int getQueue();
		public native long getTimestamp();
		public native int getSourceClient();
		public native int getSourcePort();
		public native int getDestClient();
		public native int getDestPort();

		public native void getNote(int[] anValues);
		public native void getControl(int[] anValues);
		public native void getQueueControl(int[] anValues, long[] alValues);
		public native void getVar(Object[] aValues);

		public native void setCommon(int nType, int nFlags, int nTag, int nQueue, long lTimestamp, int nSourceClient, int nSourcePort, int nDestClient, int nDestPort);

		public native void setNote(int nChannel, int nKey, int nVelocity, int nOffVelocity, int nDuration);
		public native void setControl(int nChannel, int nParam, int nValue);
		public native void setQueueControl(int nControlQueue, int nControlValue, long lControlTime);
		public native void setVar(byte[] abData, int nOffset, int nLength);
	}



	/**	General information about the sequencer.
	 *	This class encapsulates the information of
	 *	snd_seq_system_info_t.
	 */
	public static class SystemInfo
	{
		/**
		 *	Holds the pointer to snd_seq_system_info_t
		 *	for the native code.
		 *	This must be long to be 64bit-clean.
		 */
		/*private*/ long	m_lNativeHandle;



		public SystemInfo()
		{
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.SystemInfo.<init>(): begin"); }
			int	nReturn = malloc();
			if (nReturn < 0)
			{
				throw new RuntimeException("malloc of system_info failed");
			}
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.SystemInfo.<init>(): end"); }
		}



		public void finalize()
		{
			// TODO: call free()
			// call super.finalize() first or last?
			// and introduce a flag if free() has already been called?
		}



		private native int malloc();
		public native void free();



		public native int getQueues();



		public native int getClients();



		public native int getPorts();



		public native int getChannels();



		public native int getCurrentClients();



		public native int getCurrentQueues();
	}



	public static class ClientInfo
	{
		/**
		 *	Holds the pointer to snd_seq_port_info_t
		 *	for the native code.
		 *	This must be long to be 64bit-clean.
		 */
		/*private*/ long	m_lNativeHandle;



		public ClientInfo()
		{
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.ClientInfo.<init>(): begin"); }
			int	nReturn = malloc();
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.ClientInfo.<init>(): malloc() returns: " + nReturn); }
			if (nReturn < 0)
			{
				throw new RuntimeException("malloc of client_info failed");
			}
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.ClientInfo.<init>(): end"); }
		}



		public void finalize()
		{
			// TODO: call free()
			// call super.finalize() first or last?
			// and introduce a flag if free() has already been called?
		}



		private native int malloc();
		public native void free();



		public native int getClient();

		public native int getType();

		public native String getName();

		public native int getBroadcastFilter();

		public native int getErrorBounce();

		// TODO: event filter

		public native int getNumPorts();

		public native int getEventLost();


		public native void setClient(int nClient);

		public native void setName(String strName);

		public native void setBroadcastFilter(int nBroadcastFilter);


		public native void setErrorBounce(int nErrorBounce);

		// TODO: event filter
	}



	public static class PortInfo
	{
		/**
		 *	Holds the pointer to snd_seq_port_info_t
		 *	for the native code.
		 *	This must be long to be 64bit-clean.
		 */
		/*private*/ long	m_lNativeHandle;



		public PortInfo()
		{
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.PortInfo.<init>(): begin"); }
			int	nReturn = malloc();
			if (nReturn < 0)
			{
				throw new RuntimeException("malloc of port_info failed");
			}
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.PortInfo.<init>(): end"); }
		}



		public void finalize()
		{
			// TODO: call free()
			// call super.finalize() first or last?
			// and introduce a flag if free() has already been called?
		}



		private native int malloc();
		public native void free();



		public native int getClient();



		public native int getPort();


		/**	Returns the name of the port.
			Calls snd_seq_port_info_get_name().
		*/
		public native String getName();



		public native int getCapability();



		public native int getType();



		public native int getMidiChannels();



		public native int getMidiVoices();



		public native int getSynthVoices();



		public native int getReadUse();



		public native int getWriteUse();



		public native int getPortSpecified();
	}



	public static class QueueInfo
	{
		/**
		 *	Holds the pointer to snd_seq_queue_info_t
		 *	for the native code.
		 *	This must be long to be 64bit-clean.
		 */
		/*private*/ long	m_lNativeHandle;



		public QueueInfo()
		{
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.QueueInfo.<init>(): begin"); }
			int	nReturn = malloc();
			if (nReturn < 0)
			{
				throw new RuntimeException("malloc of port_info failed");
			}
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.QueueInfo.<init>(): end"); }
		}



		public void finalize()
		{
			// TODO: call free()
			// call super.finalize() first or last?
			// and introduce a flag if free() has already been called?
		}



		private native int malloc();
		public native void free();

		public native int getQueue();
		public native String getName();
		public native int getOwner();
		public native boolean getLocked();
		public native int getFlags();

		public native void setName(String strName);
		public native void setOwner(int nOwner);
		public native void setLocked(boolean bLocked);
		public native void setFlags(int nFlags);

	}



	public static class QueueStatus
	{
		/**
		 *	Holds the pointer to snd_seq_queue_status_t
		 *	for the native code.
		 *	This must be long to be 64bit-clean.
		 */
		/*private*/ long	m_lNativeHandle;



		public QueueStatus()
		{
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.QueueStatus.<init>(): begin"); }
			int	nReturn = malloc();
			if (nReturn < 0)
			{
				throw new RuntimeException("malloc of port_info failed");
			}
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.QueueStatus.<init>(): end"); }
		}



		public void finalize()
		{
			// TODO: call free()
			// call super.finalize() first or last?
			// and introduce a flag if free() has already been called?
		}



		private native int malloc();
		public native void free();

		public native int getQueue();
		public native int getEvents();
		public native long getTickTime();
		public native long getRealTime();
		public native int getStatus();
	}



	public static class QueueTempo
	{
		/**
		 *	Holds the pointer to snd_seq_queue_tempo_t
		 *	for the native code.
		 *	This must be long to be 64bit-clean.
		 */
		/*private*/ long	m_lNativeHandle;



		public QueueTempo()
		{
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.QueueTempo.<init>(): begin"); }
			int	nReturn = malloc();
			if (nReturn < 0)
			{
				throw new RuntimeException("malloc of port_info failed");
			}
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.QueueTempo.<init>(): end"); }
		}



		public void finalize()
		{
			// TODO: call free()
			// call super.finalize() first or last?
			// and introduce a flag if free() has already been called?
		}



		private native int malloc();
		public native void free();

		public native int getQueue();
		public native int getTempo();
		public native int getPpq();
		public native void setTempo(int nTempo);
		public native void setPpq(int nPpq);
	}



	public static class QueueTimer
	{
		/**
		 *	Holds the pointer to snd_seq_queue_timer_t
		 *	for the native code.
		 *	This must be long to be 64bit-clean.
		 */
		/*private*/ long	m_lNativeHandle;



		public QueueTimer()
		{
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.QueueTimer.<init>(): begin"); }
			int	nReturn = malloc();
			if (nReturn < 0)
			{
				throw new RuntimeException("malloc of port_info failed");
			}
			if (TDebug.TraceAlsaSeqNative) { TDebug.out("AlsaSeq.QueueTimer.<init>(): end"); }
		}



		public void finalize()
		{
			// TODO: call free()
			// call super.finalize() first or last?
			// and introduce a flag if free() has already been called?
		}



		private native int malloc();
		public native void free();

		public native int getQueue();
		public native int getType();
		// TODO:
		// public native ?? getTimerId();
		public native int getResolution();

		public native void setType(int nType);
		// TODO:
							 // public native void setId(???);
		public native void setResolution(int nResolution);
	}



	private class ClientInfoIterator
	implements	Iterator
	{
		private int		m_nClient;
		private ClientInfo	m_clientInfo;



		public ClientInfoIterator()
		{
			m_nClient = -1;
			m_clientInfo = createNextClientInfo();
		}



		public boolean hasNext()
		{
			// TDebug.out("hasNext(): clientInfo: " + m_clientInfo);
			return m_clientInfo != null;
		}



		public Object next()
		{
			Object	next = m_clientInfo;
			m_clientInfo = createNextClientInfo();
			return next;
		}



		public void remove()
		{
			throw new UnsupportedOperationException();
		}


		private ClientInfo createNextClientInfo()
		{
			if (TDebug.TraceAlsaSeq) { TDebug.out("AlsaSeq.createNextClientInfo(): begin"); }
			ClientInfo	clientInfo = null;
			int[]		anValues = new int[1];
			int	nSuccess = getNextClient(m_nClient, anValues);
			if (TDebug.TraceAlsaSeq) { TDebug.out("succ: " + nSuccess); }
			if (nSuccess == 0)
			{
				// TDebug.out("AlsaSeq.createNextClientInfo(): getNextClientInfo successful");
				m_nClient = anValues[0];
				clientInfo = new ClientInfo();
				// TODO: error check
				getClientInfo(m_nClient, clientInfo);
			}
			if (TDebug.TraceAlsaSeq) { TDebug.out("AlsaSeq.createNextClientInfo(): end"); }
			return clientInfo;
		}
	}



	private class PortInfoIterator
	implements	Iterator
	{
		private int		m_nClient;
		private int		m_nPort;
		private PortInfo	m_portInfo;



		public PortInfoIterator(int nClient)
		{
			if (TDebug.TraceAlsaSeq) { TDebug.out("AlsaSeq.PortInfoIterator.<init>(): begin"); }
			m_nClient = nClient;
			m_nPort = -1;
			m_portInfo = createNextPortInfo();
			if (TDebug.TraceAlsaSeq) { TDebug.out("AlsaSeq.PortInfoIterator.<init>(): end"); }
		}



		public boolean hasNext()
		{
			return m_portInfo != null;
		}



		public Object next()
		{
			Object	next = m_portInfo;
			m_portInfo = createNextPortInfo();
			return next;
		}



		public void remove()
		{
			throw new UnsupportedOperationException();
		}


		private PortInfo createNextPortInfo()
		{
			if (TDebug.TraceAlsaSeq) { TDebug.out("AlsaSeq.PortInfoIterator.createNextPortInfo(): begin"); }
			PortInfo	portInfo = null;
			int[]		anValues = new int[2];
			int	nSuccess = getNextPort(m_nClient, m_nPort, anValues);
			if (TDebug.TraceAlsaSeq) { TDebug.out("AlsaSeq.PortInfoIterator.createNextPortInfo(): getNextPort() returns: " + nSuccess); }
			if (nSuccess == 0)
			{
				m_nPort = anValues[1];
				portInfo = new PortInfo();
				// TODO: error check
				getPortInfo(m_nClient, m_nPort, portInfo);
			}
			if (TDebug.TraceAlsaSeq) { TDebug.out("AlsaSeq.PortInfoIterator.createNextPortInfo(): end"); }
			return portInfo;
		}
	}

}



/*** AlsaSeq.java ***/
