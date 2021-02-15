/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import org.epics.gpclient.datasource.DataSource;
import org.epics.gpclient.datasource.ReadSubscription;
import org.epics.gpclient.datasource.WriteSubscription;

/**
 * A {@link ChannelExpression} that connects to a {@link DataSource}.
 *
 * @author carcassi
 */
class DataSourceChannelExpression<R, C ,W> extends ChannelExpression<R, W> {

    private final String channelName;

    /**
     * A new datasource channel with the given name.
     *
     * @param channelName the name of the channel
     */
    DataSourceChannelExpression(String channelName, ReadCollector<C, R> readCollector, WriteCollector<W> writeCollector) {
        super(readCollector, writeCollector);
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

    @Override
    protected void connectWrite(PVDirector pvDirector) {
        pvDirector.getDataSource().startWrite(new WriteSubscription(channelName, getWriteCollector()));
    }

    @Override
    protected void disconnectWrite(PVDirector pvDirector) {
        pvDirector.getDataSource().stopWrite(new WriteSubscription(channelName, getWriteCollector()));
    }

}
