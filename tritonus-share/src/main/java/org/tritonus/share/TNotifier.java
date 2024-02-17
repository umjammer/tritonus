
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;


public class TNotifier
        extends Thread {

    public static class NotifyEntry {

        private EventObject m_event;
        private List<LineListener> m_listeners;

        public NotifyEntry(EventObject event, Collection<LineListener> listeners) {
            m_event = event;
            m_listeners = new ArrayList<>(listeners);
        }

        public void deliver() {
            // TDebug.out("%% TNotifier.NotifyEntry.deliver(): called.");
            for (LineListener listener : m_listeners) {
                listener.update((LineEvent) m_event);
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
    private final List<NotifyEntry> m_entries;

    public TNotifier() {
        super("Tritonus Notifier");
        m_entries = new ArrayList<>();
    }

    public void addEntry(EventObject event, Collection<LineListener> listeners) {
        // TDebug.out("%% TNotifier.addEntry(): called.");
        synchronized (m_entries) {
            m_entries.add(new NotifyEntry(event, listeners));
            m_entries.notifyAll();
        }
        // TDebug.out("%% TNotifier.addEntry(): completed.");
    }

    @Override
    public void run() {
        while (true) {
            NotifyEntry entry;
            synchronized (m_entries) {
                while (m_entries.isEmpty()) {
                    try {
                        m_entries.wait();
                    } catch (InterruptedException e) {
                        if (TDebug.TraceAllExceptions) {
                            TDebug.out(e);
                        }
                    }
                }
                entry = m_entries.remove(0);
            }
            entry.deliver();
        }
    }
}


