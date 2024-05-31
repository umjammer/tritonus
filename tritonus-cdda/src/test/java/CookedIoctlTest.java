import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.SourceDataLine;

import org.tritonus.lowlevel.cdda.cooked_ioctl.CookedIoctl;


/**
 * CookedIoctlTest.
 */
public class CookedIoctlTest {

    public static void main(String[] args) throws Exception {
        String device = "/dev/cdrom";
        boolean tocOnly = true;
        int _track = 0;
        if (args.length < 1) {
            tocOnly = true;
        } else if (args.length == 1) {
            _track = Integer.parseInt(args[0]);
            tocOnly = false;
        }
        CookedIoctl cookedIoctl = new CookedIoctl(device);
        int[] values = new int[2];
        int[] startFrame = new int[100];
        int[] length = new int[100];
        int[] type = new int[100];
        boolean[] copy = new boolean[100];
        boolean[] pre = new boolean[100];
        int[] channels = new int[100];
        cookedIoctl.readTOC(values, startFrame, length, type, copy, pre, channels);
        System.out.println("First track: " + values[0]);
        System.out.println("last track: " + values[1]);
        int tracks = values[1] - values[0] + 1;
        for (int i = 0; i < tracks; i++) {
            System.out.println("Track " + (i + values[0]) + " start frame: " + startFrame[i]);
            System.out.println("Track " + (i + values[0]) + " length: " + length[i]);
            System.out.println("Track " + (i + values[0]) + " type: " + type[i]);
            System.out.println("Track " + (i + values[0]) + " copy: " + copy[i]);
            System.out.println("Track " + (i + values[0]) + " pre: " + pre[i]);
            System.out.println("Track " + (i + values[0]) + " channels: " + channels[i]);
        }
        if (!tocOnly) {
            SourceDataLine line = null;
            AudioFormat audioFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    44100.0F, 16, 2, 4, 44100.0F, false);
            Line.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
            byte[] data = new byte[2352 * 8];
            line = (SourceDataLine) AudioSystem.getLine(info);
            line.open();
            line.start();
            int start = startFrame[_track - values[0]];
            int end = start + length[_track - values[0]];
            for (int i = start; i < end; i++) {
                cookedIoctl.readFrame(i, 1, data);
                line.write(data, 0, 2352);
            }
        }
        cookedIoctl.close();
    }
}
