/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.expression;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.epics.gpclient.expression.ActiveScanDecouplerTest.executor;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.epics.gpclient.expression.DesiredRateEvent.Type.*;
import org.epics.util.concurrent.Executors;
import org.junit.AfterClass;
import org.junit.Ignore;

/**
 *
 * @author carcassi
 */
public class PassiveScanDecouplerTest {
    
    static ScheduledExecutorService executor = java.util.concurrent.Executors.newScheduledThreadPool(3, Executors.namedPool("test"));
    
    @AfterClass
    public static void closeExecutor() {
        executor.shutdownNow();
    }

    @Test
    public void pauseResume() {
        DesiredRateEventLog log = new DesiredRateEventLog();
        SourceDesiredRateDecoupler decoupler = new PassiveScanDecoupler(executor, Duration.ofMillis(10), log);
        log.setDecoupler(decoupler);
        decoupler.start();
        assertThat(decoupler.isPaused(), equalTo(false));
        assertThat(decoupler.isStopped(), equalTo(false));
        decoupler.pause();
        assertThat(decoupler.isPaused(), equalTo(true));
        assertThat(decoupler.isStopped(), equalTo(false));
        decoupler.resume();
        assertThat(decoupler.isPaused(), equalTo(false));
        assertThat(decoupler.isStopped(), equalTo(false));
        decoupler.stop();
        assertThat(decoupler.isPaused(), equalTo(false));
        assertThat(decoupler.isStopped(), equalTo(true));
    }

    @Test
    public void noEvents() throws Exception {
        repeatTest(10, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                DesiredRateEventLog log = new DesiredRateEventLog();
                SourceDesiredRateDecoupler decoupler = new PassiveScanDecoupler(executor, Duration.ofMillis(100), log);
                log.setDecoupler(decoupler);
                decoupler.start();
                Thread.sleep(500);
                decoupler.stop();
                // 1 event: connection
                assertThat(log.getEvents().size(), equalTo(1));
                return null;
            }
        });
    }

    @Test
    public void slowEvents() throws Exception {
        repeatTest(10, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                DesiredRateEventLog log = new DesiredRateEventLog();
                SourceDesiredRateDecoupler decoupler = new PassiveScanDecoupler(executor, Duration.ofMillis(20), log);
                log.setDecoupler(decoupler);
                decoupler.start();
                Thread.sleep(100);
                decoupler.newValueEvent();
                Thread.sleep(100);
                decoupler.newValueEvent();
                Thread.sleep(100);
                decoupler.newValueEvent();
                Thread.sleep(100);
                decoupler.stop();
                // 4 events, connection and 3 values
                assertThat(log.getEvents().size(), equalTo(4));
                return null;
            }
        });
    }

    public void enableLog() {
        Logger log = Logger.getLogger(PassiveScanDecoupler.class.getName());
        log.setLevel(Level.FINEST);
        Handler[] handlers = log.getHandlers();
        for(Handler h: handlers){
            h.setLevel(Level.FINEST);
        }
    }
    
    public void disableLog() {
        Logger log = Logger.getLogger(PassiveScanDecoupler.class.getName());
        log.setLevel(Level.INFO);
        Handler[] handlers = log.getHandlers();
        for(Handler h: handlers){
            h.setLevel(Level.INFO);
        }
    }
    
    @Test
    public void fastEvents() throws Exception {
        // TODO: make this test always pass, and get debug info if it doesn't
        repeatTest(10, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                log = new DesiredRateEventLog();
                SourceDesiredRateDecoupler decoupler = new PassiveScanDecoupler(executor, Duration.ofMillis(100), log);
                log.setDecoupler(decoupler);
                decoupler.start();
                Thread.sleep(100);
                decoupler.newValueEvent();
                Thread.sleep(1);
                decoupler.newValueEvent();
                Thread.sleep(1);
                decoupler.newValueEvent();
                Thread.sleep(1);
                decoupler.newValueEvent();
                Thread.sleep(1);
                decoupler.newValueEvent();
                Thread.sleep(1);
                decoupler.newValueEvent();
                Thread.sleep(1);
                decoupler.newValueEvent();
                Thread.sleep(1);
                decoupler.newValueEvent();
                Thread.sleep(150);
                decoupler.stop();
                // 3 events: connection, first value, last value
                assertThat(log.getEvents().size(), equalTo(3));
                return null;
            }
        });
    }

    @Test
    public void rescheduling() throws Exception {
        repeatTest(10, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                log = new DesiredRateEventLog(10);
                SourceDesiredRateDecoupler decoupler = new PassiveScanDecoupler(executor, Duration.ofMillis(50), log);
                log.setDecoupler(decoupler);
                decoupler.start();
                // Wait for connection event
                Thread.sleep(60);
                
                // Send events at 100Hz
                long startTime = System.nanoTime();
                for (int i = 0; i < 4*5+2; i++) {
                    decoupler.newValueEvent();
                    Thread.sleep(10);
                }
                long period = System.nanoTime() - startTime;
                
                // Wait to drain
                Thread.sleep(100);
                decoupler.stop();
                
                // 1 event at the start of each full 50ms + 1 for the partial 50 ms
                // + 1 at the end  of the last 50ms (if event during notification) + 1 for connection
                int expectedEvents = (int) (period / 50000000) + 1 + 1;
                assertThat(log.getEvents().size(), isOneOf(expectedEvents, expectedEvents + 1));
                return null;
            }
        });
    }
    
    public static void enableLog(Class<?> clazz, Level level) {
        Handler handler = Logger.getLogger("").getHandlers()[0];
        if (level.intValue() < handler.getLevel().intValue()) {
            handler.setLevel(level);
        }
        Logger.getLogger(clazz.getName()).setLevel(level);
    }
    
    public static void disableLog(Class<?> clazz) {
        Logger.getLogger(clazz.getName()).setLevel(null);
    }

    @Test
    public void slowResponse() throws Exception {
        repeatTest(10, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                log = new DesiredRateEventLog(100);
                SourceDesiredRateDecoupler decoupler = new PassiveScanDecoupler(executor, Duration.ofMillis(20), log);
                log.setDecoupler(decoupler);
                decoupler.start();
                // Wait for connection event
                Thread.sleep(125);
                
                decoupler.newValueEvent();
                Thread.sleep(25);
                decoupler.newValueEvent();
                Thread.sleep(25);
                decoupler.newValueEvent();
                Thread.sleep(500);
                decoupler.stop();
                
                // Expect 3 events: 1 conn, first value, last value
                assertThat(log.getEvents().size(), equalTo(3));
                return null;
            }
        });
    }
    
    private DesiredRateEventLog log;
    
    public void repeatTest(int nTimes, Callable<?> task) throws Exception {
        for (int i = 0; i < nTimes; i++) {
            try {
                task.call();
            } catch (AssertionError er) {
                if (log != null) {
                    log.printLog();
                }
                throw er;
            }
        }
    }
}