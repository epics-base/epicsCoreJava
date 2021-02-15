/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.sim;

import org.epics.gpclient.ProbeCollector;
import org.epics.gpclient.datasource.ReadSubscription;

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
        sim.startRead(new ReadSubscription("gaussianWaveform()", probe.getReadCollector()));
        Thread.sleep(5000);
        sim.stopRead(new ReadSubscription("gaussianWaveform()", probe.getReadCollector()));
        sim.close();

    }

}
