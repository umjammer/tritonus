/*
 *	org_tritonus_lowlevel_alsa_AlsaSeq.cc
 */

#include	<errno.h>
#include	<sys/asoundlib.h>
#include	"common.h"
#include	"org_tritonus_lowlevel_alsa_AlsaSeq.h"
// currently doesn't work for doubious reasons
// #include	"constants_check.h"
#include	"HandleFieldHandler.hh"

static int DEBUG = 0;
static FILE*	debug_file = NULL;

static HandleFieldHandler<snd_seq_t*>	handler;


static void
sendEvent(JNIEnv* env, snd_seq_t* seq, snd_seq_event_t* pEvent);

snd_seq_client_info_t*
getClientInfoNativeHandle(JNIEnv* env, jobject obj);
snd_seq_port_info_t*
getPortInfoNativeHandle(JNIEnv* env, jobject obj);
snd_seq_queue_info_t*
getQueueInfoNativeHandle(JNIEnv* env, jobject obj);
snd_seq_queue_status_t*
getQueueStatusNativeHandle(JNIEnv* env, jobject obj);
snd_seq_queue_tempo_t*
getQueueTempoNativeHandle(JNIEnv* env, jobject obj);
snd_seq_queue_timer_t*
getQueueTimerNativeHandle(JNIEnv* env, jobject obj);
snd_seq_system_info_t*
getSystemInfoNativeHandle(JNIEnv* env, jobject obj);




/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    open
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_open
(JNIEnv* env, jobject obj)
{
	snd_seq_t*	seq;
	int		nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_open(): begin\n"); }
	nReturn = snd_seq_open(&seq, "hw", SND_SEQ_OPEN_DUPLEX, 0);
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_open(): snd_seq_open() returns: %d\n", nReturn); }
	// perror("abc");
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_open() failed");
		return (jint) nReturn;
	}
	handler.setHandle(env, obj, seq);
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_open(): end\n"); }
	return (jint) nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_close
