/*
 * SourceDataLineOutputStream.java
 *
 * Helper class for saint.
 */

import java.io.IOException;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.SourceDataLine;

import static java.lang.System.getLogger;


public class SourceDataLineOutputStream extends OutputStream {

    private static final Logger logger = getLogger(SourceDataLineOutputStream.class.getName());

    private final SourceDataLine line;

    public SourceDataLineOutputStream(SourceDataLine line) {
        this.line = line;
    }

    @Override
    public void write(int _byte) {
        logger.log(Level.TRACE, "called");
        byte[] oneByte = new byte[1];
        oneByte[0] = (byte) _byte;
        line.write(oneByte, 0, 1);
    }

    @Override
    public void write(byte[] buffer, int offset, int length) throws IOException {
        logger.log(Level.TRACE, "called");
        int written = line.write(buffer, offset, length);
        logger.log(Level.TRACE, "written: " + written);
    }

    @Override
    public void flush() {
        logger.log(Level.TRACE, "called");
//        line.drain();
    }
}
