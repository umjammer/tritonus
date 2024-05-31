/*
 *  Copyright (c) 2003 by Matthias Pfisterer
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

package org.tritonus.test.sequencer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

import org.junit.jupiter.api.Assertions;


/**
 * Tests for class javax.sound.midi.MidiMessage.
 */
public class IllegalStateTestCase extends BaseSequencerTestCase {

    @Override
    protected void checkSequencer(Sequencer seq) throws Exception {
        // Sequencer is closed
        checkOpenRequired(seq, false);
        checkOpenNotRequired(seq, false);

        // sequencer open
        seq.open();

        checkOpenRequired(seq, true);
        checkOpenNotRequired(seq, true);

        // clean up
        seq.close();
    }

    private static void checkOpenRequired(Sequencer seq, boolean open) throws Exception {
        boolean expectingException = !open;
        checkMethod(seq, "start()", expectingException, open);
        checkMethod(seq, "stop()", expectingException, open);
        checkMethod(seq, "startRecording()", expectingException, open);
        checkMethod(seq, "stopRecording()", expectingException, open);
    }

    private static void checkOpenNotRequired(Sequencer seq, boolean open) throws Exception {
        boolean expectingException = false;
        checkMethod(seq, "setSequence(Sequence)", expectingException, open);
        checkMethod(seq, "setSequence(InputStream)", expectingException, open);
        checkMethod(seq, "getSequence()", expectingException, open);
        checkMethod(seq, "isRunning()", expectingException, open);
        checkMethod(seq, "isRecording()", expectingException, open);
        checkMethod(seq, "recordEnable()", expectingException, open);
        checkMethod(seq, "recordDisable()", expectingException, open);
        checkMethod(seq, "getTempoInBPM()", expectingException, open);
        checkMethod(seq, "setTempoInBPM()", expectingException, open);
        checkMethod(seq, "getTempoInMPQ()", expectingException, open);
        checkMethod(seq, "setTempoInMPQ()", expectingException, open);
        checkMethod(seq, "setTempoFactor()", expectingException, open);
        checkMethod(seq, "getTempoFactor()", expectingException, open);
        checkMethod(seq, "getTickLength()", expectingException, open);
        checkMethod(seq, "getTickPosition()", expectingException, open);
        checkMethod(seq, "setTickPosition()", expectingException, open);
        checkMethod(seq, "getMicrosecondLength()", expectingException, open);
        checkMethod(seq, "getMicrosecondPosition()", expectingException, open);
        checkMethod(seq, "setMicrosecondPosition()", expectingException, open);
        checkMethod(seq, "setMasterSyncMode()", expectingException, open);
        checkMethod(seq, "getMasterSyncMode()", expectingException, open);
        checkMethod(seq, "getMasterSyncModes()", expectingException, open);
        checkMethod(seq, "setSlaveSyncMode()", expectingException, open);
        checkMethod(seq, "getSlaveSyncMode()", expectingException, open);
        checkMethod(seq, "getSlaveSyncModes()", expectingException, open);
        checkMethod(seq, "setTrackMute()", expectingException, open);
        checkMethod(seq, "getTrackMute()", expectingException, open);
        checkMethod(seq, "setTrackSolo()", expectingException, open);
        checkMethod(seq, "getTrackSolo()", expectingException, open);
        checkMethod(seq, "addMetaEventListener()", expectingException, open);
        checkMethod(seq, "removeMetaEventListener()", expectingException, open);
        checkMethod(seq, "addControllerEventListener()", expectingException, open);
        checkMethod(seq, "removeControllerEventListener()", expectingException, open);
    }

