/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource;

import org.epics.gpclient.WriteCollector;

/**
 * Groups all the parameters required to add a writer to a ChannelHandler.
 * <p>
 * All parameters where grouped in this class so that if something needs to be
 * added or removed the impact is lessened. The class is immutable so that
 * the ChannelHandler can cache it for reference.
 *
 * @author carcassi
 */
public class ChannelHandlerWriteSubscription {

    public ChannelHandlerWriteSubscription(WriteCollector<?> collector) {
        if (collector == null) {
            throw new NullPointerException("collector can't be null");
        }
        this.collector = collector;
    }
    
    private final WriteCollector<?> collector;

    public WriteCollector<?> getWriteCollector() {
        return collector;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + (this.collector != null ? this.collector.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChannelHandlerWriteSubscription other = (ChannelHandlerWriteSubscription) obj;
        if (!this.collector.equals(other.collector)) {
            return false;
        }
        return true;
    }
    
}
