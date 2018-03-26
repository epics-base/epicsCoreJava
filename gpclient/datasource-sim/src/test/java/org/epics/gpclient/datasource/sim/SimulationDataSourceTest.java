/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource.sim;

import org.epics.gpclient.ProbeCollector;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVEventRecorder;
import org.epics.gpclient.datasource.ChannelReadRecipe;
import org.epics.util.array.ArrayDouble;
import org.epics.vtype.VDouble;
import org.epics.vtype.VDoubleArray;
import org.epics.vtype.VString;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

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
        ChannelReadRecipe recipe = new ChannelReadRecipe("noise(-5,5,0.1)", probe.getCollector());
        sim.connectRead(recipe);
        recorder.wait(10000, PVEventRecorder.forEventCount(5));
        sim.disconnectRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(recorder.getEvents().size(), equalTo(5));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.connectionEvent()));
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
        ChannelReadRecipe recipe = new ChannelReadRecipe("delayedConnectionChannel(1.0,\"connected\")", probe.getCollector());
        sim.connectRead(recipe);
        recorder.dontExpect(800, PVEventRecorder.forAnEvent());
        recorder.wait(10000, PVEventRecorder.forEventCount(2));
        sim.disconnectRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(recorder.getEvents().size(), equalTo(2));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.connectionEvent()));
        assertThat(recorder.getEvents().get(1), equalTo(PVEvent.valueEvent()));
        assertThat(probe.getValue(), instanceOf(VString.class));
        assertThat(((VString) probe.getValue()).getValue(), equalTo("connected"));
    }
    
    @Test
    public void const1() throws InterruptedException {
        SimulationDataSource sim = new SimulationDataSource();
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        ChannelReadRecipe recipe = new ChannelReadRecipe("const(3.14)", probe.getCollector());
        sim.connectRead(recipe);
        recorder.wait(10000, PVEventRecorder.forEventCount(2));
        sim.disconnectRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(recorder.getEvents().size(), equalTo(2));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.connectionEvent()));
        assertThat(recorder.getEvents().get(1), equalTo(PVEvent.valueEvent()));
        assertThat(probe.getValue(), instanceOf(VDouble.class));
        assertThat(((VDouble) probe.getValue()).getValue(), equalTo(3.14));
    }
    
    @Test
    public void const2() throws InterruptedException {
        SimulationDataSource sim = new SimulationDataSource();
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        ChannelReadRecipe recipe = new ChannelReadRecipe("const(\"testing\")", probe.getCollector());
        sim.connectRead(recipe);
        recorder.wait(10000, PVEventRecorder.forEventCount(2));
        sim.disconnectRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(recorder.getEvents().size(), equalTo(2));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.connectionEvent()));
        assertThat(recorder.getEvents().get(1), equalTo(PVEvent.valueEvent()));
        assertThat(probe.getValue(), instanceOf(VString.class));
        assertThat(((VString) probe.getValue()).getValue(), equalTo("testing"));
    }
    
    @Test
    public void const3() throws InterruptedException {
        SimulationDataSource sim = new SimulationDataSource();
        ProbeCollector probe = ProbeCollector.create();
        PVEventRecorder recorder = probe.getRecorder();
        ChannelReadRecipe recipe = new ChannelReadRecipe("const(1,2,3,4,5)", probe.getCollector());
        sim.connectRead(recipe);
        recorder.wait(10000, PVEventRecorder.forEventCount(2));
        sim.disconnectRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(recorder.getEvents().size(), equalTo(2));
        assertThat(recorder.getEvents().get(0), equalTo(PVEvent.connectionEvent()));
        assertThat(recorder.getEvents().get(1), equalTo(PVEvent.valueEvent()));
        assertThat(probe.getValue(), instanceOf(VDoubleArray.class));
        assertThat(((VDoubleArray) probe.getValue()).getData(), equalTo(ArrayDouble.of(1,2,3,4,5)));
    }
    
}