(JNIEnv* env, jobject obj)
{
	snd_seq_t*	seq;
	int		nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_close(): begin\n"); }
	seq = handler.getHandle(env, obj);
	nReturn = snd_seq_close(seq);
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_close() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_close(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    getName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_getName
(JNIEnv* env, jobject obj)
{
	snd_seq_t*	seq;
	const char*	pName;
	jstring		strName;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getName(): begin\n"); }
	seq = handler.getHandle(env, obj);
	pName = snd_seq_name(seq);
	if (pName == NULL)
	{
		throwRuntimeException(env, "snd_seq_name() failed");
	}
	strName = env->NewStringUTF(pName);
	// TODO: check return value
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getName(): end\n"); }
	return strName;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    getType
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_getType
(JNIEnv* env, jobject obj)
{
	snd_seq_t*	seq;
	int		nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getType(): begin\n"); }
	seq = handler.getHandle(env, obj);
	nReturn = snd_seq_type(seq);
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_type() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getType(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    setNonblock
 * Signature: (Z)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_setNonblock
(JNIEnv* env, jobject obj, jboolean bNonblock)
{
	snd_seq_t*	seq;
	int		nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_setNonblock(): begin\n"); }
	seq = handler.getHandle(env, obj);
	nReturn = snd_seq_nonblock(seq, bNonblock);
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_nonblock() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_setNonblock(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    getClientId
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_getClientId
(JNIEnv* env, jobject obj)
{
	snd_seq_t*	seq;
	int		nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getClientId(): begin\n"); }
	seq = handler.getHandle(env, obj);
	nReturn = snd_seq_client_id(seq);
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getClientId(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    getOutputBufferSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_getOutputBufferSize
(JNIEnv* env, jobject obj)
{
	snd_seq_t*	seq;
	int		nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getOutputBufferSize(): begin\n"); }
	seq = handler.getHandle(env, obj);
	nReturn = snd_seq_get_output_buffer_size(seq);
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getOutputBufferSize(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    getInputBufferSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_getInputBufferSize
(JNIEnv* env, jobject obj)
{
	snd_seq_t*	seq;
	int		nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getInputBufferSize(): begin\n"); }
	seq = handler.getHandle(env, obj);
	nReturn = snd_seq_get_input_buffer_size(seq);
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getInputBufferSize(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    setOutputBufferSize
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_setOutputBufferSize
(JNIEnv* env, jobject obj, jint nBufferSize)
{
	snd_seq_t*	seq;
	int		nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_setOutputBufferSize(): begin\n"); }
	seq = handler.getHandle(env, obj);
	nReturn = snd_seq_set_output_buffer_size(seq, nBufferSize);
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_setOutputBufferSize(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    setInputBufferSize
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_setInputBufferSize
(JNIEnv* env, jobject obj, jint nBufferSize)
{
	snd_seq_t*	seq;
	int		nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_setInputBufferSize(): begin\n"); }
	seq = handler.getHandle(env, obj);
	nReturn = snd_seq_set_input_buffer_size(seq, nBufferSize);
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_setInputBufferSize(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    getSystemInfo
 * Signature: (Lorg/tritonus/lowlevel/alsa/AlsaSeq$SystemInfo;)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_getSystemInfo
(JNIEnv* env, jobject obj, jobject systemInfoObj)
{
	snd_seq_t*		seq;
	snd_seq_system_info_t*	systemInfo;
	int			nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getSystemInfo(): begin\n"); }
	seq = handler.getHandle(env, obj);
	systemInfo = getSystemInfoNativeHandle(env, systemInfoObj);
	nReturn = snd_seq_system_info(seq, systemInfo);
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_system_info() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getSystemInfo(): end\n"); }
	return (jint) nReturn;
}


/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    getClientInfo
 * Signature: (ILorg/tritonus/lowlevel/alsa/AlsaSeq$ClientInfo;)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_getClientInfo
(JNIEnv* env, jobject obj, jint nClient, jobject clientInfoObj)
{
	snd_seq_t*		seq;
	snd_seq_client_info_t*	clientInfo;
	int			nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getClientInfo(): begin\n"); }
	seq = handler.getHandle(env, obj);
	clientInfo = getClientInfoNativeHandle(env, clientInfoObj);
	if (nClient >= 0)
	{
		nReturn = snd_seq_get_any_client_info(seq, nClient, clientInfo);
	}
	else
	{
		nReturn = snd_seq_get_client_info(seq, clientInfo);
	}
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_get_client_info() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getClientInfo(): end\n"); }
	return (jint) nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    setClientInfo
 * Signature: (Lorg/tritonus/lowlevel/alsa/AlsaSeq$ClientInfo;)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_setClientInfo
(JNIEnv* env, jobject obj, jobject clientInfoObj)
{
	snd_seq_t*		seq;
	snd_seq_client_info_t*	clientInfo;
	int			nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_setClientInfo(): begin\n"); }
	seq = handler.getHandle(env, obj);
	clientInfo = getClientInfoNativeHandle(env, clientInfoObj);
	nReturn = snd_seq_set_client_info(seq, clientInfo);
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_set_client_info() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_setClientInfo(): end\n"); }
	return (jint) nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    getNextClient
 * Signature: (I[I)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_getNextClient
(JNIEnv* env, jobject obj, jint nClient, jintArray anValues)
{
	snd_seq_t*		seq;
	snd_seq_client_info_t*	clientInfo;
	int			nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getNextClient(): begin\n"); }
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getNextClient(): passed client: %d\n", (int) nClient); }
	seq = handler.getHandle(env, obj);
	snd_seq_client_info_alloca(&clientInfo);
	snd_seq_client_info_set_client(clientInfo, nClient);
	nReturn = snd_seq_query_next_client(seq, clientInfo);
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getNextClient(): return value from snd_seq_query_next_client(): %d\n", nReturn); }
	if (nReturn < 0)
	{
		// -2 (no such file or directory): returned when no more client is available
		if (nReturn != -2)
		{
			throwRuntimeException(env, "snd_seq_query_next_client() failed");
		}
	}
	else
	{
		checkArrayLength(env, anValues, 1);
		jint	nThisClient = snd_seq_client_info_get_client(clientInfo);
		env->SetIntArrayRegion(anValues, 0, 1, &nThisClient);
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getNextClient(): end\n"); }
	return (jint) nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    getPortInfo
 * Signature: (IILorg/tritonus/lowlevel/alsa/AlsaSeq$PortInfo;)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_getPortInfo
(JNIEnv* env, jobject obj, jint nClient, jint nPort, jobject portInfoObj)
{
	snd_seq_t*		seq;
	snd_seq_port_info_t*	portInfo;
	int			nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getPortInfo(): begin\n"); }
	seq = handler.getHandle(env, obj);
	portInfo = getPortInfoNativeHandle(env, portInfoObj);
	if (nClient >= 0)
	{
		nReturn = snd_seq_get_any_port_info(seq, nClient, nPort, portInfo);
	}
	else
	{
		nReturn = snd_seq_get_port_info(seq, nPort, portInfo);
	}
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_get_[any]_port_info() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getPortInfo(): end\n"); }
	return (jint) nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    getNextPort
 * Signature: (II[I)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_getNextPort
(JNIEnv* env, jobject obj, jint nClient, jint nPort, jintArray anValues)
{
	snd_seq_t*		seq;
	snd_seq_port_info_t*	portInfo;
	int			nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getNextPort(): begin\n"); }
	seq = handler.getHandle(env, obj);
	snd_seq_port_info_alloca(&portInfo);
	snd_seq_port_info_set_client(portInfo, nClient);
	snd_seq_port_info_set_port(portInfo, nPort);
	nReturn = snd_seq_query_next_port(seq, portInfo);
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getNextPort(): snd_seq_query_next_port() returns: %d\n", nReturn); }
	if (nReturn < 0)
	{
		// -2 (no such file or directory): returned when no more port is available
		if (nReturn != -2)
		{
			throwRuntimeException(env, "snd_seq_query_next_port() failed");
		}
	}
	else
	{
		checkArrayLength(env, anValues, 2);
		jint	pnValues[2];
		pnValues[0] = snd_seq_port_info_get_client(portInfo);
		pnValues[1] = snd_seq_port_info_get_port(portInfo);
		env->SetIntArrayRegion(anValues, 0, 2, pnValues);
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getNextPort(): end\n"); }
	return (jint) nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    createPort
 * Signature: (Ljava/lang/String;IIIIII)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_createPort
(JNIEnv* env, jobject obj, jstring strName, jint nCapabilities, jint nGroupPermissions, jint nType, jint nMidiChannels, jint nMidiVoices, jint nSynthVoices)
{
	snd_seq_t*		seq;
	snd_seq_port_info_t*	portInfo;
	const char*		name;
	int			nReturn;
	int			nPort;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_createPort(): begin\n");	}
	snd_seq_port_info_alloca(&portInfo);
	seq = handler.getHandle(env, obj);
	// TODO: check if another action is required instead
	// memset(&portInfo, 0, sizeof(portInfo));
	name = env->GetStringUTFChars(strName, NULL);
	if (name == NULL)
	{
		throwRuntimeException(env, "GetStringUTFChars() failed");
	}
	snd_seq_port_info_set_name(portInfo, name);
	env->ReleaseStringUTFChars(strName, name);
	snd_seq_port_info_set_capability(portInfo, nCapabilities);
	snd_seq_port_info_set_port(portInfo, nGroupPermissions);
	snd_seq_port_info_set_type(portInfo, nType);
	snd_seq_port_info_set_midi_channels(portInfo, nMidiChannels);
	snd_seq_port_info_set_midi_voices(portInfo, nMidiVoices);
	snd_seq_port_info_set_synth_voices(portInfo, nSynthVoices);
	//portInfo.write_use = 1;	// R/O attrs?
	//portInfo.read_use = 1;

	// errno = 0;
	nReturn = snd_seq_create_port(seq, portInfo);
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_create_port() failed");
	}
	nPort = snd_seq_port_info_get_port(portInfo);
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_createPort(): end\n"); }
	return (jint) nPort;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    allocQueue
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_allocQueue
(JNIEnv* env, jobject obj)
{
	snd_seq_t*	seq;
	int		nQueue;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_allocQueue(): begin\n"); }
	seq = handler.getHandle(env, obj);
	nQueue = snd_seq_alloc_queue(seq);
	if (nQueue < 0)
	{
		throwRuntimeException(env, "snd_seq_alloc_queue() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_allocQueue(): end\n"); }
	return (jint) nQueue;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    freeQueue
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_freeQueue
(JNIEnv* env, jobject obj, jint nQueue)
{
	// TODO:
	return -1;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    getQueueInfo
 * Signature: (ILorg/tritonus/lowlevel/alsa/AlsaSeq$QueueInfo;)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_getQueueInfo
(JNIEnv* env, jobject obj, jint nQueue, jobject queueInfoObj)
{
	snd_seq_t*		seq;
	snd_seq_queue_info_t*	queueInfo;
	int			nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getQueueInfo(): begin\n"); }
	seq = handler.getHandle(env, obj);
	queueInfo = getQueueInfoNativeHandle(env, queueInfoObj);
	nReturn = snd_seq_get_queue_info(seq, nQueue, queueInfo);
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_get_queue_info() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getQueueInfo(): end\n"); }
	return (jint) nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    setQueueInfo
 * Signature: (ILorg/tritonus/lowlevel/alsa/AlsaSeq$QueueInfo;)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_setQueueInfo
(JNIEnv* env, jobject obj, jint nQueue, jobject queueInfoObj)
{
	snd_seq_t*		seq;
	snd_seq_queue_info_t*	queueInfo;
	int			nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_setQueueInfo(): begin\n"); }
	seq = handler.getHandle(env, obj);
	queueInfo = getQueueInfoNativeHandle(env, queueInfoObj);
	nReturn = snd_seq_set_queue_info(seq, nQueue, queueInfo);
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_set_queue_info() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_setQueueInfo(): end\n"); }
	return (jint) nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    getQueueStatus
 * Signature: (ILorg/tritonus/lowlevel/alsa/AlsaSeq$QueueStatus;)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_getQueueStatus
(JNIEnv* env, jobject obj, jint nQueue, jobject queueStatusObj)
{
	snd_seq_t*		seq;
	snd_seq_queue_status_t*	queueStatus;
	int			nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getQueueStatus(): begin\n"); }
	seq = handler.getHandle(env, obj);
	queueStatus = getQueueStatusNativeHandle(env, queueStatusObj);
	nReturn = snd_seq_get_queue_status(seq, nQueue, queueStatus);
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_get_queue_status() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getQueueStatus(): end\n"); }
	return (jint) nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    getQueueTempo
 * Signature: (ILorg/tritonus/lowlevel/alsa/AlsaSeq$QueueTempo;)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_getQueueTempo
(JNIEnv* env, jobject obj, jint nQueue, jobject queueTempoObj)
{
	snd_seq_t*		seq;
	snd_seq_queue_tempo_t*	pQueueTempo;
	int			nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getQueueTempo(): begin\n"); }
	seq = handler.getHandle(env, obj);
	pQueueTempo = getQueueTempoNativeHandle(env, queueTempoObj);
	nReturn = snd_seq_get_queue_tempo(seq, nQueue, pQueueTempo);
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_get_queue_tempo() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getQueueTempo(): end\n"); }
	return (jint) nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    setQueueTempo
 * Signature: (ILorg/tritonus/lowlevel/alsa/AlsaSeq$QueueTempo;)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_setQueueTempo
(JNIEnv* env, jobject obj, jint nQueue, jobject queueTempoObj)
{
	snd_seq_t*		seq;
	snd_seq_queue_tempo_t*	pQueueTempo;
	int			nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_setQueueTempo(): begin\n"); }
	seq = handler.getHandle(env, obj);
	pQueueTempo = getQueueTempoNativeHandle(env, queueTempoObj);
	nReturn = snd_seq_set_queue_tempo(seq, nQueue, pQueueTempo);
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_set_queue_tempo() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_setQueueTempo(): end\n"); }
	return (jint) nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    getQueueTimer
 * Signature: (ILorg/tritonus/lowlevel/alsa/AlsaSeq$QueueTimer;)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_getQueueTimer
(JNIEnv* env, jobject obj, jint nQueue, jobject queueTimerObj)
{
	snd_seq_t*		seq;
	snd_seq_queue_timer_t*	pQueueTimer;
	int			nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getQueueTimer(): begin\n"); }
	seq = handler.getHandle(env, obj);
	pQueueTimer = getQueueTimerNativeHandle(env, queueTimerObj);
	nReturn = snd_seq_get_queue_timer(seq, nQueue, pQueueTimer);
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_get_queue_timer() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getQueueTimer(): end\n"); }
	return (jint) nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    setQueueTimer
 * Signature: (ILorg/tritonus/lowlevel/alsa/AlsaSeq$QueueTimer;)I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_setQueueTimer
(JNIEnv* env, jobject obj, jint nQueue, jobject queueTimerObj)
{
	snd_seq_t*		seq;
	snd_seq_queue_timer_t*	pQueueTimer;
	int			nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_setQueueTimer(): begin\n"); }
	seq = handler.getHandle(env, obj);
	pQueueTimer = getQueueTimerNativeHandle(env, queueTimerObj);
	nReturn = snd_seq_set_queue_timer(seq, nQueue, pQueueTimer);
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_set_queue_timer() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_setQueueTimer(): end\n"); }
	return (jint) nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    subscribePort
 * Signature: (IIIIIZZZIII)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_subscribePort
(JNIEnv* env, jobject obj,
 jint nSenderClient, jint nSenderPort, jint nDestClient, jint nDestPort,
 jint nQueue, jboolean bExclusive, jboolean bRealtime, jboolean bConvertTime,
 jint nMidiChannels, jint nMidiVoices, jint nSynthVoices)
{
	snd_seq_t*			seq;
	snd_seq_port_subscribe_t*	subs;
	snd_seq_addr_t			sender;
	snd_seq_addr_t			dest;
	int				nReturn;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_subscribePort(): begin\n"); }
	seq = handler.getHandle(env, obj);
	snd_seq_port_subscribe_alloca(&subs);
	// memset(&subs, 0, sizeof(subs));

	sender.client = nSenderClient;
	sender.port = nSenderPort;
	snd_seq_port_subscribe_set_sender(subs, &sender);

	dest.client = nDestClient;
	dest.port = nDestPort;
	snd_seq_port_subscribe_set_dest(subs, &dest);

	snd_seq_port_subscribe_set_queue(subs, nQueue);
	snd_seq_port_subscribe_set_exclusive(subs, bExclusive);
	snd_seq_port_subscribe_set_time_update(subs, bConvertTime);
	snd_seq_port_subscribe_set_time_real(subs, bRealtime);
/* TODO: what happened to this?
	subs.midi_channels = nMidiChannels;
	subs.midi_voices = nMidiVoices;
	subs.synth_voices = nSynthVoices;
*/
	nReturn = snd_seq_subscribe_port(seq, subs);
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_subscribe_port() failed");
	}
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_subscribePort(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    sendControlEvent
 * Signature: (IIJIIIIIIIIII)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_sendControlEvent
(JNIEnv* env, jobject obj,
 jint nType, jint nFlags, jint nTag, jint nQueue, jlong lTime,
 jint nSourcePort, jint nDestClient, jint nDestPort,
 jint nChannel, jint nParam, jint nValue)
{
	snd_seq_t*		seq;
	snd_seq_event_t		event;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_sendControlEvent(): begin\n"); }
	seq = handler.getHandle(env, obj);
	memset(&event, 0, sizeof(event));

	event.type = nType;
	event.flags = nFlags;
	event.tag = nTag;
	event.queue = nQueue;
	if ((event.flags & SND_SEQ_TIME_STAMP_MASK) == SND_SEQ_TIME_STAMP_TICK)
	{
		event.time.tick = lTime;
	}
	else
	{
		event.time.time.tv_sec = lTime / 1000000000;
		event.time.time.tv_nsec = lTime % 1000000000;
	}

	// is set by the sequencer to sending client
	//event.source.client = nSourceClient;
	event.source.port = nSourcePort;
	event.dest.client = nDestClient;
	event.dest.port = nDestPort;

	event.data.control.channel = nChannel;
	event.data.control.param = nParam;
	event.data.control.value = nValue;

	sendEvent(env, seq, &event);
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_sendControlEvent(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    sendNoteEvent
 * Signature: (IIJIIIIIIIIIII)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_sendNoteEvent
(JNIEnv* env, jobject obj,
 jint nType, jint nFlags, jint nTag, jint nQueue, jlong lTime,
 jint nSourcePort, jint nDestClient, jint nDestPort,
 jint nChannel, jint nNote, jint nVelocity, jint nOffVelocity, jint nDuration)
{
	snd_seq_t*		seq;
	snd_seq_event_t		event;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_sendNoteEvent(): begin\n"); }
	seq = handler.getHandle(env, obj);
	memset(&event, 0, sizeof(event));

	event.type = nType;
	//printf("event.type: %d\n", event.type);
	//printf(": %d\n", );
	event.flags = nFlags;
	//printf("event.flags: %d\n", event.flags);
	event.tag = nTag;
	//printf("event.tag: %d\n", event.tag);
	event.queue = nQueue;
	//printf("event.queue: %d\n", event.queue);
	if ((event.flags & SND_SEQ_TIME_STAMP_MASK) == SND_SEQ_TIME_STAMP_TICK)
	{
		event.time.tick = lTime;
		//printf("event.time.tick: %d\n", event.time.tick);
	}
	else
	{
		event.time.time.tv_sec = lTime / 1000000000;
		event.time.time.tv_nsec = lTime % 1000000000;
	}

	// is set by the sequencer to sending client
	//event.source.client = nSourceClient;
	event.source.port = nSourcePort;
	//printf("event.source.port: %d\n", event.source.port);
	event.dest.client = nDestClient;
	//printf("event.dest.client: %d\n", event.dest.client);
	event.dest.port = nDestPort;
	//printf("event.dest.port: %d\n", event.dest.port);

	event.data.note.channel = nChannel;
	event.data.note.note = nNote;
	event.data.note.velocity = nVelocity;
	event.data.note.off_velocity = nOffVelocity;
	event.data.note.duration = nDuration;

	sendEvent(env, seq, &event);
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_sendNoteEvent(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    sendQueueControlEvent
 * Signature: (IIIIJIIIIIIJ)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_sendQueueControlEvent
(JNIEnv* env, jobject obj,
 jint nType, jint nFlags, jint nTag, jint nQueue, jlong lTime,
 jint nSourcePort, jint nDestClient, jint nDestPort,
 jint nControlQueue, jint nControlValue, jlong lControlTime)
{
	snd_seq_t*		seq;
	snd_seq_event_t		event;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_sendQueueControlEvent(): begin\n"); }
	seq = handler.getHandle(env, obj);
	memset(&event, 0, sizeof(event));

	event.type = nType;
	event.flags = nFlags;
	event.tag = nTag;
	event.queue = nQueue;
	if ((event.flags & SND_SEQ_TIME_STAMP_MASK) == SND_SEQ_TIME_STAMP_TICK)
	{
		event.time.tick = lTime;
	}
	else
	{
		event.time.time.tv_sec = lTime / 1000000000;
		event.time.time.tv_nsec = lTime % 1000000000;
	}

	// is set by the sequencer to sending client
	//event.source.client = nSourceClient;
	event.source.port = nSourcePort;
	event.dest.client = nDestClient;
	event.dest.port = nDestPort;

	event.data.queue.queue = nControlQueue;
	if (nType == SND_SEQ_EVENT_TEMPO)
	{
		event.data.queue.param.value = nControlValue;
	}
	else if (nType == SND_SEQ_EVENT_SETPOS_TICK)
	{
		event.data.queue.param.time.tick = lTime;
	}
	else if (nType == SND_SEQ_EVENT_SETPOS_TIME)
	{
		event.data.queue.param.time.time.tv_sec = lTime / 1000000000;
		event.data.queue.param.time.time.tv_nsec = lTime % 1000000000;
	}

	sendEvent(env, seq, &event);
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_sendQueueControlEvent(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    sendVarEvent
 * Signature: (IIIIJIII[BII)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_sendVarEvent
(JNIEnv* env, jobject obj,
 jint nType, jint nFlags, jint nTag, jint nQueue, jlong lTime,
 jint nSourcePort, jint nDestClient, jint nDestPort,
 jbyteArray abData, jint nOffset, jint nLength)
{
	snd_seq_t*		seq;
	snd_seq_event_t		event;
	jbyte*			data;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_sendVarEvent(): begin\n"); }
	seq = handler.getHandle(env, obj);
	memset(&event, 0, sizeof(event));

	event.type = nType;
	event.flags = nFlags;
	event.tag = nTag;
	event.queue = nQueue;
	if ((event.flags & SND_SEQ_TIME_STAMP_MASK) == SND_SEQ_TIME_STAMP_TICK)
	{
		event.time.tick = lTime;
	}
	else
	{
		event.time.time.tv_sec = lTime / 1000000000;
		event.time.time.tv_nsec = lTime % 1000000000;
	}

	// is set by the sequencer to sending client
	//event.source.client = nSourceClient;
	event.source.port = nSourcePort;
	event.dest.client = nDestClient;
	event.dest.port = nDestPort;

	data = env->GetByteArrayElements(abData, NULL);
	if (data == NULL)
	{
		throwRuntimeException(env, "GetByteArrayElements() failed");
	}
	event.data.ext.ptr = data + nOffset;
	event.data.ext.len = nLength;

	sendEvent(env, seq, &event);
	// TODO: some sort of release of the array elements necessary?
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_sendVarEvent(): end\n"); }
}



static void
sendEvent(JNIEnv* env,
	  snd_seq_t*		seq,
	  snd_seq_event_t*	pEvent)
{
	int	nReturn = 0;

	nReturn = snd_seq_event_output(seq, pEvent);
	// IDEA: execute drain only if event wants to circumvent queues?
	if (nReturn < 0)
	{
		printf("snd_seq_event_output() returns: %d\n", nReturn);
		// printf("errno: %d\n", errno);
		perror("abc");
		throwRuntimeException(env, "snd_seq_event_output() failed");
	}
	// we have to restart this call in case it is interrupted by a signal.
	do
	{
		errno = 0;
		if (DEBUG) { (void) fprintf(debug_file, "before draining output\n"); }
		nReturn = snd_seq_drain_output(seq);
		if (DEBUG) { (void) fprintf(debug_file, "after draining output\n"); }
	}
	while ((nReturn == -1 && errno == EINTR) || nReturn == -EINTR);
	if (nReturn < 0)
	{
		(void) fprintf(debug_file, "return: %d\n", nReturn);
		// (void) fprintf(debug_file, "errno: %d\n", errno);
		perror("abc");
		throwRuntimeException(env, "snd_seq_drain_output() failed");
	}
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    getEvent
 * Signature: ([I[J[Ljava/lang/Object;)Z
 */
JNIEXPORT jboolean JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_getEvent
(JNIEnv* env, jobject obj, jintArray anValues, jlongArray alValues, jobjectArray aObjValues)
{
	snd_seq_t*		seq;
	snd_seq_event_t*	pEvent;
	int			nReturn;
	jint*			panValues;
	jlong*			palValues;
	jbyteArray		byteArray;

	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getEvent(): begin\n"); }
	seq = handler.getHandle(env, obj);

	/*
	 *	snd_seq_event_input() results in a blocking read on a
	 *	device file. There are two problems:
	 *	1. green threads VMs do no blocking read. Therefore, this
	 *	code doesn't work with green threads at all. A solution is
	 *	outstanding.
	 *	2. In some cases, the read is interrupted by a signal. This
	 *	is the reason for the do..while.
	 */
	do
	{
		// printf("1\n");
		//errno = 0;
		nReturn = snd_seq_event_input(seq, &pEvent);
		//printf("return: %d\n", nReturn);
		// printf("event: %p\n", pEvent);
		// printf("errno: %d\n", errno);
		//perror("abc");
		// printf("2\n");
	}
	while (nReturn == -EINTR);
	if (pEvent == NULL)
	{
		return JNI_FALSE;
	}
	// now uesless?
	if (nReturn < 0)
	{
		throwRuntimeException(env, "snd_seq_event_input() failed");
	}
	checkArrayLength(env, anValues, 13);
	panValues = env->GetIntArrayElements(anValues, NULL);
	// printf("4\n");
	if (panValues == NULL)
	{
		throwRuntimeException(env, "GetIntArrayElements() failed");
	}

	checkArrayLength(env, alValues, 1);
	palValues = env->GetLongArrayElements(alValues, NULL);
	// printf("6\n");
	if (palValues == NULL)
	{
		throwRuntimeException(env, "GetLongArrayElements() failed");
	}
	// printf("6a\n");
	panValues[0] = pEvent->type;
	// printf("6b\n");
	panValues[1] = pEvent->flags;
	// printf("6c\n");
	panValues[2] = pEvent->tag;
	// printf("6d\n");
	panValues[3] = pEvent->queue;

	panValues[4] = pEvent->source.client;
	panValues[5] = pEvent->source.port;

	panValues[6] = pEvent->dest.client;
	panValues[7] = pEvent->dest.port;

	if ((pEvent->flags & SND_SEQ_TIME_STAMP_MASK) == SND_SEQ_TIME_STAMP_TICK)
	{
		palValues[0] = pEvent->time.tick;
	}
	else	// time
	{
		palValues[0] = (jlong) pEvent->time.time.tv_sec * (jlong) 1E9 + (jlong) pEvent->time.time.tv_nsec;
	}

	switch (pEvent->type)
	{
	case SND_SEQ_EVENT_NOTE:
	case SND_SEQ_EVENT_NOTEON:
	case SND_SEQ_EVENT_NOTEOFF:
		panValues[8] = pEvent->data.note.channel;
		panValues[9] = pEvent->data.note.note;
		panValues[10] = pEvent->data.note.velocity;
		panValues[11] = pEvent->data.note.off_velocity;
		panValues[12] = pEvent->data.note.duration;
		break;

	case SND_SEQ_EVENT_KEYPRESS:
	case SND_SEQ_EVENT_CONTROLLER:
	case SND_SEQ_EVENT_PGMCHANGE:
	case SND_SEQ_EVENT_CHANPRESS:
	case SND_SEQ_EVENT_PITCHBEND:
	case SND_SEQ_EVENT_CONTROL14:
	case SND_SEQ_EVENT_NONREGPARAM:
	case SND_SEQ_EVENT_REGPARAM:
		panValues[8] = pEvent->data.control.channel;
		panValues[9] = pEvent->data.control.param;
		panValues[10] = pEvent->data.control.value;
		break;

	case SND_SEQ_EVENT_SYSEX:
	case SND_SEQ_EVENT_BOUNCE:
	case SND_SEQ_EVENT_USR_VAR0:
	case SND_SEQ_EVENT_USR_VAR1:
	case SND_SEQ_EVENT_USR_VAR2:
	case SND_SEQ_EVENT_USR_VAR3:
	case SND_SEQ_EVENT_USR_VAR4:
	{
		jbyteArray	abData;
		abData = env->NewByteArray(pEvent->data.ext.len);
		if (abData == NULL)
		{
			throwRuntimeException(env, "NewByteArray() failed");
		}
		env->SetByteArrayRegion(abData, (jsize) 0, (jsize) pEvent->data.ext.len, (jbyte*) pEvent->data.ext.ptr);
		checkArrayLength(env, aObjValues, 1);
		env->SetObjectArrayElement(aObjValues, 0, abData);
	}
	break;
	}



	env->ReleaseIntArrayElements(anValues, panValues, 0);
	env->ReleaseLongArrayElements(alValues, palValues, 0);

	if ((pEvent->flags & SND_SEQ_EVENT_LENGTH_MASK) == SND_SEQ_EVENT_LENGTH_VARUSR)
	{
		byteArray = env->NewByteArray(pEvent->data.ext.len);
		if (byteArray == NULL)
		{
			throwRuntimeException(env, "NewByteArray() failed");
		}
		env->SetByteArrayRegion(byteArray, (jsize) 0, (jsize) pEvent->data.ext.len, (jbyte*) pEvent->data.ext.ptr);
		env->SetObjectArrayElement(aObjValues, 0, byteArray);
	}
	// TODO: should events be freed with snd_seq_free_event()?
	if (DEBUG) { (void) fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_getEvent(): end\n"); }
	return JNI_TRUE;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq
 * Method:    setTrace
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_setTrace
(JNIEnv* env, jclass cls, jboolean bTrace)
{
	DEBUG = bTrace;
	debug_file = stderr;
}



/*** org_tritonus_lowlevel_alsa_AlsaSeq.cc ***/
