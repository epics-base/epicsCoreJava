
/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.epics.gpclient.GPClientConfiguration;
import org.epics.gpclient.GPClientInstance;
import org.epics.gpclient.PV;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVListener;
import org.epics.gpclient.PVWriter;
import org.epics.gpclient.datasource.DataSourceProvider;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.epics.vtype.VString;
import org.epics.vtype.VType;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 *
 * @author carcassi
 */
public class WriteTest {

    static GPClientInstance gpClient;

    @BeforeClass
    public static void createClient() {
        gpClient = new GPClientConfiguration().defaultMaxRate(Duration.ofMillis(50))
                .notificationExecutor(org.epics.util.concurrent.Executors.localThread())
                .dataSource(DataSourceProvider.createDataSource())
                .dataProcessingThreadPool(java.util.concurrent.Executors.newScheduledThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1),
                        org.epics.util.concurrent.Executors.namedPool("PVMgr Worker "))).build();
    }

    @AfterClass
    public static void closeClient() {
        gpClient.close();
    }

    @Test(expected = RuntimeException.class)
    public void writeInexistentChannel() {
        PV<VType, Object> pv = gpClient.readAndWrite("none://nothing")
                .addListener((PVListener<VType, Object>) (PVEvent event, PV<VType, Object> pv1) -> {
                    // Do nothing
                })
                .start();
        assertThat(pv.isWriteConnected(), equalTo(false));
        pv.write("Value");
    }

    @Test(expected = RuntimeException.class)
    public void writeDisconnectedChannel() {
        PV<VType, Object> pv = gpClient.readAndWrite("loc://a")
                .addListener((PVListener<VType, Object>) (PVEvent event, PV<VType, Object> pv1) -> {
                    // Do nothing
                })
                .start();
        assertThat(pv.isWriteConnected(), equalTo(false));
        pv.write("Value");
    }

    @Test(expected = RuntimeException.class)
    public void writeDisconnectedChannelAsynch() {
        PV<VType, Object> pv = gpClient.readAndWrite("loc://a")
                .addListener((PVEvent event, PV<VType, Object> pv1) -> {
                    // Do nothing
                })
                .start();
        assertThat(pv.isWriteConnected(), equalTo(false));
        pv.write("Value", (PVEvent event, PVWriter<Object> pv1) -> {
            // Do nothing
        });
    }

    @Test(expected = RuntimeException.class)
    public void writeDisconnectedChannelSynch() {
        PV<VType, Object> pv = gpClient.readAndWrite("loc://a")
                .addListener((PVListener<VType, Object>) (PVEvent event, PV<VType, Object> pv1) -> {
                    // Do nothing
                })
                .start();
        assertThat(pv.isWriteConnected(), equalTo(false));
        pv.writeAndWait("Value");
    }

    @Test
    public void writeChannelSynch() throws InterruptedException {
        CountDownLatch connectionLatch = new CountDownLatch(1);
        CountDownLatch valueLatch = new CountDownLatch(1);
        PV<VType, Object> pv = gpClient.readAndWrite("loc://a")
                .addListener((PVListener<VType, Object>) (PVEvent event, PV<VType, Object> pv1) -> {
                    if (event.isType(PVEvent.Type.WRITE_CONNECTION)) {
                        connectionLatch.countDown();
                    }
                    if (event.isType(PVEvent.Type.VALUE)) {
                        valueLatch.countDown();
                    }
                })
                .start();
        assertThat(pv.isWriteConnected(), equalTo(false));
        awaitTimeout(connectionLatch, Duration.ofSeconds(1));
        assertThat(pv.getValue(), equalTo(null));
        System.out.println("Writing");
        pv.writeAndWait("Value");
        awaitTimeout(valueLatch, Duration.ofSeconds(1));
        assertThat(pv.getValue(), instanceOf(VString.class));
        assertThat(((VString) pv.getValue()).getValue(), equalTo("Value"));
    }
    
    public static void awaitTimeout(CountDownLatch latch, Duration duration) throws InterruptedException {
        if (!latch.await(duration.toMillis(), TimeUnit.MILLISECONDS)) {
            fail("Latch didn't count to zero");
        }
    }
}
