/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource.sim;

import org.epics.gpclient.datasource.ReadRecipe;
import org.epics.gpclient.datasource.ReadRecipeBuilder;
import org.epics.gpclient.expression.ProbeCollector;
import org.epics.gpclient.expression.ReadEvent;
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
        ReadRecipe recipe = new ReadRecipeBuilder().addChannel("noise(-5,5,0.1)", probe.getCollector()).build();
        sim.connectRead(recipe);
        probe.wait(10000, ProbeCollector.forEventCount(5));
        sim.disconnectRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(probe.getEvents().size(), equalTo(5));
        assertThat(probe.getEvents().get(0), equalTo(ReadEvent.connectionEvent()));
        assertThat(probe.getEvents().get(1), equalTo(ReadEvent.valueEvent()));
        assertThat(probe.getEvents().get(2), equalTo(ReadEvent.valueEvent()));
        assertThat(probe.getEvents().get(3), equalTo(ReadEvent.valueEvent()));
        assertThat(probe.getEvents().get(4), equalTo(ReadEvent.valueEvent()));
    }
    
    @Test
    public void delayedConnection() throws InterruptedException {
        SimulationDataSource sim = new SimulationDataSource();
        ProbeCollector probe = ProbeCollector.create();
        ReadRecipe recipe = new ReadRecipeBuilder().addChannel("delayedConnectionChannel(1.0,\"connected\")", probe.getCollector()).build();
        sim.connectRead(recipe);
        probe.dontExpect(800, ProbeCollector.forAnEvent());
        probe.wait(10000, ProbeCollector.forEventCount(2));
        sim.disconnectRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(probe.getEvents().size(), equalTo(2));
        assertThat(probe.getEvents().get(0), equalTo(ReadEvent.connectionEvent()));
        assertThat(probe.getEvents().get(1), equalTo(ReadEvent.valueEvent()));
        assertThat(probe.getValue(), instanceOf(VString.class));
        assertThat(((VString) probe.getValue()).getValue(), equalTo("connected"));
    }
    
    @Test
    public void const1() throws InterruptedException {
        SimulationDataSource sim = new SimulationDataSource();
        ProbeCollector probe = ProbeCollector.create();
        ReadRecipe recipe = new ReadRecipeBuilder().addChannel("const(3.14)", probe.getCollector()).build();
        sim.connectRead(recipe);
        probe.wait(10000, ProbeCollector.forEventCount(2));
        sim.disconnectRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(probe.getEvents().size(), equalTo(2));
        assertThat(probe.getEvents().get(0), equalTo(ReadEvent.connectionEvent()));
        assertThat(probe.getEvents().get(1), equalTo(ReadEvent.valueEvent()));
        assertThat(probe.getValue(), instanceOf(VDouble.class));
        assertThat(((VDouble) probe.getValue()).getValue(), equalTo(3.14));
    }
    
    @Test
    public void const2() throws InterruptedException {
        SimulationDataSource sim = new SimulationDataSource();
        ProbeCollector probe = ProbeCollector.create();
        ReadRecipe recipe = new ReadRecipeBuilder().addChannel("const(\"testing\")", probe.getCollector()).build();
        sim.connectRead(recipe);
        probe.wait(10000, ProbeCollector.forEventCount(2));
        sim.disconnectRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(probe.getEvents().size(), equalTo(2));
        assertThat(probe.getEvents().get(0), equalTo(ReadEvent.connectionEvent()));
        assertThat(probe.getEvents().get(1), equalTo(ReadEvent.valueEvent()));
        assertThat(probe.getValue(), instanceOf(VString.class));
        assertThat(((VString) probe.getValue()).getValue(), equalTo("testing"));
    }
    
    @Test
    public void const3() throws InterruptedException {
        SimulationDataSource sim = new SimulationDataSource();
        ProbeCollector probe = ProbeCollector.create();
        ReadRecipe recipe = new ReadRecipeBuilder().addChannel("const(1,2,3,4,5)", probe.getCollector()).build();
        sim.connectRead(recipe);
        probe.wait(10000, ProbeCollector.forEventCount(2));
        sim.disconnectRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(probe.getEvents().size(), equalTo(2));
        assertThat(probe.getEvents().get(0), equalTo(ReadEvent.connectionEvent()));
        assertThat(probe.getEvents().get(1), equalTo(ReadEvent.valueEvent()));
        assertThat(probe.getValue(), instanceOf(VDoubleArray.class));
        assertThat(((VDoubleArray) probe.getValue()).getData(), equalTo(ArrayDouble.of(1,2,3,4,5)));
    }
    
}
