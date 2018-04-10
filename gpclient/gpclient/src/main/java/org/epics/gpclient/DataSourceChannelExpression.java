/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient;

import org.epics.gpclient.datasource.DataSource;
import org.epics.gpclient.datasource.ReadSubscription;

/**
 * A {@link DataChannel} that connects to a {@link DataSource}.
 *
 * @author carcassi
 */
class DataSourceChannelExpression<T, R> extends ChannelExpression<T> {
    
    private final String channelName;

    /**
     * A new datasource channel with the given name.
     * 
     * @param channelName the name of the channel
     * @param readType the type to read
     */
    DataSourceChannelExpression(String channelName, ReadCollector<R, T> readCollector) {
        super(readCollector);
        this.channelName = channelName;
    }

    @Override
    protected void connectRead(PVDirector pvDirector) {
        pvDirector.getDataSource().startRead(new ReadSubscription(channelName, getReadCollector()));
    }

    @Override
    protected void disconnectRead(PVDirector pvDirector) {
        pvDirector.getDataSource().stopRead(new ReadSubscription(channelName, getReadCollector()));
    }
    
}