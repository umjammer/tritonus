/*
 *	org_tritonus_lowlevel_alsa_AlsaPcm_SWParams.cc
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
#include	"org_tritonus_lowlevel_alsa_AlsaPcm_SWParams.h"


static HandleFieldHandler<snd_pcm_sw_params_t*>	handler;



snd_pcm_sw_params_t*
getSWParamsNativeHandle(JNIEnv *env, jobject obj)
{
	return handler.getHandle(env, obj);
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaPcm_SWParams
 * Method:    malloc
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaPcm_00024SWParams_malloc
(JNIEnv *env, jobject obj)
{
	snd_pcm_sw_params_t*	handle;
	int			nReturn;

	// extremely hacky
	debug_file = stderr;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaPcm_SWParams_malloc(): begin\n"); }
	nReturn = snd_pcm_sw_params_malloc(&handle);
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaPcm_SWParams_malloc(): handle: %p\n", handle); }
	handler.setHandle(env, obj, handle);
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaPcm_SWParams_malloc(): end\n"); }
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaPcm_SWParams
 * Method:    free
 * Signature: ()V
 */
JNIEXPORT void JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaPcm_00024SWParams_free
(JNIEnv *env, jobject obj)
{
	snd_pcm_sw_params_t*	handle;

	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaPcm_SWParams_free(): begin\n"); }
	handle = handler.getHandle(env, obj);
	snd_pcm_sw_params_free(handle);
	if (DEBUG) { fprintf(debug_file, "Java_org_tritonus_lowlevel_alsa_AlsaPcm_SWParams_free(): end\n"); }
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaPcm_SWParams
 * Method:    getStartMode
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaPcm_00024SWParams_getStartMode
(JNIEnv *env, jobject obj)
{
	snd_pcm_sw_params_t*	handle;
	int			nReturn;

	handle = handler.getHandle(env, obj);
	nReturn = snd_pcm_sw_params_get_start_mode(handle);
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaPcm_SWParams
 * Method:    getXrunMode
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaPcm_00024SWParams_getXrunMode
(JNIEnv *env, jobject obj)
{
	snd_pcm_sw_params_t*	handle;
	int			nReturn;

	handle = handler.getHandle(env, obj);
	nReturn = snd_pcm_sw_params_get_xrun_mode(handle);
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaPcm_SWParams
 * Method:    getTStampMode
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaPcm_00024SWParams_getTStampMode
(JNIEnv *env, jobject obj)
{
	snd_pcm_sw_params_t*	handle;
	int			nReturn;

	handle = handler.getHandle(env, obj);
	nReturn = snd_pcm_sw_params_get_tstamp_mode(handle);
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaPcm_SWParams
 * Method:    getSleepMin
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaPcm_00024SWParams_getSleepMin
(JNIEnv *env, jobject obj)
{
	snd_pcm_sw_params_t*	handle;
	int			nReturn;

	handle = handler.getHandle(env, obj);
	nReturn = snd_pcm_sw_params_get_sleep_min(handle);
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaPcm_SWParams
 * Method:    getAvailMin
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaPcm_00024SWParams_getAvailMin
(JNIEnv *env, jobject obj)
{
	snd_pcm_sw_params_t*	handle;
	int			nReturn;

	handle = handler.getHandle(env, obj);
	nReturn = snd_pcm_sw_params_get_avail_min(handle);
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaPcm_SWParams
 * Method:    getXferAlign
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaPcm_00024SWParams_getXferAlign
(JNIEnv *env, jobject obj)
{
	snd_pcm_sw_params_t*	handle;
	int			nReturn;

	handle = handler.getHandle(env, obj);
	nReturn = snd_pcm_sw_params_get_xfer_align(handle);
	return nReturn;
}




/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaPcm_SWParams
 * Method:    getStartThreshold
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaPcm_00024SWParams_getStartThreshold
(JNIEnv *env, jobject obj)
{
	snd_pcm_sw_params_t*	handle;
	int			nReturn;

	handle = handler.getHandle(env, obj);
	nReturn = snd_pcm_sw_params_get_start_threshold(handle);
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaPcm_SWParams
 * Method:    getStopThreshold
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaPcm_00024SWParams_getStopThreshold
(JNIEnv *env, jobject obj)
{
	snd_pcm_sw_params_t*	handle;
	int			nReturn;

	handle = handler.getHandle(env, obj);
	nReturn = snd_pcm_sw_params_get_stop_threshold(handle);
	return nReturn;
}


/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaPcm_SWParams
 * Method:    getSilenceThreshold
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaPcm_00024SWParams_getSilenceThreshold
(JNIEnv *env, jobject obj)
{
	snd_pcm_sw_params_t*	handle;
	int			nReturn;

	handle = handler.getHandle(env, obj);
	nReturn = snd_pcm_sw_params_get_silence_threshold(handle);
	return nReturn;
}



/*
 * Class:     org_tritonus_lowlevel_alsa_AlsaPcm_SWParams
 * Method:    getSilenceSize
 * Signature: ()I
 */
JNIEXPORT jint JNICALL
Java_org_tritonus_lowlevel_alsa_AlsaPcm_00024SWParams_getSilenceSize
(JNIEnv *env, jobject obj)
{
	snd_pcm_sw_params_t*	handle;
	int			nReturn;

	handle = handler.getHandle(env, obj);
	nReturn = snd_pcm_sw_params_get_silence_size(handle);
	return nReturn;
}



/*** org_tritonus_lowlevel_alsa_AlsaPcm_SWParams.cc ***/
