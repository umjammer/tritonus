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

    private Mixer.Info m_mixerInfo;
    private Collection<AudioFormat> m_supportedSourceFormats;
    private Collection<AudioFormat> m_supportedTargetFormats;
    private Collection<Line.Info> m_supportedSourceLineInfos;
    private Collection<Line.Info> m_supportedTargetLineInfos;
    private final Set<SourceDataLine> m_openSourceDataLines;
    private final Set<TargetDataLine> m_openTargetDataLines;

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

        logger.log(Level.TRACE, "TMixer.<init>(): begin");

        m_mixerInfo = mixerInfo;
        setSupportInformation(
                supportedSourceFormats,
                supportedTargetFormats,
                supportedSourceLineInfos,
                supportedTargetLineInfos);
        m_openSourceDataLines = new ArraySet<>();
        m_openTargetDataLines = new ArraySet<>();

        logger.log(Level.TRACE, "TMixer.<init>(): end");
    }

    protected void setSupportInformation(
            Collection<AudioFormat> supportedSourceFormats,
            Collection<AudioFormat> supportedTargetFormats,
            Collection<Line.Info> supportedSourceLineInfos,
            Collection<Line.Info> supportedTargetLineInfos) {

        logger.log(Level.TRACE, "TMixer.setSupportInformation(): begin");

        m_supportedSourceFormats = supportedSourceFormats;
        m_supportedTargetFormats = supportedTargetFormats;
        m_supportedSourceLineInfos = supportedSourceLineInfos;
        m_supportedTargetLineInfos = supportedTargetLineInfos;

        logger.log(Level.TRACE, "TMixer.setSupportInformation(): end");
    }

    @Override
    public Mixer.Info getMixerInfo() {
        logger.log(Level.TRACE, "TMixer.getMixerInfo(): begin");

        logger.log(Level.TRACE, "TMixer.getMixerInfo(): end");

        return m_mixerInfo;
    }

    @Override
    public Line.Info[] getSourceLineInfo() {
        logger.log(Level.TRACE, "TMixer.getSourceLineInfo(): begin");

        Line.Info[] infos = m_supportedSourceLineInfos.toArray(EMPTY_LINE_INFO_ARRAY);

        logger.log(Level.TRACE, "TMixer.getSourceLineInfo(): end");

        return infos;
    }

    @Override
    public Line.Info[] getTargetLineInfo() {
        logger.log(Level.TRACE, "TMixer.getTargetLineInfo(): begin");

        Line.Info[] infos = m_supportedTargetLineInfos.toArray(EMPTY_LINE_INFO_ARRAY);

        logger.log(Level.TRACE, "TMixer.getTargetLineInfo(): end");

        return infos;
    }

    @Override
    public Line.Info[] getSourceLineInfo(Line.Info info) {
        logger.log(Level.TRACE, "TMixer.getSourceLineInfo(Line.Info): info to test: " + info);

        // TODO
        return EMPTY_LINE_INFO_ARRAY;
    }

    @Override
    public Line.Info[] getTargetLineInfo(Line.Info info) {
        logger.log(Level.TRACE, "TMixer.getTargetLineInfo(Line.Info): info to test: " + info);

        // TODO
        return EMPTY_LINE_INFO_ARRAY;
    }

    @Override
    public boolean isLineSupported(Line.Info info) {
        logger.log(Level.TRACE, "TMixer.isLineSupported(): info to test: " + info);

        Class<?> lineClass = info.getLineClass();
        if (lineClass.equals(SourceDataLine.class)) {
            return isLineSupportedImpl(info, m_supportedSourceLineInfos);
        } else if (lineClass.equals(TargetDataLine.class)) {
            return isLineSupportedImpl(info, m_supportedTargetLineInfos);
        } else if (lineClass.equals(Port.class)) {
            return isLineSupportedImpl(info, m_supportedSourceLineInfos) || isLineSupportedImpl(info, m_supportedTargetLineInfos);
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
        logger.log(Level.TRACE, "TMixer.getLine(): begin");

        Class<?> lineClass = info.getLineClass();
        DataLine.Info dataLineInfo = null;
        Port.Info portInfo = null;
        AudioFormat[] aFormats = null;
        if (info instanceof DataLine.Info) {
            dataLineInfo = (DataLine.Info) info;
            aFormats = dataLineInfo.getFormats();
        } else if (info instanceof Port.Info) {
            portInfo = (Port.Info) info;
        }
        AudioFormat format;
        Line line;
        if (lineClass == SourceDataLine.class) {
            logger.log(Level.TRACE, "TMixer.getLine(): type: SourceDataLine");

            if (dataLineInfo == null) {
                throw new IllegalArgumentException("need DataLine.Info for SourceDataLine");
            }
            format = getSupportedSourceFormat(aFormats);
            line = getSourceDataLine(format, dataLineInfo.getMaxBufferSize());
        } else if (lineClass == Clip.class) {
            logger.log(Level.TRACE, "TMixer.getLine(): type: Clip");

            if (dataLineInfo == null) {
                throw new IllegalArgumentException("need DataLine.Info for Clip");
            }
            format = getSupportedSourceFormat(aFormats);
            line = getClip(format);
        } else if (lineClass == TargetDataLine.class) {
            logger.log(Level.TRACE, "TMixer.getLine(): type: TargetDataLine");

            if (dataLineInfo == null) {
                throw new IllegalArgumentException("need DataLine.Info for TargetDataLine");
            }
            format = getSupportedTargetFormat(aFormats);
            line = getTargetDataLine(format, dataLineInfo.getMaxBufferSize());
        } else if (lineClass == Port.class) {
            logger.log(Level.TRACE, "TMixer.getLine(): type: TargetDataLine");

            if (portInfo == null) {
                throw new IllegalArgumentException("need Port.Info for Port");
            }
            line = getPort(portInfo);
        } else {
            logger.log(Level.TRACE, "TMixer.getLine(): unknown line type, will throw exception");

            throw new LineUnavailableException("unknown line class: " + lineClass);
        }

        logger.log(Level.TRACE, "TMixer.getLine(): end");

        return line;
    }

    protected SourceDataLine getSourceDataLine(AudioFormat format, int nBufferSize) throws LineUnavailableException {
        logger.log(Level.TRACE, "TMixer.getSourceDataLine(): begin");

        throw new IllegalArgumentException("this mixer does not support SourceDataLines");
    }

    protected Clip getClip(AudioFormat format) throws LineUnavailableException {
        logger.log(Level.TRACE, "TMixer.getClip(): begin");

        throw new IllegalArgumentException("this mixer does not support Clips");
    }

    protected TargetDataLine getTargetDataLine(AudioFormat format, int nBufferSize) throws LineUnavailableException {
        logger.log(Level.TRACE, "TMixer.getTargetDataLine(): begin");

        throw new IllegalArgumentException("this mixer does not support TargetDataLines");
    }

    protected Port getPort(Port.Info info) throws LineUnavailableException {
        logger.log(Level.TRACE, "TMixer.getTargetDataLine(): begin");

        throw new IllegalArgumentException("this mixer does not support Ports");
    }

    private AudioFormat getSupportedSourceFormat(AudioFormat[] aFormats) {
        logger.log(Level.TRACE, "TMixer.getSupportedSourceFormat(): begin");

        AudioFormat format = null;
        for (AudioFormat aFormat : aFormats) {
            logger.log(Level.TRACE, "TMixer.getSupportedSourceFormat(): checking " + aFormat + "...");

            if (isSourceFormatSupported(aFormat)) {
                logger.log(Level.TRACE, "TMixer.getSupportedSourceFormat(): ...supported");

                format = aFormat;
                break;
            } else {
                logger.log(Level.TRACE, "TMixer.getSupportedSourceFormat(): ...no luck");
            }
        }
        if (format == null) {
            throw new IllegalArgumentException("no line matchine one of the passed formats");
        }

        logger.log(Level.TRACE, "TMixer.getSupportedSourceFormat(): end");

        return format;
    }

    private AudioFormat getSupportedTargetFormat(AudioFormat[] aFormats) {
        logger.log(Level.TRACE, "TMixer.getSupportedTargetFormat(): begin");

        AudioFormat format = null;
        for (AudioFormat aFormat : aFormats) {
            logger.log(Level.TRACE, "TMixer.getSupportedTargetFormat(): checking " + aFormat + " ...");

            if (isTargetFormatSupported(aFormat)) {
                logger.log(Level.TRACE, "TMixer.getSupportedTargetFormat(): ...supported");

                format = aFormat;
                break;
            } else {
                logger.log(Level.TRACE, "TMixer.getSupportedTargetFormat(): ...no luck");
            }
        }
        if (format == null) {
            throw new IllegalArgumentException("no line matchine one of the passed formats");
        }

        logger.log(Level.TRACE, "TMixer.getSupportedTargetFormat(): end");

        return format;
    }

    //  not implemented here:
    //  getMaxLines(Line.Info)

    @Override
    public Line[] getSourceLines() {
        logger.log(Level.TRACE, "TMixer.getSourceLines(): called");

        return m_openSourceDataLines.toArray(EMPTY_LINE_ARRAY);
    }

    @Override
    public Line[] getTargetLines() {
        logger.log(Level.TRACE, "TMixer.getTargetLines(): called");

        return m_openTargetDataLines.toArray(EMPTY_LINE_ARRAY);
    }

    @Override
    public void synchronize(Line[] aLines, boolean bMaintainSync) {
        throw new UnsupportedOperationException("synchronization not supported");
    }

    @Override
    public void unsynchronize(Line[] aLines) {
        throw new UnsupportedOperationException("synchronization not supported");
    }

    @Override
    public boolean isSynchronizationSupported(Line[] aLines, boolean bMaintainSync) {
        return false;
    }

    protected boolean isSourceFormatSupported(AudioFormat format) {
        logger.log(Level.TRACE, "TMixer.isSourceFormatSupported(): format to test: " + format);

        for (AudioFormat supportedFormat : m_supportedSourceFormats) {
            if (AudioFormats.matches(supportedFormat, format)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isTargetFormatSupported(AudioFormat format) {
        logger.log(Level.TRACE, "TMixer.isTargetFormatSupported(): format to test: " + format);

        for (AudioFormat supportedFormat : m_supportedTargetFormats) {
            if (AudioFormats.matches(supportedFormat, format)) {
                return true;
            }
        }
        return false;
    }

    /* package */ void registerOpenLine(Line line) {
        logger.log(Level.TRACE, "TMixer.registerOpenLine(): line to register: " + line);

        if (line instanceof SourceDataLine) {
            synchronized (m_openSourceDataLines) {
                m_openSourceDataLines.add((SourceDataLine) line);
            }
        } else if (line instanceof TargetDataLine) {
            synchronized (m_openSourceDataLines) {
                m_openTargetDataLines.add((TargetDataLine) line);
            }
        }
    }

    /* package */ void unregisterOpenLine(Line line) {
        logger.log(Level.TRACE, "TMixer.unregisterOpenLine(): line to unregister: " + line);

        if (line instanceof SourceDataLine) {
            synchronized (m_openSourceDataLines) {
                m_openSourceDataLines.remove(line);
            }
        } else if (line instanceof TargetDataLine) {
            synchronized (m_openTargetDataLines) {
                m_openTargetDataLines.remove(line);
            }
        }
    }
}
