import org.tritonus.lowlevel.alsa.AlsaMixer;
import org.tritonus.lowlevel.alsa.AlsaMixerElement;


public class AlsaMixerTest {

    private static boolean showInactiveElements;

    public static void main(String[] args) throws Exception {
        String mixerName = "hw:0";
        if (args.length > 0) {
            mixerName = args[0];
        }
        out("Mixer: " + mixerName);
        AlsaMixer mixer = new AlsaMixer(mixerName);
        int[] indices = new int[200];
        String[] names = new String[200];
        int ret = mixer.readControlList(indices, names);
        out("readControlList() returns: " + ret);
        if (ret > 0) {
            out("Mixer controls:");
            for (int i = 0; i < ret; i++) {
                out(i + " " + indices[i] + " " + names[i]);
                AlsaMixerElement element = new AlsaMixerElement(mixer, indices[i], names[i]);
                if (element.isActive() || showInactiveElements) {
                    out("--------------------------------------------------------------------------------");
                    output(element);
                }
            }
            out("--------------------------------------------------------------------------------");
        }
        mixer.close();
    }

    private static void output(AlsaMixerElement element) {
        out("  name: " + element.getName());
        out("  index: " + element.getName());
        out("  active: " + element.isActive());

        if (hasPlaybackChannels(element)) {
            outputPlayback(element);
        } else {
            out("* no playback channels");
        }
        if (hasCaptureChannels(element)) {
            outputCapture(element);
        } else {
            out("* no capture channels");
        }
    }

    private static void outputPlayback(AlsaMixerElement element) {
        out("  playback mono: " + element.isPlaybackMono());
        for (int channel = AlsaMixerElement.SND_MIXER_SCHN_FRONT_LEFT;
             channel <= AlsaMixerElement.SND_MIXER_SCHN_WOOFER;
             channel++) {
            out("  playback channel (" + AlsaMixerElement.getChannelName(channel) + "): " + element.hasPlaybackChannel(channel));
        }
        out("  common volume: " + element.hasCommonVolume());
        out("  playback volume: " + element.hasPlaybackVolume());
        out("  playback volume joined: " + element.hasPlaybackVolumeJoined());
        out("  common switch: " + element.hasCommonSwitch());
        out("  playback switch: " + element.hasPlaybackSwitch());
        out("  playback switch joined: " + element.hasPlaybackSwitchJoined());
    }

    private static void outputCapture(AlsaMixerElement element) {
        out("  capture mono: " + element.isCaptureMono());
        for (int channel = AlsaMixerElement.SND_MIXER_SCHN_FRONT_LEFT;
             channel <= AlsaMixerElement.SND_MIXER_SCHN_WOOFER;
             channel++) {
            out("  capture channel (" + AlsaMixerElement.getChannelName(channel) + "): " + element.hasCaptureChannel(channel));
        }
        out("  common volume: " + element.hasCommonVolume());
        out("  capture volume: " + element.hasCaptureVolume());
        out("  capture volume joined: " + element.hasCaptureVolumeJoined());
        out("  common switch: " + element.hasCommonSwitch());
        out("  capture switch: " + element.hasCaptureSwitch());
        out("  capture switch joined: " + element.hasCaptureSwitchJoinded());
        out("  capture switch exclusive: " + element.hasCaptureSwitchExclusive());
        if (element.hasCaptureSwitchExclusive()) {
            out("  capture group: " + element.getCaptureGroup());
        }
    }

    private static boolean hasPlaybackChannels(AlsaMixerElement element) {
        boolean hasChannels = false;
        for (int channel = AlsaMixerElement.SND_MIXER_SCHN_FRONT_LEFT;
             channel <= AlsaMixerElement.SND_MIXER_SCHN_WOOFER;
             channel++) {
            hasChannels |= element.hasPlaybackChannel(channel);
        }
        return hasChannels;
    }

    private static boolean hasCaptureChannels(AlsaMixerElement element) {
        boolean hasChannels = false;
        for (int channel = AlsaMixerElement.SND_MIXER_SCHN_FRONT_LEFT;
             channel <= AlsaMixerElement.SND_MIXER_SCHN_WOOFER;
             channel++) {
            hasChannels |= element.hasCaptureChannel(channel);
        }
        return hasChannels;
    }

    private static void out(String message) {
        System.out.println(message);
    }
}
