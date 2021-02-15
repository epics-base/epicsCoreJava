/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.joda.time.Duration;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.epics.util.concurrent.Executors;
import org.junit.AfterClass;

/**
 *
 * @author carcassi
 */
public class PassiveRateDecouplerTest {

    static ScheduledExecutorService executor = java.util.concurrent.Executors.newScheduledThreadPool(3, Executors.namedPool("test"));

    @AfterClass
    public static void closeExecutor() {
        executor.shutdownNow();
    }

    @Test
    public void pauseResume() {
        DesiredRateEventLog log = new DesiredRateEventLog();
        RateDecoupler decoupler = new PassiveRateDecoupler(executor, Duration.millis(10), log, null);
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
            public Object call() throws Exception {
                DesiredRateEventLog log = new DesiredRateEventLog();
                RateDecoupler decoupler = new PassiveRateDecoupler(executor, Duration.millis(100), log, null);
                log.setDecoupler(decoupler);
                decoupler.start();
                Thread.sleep(500);
                decoupler.stop();
                assertThat(log.getEvents().size(), equalTo(0));
                return null;
            }
        });
    }

    @Test
    public void slowEvents() throws Exception {
        repeatTest(10, new Callable<Object>() {
            public Object call() throws Exception {
                DesiredRateEventLog log = new DesiredRateEventLog();
                RateDecoupler decoupler = new PassiveRateDecoupler(executor, Duration.millis(20), log, null);
                log.setDecoupler(decoupler);
                decoupler.start();
                decoupler.getUpdateListener().accept(PVEvent.readConnectionEvent());
                Thread.sleep(100);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(100);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(100);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(100);
                decoupler.stop();
                // 4 events, connection and 3 values
                assertThat(log.getEvents().size(), equalTo(4));
                return null;
            }
        });
    }

    public void enableLog() {
        Logger log = Logger.getLogger(PassiveRateDecoupler.class.getName());
        log.setLevel(Level.FINEST);
        Handler[] handlers = log.getHandlers();
        for(Handler h: handlers){
            h.setLevel(Level.FINEST);
        }
    }

    public void disableLog() {
        Logger log = Logger.getLogger(PassiveRateDecoupler.class.getName());
        log.setLevel(Level.INFO);
        Handler[] handlers = log.getHandlers();
        for(Handler h: handlers){
            h.setLevel(Level.INFO);
        }
    }

    @Test
    public void fastEvents() throws Exception {
        repeatTest(10, new Callable<Object>() {
            public Object call() throws Exception {
                log = new DesiredRateEventLog();
                RateDecoupler decoupler = new PassiveRateDecoupler(executor, Duration.millis(100), log, null);
                log.setDecoupler(decoupler);
                decoupler.start();
                decoupler.getUpdateListener().accept(PVEvent.readConnectionEvent());
                Thread.sleep(100);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(1);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(1);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(1);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(1);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(1);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(1);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(1);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(150);
                decoupler.stop();
                // 3 events: connection, first value, last value
                assertThat(log.getEvents().size(), equalTo(3));
                assertThat(log.getEvents().get(0).getType(), equalTo(Collections.singletonList(PVEvent.Type.READ_CONNECTION)));
                assertThat(log.getEvents().get(1).getType(), equalTo(Collections.singletonList(PVEvent.Type.VALUE)));
                assertThat(log.getEvents().get(2).getType(), equalTo(Collections.singletonList(PVEvent.Type.VALUE)));
                return null;
            }
        });
    }

    @Test
    public void fastEvents2() throws Exception {
        repeatTest(10, new Callable<Object>() {
            public Object call() throws Exception {
                log = new DesiredRateEventLog();
                RateDecoupler decoupler = new PassiveRateDecoupler(executor, Duration.millis(100), log, null);
                log.setDecoupler(decoupler);
                decoupler.start();
                decoupler.getUpdateListener().accept(PVEvent.readConnectionEvent());
                Thread.sleep(50);
                decoupler.getUpdateListener().accept(PVEvent.readConnectionEvent());
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(150);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(1);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(1);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(1);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(1);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(1);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(1);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(1);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(150);
                decoupler.stop();
                // 3 events: connection, first value, last value
                assertThat(log.getEvents().size(), equalTo(4));
                assertThat(log.getEvents().get(0).getType(), equalTo(Collections.singletonList(PVEvent.Type.READ_CONNECTION)));
                assertThat(log.getEvents().get(1).getType(), equalTo(Arrays.asList(PVEvent.Type.READ_CONNECTION, PVEvent.Type.VALUE)));
                assertThat(log.getEvents().get(2).getType(), equalTo(Collections.singletonList(PVEvent.Type.VALUE)));
                assertThat(log.getEvents().get(3).getType(), equalTo(Collections.singletonList(PVEvent.Type.VALUE)));
                return null;
            }
        });
    }

    @Test
    public void rescheduling() throws Exception {
        repeatTest(10, new Callable<Object>() {
            public Object call() throws Exception {
                log = new DesiredRateEventLog(10);
                RateDecoupler decoupler = new PassiveRateDecoupler(executor, Duration.millis(50), log, null);
                log.setDecoupler(decoupler);
                decoupler.start();
                decoupler.getUpdateListener().accept(PVEvent.readConnectionEvent());
                // Wait for connection event
                Thread.sleep(60);

                // Send events at 100Hz
                long startTime = System.nanoTime();
                for (int i = 0; i < 4*5+2; i++) {
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
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
            public Object call() throws Exception {
                log = new DesiredRateEventLog(100);
                RateDecoupler decoupler = new PassiveRateDecoupler(executor, Duration.millis(20), log, null);
                log.setDecoupler(decoupler);
                decoupler.start();
                decoupler.getUpdateListener().accept(PVEvent.readConnectionEvent());
                // Wait for connection event
                Thread.sleep(125);

                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(25);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
                Thread.sleep(25);
                decoupler.getUpdateListener().accept(PVEvent.valueEvent());
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
