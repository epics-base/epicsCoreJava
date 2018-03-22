/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient;

import java.time.Duration;
import java.util.concurrent.Executors;
import org.epics.vtype.VType;

/**
 *
 * @author carcassi
 */
public class GPClient {
    
    static {
        gpClient = new GPClientConfiguration().defaultMaxRate(Duration.ofMillis(1000))
                .dataProcessingThreadPool(Executors.newScheduledThreadPool(Math.max(1, Runtime.getRuntime().availableProcessors() - 1),
                org.epics.util.concurrent.Executors.namedPool("PVMgr Worker "))).build();
    }
    
    private static final GPClientInstance gpClient;
    
    public static PVReaderConfiguration<VType> read(String channelName) {
        return gpClient.read(channelName);
    }
}
