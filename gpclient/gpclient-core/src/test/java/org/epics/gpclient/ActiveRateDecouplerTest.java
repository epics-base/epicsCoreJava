/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.epics.util.concurrent.Executors;
import org.junit.AfterClass;

/**
 *
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
        RateDecoupler decoupler = new ActiveRateDecoupler(executor, Duration.ofMillis(10), log, null);
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
            @Override
            public Object call() throws Exception {
                DesiredRateEventLog log = new DesiredRateEventLog();
                RateDecoupler decoupler = new ActiveRateDecoupler(executor, Duration.ofMillis(100), log, null);
                log.setDecoupler(decoupler);
                decoupler.start();
                Thread.sleep(500);
                decoupler.stop();
                assertThat(log.getEvents().size(), lessThanOrEqualTo(5));
                assertThat(log.getEvents().size(), greaterThanOrEqualTo(4));
                return null;
            }
        });
    }

    @Test
    public void pausedScanningRate() throws Exception {
        repeatTest(10, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                DesiredRateEventLog log = new DesiredRateEventLog();
                RateDecoupler decoupler = new ActiveRateDecoupler(executor, Duration.ofMillis(100), log, null);
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
