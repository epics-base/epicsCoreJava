/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.sample;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.epics.gpclient.GPClientConfiguration;
import org.epics.gpclient.GPClientInstance;
import org.epics.gpclient.PV;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVListener;
import org.epics.gpclient.PVReader;
import org.epics.gpclient.PVWriter;
import org.epics.gpclient.TimeoutException;
import org.epics.gpclient.datasource.DataSourceProvider;
import org.epics.vtype.VDouble;
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
public class ReadTest extends BlackBoxTestBase {

    @Test
    public void readConstant() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        PVReader<VType> pv = gpClient.read("sim://const(4)")
                .addListener((PVEvent event, PVReader<VType> pv1) -> {
                    if (event.isType(PVEvent.Type.READ_CONNECTION)) {
                        latch.countDown();
                    }
                    if (event.isType(PVEvent.Type.VALUE)) {
                        latch.countDown();
                    }
                })
                .start();
        assertThat(pv.isConnected(), equalTo(false));
        assertThat(pv.getValue(), nullValue());
        awaitTimeout(latch, Duration.ofSeconds(1));
        assertThat(pv.isConnected(), equalTo(true));
        assertThat(pv.getValue(), instanceOf(VDouble.class));
        assertThat(((VDouble) pv.getValue()).getValue(), equalTo(4.0));
    }

    @Test
    public void readTimeout() throws InterruptedException {
        CountDownLatch timeoutLatch = new CountDownLatch(1);
        CountDownLatch connectionLatch = new CountDownLatch(1);
        CountDownLatch valueLatch = new CountDownLatch(1);
        PVReader<VType> pv = gpClient.read("sim://delayedConnectionChannel(2, \"Connected\")")
                .addListener((PVEvent event, PVReader<VType> pv1) -> {
                    if (event.isType(PVEvent.Type.EXCEPTION)) {
                        if (event.getException() instanceof TimeoutException) {
                            timeoutLatch.countDown();
                        }
                    }
                    if (event.isType(PVEvent.Type.READ_CONNECTION)) {
                        connectionLatch.countDown();
                    }
                    if (event.isType(PVEvent.Type.VALUE)) {
                        valueLatch.countDown();
                    }
                })
                .connectionTimeout(Duration.ofMillis(500))
                .start();
        assertThat(pv.isConnected(), equalTo(false));
        assertThat(pv.getValue(), nullValue());
        awaitTimeout(timeoutLatch, Duration.ofSeconds(2));
        assertThat(pv.isConnected(), equalTo(false));
        assertThat(pv.getValue(), nullValue());
        awaitTimeout(connectionLatch, Duration.ofSeconds(2));
        assertThat(pv.isConnected(), equalTo(true));
        awaitTimeout(valueLatch, Duration.ofSeconds(2));
        assertThat(pv.getValue(), instanceOf(VString.class));
        assertThat(((VString) pv.getValue()).getValue(), equalTo("Connected"));
    }
}
