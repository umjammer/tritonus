/*
 *  Copyright (c) 1999 - 2004 by Matthias Pfisterer
 *
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
 *
 */

package org.tritonus.share.sampled.mixer;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import org.tritonus.share.ArraySet;
import org.tritonus.share.sampled.AudioFormats;

import static java.lang.System.getLogger;


// TODO global controls (that use the system mixer)
public abstract class TMixer extends TLine implements Mixer {

    private static final Logger logger= getLogger("org.tritonus.TraceMixer");

    private static Line.Info[] EMPTY_LINE_INFO_ARRAY = new Line.Info[0];
    private static Line[] EMPTY_LINE_ARRAY = new Line[0];

    private final Mixer.Info mixerInfo;
    private Collection<AudioFormat> supportedSourceFormats;
    private Collection<AudioFormat> supportedTargetFormats;
    private Collection<Line.Info> supportedSourceLineInfos;
    private Collection<Line.Info> supportedTargetLineInfos;
    private final Set<SourceDataLine> openSourceDataLines;
    private final Set<TargetDataLine> openTargetDataLines;

    /**
     * Constructor for mixers that use setSupportInformation().
     */
    protected TMixer(Mixer.Info mixerInfo, Line.Info lineInfo) {
        this(mixerInfo, lineInfo, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Constructor for mixers.
     */
    protected TMixer(Mixer.Info mixerInfo,
                     Line.Info lineInfo,
                     Collection<AudioFormat> supportedSourceFormats,
                     Collection<AudioFormat> supportedTargetFormats,
                     Collection<Line.Info> supportedSourceLineInfos,
                     Collection<Line.Info> supportedTargetLineInfos) {
        super(null, lineInfo);

        logger.log(Level.TRACE, "begin");

        this.mixerInfo = mixerInfo;
        setSupportInformation(
                supportedSourceFormats,
                supportedTargetFormats,
                supportedSourceLineInfos,
                supportedTargetLineInfos);
        openSourceDataLines = new ArraySet<>();
        openTargetDataLines = new ArraySet<>();

        logger.log(Level.TRACE, "end");
    }

    protected void setSupportInformation(
            Collection<AudioFormat> supportedSourceFormats,
            Collection<AudioFormat> supportedTargetFormats,
            Collection<Line.Info> supportedSourceLineInfos,
            Collection<Line.Info> supportedTargetLineInfos) {

        logger.log(Level.TRACE, "begin");

        this.supportedSourceFormats = supportedSourceFormats;
        this.supportedTargetFormats = supportedTargetFormats;
        this.supportedSourceLineInfos = supportedSourceLineInfos;
        this.supportedTargetLineInfos = supportedTargetLineInfos;

        logger.log(Level.TRACE, "end");
    }

    @Override
    public Mixer.Info getMixerInfo() {
        logger.log(Level.TRACE, "begin");

        logger.log(Level.TRACE, "end");

        return mixerInfo;
    }

    @Override
    public Line.Info[] getSourceLineInfo() {
        logger.log(Level.TRACE, "begin");

        Line.Info[] infos = supportedSourceLineInfos.toArray(EMPTY_LINE_INFO_ARRAY);

        logger.log(Level.TRACE, "end");

        return infos;
    }

    @Override
    public Line.Info[] getTargetLineInfo() {
        logger.log(Level.TRACE, "begin");

        Line.Info[] infos = supportedTargetLineInfos.toArray(EMPTY_LINE_INFO_ARRAY);

        logger.log(Level.TRACE, "end");

        return infos;
    }

    @Override
    public Line.Info[] getSourceLineInfo(Line.Info info) {
        logger.log(Level.TRACE, "info to test: " + info);

        // TODO
        return EMPTY_LINE_INFO_ARRAY;
    }

    @Override
    public Line.Info[] getTargetLineInfo(Line.Info info) {
        logger.log(Level.TRACE, "info to test: " + info);

        // TODO
        return EMPTY_LINE_INFO_ARRAY;
    }

    @Override
    public boolean isLineSupported(Line.Info info) {
        logger.log(Level.TRACE, "info to test: " + info);

        Class<?> lineClass = info.getLineClass();
        if (lineClass.equals(SourceDataLine.class)) {
            return isLineSupportedImpl(info, supportedSourceLineInfos);
        } else if (lineClass.equals(TargetDataLine.class)) {
            return isLineSupportedImpl(info, supportedTargetLineInfos);
        } else if (lineClass.equals(Port.class)) {
            return isLineSupportedImpl(info, supportedSourceLineInfos) || isLineSupportedImpl(info, supportedTargetLineInfos);
        } else {
            return false;
        }
    }

    private static boolean isLineSupportedImpl(Line.Info info, Collection<Line.Info> supportedLineInfos) {
        for (Line.Info info2 : supportedLineInfos) {
            if (info2.matches(info)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Line getLine(Line.Info info) throws LineUnavailableException {
        logger.log(Level.TRACE, "begin");

        Class<?> lineClass = info.getLineClass();
        DataLine.Info dataLineInfo = null;
        Port.Info portInfo = null;
        AudioFormat[] formats = null;
        if (info instanceof DataLine.Info) {
            dataLineInfo = (DataLine.Info) info;
            formats = dataLineInfo.getFormats();
        } else if (info instanceof Port.Info) {
            portInfo = (Port.Info) info;
        }
        AudioFormat format;
        Line line;
        if (lineClass == SourceDataLine.class) {
            logger.log(Level.TRACE, "type: SourceDataLine");

            if (dataLineInfo == null) {
                throw new IllegalArgumentException("need DataLine.Info for SourceDataLine");
            }
            format = getSupportedSourceFormat(formats);
            line = getSourceDataLine(format, dataLineInfo.getMaxBufferSize());
        } else if (lineClass == Clip.class) {
            logger.log(Level.TRACE, "type: Clip");

            if (dataLineInfo == null) {
                throw new IllegalArgumentException("need DataLine.Info for Clip");
            }
            format = getSupportedSourceFormat(formats);
            line = getClip(format);
        } else if (lineClass == TargetDataLine.class) {
            logger.log(Level.TRACE, "type: TargetDataLine");

            if (dataLineInfo == null) {
                throw new IllegalArgumentException("need DataLine.Info for TargetDataLine");
            }
            format = getSupportedTargetFormat(formats);
            line = getTargetDataLine(format, dataLineInfo.getMaxBufferSize());
        } else if (lineClass == Port.class) {
            logger.log(Level.TRACE, "type: TargetDataLine");

            if (portInfo == null) {
                throw new IllegalArgumentException("need Port.Info for Port");
            }
            line = getPort(portInfo);
        } else {
            logger.log(Level.TRACE, "unknown line type, will throw exception");

            throw new LineUnavailableException("unknown line class: " + lineClass);
        }

        logger.log(Level.TRACE, "end");

        return line;
    }

    protected SourceDataLine getSourceDataLine(AudioFormat format, int bufferSize) throws LineUnavailableException {
        logger.log(Level.TRACE, "begin");

        throw new IllegalArgumentException("this mixer does not support SourceDataLines");
    }

    protected Clip getClip(AudioFormat format) throws LineUnavailableException {
        logger.log(Level.TRACE, "begin");

        throw new IllegalArgumentException("this mixer does not support Clips");
    }

    protected TargetDataLine getTargetDataLine(AudioFormat format, int bufferSize) throws LineUnavailableException {
        logger.log(Level.TRACE, "begin");

        throw new IllegalArgumentException("this mixer does not support TargetDataLines");
    }

    protected Port getPort(Port.Info info) throws LineUnavailableException {
        logger.log(Level.TRACE, "begin");

        throw new IllegalArgumentException("this mixer does not support Ports");
    }

    private AudioFormat getSupportedSourceFormat(AudioFormat[] formats) {
        logger.log(Level.TRACE, "begin");

        AudioFormat format = null;
        for (AudioFormat aFormat : formats) {
            logger.log(Level.TRACE, "checking " + aFormat + "...");

            if (isSourceFormatSupported(aFormat)) {
                logger.log(Level.TRACE, "...supported");

                format = aFormat;
                break;
            } else {
                logger.log(Level.TRACE, "...no luck");
            }
        }
        if (format == null) {
            throw new IllegalArgumentException("no line matchine one of the passed formats");
        }

        logger.log(Level.TRACE, "end");

        return format;
    }

    private AudioFormat getSupportedTargetFormat(AudioFormat[] formats) {
        logger.log(Level.TRACE, "begin");

        AudioFormat format = null;
        for (AudioFormat aFormat : formats) {
            logger.log(Level.TRACE, "checking " + aFormat + " ...");

            if (isTargetFormatSupported(aFormat)) {
                logger.log(Level.TRACE, "...supported");

                format = aFormat;
                break;
            } else {
                logger.log(Level.TRACE, "...no luck");
            }
        }
        if (format == null) {
            throw new IllegalArgumentException("no line matchine one of the passed formats");
        }

        logger.log(Level.TRACE, "end");

        return format;
    }

    //  not implemented here:
    //  getMaxLines(Line.Info)

    @Override
    public Line[] getSourceLines() {
        logger.log(Level.TRACE, "called");

        return openSourceDataLines.toArray(EMPTY_LINE_ARRAY);
    }

    @Override
    public Line[] getTargetLines() {
        logger.log(Level.TRACE, "called");

        return openTargetDataLines.toArray(EMPTY_LINE_ARRAY);
    }

    @Override
    public void synchronize(Line[] lines, boolean maintainSync) {
        throw new UnsupportedOperationException("synchronization not supported");
    }

    @Override
    public void unsynchronize(Line[] lines) {
        throw new UnsupportedOperationException("synchronization not supported");
    }

    @Override
    public boolean isSynchronizationSupported(Line[] lines, boolean maintainSync) {
        return false;
    }

    protected boolean isSourceFormatSupported(AudioFormat format) {
        logger.log(Level.TRACE, "format to test: " + format);

        for (AudioFormat supportedFormat : supportedSourceFormats) {
            if (AudioFormats.matches(supportedFormat, format)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isTargetFormatSupported(AudioFormat format) {
        logger.log(Level.TRACE, "format to test: " + format);

        for (AudioFormat supportedFormat : supportedTargetFormats) {
            if (AudioFormats.matches(supportedFormat, format)) {
                return true;
            }
        }
        return false;
    }

    /* package */ void registerOpenLine(Line line) {
        logger.log(Level.TRACE, "line to register: " + line);

        if (line instanceof SourceDataLine) {
            synchronized (openSourceDataLines) {
                openSourceDataLines.add((SourceDataLine) line);
            }
        } else if (line instanceof TargetDataLine) {
            synchronized (openSourceDataLines) {
                openTargetDataLines.add((TargetDataLine) line);
            }
        }
    }

    /* package */ void unregisterOpenLine(Line line) {
        logger.log(Level.TRACE, "line to unregister: " + line);

        if (line instanceof SourceDataLine) {
            synchronized (openSourceDataLines) {
                openSourceDataLines.remove(line);
            }
        } else if (line instanceof TargetDataLine) {
            synchronized (openTargetDataLines) {
                openTargetDataLines.remove(line);
            }
        }
    }
}
