/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.gpclient.datasource.ReadRecipe;
import org.epics.gpclient.datasource.ReadRecipeBuilder;
import org.epics.gpclient.expression.ProbeCollector;

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
        ReadRecipe recipe = new ReadRecipeBuilder().addChannel("ramp()", probe.getCollector()).build();
        sim.connectRead(recipe);
        Thread.sleep(5000);
        sim.disconnectRead(recipe);
        sim.close();
        
    }
    
}
