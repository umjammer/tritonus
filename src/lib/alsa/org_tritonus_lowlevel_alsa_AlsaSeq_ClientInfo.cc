/*
 *	org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo.cc
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

#include	"common.h"
#include	"org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo.h"


static HandleFieldHandler<snd_seq_client_info_t*>	handler;


snd_seq_client_info_t*
getClientInfoNativeHandle(JNIEnv *env, jobject obj)
{
	return handler.getHandle(env, obj);
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo
 * Method:    malloc
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_malloc
(JNIEnv* env, jobject obj)
{
	snd_seq_client_info_t*	handle;
	int			nReturn;

	// extremely hacky
	debug_file = stderr;

	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_malloc(): begin\n"); }
	nReturn = snd_seq_client_info_malloc(&handle);
	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_malloc(): handle: %p\n", handle); }
	handler.setHandle(env, obj, handle);
	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_malloc(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo
 * Method:    free
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_free
(JNIEnv* env, jobject obj)
{
	snd_seq_client_info_t*	handle;

	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_free(): begin\n"); }
	handle = handler.getHandle(env, obj);
	snd_seq_client_info_free(handle);
	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_free(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo
 * Method:    getClient
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getClient
(JNIEnv* env, jobject obj)
{
	snd_seq_client_info_t*	handle;
	int			nReturn;

	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getClient(): begin\n"); }
	handle = handler.getHandle(env, obj);
	nReturn = snd_seq_client_info_get_client(handle);
	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getClient(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo
 * Method:    getType
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getType
(JNIEnv* env, jobject obj)
{
	snd_seq_client_info_t*	handle;
	int			nReturn;

	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getType(): begin\n"); }
	handle = handler.getHandle(env, obj);
	nReturn = snd_seq_client_info_get_type(handle);
	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getType(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo
 * Method:    getName
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getName
(JNIEnv* env, jobject obj)
{
	snd_seq_client_info_t*	handle;
	const char*		pName;
	jstring			strName;

	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getName(): begin\n"); }
	handle = handler.getHandle(env, obj);
	pName = snd_seq_client_info_get_name(handle);
	strName = env->NewStringUTF(pName);
	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getName(): end\n"); }
	return strName;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo
 * Method:    getBroadcastFilter
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getBroadcastFilter
(JNIEnv* env, jobject obj)
{
	snd_seq_client_info_t*	handle;
	int			nReturn;

	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getBroadcastFilter(): begin\n"); }
	handle = handler.getHandle(env, obj);
	nReturn = snd_seq_client_info_get_broadcast_filter(handle);
	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getBroadcastFilter(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo
 * Method:    getErrorBounce
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getErrorBounce
(JNIEnv* env, jobject obj)
{
	snd_seq_client_info_t*	handle;
	int			nReturn;

	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getErrorBounce(): begin\n"); }
	handle = handler.getHandle(env, obj);
	nReturn = snd_seq_client_info_get_error_bounce(handle);
	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getErrorBounce(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo
 * Method:    getNumPorts
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getNumPorts
(JNIEnv* env, jobject obj)
{
	snd_seq_client_info_t*	handle;
	int			nReturn;

	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getNumPorts(): begin\n"); }
	handle = handler.getHandle(env, obj);
	nReturn = snd_seq_client_info_get_num_ports(handle);
	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getNumPorts(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo
 * Method:    getEventLost
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getEventLost
(JNIEnv* env, jobject obj)
{
	snd_seq_client_info_t*	handle;
	int			nReturn;

	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getEventLost(): begin\n"); }
	handle = handler.getHandle(env, obj);
	nReturn = snd_seq_client_info_get_event_lost(handle);
	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_getEventLost(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo
 * Method:    setClient
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_setClient
(JNIEnv* env, jobject obj, jint nClient)
{
	snd_seq_client_info_t*	handle;

	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_setClient(): begin\n"); }
	handle = handler.getHandle(env, obj);
	snd_seq_client_info_set_client(handle, nClient);
	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_setClient(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo
 * Method:    setName
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_setName
(JNIEnv* env, jobject obj, jstring strName)
{
	snd_seq_client_info_t*	handle;
	const char*		pName;

	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_setName(): begin\n"); }
	handle = handler.getHandle(env, obj);
	pName = env->GetStringUTFChars(strName, NULL);
	snd_seq_client_info_set_name(handle, pName);
	env->ReleaseStringUTFChars(strName, pName);
	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_setName(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo
 * Method:    setBroadcastFilter
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_setBroadcastFilter
(JNIEnv* env, jobject obj, jint nBroadcastFilter)
{
	snd_seq_client_info_t*	handle;

	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_setBroadcastFilter(): begin\n"); }
	handle = handler.getHandle(env, obj);
	snd_seq_client_info_set_broadcast_filter(handle, nBroadcastFilter);
	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_setBroadcastFilter(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo
 * Method:    setErrorBounce
 * Signature: (I)V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_setErrorBounce
(JNIEnv* env, jobject obj, jint nErrorBounce)
{
	snd_seq_client_info_t*	handle;

	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_setErrorBounce(): begin\n"); }
	handle = handler.getHandle(env, obj);
	snd_seq_client_info_set_error_bounce(handle, nErrorBounce);
	if (debug_flag) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaSeq_00024ClientInfo_setErrorBounce(): end\n"); }
}



/*** org_tritonus_lowlevel_alsa_AlsaSeq_ClientInfo.cc ***/
