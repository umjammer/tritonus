/*
 *  Copyright (c) 1999 by Matthias Pfisterer
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

package org.tritonus.share;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

import static java.lang.System.getLogger;


public class TNotifier extends Thread {

    private static final Logger logger= getLogger("org.tritonus.TraceAllExceptions");

    public static class NotifyEntry {

        private final EventObject event;
        private final List<LineListener> listeners;

        public NotifyEntry(EventObject event, Collection<LineListener> listeners) {
            this.event = event;
            this.listeners = new ArrayList<>(listeners);
        }

        public void deliver() {
//logger.log(Level.DEBUG, "%% TNotifier.NotifyEntry.deliver(): called.");
            for (LineListener listener : listeners) {
                listener.update((LineEvent) event);
            }
        }
    }

    public static TNotifier notifier;

    static {
        notifier = new TNotifier();
        notifier.setDaemon(true);
        notifier.start();
    }

    /**
     * The queue of events to deliver.
     * The entries are of class NotifyEntry.
     */
    private final List<NotifyEntry> entries;

    public TNotifier() {
        super("Tritonus Notifier");
        entries = new ArrayList<>();
    }

    public void addEntry(EventObject event, Collection<LineListener> listeners) {
//logger.log(Level.TRACE, "%% TNotifier.addEntry(): called.");
        synchronized (entries) {
            entries.add(new NotifyEntry(event, listeners));
            entries.notifyAll();
        }
//logger.log(Level.TRACE, "%% TNotifier.addEntry(): completed.");
    }

    @Override
    public void run() {
        while (true) {
            NotifyEntry entry;
            synchronized (entries) {
                while (entries.isEmpty()) {
                    try {
                        entries.wait();
                    } catch (InterruptedException e) {
                        logger.log(Level.TRACE, e);
                    }
                }
                entry = entries.remove(0);
            }
            entry.deliver();
        }
    }
}
