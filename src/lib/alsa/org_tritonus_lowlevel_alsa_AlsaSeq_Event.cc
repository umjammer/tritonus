/*
 *	org_tritonus_lowlevel_alsa_AlsaSeq_Event.cc
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
 */


#include	<sys/asoundlib.h>
#include	"common.h"
#include	"org_tritonus_lowlevel_alsa_AlsaSeq_Event.h"
#include	"HandleFieldHandler.hh"

static int DEBUG = 0;
static FILE*	debug_file = NULL;

static HandleFieldHandler<snd_seq_event_t*>	handler;


snd_seq_event_t*
getEventNativeHandle(JNIEnv* env, jobject obj)
{
	return handler.getHandle(env, obj);
}


void
setEventNativeHandle(JNIEnv* env, jobject obj, snd_seq_event_t* handle)
{
	handler.setHandle(env, obj, handle);
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    malloc
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_malloc
(JNIEnv* env, jobject obj)
{
	snd_seq_event_t*	handle;
	int			nReturn;

	// extremely hacky
	debug_file = stderr;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_malloc(): begin\n"); }
	handle = snd_seq_create_event();
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_malloc(): handle: %p\n", handle); }
	handler.setHandle(env, obj, handle);
	if (handle != NULL)
	{
		nReturn = 0;
	}
	else
	{
		nReturn = -1;
	}
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_malloc(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    free
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_free
(JNIEnv* env, jobject obj)
{
	snd_seq_event_t*	handle;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_free(): begin\n"); }
	handle = handler.getHandle(env, obj);
	snd_seq_free_event(handle);
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_free(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    getType
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getType
(JNIEnv* env, jobject obj)
{
	snd_seq_event_t*	handle;
	int			nReturn;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getType(): begin\n"); }
	handle = handler.getHandle(env, obj);
	nReturn = handle->type;
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getType(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    getFlags
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getFlags
(JNIEnv* env, jobject obj)
{
	snd_seq_event_t*	handle;
	int			nReturn;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getFlags(): begin\n"); }
	handle = handler.getHandle(env, obj);
	nReturn = handle->flags;
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getFlags(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    getTag
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getTag
(JNIEnv* env, jobject obj)
{
	snd_seq_event_t*	handle;
	int			nReturn;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getTag(): begin\n"); }
	handle = handler.getHandle(env, obj);
	nReturn = handle->tag;
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getTag(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    getQueue
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getQueue
(JNIEnv* env, jobject obj)
{
	snd_seq_event_t*	handle;
	int			nReturn;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getQueue(): begin\n"); }
	handle = handler.getHandle(env, obj);
	nReturn = handle->type;
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getQueue(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    getTimestamp
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getTimestamp
(JNIEnv* env, jobject obj)
{
	snd_seq_event_t*	handle;
	jlong			lTimestamp;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getTimestamp(): begin\n"); }
	handle = handler.getHandle(env, obj);
	if ((handle->flags & SND_SEQ_TIME_STAMP_MASK) == SND_SEQ_TIME_STAMP_TICK)
	{
		lTimestamp = handle->time.tick;
	}
	else	// time
	{
		lTimestamp = (jlong) handle->time.time.tv_sec * (jlong) 1000000000 + (jlong) handle->time.time.tv_nsec;
	}
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getTimestamp(): end\n"); }
	return lTimestamp;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    getSourceClient
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getSourceClient
(JNIEnv* env, jobject obj)
{
	snd_seq_event_t*	handle;
	int			nReturn;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getSourceClient(): begin\n"); }
	handle = handler.getHandle(env, obj);
	nReturn = handle->source.client;
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getSourceClient(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    getSourcePort
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getSourcePort
(JNIEnv* env, jobject obj)
{
	snd_seq_event_t*	handle;
	int			nReturn;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getSourcePort(): begin\n"); }
	handle = handler.getHandle(env, obj);
	nReturn = handle->source.port;
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getSourcePort(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    getDestClient
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getDestClient
(JNIEnv* env, jobject obj)
{
	snd_seq_event_t*	handle;
	int			nReturn;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getDestClient(): begin\n"); }
	handle = handler.getHandle(env, obj);
	nReturn = handle->dest.client;
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getDestClient(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    getDestPort
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getDestPort
(JNIEnv* env, jobject obj)
{
	snd_seq_event_t*	handle;
	int			nReturn;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getDestPort(): begin\n"); }
	handle = handler.getHandle(env, obj);
	nReturn = handle->dest.port;
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getDestPort(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    getNote
 * Signature: ([I)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getNote
(JNIEnv* env, jobject obj, jintArray anValues)
{
	snd_seq_event_t*	handle;
	jint*			panValues;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getNote(): begin\n"); }
	handle = handler.getHandle(env, obj);
	checkArrayLength(env, anValues, 5);
	panValues = env->GetIntArrayElements(anValues, NULL);
	if (panValues == NULL)
	{
		throwRuntimeException(env, "GetIntArrayElements() failed");
	}
	panValues[0] = handle->data.note.channel;
	panValues[1] = handle->data.note.note;
	panValues[2] = handle->data.note.velocity;
	panValues[3] = handle->data.note.off_velocity;
	panValues[4] = handle->data.note.duration;
	env->ReleaseIntArrayElements(anValues, panValues, 0);
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getNote(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    getControl
 * Signature: ([I)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getControl
(JNIEnv* env, jobject obj, jintArray anValues)
{
	snd_seq_event_t*	handle;
	jint*			panValues;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getControl(): begin\n"); }
	handle = handler.getHandle(env, obj);
	checkArrayLength(env, anValues, 3);
	panValues = env->GetIntArrayElements(anValues, NULL);
	if (panValues == NULL)
	{
		throwRuntimeException(env, "GetIntArrayElements() failed");
	}
	panValues[0] = handle->data.control.channel;
	panValues[1] = handle->data.control.param;
	panValues[2] = handle->data.control.value;
	env->ReleaseIntArrayElements(anValues, panValues, 0);
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getControl(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    getQueueControl
 * Signature: ([I[J)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getQueueControl
(JNIEnv* env, jobject obj, jintArray anValues, jlongArray alValues)
{
	snd_seq_event_t*	handle;
	jint*			panValues;
	jlong*			palValues;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getQueueControl(): begin\n"); }
	handle = handler.getHandle(env, obj);
	checkArrayLength(env, anValues, 2);
	checkArrayLength(env, alValues, 1);
	panValues = env->GetIntArrayElements(anValues, NULL);
	if (panValues == NULL)
	{
		throwRuntimeException(env, "GetIntArrayElements() failed");
	}
	palValues = env->GetLongArrayElements(alValues, NULL);
	if (palValues == NULL)
	{
		throwRuntimeException(env, "GetLongArrayElements() failed");
	}
	panValues[0] = handle->data.queue.queue;
	int	nType = handle->type;

	if (nType == SND_SEQ_EVENT_TEMPO)
	{
		panValues[1] = handle->data.queue.param.value;
	}
	else if (nType == SND_SEQ_EVENT_SETPOS_TICK)
	{
		palValues[0] = handle->data.queue.param.time.tick;
	}
	else if (nType == SND_SEQ_EVENT_SETPOS_TIME)
	{
		palValues[0] = (jlong) handle->data.queue.param.time.time.tv_sec * (jlong) 1000000000 + (jlong) handle->data.queue.param.time.time.tv_nsec;
	}
	env->ReleaseIntArrayElements(anValues, panValues, 0);
	env->ReleaseLongArrayElements(alValues, palValues, 0);
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getQueueControl(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    getVar
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getVar
(JNIEnv* env, jobject obj)
{
	snd_seq_event_t*	handle;
	jbyteArray		abData;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getVar(): begin\n"); }
	handle = handler.getHandle(env, obj);
	abData = env->NewByteArray(handle->data.ext.len);
	if (abData == NULL)
	{
		throwRuntimeException(env, "NewByteArray() failed");
	}
	env->SetByteArrayRegion(abData, (jsize) 0, (jsize) handle->data.ext.len, (jbyte*) handle->data.ext.ptr);
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_getVar(): end\n"); }
	return abData;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    setCommon
 * Signature: (IIIIJIIII)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setCommon
(JNIEnv* env, jobject obj, jint nType, jint nFlags, jint nTag, jint nQueue, jlong lTimestamp, jint nSourceClient, jint nSourcePort, jint nDestClient, jint nDestPort)
{
	snd_seq_event_t*	handle;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setCommon(): begin\n"); }
	handle = handler.getHandle(env, obj);
	handle->type = nType;
	handle->flags = nFlags;
	handle->tag = nTag;
	handle->queue = nQueue;
	if ((handle->flags & SND_SEQ_TIME_STAMP_MASK) == SND_SEQ_TIME_STAMP_TICK)
	{
		handle->time.tick = lTimestamp;
	}
	else
	{
		handle->time.time.tv_sec = lTimestamp / 1000000000;
		handle->time.time.tv_nsec = lTimestamp % 1000000000;
	}

	// source client is set by the sequencer to sending client
	handle->source.port = nSourcePort;
	handle->dest.client = nDestClient;
	handle->dest.port = nDestPort;
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setCommon(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    setTimestamp
 * Signature: (J)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setTimestamp
(JNIEnv* env, jobject obj, jlong lTimestamp)
{
	snd_seq_event_t*	handle;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setTimestamp(): begin\n"); }
	handle = handler.getHandle(env, obj);
	if ((handle->flags & SND_SEQ_TIME_STAMP_MASK) == SND_SEQ_TIME_STAMP_TICK)
	{
		handle->time.tick = lTimestamp;
	}
	else
	{
		handle->time.time.tv_sec = lTimestamp / 1000000000;
		handle->time.time.tv_nsec = lTimestamp % 1000000000;
	}
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setTimestamp(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    setNote
 * Signature: (IIIII)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setNote
(JNIEnv* env, jobject obj, jint nChannel, jint nNote, jint nVelocity, jint nOffVelocity, jint nDuration)
{
	snd_seq_event_t*	handle;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setNote(): begin\n"); }
	handle = handler.getHandle(env, obj);
	handle->data.note.channel = nChannel;
	handle->data.note.note = nNote;
	handle->data.note.velocity = nVelocity;
	handle->data.note.off_velocity = nOffVelocity;
	handle->data.note.duration = nDuration;
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setNote(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    setControl
 * Signature: (III)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setControl
(JNIEnv* env, jobject obj, jint nChannel, jint nParam, jint nValue)
{
	snd_seq_event_t*	handle;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setControl(): begin\n"); }
	handle = handler.getHandle(env, obj);
	handle->data.control.channel = nChannel;
	handle->data.control.param = nParam;
	handle->data.control.value = nValue;
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setControl(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    setQueueControl
 * Signature: (IIJ)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setQueueControl
(JNIEnv* env, jobject obj, jint nQueue, jint nValue, jlong lTime)
{
	snd_seq_event_t*	handle;
	int			nType;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setQueueControl(): begin\n"); }
	handle = handler.getHandle(env, obj);
	nType = handle->type;
	handle->data.queue.queue = nQueue;
	if (nType == SND_SEQ_EVENT_TEMPO)
	{
		handle->data.queue.param.value = nValue;
	}
	else if (nType == SND_SEQ_EVENT_SETPOS_TICK)
	{
		handle->data.queue.param.time.tick = lTime;
	}
	else if (nType == SND_SEQ_EVENT_SETPOS_TIME)
	{
		handle->data.queue.param.time.time.tv_sec = lTime / 1000000000;
		handle->data.queue.param.time.time.tv_nsec = lTime % 1000000000;
	}
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setQueueControl(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_Event
 * Method:    setVar
 * Signature: ([BII)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setVar
(JNIEnv* env, jobject obj, jbyteArray abData, jint nOffset, jint nLength)
{
	snd_seq_event_t*	handle;
	jbyte*			pData;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setVar(): begin\n"); }
	handle = handler.getHandle(env, obj);
	pData = (jbyte*) malloc(nLength);
	if (pData == NULL)
	{
		throwRuntimeException(env, "malloc() failed");
	}
	env->GetByteArrayRegion(abData, nOffset, nLength, pData);
	handle->data.ext.ptr = pData;
	handle->data.ext.len = nLength;
	// TODO: the memory allocated here is never free'd!!
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024Event_setVar(): end\n"); }
}



/*** org_tritonus_lowlevel_alsa_AlsaSeq_Event.cc ***/
