package org.epics.gpclient.datasource.ca;

import org.epics.gpclient.*;
import org.epics.gpclient.datasource.DataSourceProvider;
import org.epics.util.compat.legacy.functional.Function;
import org.epics.vtype.*;
import org.joda.time.Duration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.logging.Logger;

public class CAChannelTest {
    private static final Logger log = Logger.getLogger(CAChannelTest.class.getName());

    static GPClientInstance gpClient;

    @BeforeClass
    public static void setup() {

        log.info("Creating the context");
        // Start the test server
        InMemoryCAServer.initializeServerInstance();

        gpClient = new GPClientConfiguration().defaultMaxRate(Duration.millis(50))
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

    @SuppressWarnings("rawtypes")
    @Test
    public void createSimpleChannel() throws InterruptedException {
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        PVReader<VType> pv = gpClient.read("ca://test_double_0").addListener(recorder).start();
        recorder.wait(500, PVEventRecorder.forAConnectionEvent());
        recorder.wait(200, PVEventRecorder.anEventOfType(PVEvent.Type.VALUE));
        pv.close();
        Thread.sleep(1000);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void createSimpleWriteChannel() throws InterruptedException {
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        PV<VType, Object> pv = gpClient.readAndWrite("ca://test_double_0").addListener(recorder).start();
        recorder.wait(1000, PVEventRecorder.forAConnectionEvent());
        recorder.wait(200, PVEventRecorder.anEventOfType(PVEvent.Type.VALUE));
        pv.write(VDouble.of(1.0, Alarm.none(), Time.now(), Display.none()));
        Thread.sleep(1000);
        pv.write(VDouble.of(2.0, Alarm.none(), Time.now(), Display.none()));
        Thread.sleep(1000);
        pv.write(VDouble.of(3.0, Alarm.none(), Time.now(), Display.none()));
        Thread.sleep(1000);
        pv.write(VDouble.of(4.0, Alarm.none(), Time.now(), Display.none()));
        Thread.sleep(1000);
        pv.write(VDouble.of(5.0, Alarm.none(), Time.now(), Display.none()));
        Thread.sleep(1000);
        pv.close();
        Thread.sleep(1000);
        recorder.hasReceived(
                new Function<List<PVEvent>, Boolean>() {
                    @Override
                    public Boolean apply(List<PVEvent> list) {
                        // count where write has succeeded should be 5
                        int count = 0;
                        for (PVEvent event : list) {
                            if (event.isType(PVEvent.Type.WRITE_SUCCEEDED)) {
                                count++;
                            }
                        }
                        return count == 5;
                    }

                    @Override
                    public String toString() {
                        return "an event";
                    }
                }
        );
    }
}
