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
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.System.getLogger;


/**
 * RTSystem.
 * <p>
 * This file is part of Tritonus: http://www.tritonus.org/
 */
public class RTSystem extends Thread {

    private static final Logger logger = getLogger(RTSystem.class.getName());
    
    private final SystemOutput output;
    private final Map<String, Class<AbstractInstrument>> instrumentMap;
    private boolean running;
    private int time;
    private float timeStep;
    private int aRate;
    private int kRate;
    private int aToKRateFactor;
    private final List<AbstractInstrument> activeInstruments;
    private final List<AbstractInstrument> scheduledInstruments;
    private int scheduledEndTime;
    private float floatToIntTimeFactor;
    private float intToFloatTimeFactor;

    public RTSystem(SystemOutput output, Map<String, Class<AbstractInstrument>> instrumentMap) {
        this.output = output;
        this.instrumentMap = instrumentMap;
        // TODO
        setRates(44100, 100);
        activeInstruments = new LinkedList<>();
        scheduledInstruments = new LinkedList<>();
        scheduledEndTime = Integer.MAX_VALUE;
    }

    private void setRates(int aRate, int kRate) {
        this.aRate = aRate;
        this.kRate = kRate;
        aToKRateFactor = aRate / kRate;
        timeStep = 1.0F / kRate;
        // following is only correct for 60 BPM
        floatToIntTimeFactor = kRate;
        intToFloatTimeFactor = timeStep;
    }

    @Override
    public void run() {
        try {
            runImpl();
        } catch (IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    private void runImpl() throws IOException {
        running = true;
        time = 0;
        while (running) {
            doI();
            doK();
            for (int i = 0; i < aToKRateFactor; i++) {
                doA();
            }
            advanceTime();
        }
        output.close();
    }

    private void doI() {
        logger.log(Level.DEBUG, "begin");
        logger.log(Level.DEBUG, "time: " + getTime());
        synchronized (scheduledInstruments) {
            Iterator<AbstractInstrument> scheduledInstruments = this.scheduledInstruments.iterator();
            while (scheduledInstruments.hasNext()) {
                logger.log(Level.DEBUG, "scheduled instrument");
                AbstractInstrument instrument = scheduledInstruments.next();
                logger.log(Level.DEBUG, "instrument start time: " + instrument.getStartTime());
                if (getTime() >= instrument.getStartTime()) {
                    logger.log(Level.DEBUG, "...activating");
                    scheduledInstruments.remove();
                    instrument.doIPass(this);
                    activeInstruments.add(instrument);
                }
            }
        }
        Iterator<AbstractInstrument> activeInstruments = this.activeInstruments.iterator();
        while (activeInstruments.hasNext()) {
            AbstractInstrument instrument = activeInstruments.next();
            if (getTime() > instrument.getEndTime()) {
                logger.log(Level.DEBUG, "...DEactivating");
                activeInstruments.remove();
            }
        }
        if (getTime() >= getScheduledEndTime()) {
            stopEngine();
        }
    }

    private void doK() {
        for (AbstractInstrument instrument : activeInstruments) {
            instrument.doKPass(this);
        }
    }

    private void doA() throws IOException {
//logger.log(Level.TRACE, "begin");
        output.clear();
        for (AbstractInstrument instrument : activeInstruments) {
//logger.log(Level.TRACE, "has active Instrument");
            instrument.doAPass(this);
        }
        output.emit();
    }

    public void scheduleInstrument(String instrumentName, float startTime, float duration) {
        AbstractInstrument instrument = createInstrumentInstance(instrumentName);
        int _startTime = Math.round(startTime * floatToIntTimeFactor);
        int endTime = Math.round((startTime + duration) * floatToIntTimeFactor);
        instrument.setStartAndEndTime(_startTime, endTime);
        synchronized (scheduledInstruments) {
            scheduledInstruments.add(instrument);
            logger.log(Level.DEBUG, "adding instrument");
            logger.log(Level.DEBUG, "start: " + _startTime);
            logger.log(Level.DEBUG, "end: " + endTime);
        }
    }

    public void scheduleEnd(float endTime) {
        scheduledEndTime = Math.round(endTime * floatToIntTimeFactor);
        // TODO
    }

    public void stopEngine() {
        running = false;
    }

    private void advanceTime() {
        time++;
    }

    public int getTime() {
        return time;
    }

    public void output(float fValue) {
        output.output(fValue);
    }

    private int getScheduledEndTime() {
        return scheduledEndTime;
    }

    private AbstractInstrument createInstrumentInstance(String instrumentName) {
        AbstractInstrument instrument = null;
        Class<AbstractInstrument> instrumentClass = instrumentMap.get(instrumentName);
        try {
            Constructor<AbstractInstrument> constructor = instrumentClass.getConstructor(RTSystem.class);
            instrument = constructor.newInstance(this);
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
        return instrument;
    }
}
