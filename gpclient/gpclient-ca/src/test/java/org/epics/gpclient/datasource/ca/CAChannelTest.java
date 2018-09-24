package org.epics.gpclient.datasource.ca;

import java.time.Duration;
import java.util.List;
import java.util.logging.Logger;

import org.epics.gpclient.GPClientConfiguration;
import org.epics.gpclient.GPClientInstance;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVEventRecorder;
import org.epics.gpclient.PVReader;
import org.epics.gpclient.ProbeCollector;
import org.epics.gpclient.datasource.DataSource;
import org.epics.gpclient.datasource.DataSourceProvider;
import org.epics.vtype.VType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class CAChannelTest {
    private static final Logger log = Logger.getLogger(CAChannelTest.class.getName());

    static GPClientInstance gpClient;

    @BeforeClass
    public static void setup() {

        log.info("Creating the context");
        // Start the test server
        InMemoryCAServer.initializeServerInstance();

        gpClient = new GPClientConfiguration().defaultMaxRate(Duration.ofMillis(50))
                .notificationExecutor(org.epics.util.concurrent.Executors.localThread())
                .dataSource(DataSourceProvider.createDataSource())
                .dataProcessingThreadPool(java.util.concurrent.Executors.newScheduledThreadPool(
                        Math.max(1, Runtime.getRuntime().availableProcessors() - 1),
                        org.epics.util.concurrent.Executors.namedPool("PVMgr Worker ")))
                .build();
    }

    @AfterClass
    public static void teardown() {

        log.info("cleaning up the context and channels");
        try {
            gpClient.close();
            // Stop the server
            InMemoryCAServer.closeServerInstance();

        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createSimpleChannel() throws InterruptedException {
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        PVReader<VType> pv = gpClient.read("ca://test_double_0").addListener(recorder).start();
        recorder.wait(500, recorder.forAConnectionEvent());
        recorder.wait(50, recorder.anEventOfType(PVEvent.Type.VALUE));
        pv.close();
        Thread.sleep(1000);
        List<PVEvent> events = recorder.getEvents();
        events.stream().forEachOrdered(event -> {
            System.out.println(event.toString());
        });
    }
}
