
/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.gpclient.GPClientConfiguration;
import org.epics.gpclient.GPClientInstance;
import org.epics.gpclient.PV;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVListener;
import org.epics.gpclient.datasource.DataSourceProvider;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.epics.util.concurrent.Executors;
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
}
