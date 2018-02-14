/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource.sim;

import org.epics.gpclient.datasource.ReadRecipe;
import org.epics.gpclient.datasource.ReadRecipeBuilder;
import org.epics.gpclient.expression.ProbeCollector;
import org.epics.gpclient.expression.SourceRateReadEvent;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class SimulationDataSourceTest extends FeatureTestSimFunction {
    
    @Test
    public void values1() throws InterruptedException {
        SimulationDataSource sim = new SimulationDataSource();
        ProbeCollector probe = ProbeCollector.create();
        ReadRecipe recipe = new ReadRecipeBuilder().addChannel("noise(-5,5,0.1)", probe.getCollector()).build();
        sim.connectRead(recipe);
        probe.wait(10000, ProbeCollector.forEventCount(5));
        sim.disconnectRead(recipe);
        sim.close();
        Thread.sleep(300);
        assertThat(probe.getEvents().size(), equalTo(5));
        assertThat(probe.getEvents().get(0), equalTo(new SourceRateReadEvent(null, SourceRateReadEvent.Type.READ_CONNECTION)));
        assertThat(probe.getEvents().get(1), equalTo(new SourceRateReadEvent(null, SourceRateReadEvent.Type.VALUE)));
        assertThat(probe.getEvents().get(2), equalTo(new SourceRateReadEvent(null, SourceRateReadEvent.Type.VALUE)));
        assertThat(probe.getEvents().get(3), equalTo(new SourceRateReadEvent(null, SourceRateReadEvent.Type.VALUE)));
        assertThat(probe.getEvents().get(4), equalTo(new SourceRateReadEvent(null, SourceRateReadEvent.Type.VALUE)));
    }
    
}
