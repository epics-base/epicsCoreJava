/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient.sample;

import java.time.Duration;
import org.epics.gpclient.GPClient;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVReader;
import org.epics.gpclient.PVReaderListener;
import org.epics.gpclient.datasource.sim.SimulationDataSource;
import org.epics.util.concurrent.Executors;
import org.epics.vtype.VType;

/**
 *
 * @author carcassi
 */
public class BasicExamples {
    
    public static void b1_readLatestValue() throws Exception {
        SimulationDataSource sim = new SimulationDataSource();
        PVReader<VType> pv = GPClient.read("sim://noise")
                .addListener((PVEvent event, PVReader<VType> pvReader) -> {
                    System.out.println(event + " " + pvReader.isConnected() + " " + pvReader.getValue());
                })
                .notifyOn(Executors.localThread())
                .maxRate(Duration.ofMillis(50))
                .start();
        
        Thread.sleep(2000);
        
        pv.close();
    }
    
    public static void main(String[] args) throws Exception {
        b1_readLatestValue();
    }
}
