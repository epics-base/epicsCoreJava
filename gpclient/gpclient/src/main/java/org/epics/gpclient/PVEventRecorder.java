/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class to record the flow of events mainly for debugging and testing
 * purposes. It allows to add test conditions that lock the thread until
 * the conditions is verified. It also allows to add some logic that is
 * executed when each event is received, prior to checking the test condition.
 *
 * @author carcassi
 */
public class PVEventRecorder implements Consumer<PVEvent> {
    
    private final Object lock = new Object();
    private final List<PVEvent> events = new ArrayList<>();
    private Runnable test;
    
    @Override
    public final void accept(PVEvent event) {
        synchronized (lock) {
            events.add(event);
            onEvent(event);
            if (test != null) {
                test.run();
            }
        }
    }
    
    /**
     * Custom logic executed at each event. This method is executed after the event
     * is added to the list but before the check is performed.
     * 
     * @param event a new event
     */
    protected void onEvent(PVEvent event) {
    }

    /**
     * Returns all the events collected so far.
     * 
     * @return 
     */
    public List<PVEvent> getEvents() {
        return events;
    }

    public void wait(int ms, Function<List<PVEvent>, Boolean> condition) {
        CountDownLatch latch = new CountDownLatch(1);
        Runnable newTest = new Runnable() {
            @Override
            public void run() {
                if (condition.apply(events)) {
                    latch.countDown();
                }
            }
        };
        synchronized(lock) {
            test = newTest;
            test.run();
        }
        
        boolean success = false;
        
        try {
            success = latch.await(ms, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
        }
        
        if (!success) {
            throw new AssertionError("Waited for " + condition + " but it didn't happen within " + ms + " ms");
        }
    }

    public void dontExpect(int ms, Function<List<PVEvent>, Boolean> condition) {
        CountDownLatch latch = new CountDownLatch(1);
        Runnable newTest = new Runnable() {
            @Override
            public void run() {
                if (condition.apply(events)) {
                    latch.countDown();
                }
            }
        };
        synchronized(lock) {
            test = newTest;
            test.run();
        }
        
        boolean success = false;
        
        try {
            success = latch.await(ms, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
        }
        
        if (success) {
            throw new AssertionError("Wasn't expecting " + condition + " but it it happened");
        }
    }
    
    public static Function<List<PVEvent>, Boolean> forAnEvent() {
        return new Function<List<PVEvent>, Boolean>() {
            @Override
            public Boolean apply(List<PVEvent> list) {
                return !list.isEmpty();
            }

            @Override
            public String toString() {
                return "an event received";
            }
        };
    }
    
    public static Function<List<PVEvent>, Boolean> forAConnectionEvent() {
        return new Function<List<PVEvent>, Boolean>() {
            @Override
            public Boolean apply(List<PVEvent> list) {
                return list.get(list.size() - 1).getType().contains(PVEvent.Type.READ_CONNECTION);
            }

            @Override
            public String toString() {
                return "a connection event";
            }
        };
    }
    
    public static Function<List<PVEvent>, Boolean> forEventCount(final int count) {
        return new Function<List<PVEvent>, Boolean>() {
            @Override
            public Boolean apply(List<PVEvent> list) {
                return list.size() >= count;
            }

            @Override
            public String toString() {
                return count + " events received";
            }
        };
    }
    
}
