/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import org.epics.util.concurrent.Executors;
import org.joda.time.Duration;
import org.junit.AfterClass;
import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author carcassi
 */
public class ActiveRateDecouplerTest {

    static ScheduledExecutorService executor = java.util.concurrent.Executors.newScheduledThreadPool(3, Executors.namedPool("test"));

    @AfterClass
    public static void closeExecutor() {
        executor.shutdownNow();
    }

    @Test
    public void pauseResume() {
        DesiredRateEventLog log = new DesiredRateEventLog();
        RateDecoupler decoupler = new ActiveRateDecoupler(executor, Duration.millis(10), log, null);
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
    public void activeScanningRate() throws Exception {
        repeatTest(10, new Callable<Object>() {
            public Object call() throws Exception {
                DesiredRateEventLog log = new DesiredRateEventLog();
                RateDecoupler decoupler = new ActiveRateDecoupler(executor, Duration.millis(100), log, null);
                log.setDecoupler(decoupler);
                decoupler.start();
                Thread.sleep(500);
                decoupler.stop();
                // XXX changing the max event's in order to accommodate for irregularities on CI setups
                assertThat(log.getEvents().size(), lessThanOrEqualTo(6));
                assertThat(log.getEvents().size(), greaterThanOrEqualTo(4));
                return null;
            }
        });
    }

    @Test
    public void pausedScanningRate() throws Exception {
        repeatTest(10, new Callable<Object>() {
            public Object call() throws Exception {
                DesiredRateEventLog log = new DesiredRateEventLog();
                RateDecoupler decoupler = new ActiveRateDecoupler(executor, Duration.millis(100), log, null);
                log.setDecoupler(decoupler);
                decoupler.start();
                decoupler.pause();
                Thread.sleep(300);
                decoupler.resume();
                decoupler.stop();
                assertThat(log.getEvents().size(), lessThan(2));
                return null;
            }
        });
    }


    public static void repeatTest(int nTimes, Callable<?> task) throws Exception {
        for (int i = 0; i < nTimes; i++) {
            task.call();
        }
    }
}
