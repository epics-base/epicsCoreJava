/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient;

import org.epics.gpclient.expression.ChannelExpression;
import org.epics.gpclient.expression.LatestValueCollector;
import org.epics.vtype.VType;

/**
 *
 * @author carcassi
 */
public class GPClient {
    
    public static PVReaderConfiguration<VType> read(String channelName) {
        return new PVConfiguration<>(new ChannelExpression<>(channelName, new LatestValueCollector<>(VType.class)));
    }
}
