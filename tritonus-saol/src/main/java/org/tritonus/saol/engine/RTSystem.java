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
    
    private SystemOutput m_output;
    private Map<String, Class<AbstractInstrument>> m_instrumentMap;
    private boolean m_bRunning;
    private int m_nTime;
    private float m_fTimeStep;
    private int m_nARate;
    private int m_nKRate;
    private int m_nAToKRateFactor;
    private List<AbstractInstrument> m_activeInstruments;
    private final List<AbstractInstrument> m_scheduledInstruments;
    private int m_nScheduledEndTime;
    private float m_fFloatToIntTimeFactor;
    private float m_fIntToFloatTimeFactor;

    public RTSystem(SystemOutput output, Map<String, Class<AbstractInstrument>> instrumentMap) {
        m_output = output;
        m_instrumentMap = instrumentMap;
        // TODO
        setRates(44100, 100);
        m_activeInstruments = new LinkedList<>();
        m_scheduledInstruments = new LinkedList<>();
        m_nScheduledEndTime = Integer.MAX_VALUE;
    }

    private void setRates(int nARate, int nKRate) {
        m_nARate = nARate;
        m_nKRate = nKRate;
        m_nAToKRateFactor = nARate / nKRate;
        m_fTimeStep = 1.0F / nKRate;
        // following is only correct for 60 BPM
        m_fFloatToIntTimeFactor = nKRate;
        m_fIntToFloatTimeFactor = m_fTimeStep;
    }

    @Override
    public void run() {
        try {
            runImpl();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void runImpl() throws IOException {
        m_bRunning = true;
        m_nTime = 0;
        while (m_bRunning) {
            doI();
            doK();
            for (int i = 0; i < m_nAToKRateFactor; i++) {
                doA();
            }
            advanceTime();
        }
        m_output.close();
    }

    private void doI() {
        logger.log(Level.DEBUG, "doI()");
        logger.log(Level.DEBUG, "time: " + getTime());
        synchronized (m_scheduledInstruments) {
            Iterator<AbstractInstrument> scheduledInstruments = m_scheduledInstruments.iterator();
            while (scheduledInstruments.hasNext()) {
                logger.log(Level.DEBUG, "scheduled instrument");
                AbstractInstrument instrument = scheduledInstruments.next();
                logger.log(Level.DEBUG, "instrument start time: " + instrument.getStartTime());
                if (getTime() >= instrument.getStartTime()) {
                    logger.log(Level.DEBUG, "...activating");
                    scheduledInstruments.remove();
                    instrument.doIPass(this);
                    m_activeInstruments.add(instrument);
                }
            }
        }
        Iterator<AbstractInstrument> activeInstruments = m_activeInstruments.iterator();
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
        for (AbstractInstrument instrument : m_activeInstruments) {
            instrument.doKPass(this);
        }
    }

    private void doA() throws IOException {
//        logger.log(Level.TRACE, "doA()");
        m_output.clear();
        for (AbstractInstrument instrument : m_activeInstruments) {
//            logger.log(Level.TRACE, "doA(): has active Instrument");
            instrument.doAPass(this);
        }
        m_output.emit();
    }

    public void scheduleInstrument(String strInstrumentName, float fStartTime, float fDuration) {
        AbstractInstrument instrument = createInstrumentInstance(strInstrumentName);
        int nStartTime = Math.round(fStartTime * m_fFloatToIntTimeFactor);
        int nEndTime = Math.round((fStartTime + fDuration) * m_fFloatToIntTimeFactor);
        instrument.setStartAndEndTime(nStartTime, nEndTime);
        synchronized (m_scheduledInstruments) {
            m_scheduledInstruments.add(instrument);
            logger.log(Level.DEBUG, "adding instrument");
            logger.log(Level.DEBUG, "start: " + nStartTime);
            logger.log(Level.DEBUG, "end: " + nEndTime);
        }
    }

    public void scheduleEnd(float fEndTime) {
        m_nScheduledEndTime = Math.round(fEndTime * m_fFloatToIntTimeFactor);
        // TODO
    }

    public void stopEngine() {
        m_bRunning = false;
    }

    private void advanceTime() {
        m_nTime++;
    }

    public int getTime() {
        return m_nTime;
    }

    public void output(float fValue) {
        m_output.output(fValue);
    }

    private int getScheduledEndTime() {
        return m_nScheduledEndTime;
    }

    private AbstractInstrument createInstrumentInstance(String strInstrumentName) {
        AbstractInstrument instrument = null;
        Class<AbstractInstrument> instrumentClass = m_instrumentMap.get(strInstrumentName);
        try {
            Constructor<AbstractInstrument> constructor = instrumentClass.getConstructor(RTSystem.class);
            instrument = constructor.newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instrument;
    }
}
