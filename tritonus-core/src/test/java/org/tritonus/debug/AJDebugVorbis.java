/*
 *  Copyright (c) 1999 - 2002 by Matthias Pfisterer
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

package org.tritonus.debug;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.tritonus.share.TDebug;


/**
 * Debugging output aspect.
 */
@Aspect /* privileged aspect */
abstract class AJDebugVorbis extends Utils {

    @Pointcut("handler(Throwable+)")
    public void allExceptions() {
    }

    @Pointcut("execution(org.tritonus.sampled.convert.jorbis.JorbisFormatConversionProvider.new(..)) ||" +
            "execution(* org.tritonus.sampled.convert.jorbis.JorbisFormatConversionProvider.*(..)) ||" +
            "execution(org.tritonus.sampled.convert.jorbis.JorbisFormatConversionProvider.DecodedJorbisAudioInputStream.new(..)) ||" +
            "execution(* org.tritonus.sampled.convert.jorbis.JorbisFormatConversionProvider.DecodedJorbisAudioInputStream.*(..))")
    public void AudioConverterCalls() {
    }

//    @Pointcut("call(* SourceDataLine+.*(..))")
//    public void sourceDataLine() {}

    // currently not used

//    @Pointcut("execution(* JavaSoundToneGenerator.playTone(..)) && call(JavaSoundToneGenerator.ToneThread.new(..))")
//    public void printVelocity() {}

//    @Pointcut("execution(protected void JavaSoundAudioPlayer.doRealize() throws Exception)")
//    public void tracedCall() {}

    // ACTIONS

    @Before("AudioConverterCalls()")
    public void beforeAudioConverterCalls(JoinPoint thisJoinPoint) {
        if (TDebug.TraceAudioConverter) {
            outEnteringJoinPoint(thisJoinPoint);
        }
    }

    @After("AudioConverterCalls()")
    public void afterAudioConverterCalls(JoinPoint thisJoinPoint) {
        if (TDebug.TraceAudioConverter) {
            outLeavingJoinPoint(thisJoinPoint);
        }
    }

    @Before("allExceptions() && args(t)")
    public void beforeThrowable(Throwable t) {
        if (TDebug.TraceAllExceptions) {
            TDebug.out(t);
        }
    }
}



