/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient.expression;

import java.io.PrintStream;
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
import org.epics.gpclient.expression.ReadEvent;
import org.epics.gpclient.expression.ReadEvent;

/**
 *
 * @author carcassi
 */
public class ProbeCollector<T> {
    
    private final ReadCollector<T, T> collector;
    private final Object lock = new Object();
    private final List<ReadEvent> events = new ArrayList<>();
    private final PrintStream out;
    private Runnable test;

    public ProbeCollector(Class<T> type, PrintStream out) {
        this.out = out;
        this.collector = new LatestValueCollector<>(type);
        this.collector.setUpdateListener(new Consumer<ReadEvent>() {
            @Override
            public void accept(ReadEvent event) {
                synchronized(lock) {
                    events.add(event);
                    print(event);
                    if (test != null) {
                        test.run();
                    }
                }
            }
        });
    }
    
    private void print(ReadEvent event) {
        if (out != null) {
            if (event.getType().contains(ReadEvent.Type.READ_CONNECTION)) {
                out.println("CONN: " + collector.getConnection());
            }
            if (event.getType().contains(ReadEvent.Type.VALUE)) {
                out.println("VAL: " + collector.getValue());
            }
            if (event.getType().contains(ReadEvent.Type.READ_EXCEPTION)) {
                out.println("ERR: " + event.getException().getMessage());
            }
        }
    }
    
    public boolean getConnection() {
        return collector.getConnection();
    }
    
    public T getValue() {
        return collector.getValue();
    }

    public ReadCollector<T, T> getCollector() {
        return collector;
    }

    public List<ReadEvent> getEvents() {
        return events;
    }

    public void wait(int ms, Function<List<ReadEvent>, Boolean> condition) {
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

    public void dontExpect(int ms, Function<List<ReadEvent>, Boolean> condition) {
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
    
    public static Function<List<ReadEvent>, Boolean> forAnEvent() {
        return new Function<List<ReadEvent>, Boolean>() {
            @Override
            public Boolean apply(List<ReadEvent> list) {
                return !list.isEmpty();
            }

            @Override
            public String toString() {
                return "an event received";
            }
        };
    }
    
    public static Function<List<ReadEvent>, Boolean> forAConnectionEvent() {
        return new Function<List<ReadEvent>, Boolean>() {
            @Override
            public Boolean apply(List<ReadEvent> list) {
                return list.get(list.size() - 1).getType().contains(ReadEvent.Type.READ_CONNECTION);
            }

            @Override
            public String toString() {
                return "a connection event";
            }
        };
    }
    
    public static Function<List<ReadEvent>, Boolean> forEventCount(final int count) {
        return new Function<List<ReadEvent>, Boolean>() {
            @Override
            public Boolean apply(List<ReadEvent> list) {
                return list.size() >= count;
            }

            @Override
            public String toString() {
                return count + " events received";
            }
        };
    }
    
    public static ProbeCollector<?> create() {
        return new ProbeCollector<>(Object.class, null);
    }
    
}