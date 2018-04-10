/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

import java.util.function.Supplier;
import org.epics.gpclient.ReadCollector;
import org.epics.gpclient.PVDirector;
import org.epics.gpclient.datasource.ReadSubscription;
import org.epics.gpclient.datasource.DataSource;
import org.epics.gpclient.expression.ReadExpressionImpl;
import org.epics.gpclient.expression.ReadExpressionList;

/**
 * Represents a channel, which can be both read or written.
 *
 * @param <R> type of the read payload
 * @author carcassi
 */
class LatestValueFromChannelExpression<R> extends ReadExpressionImpl<R> {
    
    private final DataChannel<R> dataChannel;
    private final LatestValueCollector<R> readCollector;

    LatestValueFromChannelExpression(DataChannel<R> dataChannel, LatestValueCollector<R> readCollector) {
        super(null, readCollector.getReadFunction());
        this.dataChannel = dataChannel;
        this.readCollector = readCollector;
        dataChannel.setCollector(readCollector);
    }

    @Override
    public void startRead(PVDirector director) {
        director.registerCollector(readCollector);
        dataChannel.startRead(director);
    }

    @Override
    public void stopRead(PVDirector director) {
        director.deregisterCollector(readCollector);
        dataChannel.stopRead(director);
    }
    
}