    private static void checkMethod(Sequencer seq, String methodName,
                                    boolean exceptionExpected, boolean open) throws Exception {
        try {
            if ("start()".equals(methodName))
                seq.start();
            else if ("stop()".equals(methodName))
                seq.stop();
            else if ("startRecording()".equals(methodName))
                seq.startRecording();
            else if ("stopRecording()".equals(methodName))
                seq.stopRecording();
            else if ("setSequence(Sequence)".equals(methodName))
                seq.setSequence(createSequence());
            else if ("setSequence(InputStream)".equals(methodName))
                seq.setSequence(createSequenceInputStream());
            else if ("getSequence()".equals(methodName))
                seq.getSequence();
            else if ("isRunning()".equals(methodName))
                seq.isRunning();
            else if ("isRecording()".equals(methodName))
                seq.isRecording();
            else if ("recordEnable()".equals(methodName))
                seq.recordEnable(seq.getSequence().getTracks()[0], -1);
            else if ("recordDisable()".equals(methodName))
                seq.recordDisable(seq.getSequence().getTracks()[0]);
            else if ("getTempoInBPM()".equals(methodName))
                seq.getTempoInBPM();
            else if ("setTempoInBPM()".equals(methodName))
                seq.setTempoInBPM(122);
            else if ("getTempoInMPQ()".equals(methodName))
                seq.getTempoInMPQ();
            else if ("setTempoInMPQ()".equals(methodName))
                seq.setTempoInMPQ(300000);
            else if ("setTempoFactor()".equals(methodName))
                seq.setTempoFactor(2.0F);
            else if ("getTempoFactor()".equals(methodName))
                seq.getTempoFactor();
            else if ("getTickLength()".equals(methodName))
                seq.getTickLength();
            else if ("getTickPosition()".equals(methodName))
                seq.getTickPosition();
            else if ("setTickPosition()".equals(methodName))
                seq.setTickPosition(1);
            else if ("getMicrosecondLength()".equals(methodName))
                seq.getMicrosecondLength();
            else if ("getMicrosecondPosition()".equals(methodName))
                seq.getMicrosecondPosition();
            else if ("setMicrosecondPosition()".equals(methodName))
                seq.setMicrosecondPosition(1);
            else if ("setMasterSyncMode()".equals(methodName))
                seq.setMasterSyncMode(Sequencer.SyncMode.INTERNAL_CLOCK);
            else if ("getMasterSyncMode()".equals(methodName))
                seq.getMasterSyncMode();
            else if ("getMasterSyncModes()".equals(methodName))
                seq.getMasterSyncModes();
            else if ("setSlaveSyncMode()".equals(methodName))
                seq.setSlaveSyncMode(Sequencer.SyncMode.NO_SYNC);
            else if ("getSlaveSyncMode()".equals(methodName))
                seq.getSlaveSyncMode();
            else if ("getSlaveSyncModes()".equals(methodName))
                seq.getSlaveSyncModes();
            else if ("setTrackMute()".equals(methodName))
                seq.setTrackMute(0, true);
            else if ("getTrackMute()".equals(methodName))
                seq.getTrackMute(0);
            else if ("setTrackSolo()".equals(methodName))
                seq.setTrackSolo(0, true);
            else if ("getTrackSolo()".equals(methodName))
                seq.getTrackSolo(0);
            else if ("addMetaEventListener()".equals(methodName))
                seq.addMetaEventListener(new DummyMetaEventListener());
            else if ("removeMetaEventListener()".equals(methodName))
                seq.removeMetaEventListener(new DummyMetaEventListener());
            else if ("addControllerEventListener()".equals(methodName))
                seq.addControllerEventListener(new DummyControllerEventListener(), new int[] {0});
            else if ("removeControllerEventListener()".equals(methodName))
                seq.removeControllerEventListener(new DummyControllerEventListener(), new int[] {0});
            else
                throw new RuntimeException("unknown method name");
            if (exceptionExpected) {
                Assertions.fail(constructErrorMessage(seq, methodName, exceptionExpected, open));
            }
        } catch (IllegalStateException e) {
            if (!exceptionExpected) {
                Assertions.fail(constructErrorMessage(seq, methodName, exceptionExpected, open));
            }
        }
    }

    private static Sequence createSequence() throws Exception {
        Sequence sequence = new Sequence(Sequence.PPQ, 480);
        sequence.createTrack();
        return sequence;
    }

    private static InputStream createSequenceInputStream() throws Exception {
        Sequence sequence = createSequence();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MidiSystem.write(sequence, 0, baos);
        byte[] data = baos.toByteArray();
        InputStream inputStream = new ByteArrayInputStream(data);
        return inputStream;
    }

    private static String constructErrorMessage(
            Sequencer seq, String methodName, boolean exceptionExpected, boolean open) {
        String message = getMessagePrefix(seq) + ": IllegalStateException ";
        message += (exceptionExpected ? "not thrown" : "thrown");
        message += " on " + methodName + " in ";
        message += (open ? "open" : "closed");
        message += " state";
        return message;
    }

    private static class DummyMetaEventListener implements MetaEventListener {

        @Override
        public void meta(MetaMessage meta) {
            // DO NOTHING
        }
    }

    private static class DummyControllerEventListener implements ControllerEventListener {

        @Override
        public void controlChange(ShortMessage event) {
            // DO NOTHING
        }
    }
}
