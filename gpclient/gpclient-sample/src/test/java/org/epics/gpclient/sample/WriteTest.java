/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.sample;

import org.epics.gpclient.*;
import org.epics.gpclient.datasource.ReadOnlyChannelException;
import org.epics.vtype.VString;
import org.epics.vtype.VType;
import org.joda.time.Duration;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.epics.gpclient.PVEvent.Type.*;
import static org.epics.gpclient.PVEventRecorder.anEventOfType;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * @author carcassi
 */
public class WriteTest extends BlackBoxTestBase {

    @Test
    public void writeInexistentChannel() {
        PVEventRecorder recorder = new PVEventRecorder();
        PV<VType, Object> pv = gpClient.readAndWrite("none://nothing")
                .addListener(recorder)
                .start();
        recorder.dontExpect(500, anEventOfType(READ_CONNECTION));
        recorder.wait(1000, anEventOfType(EXCEPTION));
        assertThat(pv.isWriteConnected(), equalTo(false));
    }

    @Test
    public void writeReadOnlyChannel() {
        for (int i = 0; i < 100; i++) {
            PVEventRecorder recorder = new PVEventRecorder();
            PV<VType, Object> pv = gpClient.readAndWrite("sim://ramp")
                    .addListener(recorder)
                    .start();
            recorder.wait(1000, anEventOfType(READ_CONNECTION));
            recorder.wait(1000, anEventOfType(EXCEPTION));
            assertThat(pv.isConnected(), equalTo(true));
            assertThat(pv.isWriteConnected(), equalTo(false));
            assertThat(recorder.getEvents().get(recorder.getEvents().size() - 1).getException(), instanceOf(ReadOnlyChannelException.class));
        }
    }

    @Test(expected = RuntimeException.class)
    public void writeDisconnectedChannel() {
        PV<VType, Object> pv = gpClient.readAndWrite("loc://writeDisconnectedChannel")
                .addListener(new PVListener<VType, Object>() {
                    public void pvChanged(PVEvent event, PV<VType, Object> pv) {
                        // Do nothing
                    }
                })
                .start();
//        assertThat(pv.isWriteConnected(), equalTo(false));
        pv.write("Value");
    }

    //    TODO this unit test fails on travis repeatedly, disabling it temporarily
//    @Test(expected = RuntimeException.class)
    public void writeDisconnectedChannelAsynch() {
        PV<VType, Object> pv = gpClient.readAndWrite("loc://writeDisconnectedChannelAsynch")
                .addListener(new PVListener<VType, Object>() {
                    public void pvChanged(PVEvent event, PV<VType, Object> pv) {
                        // Do Nothing
                    }
                })
                .start();
//        assertThat(pv.isWriteConnected(), equalTo(false));
        pv.write("Value", new PVWriterListener<Object>() {
            public void pvChanged(PVEvent event, PVWriter<Object> pvWriter) {
                // Do nothing
            }
        });
    }

    @Test(expected = RuntimeException.class)
    public void writeDisconnectedChannelSynch() {
        PV<VType, Object> pv = gpClient.readAndWrite("loc://writeDisconnectedChannelSynch")
                .addListener(new PVListener<VType, Object>() {
                    public void pvChanged(PVEvent event, PV<VType, Object> pv) {
                        // Do Nothing
                    }
                })
                .start();
//        assertThat(pv.isWriteConnected(), equalTo(false));
        pv.writeAndWait("Value");
    }

    @Test
    public void writeChannelSynch() throws InterruptedException {
        PVEventRecorder recorder = new PVEventRecorder();
        PV<VType, Object> pv = gpClient.readAndWrite("loc://writeChannelSynch")
                .addListener(recorder)
                .start();
        assertThat(pv.isWriteConnected(), equalTo(false));
        recorder.wait(1000, anEventOfType(WRITE_CONNECTION));
        assertThat(pv.getValue(), equalTo(null));
        pv.writeAndWait("Value");
        recorder.wait(1000, anEventOfType(VALUE));
        assertThat(pv.getValue(), instanceOf(VString.class));
        assertThat(((VString) pv.getValue()).getValue(), equalTo("Value"));
    }

    @Test
    public void writeChannelAsynch() throws InterruptedException {
        PVEventRecorder recorder = new PVEventRecorder();
        PV<VType, Object> pv = gpClient.readAndWrite("loc://writeChannelAsynch")
                .addListener(recorder)
                .start();
        assertThat(pv.isWriteConnected(), equalTo(false));
        recorder.wait(1000, anEventOfType(WRITE_CONNECTION));
        assertThat(pv.getValue(), equalTo(null));
        pv.write("Value");
        recorder.wait(1000, anEventOfType(VALUE));
        recorder.wait(1000, anEventOfType(WRITE_SUCCEEDED));
        assertThat(pv.getValue(), instanceOf(VString.class));
        assertThat(((VString) pv.getValue()).getValue(), equalTo("Value"));
    }

    @Test
    public void writeChannelAsynchDirect() throws InterruptedException {
        PVEventRecorder recorder = new PVEventRecorder();
        PV<VType, Object> pv = gpClient.readAndWrite("loc://writeChannelAsynchDirect")
                .addListener(recorder)
                .start();
        assertThat(pv.isWriteConnected(), equalTo(false));
        recorder.wait(1000, anEventOfType(WRITE_CONNECTION));
        assertThat(pv.getValue(), equalTo(null));
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<PVEvent> pvEvent = new AtomicReference<PVEvent>();
        pv.write("Value", new PVWriterListener<Object>() {
            public void pvChanged(PVEvent event, PVWriter<Object> pvWriter) {
                pvEvent.set(event);
                latch.countDown();
            }
        });
        awaitTimeout(latch, Duration.millis(1000));
        recorder.wait(1000, anEventOfType(VALUE));
        assertThat(pv.getValue(), instanceOf(VString.class));
        assertThat(((VString) pv.getValue()).getValue(), equalTo("Value"));
        recorder.hasNotReceived(anEventOfType(WRITE_SUCCEEDED));
    }
}
