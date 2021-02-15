/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.sample;

import org.joda.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.epics.gpclient.GPClientConfiguration;
import org.epics.gpclient.GPClientInstance;
import org.epics.gpclient.datasource.DataSourceProvider;
import static org.junit.Assert.*;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author carcassi
 */
public class BlackBoxTestBase {

    static GPClientInstance gpClient;

    @BeforeClass
    public static void createClient() {
        gpClient = new GPClientConfiguration().defaultMaxRate(Duration.millis(50))
                .notificationExecutor(org.epics.util.concurrent.Executors.localThread())
                .dataSource(DataSourceProvider.createDataSource())
                .dataProcessingThreadPool(java.util.concurrent.Executors.newScheduledThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1),
                        org.epics.util.concurrent.Executors.namedPool("PVMgr Worker "))).build();
    }

    @AfterClass
    public static void closeClient() {
        gpClient.close();
    }

    public static void awaitTimeout(CountDownLatch latch, Duration duration) throws InterruptedException {
        if (!latch.await(duration.getMillis(), TimeUnit.MILLISECONDS)) {
            fail("Latch didn't count to zero");
        }
    }
}
