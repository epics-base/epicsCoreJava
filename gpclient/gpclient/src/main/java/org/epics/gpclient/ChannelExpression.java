/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

import org.epics.gpclient.expression.ReadExpressionImpl;

/**
 * Represents a channel, which can be both read or written.
 *
 * @param <R> type of the read payload
 * @author carcassi
 */
public abstract class ChannelExpression<R> extends ReadExpressionImpl<R> {
    
    private final ReadCollector<?, R> readCollector;

    protected ReadCollector<?, R> getReadCollector() {
        return readCollector;
    }

    ChannelExpression(ReadCollector<?, R> readCollector) {
        super(null, readCollector.getReadFunction());
        this.readCollector = readCollector;
    }

    @Override
    public final void startRead(PVDirector director) {
        director.registerCollector(readCollector);
        connectRead(director);
    }
    
    protected abstract void connectRead(PVDirector director);

    @Override
    public final void stopRead(PVDirector director) {
        director.deregisterCollector(readCollector);
        disconnectRead(director);
    }
    
    protected abstract void disconnectRead(PVDirector director);
    
}
