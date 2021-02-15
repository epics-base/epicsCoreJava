/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import org.epics.util.compat.legacy.functional.Consumer;
import org.joda.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author carcassi
 */
 class DesiredRateEventLog extends Consumer<PVEvent> {

    private final Object lock = new Object();

    private final List<PVEvent> events = new ArrayList<PVEvent>();
    private final List<Instant> timestamps = new ArrayList<Instant>();
    private RateDecoupler decoupler;
    private final Integer pause;

    public DesiredRateEventLog() {
        pause = null;
    }

    public DesiredRateEventLog(Integer msPause) {
        this.pause = msPause;
    }

    public void setDecoupler(RateDecoupler decoupler) {
        synchronized(lock) {
            this.decoupler = decoupler;
        }
    }

    public RateDecoupler getDecoupler() {
        synchronized(lock) {
            return decoupler;
        }
    }

    @Override
    public void accept(PVEvent event) {
        synchronized(lock) {
            events.add(event);
            timestamps.add(Instant.now());
        }
        if (pause != null) {
            try {
                Thread.sleep(pause);
            } catch (InterruptedException ex) {
                Logger.getLogger(DesiredRateEventLog.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        RateDecoupler decoupler;
        synchronized(lock) {
            decoupler = this.decoupler;
        }
        decoupler.readyForNextEvent();
    }

    public List<PVEvent.Type> getEventTypes(int n) {
        synchronized(lock) {
            return events.get(n).getType();
        }
    }

    public List<PVEvent> getEvents() {
        synchronized(lock) {
            return events;
        }
    }

    public void printLog() {
        synchronized(lock) {
            for (int i = 0; i < events.size(); i++) {
                System.out.println(timestamps.get(i) + " " + events.get(i).getType());
            }
        }
    }

}
