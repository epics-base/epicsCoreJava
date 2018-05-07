/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.sample;

import java.time.Duration;
import org.epics.gpclient.PVEventRecorder;
import static org.epics.gpclient.PVEventRecorder.*;
import static org.epics.gpclient.PVEvent.Type.*;
import org.epics.gpclient.PVReader;
import org.epics.gpclient.TimeoutException;
import org.epics.vtype.VDouble;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.epics.vtype.VString;
import org.epics.vtype.VType;

/**
 *
 * @author carcassi
 */
public class ReadTest extends BlackBoxTestBase {

    @Test
    public void readConstant() throws InterruptedException {
        PVEventRecorder recorder = new PVEventRecorder();
        PVReader<VType> pv = gpClient.read("sim://const(4)")
                .addListener(recorder)
                .start();
        assertThat(pv.isConnected(), equalTo(false));
        assertThat(pv.getValue(), nullValue());
        recorder.wait(1000, forEventOfType(READ_CONNECTION));
        recorder.wait(100, forEventOfType(VALUE));
        assertThat(pv.isConnected(), equalTo(true));
        assertThat(pv.getValue(), instanceOf(VDouble.class));
        assertThat(((VDouble) pv.getValue()).getValue(), equalTo(4.0));
    }

    @Test
    public void readTimeout() throws InterruptedException {
        PVEventRecorder recorder = new PVEventRecorder();
        PVReader<VType> pv = gpClient.read("sim://delayedConnectionChannel(2, \"Connected\")")
                .addListener(recorder)
                .connectionTimeout(Duration.ofMillis(500))
                .start();
        assertThat(pv.isConnected(), equalTo(false));
        assertThat(pv.getValue(), nullValue());
        recorder.wait(2000, forEventOfType(EXCEPTION));
        assertThat(recorder.getEvents().get(0).getException(), instanceOf(TimeoutException.class));
        assertThat(pv.isConnected(), equalTo(false));
        assertThat(pv.getValue(), nullValue());
        recorder.wait(2000, forEventOfType(READ_CONNECTION));
        assertThat(pv.isConnected(), equalTo(true));
        recorder.wait(100, forEventOfType(VALUE));
        assertThat(pv.getValue(), instanceOf(VString.class));
        assertThat(((VString) pv.getValue()).getValue(), equalTo("Connected"));
    }
}
