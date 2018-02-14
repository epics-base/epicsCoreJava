/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.gpclient.expression.LatestValueCollector;
import org.epics.gpclient.expression.LatestValueCollector;
import org.epics.gpclient.expression.ReadCollector;
import org.epics.gpclient.expression.ReadCollector;
import org.epics.gpclient.expression.SourceRateReadEvent;
import org.epics.gpclient.expression.SourceRateReadEvent;

/**
 *
 * @author carcassi
 */
public class ProbeCollector<T> {
    
    private final ReadCollector<T, T> collector;
    private final Object lock = new Object();
    private final List<SourceRateReadEvent> events = new ArrayList<>();
    private Runnable test;

    public ProbeCollector(Class<T> type) {
        this.collector = new LatestValueCollector<>(type);
        this.collector.setUpdateListener(new Consumer<SourceRateReadEvent>() {
            @Override
            public void accept(SourceRateReadEvent event) {
                synchronized(lock) {
                    events.add(event);
                    if (test != null) {
                        test.run();
                    }
                }
            }
        });
    }

    public ReadCollector<T, T> getCollector() {
        return collector;
    }

    public List<SourceRateReadEvent> getEvents() {
        return events;
    }

    public void wait(int ms, Function<List<SourceRateReadEvent>, Boolean> condition) {
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
    
    public static Function<List<SourceRateReadEvent>, Boolean> forAnEvent() {
        return new Function<List<SourceRateReadEvent>, Boolean>() {
            @Override
            public Boolean apply(List<SourceRateReadEvent> list) {
                return !list.isEmpty();
            }

            @Override
            public String toString() {
                return "an event received";
            }
        };
    }
    
    public static Function<List<SourceRateReadEvent>, Boolean> forEventCount(final int count) {
        return new Function<List<SourceRateReadEvent>, Boolean>() {
            @Override
            public Boolean apply(List<SourceRateReadEvent> list) {
                return list.size() >= count;
            }

            @Override
            public String toString() {
                return count + " events received";
            }
        };
    }
    
    public static ProbeCollector<?> create() {
        return new ProbeCollector<>(Object.class);
    }
    
}
