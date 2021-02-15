/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVEventRecorder;
import org.epics.gpclient.ProbeCollector;
import org.epics.gpclient.datasource.ReadSubscription;
import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ListDouble;
import org.epics.vtype.VDouble;
import org.epics.vtype.VDoubleArray;
import org.epics.vtype.VString;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 *
 * @author carcassi
 */
public class SimulationDataSourceTest extends FeatureTestSimFunction {

    @Test
    public void noise1() throws InterruptedException {
        SimulationDataSource sim = new SimulationDataSource();
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        ReadSubscription recipe = new ReadSubscription("noise(-5,5,0.1)", probe.getReadCollector());
        sim.startRead(recipe);
        recorder.wait(10000, PVEventRecorder.forEventCount(5));
        sim.stopRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(recorder.getEvents().size(), equalTo(5));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.readConnectionEvent()));
        assertThat(recorder.getEvents().get(1), equalTo(PVEvent.valueEvent()));
        assertThat(recorder.getEvents().get(2), equalTo(PVEvent.valueEvent()));
        assertThat(recorder.getEvents().get(3), equalTo(PVEvent.valueEvent()));
        assertThat(recorder.getEvents().get(4), equalTo(PVEvent.valueEvent()));
    }

    @Test
    public void delayedConnection() throws InterruptedException {
        SimulationDataSource sim = new SimulationDataSource();
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        ReadSubscription recipe = new ReadSubscription("delayedConnectionChannel(1.0,\"connected\")", probe.getReadCollector());
        sim.startRead(recipe);
        recorder.dontExpect(800, PVEventRecorder.forAnEvent());
        recorder.wait(10000, PVEventRecorder.forEventCount(2));
        sim.stopRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(recorder.getEvents().size(), equalTo(2));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.readConnectionEvent()));
        assertThat(recorder.getEvents().get(1), equalTo(PVEvent.valueEvent()));
        assertThat(probe.getValue(), instanceOf(VString.class));
        assertThat(((VString) probe.getValue()).getValue(), equalTo("connected"));
    }

    @Test
    public void const1() throws InterruptedException {
        SimulationDataSource sim = new SimulationDataSource();
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        ReadSubscription recipe = new ReadSubscription("const(3.14)", probe.getReadCollector());
        sim.startRead(recipe);
        recorder.wait(10000, PVEventRecorder.forEventCount(2));
        sim.stopRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(recorder.getEvents().size(), equalTo(2));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.readConnectionEvent()));
        assertThat(recorder.getEvents().get(1), equalTo(PVEvent.valueEvent()));
        assertThat(probe.getValue(), instanceOf(VDouble.class));
        assertThat(((VDouble) probe.getValue()).getValue(), equalTo(3.14));
    }

    @Test
    public void const2() throws InterruptedException {
        SimulationDataSource sim = new SimulationDataSource();
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        ReadSubscription recipe = new ReadSubscription("const(\"testing\")", probe.getReadCollector());
        sim.startRead(recipe);
        recorder.wait(10000, PVEventRecorder.forEventCount(2));
        sim.stopRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(recorder.getEvents().size(), equalTo(2));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.readConnectionEvent()));
        assertThat(recorder.getEvents().get(1), equalTo(PVEvent.valueEvent()));
        assertThat(probe.getValue(), instanceOf(VString.class));
        assertThat(((VString) probe.getValue()).getValue(), equalTo("testing"));
    }

    @Test
    public void const3() throws InterruptedException {
        SimulationDataSource sim = new SimulationDataSource();
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        ReadSubscription recipe = new ReadSubscription("const(1,2,3,4,5)", probe.getReadCollector());
        sim.startRead(recipe);
        recorder.wait(10000, PVEventRecorder.forEventCount(2));
        sim.stopRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(recorder.getEvents().size(), equalTo(2));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.readConnectionEvent()));
        assertThat(recorder.getEvents().get(1), equalTo(PVEvent.valueEvent()));
        assertThat(probe.getValue(), instanceOf(VDoubleArray.class));
        assertThat(((VDoubleArray) probe.getValue()).getData(), Matchers.<ListDouble>equalTo(ArrayDouble.of(1,2,3,4,5)));
    }

}
