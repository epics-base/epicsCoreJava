/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.gpclient.ProbeCollector;
import org.epics.gpclient.datasource.ChannelReadRecipe;

/**
 *
 * @author carcassi
 */
public class DebugSimChannel {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        SimulationDataSource sim = new SimulationDataSource();
        ProbeCollector probe = new ProbeCollector(Object.class, System.out);
        sim.connectRead(new ChannelReadRecipe("gaussianWaveform()", probe.getCollector()));
        Thread.sleep(5000);
        sim.disconnectRead(new ChannelReadRecipe("gaussianWaveform()", probe.getCollector()));
        sim.close();
        
    }
    
}
