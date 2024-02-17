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
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.tritonus.share.TDebug;


/**
 * Debugging output aspect.
 */
@Aspect
public abstract class AJDebug extends Utils {

    @Pointcut("handler(Throwable+)")
    public void allExceptions() {
    }

    // TAudioConfig, TMidiConfig, TInit

    @Pointcut("execution(* org.tritonus.core.TMidiConfig.*(..))")
    public void TMidiConfigCalls() {
    }

    @Pointcut("execution(* org.tritonus.core.TInit.*(..))")
    public void TInitCalls() {
    }

    // share

    // midi

    @Pointcut("execution(* javax.sound.midi.MidiSystem .*(..))")
    public void MidiSystemCalls() {
    }

    @Pointcut("execution(org.tritonus.share.midi.TSequencer+.new(..)) ||" +
            "execution(* org.tritonus.share.midi.TSequencer+.*(..)) ||" +
            "execution(* org.tritonus.midi.device.alsa.AlsaSequencer.PlaybackAlsaMidiInListener.*(..)) ||" +
            "execution(* org.tritonus.midi.device.alsa.AlsaSequencer.RecordingAlsaMidiInListener.*(..)) ||" +
            "execution(* org.tritonus.midi.device.alsa.AlsaSequencer.AlsaSequencerReceiver.*(..)) ||" +
            "execution(* org.tritonus.midi.device.alsa.AlsaSequencer.AlsaSequencerTransmitter.*(..)) ||" +
            "execution(org.tritonus.midi.device.alsa.AlsaSequencer.LoaderThread.new(..)) ||" +
            "execution(* org.tritonus.midi.device.alsa.AlsaSequencer.LoaderThread.*(..)) ||" +
            "execution(org.tritonus.midi.device.alsa.AlsaSequencer.MasterSynchronizer.new(..)) ||" +
            "execution(* org.tritonus.midi.device.alsa.AlsaSequencer.MasterSynchronizer.*(..))")
    public void Sequencer() {
    }

    // audio

    @Pointcut("execution(* javax.sound.sampled.AudioSystem.*(..))")
    public void AudioSystemCalls() {
    }

    @Pointcut("call(* javax.sound.sampled.SourceDataLine+.*(..))")
    public void sourceDataLine() {
    }

    // OLD

// 	  @Pointcut("execution(private void TPlayer.setState(int))")
//    public abstract void playerStates();

    // currently not used
//    @Pointcut("execution(* JavaSoundToneGenerator.playTone(..)) && call(JavaSoundToneGenerator.ToneThread.new(..))")
//    public void printVelocity() {}

//    @Pointcut("execution(protected void JavaSoundAudioPlayer.doRealize() throws Exception)")
//    public abstract void tracedCall();

    // ACTIONS

    @Before("MidiSystemCalls()")
    public void beforeMidiSystemCalls(JoinPoint thisJoinPoint) {
        if (TDebug.TraceMidiSystem) outEnteringJoinPoint(thisJoinPoint);
    }

    @After("MidiSystemCalls()")
    public void afterMidiSystemCalls(JoinPoint thisJoinPoint) {
        if (TDebug.TraceSequencer) outLeavingJoinPoint(thisJoinPoint);
    }

    @Before("Sequencer()")
    public void beforeSequencer(JoinPoint thisJoinPoint) {
        if (TDebug.TraceSequencer) outEnteringJoinPoint(thisJoinPoint);
    }

    @After("Sequencer()")
    public void afterSequencer(JoinPoint thisJoinPoint) {
        if (TDebug.TraceSequencer) outLeavingJoinPoint(thisJoinPoint);
    }

    @Before("TInitCalls()")
    public void beforeTInitCalls(JoinPoint thisJoinPoint) {
        if (TDebug.TraceInit) outEnteringJoinPoint(thisJoinPoint);
    }

    @After("TInitCalls()")
    public void afterTInitCalls(JoinPoint thisJoinPoint) {
        if (TDebug.TraceInit) outLeavingJoinPoint(thisJoinPoint);
    }

