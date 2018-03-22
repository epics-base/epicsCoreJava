/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient;

import java.util.concurrent.ScheduledExecutorService;
import org.epics.gpclient.expression.ChannelExpression;
import org.epics.gpclient.expression.LatestValueCollector;
import org.epics.vtype.VType;

/**
 *
 * @author carcassi
 */
public class GPClientInstance {
    
    final GPClientConfiguration config;

    GPClientInstance(GPClientConfiguration config) {
        this.config = config;
    }
    
    public PVReaderConfiguration<VType> read(String channelName) {
        return new PVConfiguration<>(config, new ChannelExpression<>(channelName, new LatestValueCollector<>(VType.class)));
    }
}
