/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.midi.timidity;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.spi.MidiDeviceProvider;

import org.tritonus.share.GlobalInfo;
import org.tritonus.share.midi.TMidiDevice;

import static java.lang.System.getLogger;


/**
 * TiMiditySynthesizerProvider.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-11-26 nsano initial version <br>
 */
public class TiMiditySynthesizerProvider extends MidiDeviceProvider {

    private static final Logger logger = getLogger(TiMiditySynthesizerProvider.class.getName());

    private static final MidiDevice.Info info = new TMidiDevice.Info(
            "Tritonus TiMidity++ Synthesizer",
            GlobalInfo.getVendor(),
            "a synthesizer based on TiMidity++ (timidity41)",
            GlobalInfo.getVersion());

    /** the native library is available or not */
    private static final boolean available;

    static {
        boolean ok;
        try {
            logger.log(Level.DEBUG, "timidity: " + TiMidity.version());
            ok = true;
        } catch (Throwable t) {
            logger.log(Level.DEBUG, "timidity native library is not available: " + t);
            ok = false;
        }
        available = ok;
    }

    @Override
    public MidiDevice.Info[] getDeviceInfo() {
        return available ? new MidiDevice.Info[] {info} : new MidiDevice.Info[0];
    }

    @Override
    public MidiDevice getDevice(MidiDevice.Info info) {
        if (available && TiMiditySynthesizerProvider.info.equals(info)) {
            return new TiMiditySynthesizer(info);
        }
        throw new IllegalArgumentException("no device for " + info);
    }
}
