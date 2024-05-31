/*
 *  Copyright (c) 2002 by Matthias Pfisterer
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

package org.tritonus.saol.engine;


import java.io.IOException;

import org.tritonus.share.sampled.TConversionTool;
import org.tritonus.share.sampled.file.AudioOutputStream;


public class AudioOutputStreamOutput extends Bus implements SystemOutput {

    private final AudioOutputStream audioOutputStream;
    private final byte[] buffer;

    public AudioOutputStreamOutput(AudioOutputStream audioOutputStream) {
        super(audioOutputStream.getFormat().getChannels());
        this.audioOutputStream = audioOutputStream;
        buffer = new byte[audioOutputStream.getFormat().getFrameSize()];
    }

    @Override
    public void emit() throws IOException {
        float[] values = getValues();
        boolean bigEndian = audioOutputStream.getFormat().isBigEndian();
        int offset = 0;
        for (float value : values) {
            float _output = Math.max(Math.min(value, 1.0F), -1.0F);
            // assumes 16 bit linear
            int output = (int) (_output * 32767.0F);
            TConversionTool.shortToBytes16((short) output, buffer, offset, bigEndian);
            offset += 2;
        }
        audioOutputStream.write(buffer, 0, buffer.length);
    }

    @Override
    public void close() throws IOException {
        audioOutputStream.close();
    }
}