    @Before("TMidiConfigCalls()")
    public void beforeTMidiConfigCalls(JoinPoint thisJoinPoint) {
        if (TDebug.TraceMidiConfig) outEnteringJoinPoint(thisJoinPoint);
    }

    @After("TMidiConfigCalls()")
    public void afterTMidiConfigCalls(JoinPoint thisJoinPoint) {
        if (TDebug.TraceMidiConfig) outLeavingJoinPoint(thisJoinPoint);
    }

    // execution(* TAsynchronousFilteredAudioInputStream.read(..))

    @Before("execution(* org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream.read())")
    public void beforeTAsynchronousFilteredAudioInputStream_Read(JoinPoint thisJoinPoint) {
        if (TDebug.TraceAudioConverter) outEnteringJoinPoint(thisJoinPoint);
    }

    @After("execution(* org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream.read())")
    public void afterTAsynchronousFilteredAudioInputStream_Read(JoinPoint thisJoinPoint) {
        if (TDebug.TraceAudioConverter) outLeavingJoinPoint(thisJoinPoint);
    }

    @Before("execution(* org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream.read(byte[]))")
    public void beforeTAsynchronousFilteredAudioInputStream_read_XB(JoinPoint thisJoinPoint) {
        if (TDebug.TraceAudioConverter) outEnteringJoinPoint(thisJoinPoint);
    }

    @After("execution(* org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream.read(byte[]))")
    public void afterTAsynchronousFilteredAudioInputStream_read2_XB(JoinPoint thisJoinPoint) {
        if (TDebug.TraceAudioConverter) outLeavingJoinPoint(thisJoinPoint);
    }

    @Before("execution(* org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream.read(byte[],int,int))")
    public void beforeTAsynchronousFilteredAudioInputStream_read_XBII(JoinPoint thisJoinPoint) {
        if (TDebug.TraceAudioConverter) outEnteringJoinPoint(thisJoinPoint);
    }

    @After("execution(* org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream.read(byte[],int,int))")
    public void afterTAsynchronousFilteredAudioInputStream_read_XBII(JoinPoint thisJoinPoint) {
        if (TDebug.TraceAudioConverter) outLeavingJoinPoint(thisJoinPoint);
    }

    @AfterReturning(pointcut = "call(* org.tritonus.share.sampled.convert.TAsynchronousFilteredAudioInputStream.read(byte[],int,int))", returning = "nBytes")
    public void afterReturningTAsynchronousFilteredAudioInputStream_read_XBII(int nBytes) {
        if (TDebug.TraceAudioConverter) TDebug.out("returning bytes: " + nBytes);
    }

//    @Before("playerStates() && args(nState)")
//    public void beforeX(int nState) {
//        if (TDebug.TracePlayerStates) {
//            TDebug.out("TPlayer.setState(): " + nState);
//        }
//    }

//    @Before("playerStateTransitions()")
//    public void beforePlayerStateTransitions() {
//        if (TDebug.TracePlayerStateTransitions) {
//            TDebug.out("Entering: " + thisJoinPoint);
//        }
//    }

//    @Around("call(*MidiSystem.getSynthesizer())")
//    public void aroundSynthesizer() {
//        Synthesizer s = proceed();
//        if (TDebug.TraceToneGenerator) {
//            TDebug.out("MidiSystem.getSynthesizer() gives:  " + s);
//        }
//        return s;
//        // only to get no compilation errors
//        return null;
//    }

    // TODO: v gives an error; find out what to do
// 	@Before("printVelocity() && args(nVelocity)")
//    public void beforePrintVelocity(int v) {
//        if (TDebug.TraceToneGenerator) {
//            TDebug.out("velocity: " + v);
//        }
//    }

    @AfterThrowing("allExceptions() && args(t)")
    public void beforeAllExceptions(Throwable t) {
        if (TDebug.TraceAllExceptions) TDebug.out(t);
    }
}



