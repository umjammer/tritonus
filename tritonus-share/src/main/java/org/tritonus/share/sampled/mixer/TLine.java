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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

import org.tritonus.share.TNotifier;

import static java.lang.System.getLogger;


/**
 * Base class for classes implementing Line.
 */
public abstract class TLine implements Line {

    private static final Logger logger= getLogger("org.tritonus.TraceLine");

    private static final Control[] EMPTY_CONTROL_ARRAY = new Control[0];

    private Line.Info info;
    private boolean open;
    private final List<Control> controls;
    private final Set<LineListener> lineListeners;
    private final TMixer mixer;

    protected TLine(TMixer mixer, Line.Info info) {
        setLineInfo(info);
        setOpen(false);
        controls = new ArrayList<>();
        lineListeners = new HashSet<>();
        this.mixer = mixer;
    }

    protected TLine(TMixer mixer, Line.Info info, Collection<Control> controls) {
        this(mixer, info);
        this.controls.addAll(controls);
    }

    protected TMixer getMixer() {
        return mixer;
    }

    @Override
    public Line.Info getLineInfo() {
        return info;
    }

    protected void setLineInfo(Line.Info info) {
        logger.log(Level.TRACE, "setting: " + info);

        synchronized (this) {
            this.info = info;
        }
    }

    @Override
    public void open() throws LineUnavailableException {
        logger.log(Level.TRACE, "called");

        if (!isOpen()) {
            logger.log(Level.TRACE, "opening");

            openImpl();
            if (getMixer() != null) {
                getMixer().registerOpenLine(this);
            }
            setOpen(true);
        } else {
            logger.log(Level.TRACE, "already open");
        }
    }

    /**
     * Subclasses should override this method.
     */
    protected void openImpl() throws LineUnavailableException {
        logger.log(Level.TRACE, "called");
    }

    @Override
    public void close() {
        logger.log(Level.TRACE, "called");

        if (isOpen()) {
            logger.log(Level.TRACE, "closing");

            if (getMixer() != null) {
                getMixer().unregisterOpenLine(this);
            }
            closeImpl();
            setOpen(false);
        } else {
            logger.log(Level.TRACE, "not open");
        }
    }

    /**
     * Subclasses should override this method.
     */
    protected void closeImpl() {
        logger.log(Level.TRACE, "called");
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    protected void setOpen(boolean open) {
        logger.log(Level.TRACE, "called, value: " + open);

        boolean oldValue = isOpen();
        this.open = open;
        if (oldValue != isOpen()) {
            if (isOpen()) {
                logger.log(Level.TRACE, "opened");

                notifyLineEvent(LineEvent.Type.OPEN);
            } else {
                logger.log(Level.TRACE, "closed");

                notifyLineEvent(LineEvent.Type.CLOSE);
            }
        }
    }

    protected void addControl(Control control) {
        synchronized (controls) {
            controls.add(control);
        }
    }

    protected void removeControl(Control control) {
        synchronized (controls) {
            controls.remove(control);
        }
    }

    @Override
    public Control[] getControls() {
        synchronized (controls) {
            return controls.toArray(EMPTY_CONTROL_ARRAY);
        }
    }

    @Override
    public Control getControl(Control.Type controlType) {
        synchronized (controls) {
            for (Control control : controls) {
                if (control.getType().equals(controlType)) {
                    return control;
                }
            }
            throw new IllegalArgumentException("no control of type " + controlType);
        }
    }

    @Override
    public boolean isControlSupported(Control.Type controlType) {
//logger.log(Level.TRACE, "called");
        try {
            return getControl(controlType) != null;
        } catch (IllegalArgumentException e) {
                logger.log(Level.ERROR, e.getMessage(), e);

//logger.log(Level.TRACE, "returning false");
            return false;
        }
    }

    @Override
    public void addLineListener(LineListener listener) {
//logger.log(Level.TRACE, "%% called");
        synchronized (lineListeners) {
            lineListeners.add(listener);
        }
    }

    @Override
    public void removeLineListener(LineListener listener) {
        synchronized (lineListeners) {
            lineListeners.remove(listener);
        }
    }

    private Set<LineListener> getLineListeners() {
        synchronized (lineListeners) {
            return new HashSet<>(lineListeners);
        }
    }

    /** is overridden in TDataLine to provide a position */
    protected void notifyLineEvent(LineEvent.Type type) {
        notifyLineEvent(new LineEvent(this, type, AudioSystem.NOT_SPECIFIED));
    }

    protected void notifyLineEvent(LineEvent event) {
//logger.log(Level.TRACE, "%% called");
//        Channel.Event event = new Channel.Event(this, type, getPosition());
        TNotifier.notifier.addEntry(event, getLineListeners());
    }
}
