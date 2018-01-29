/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource;

import org.epics.gpclient.expression.ReadCollector;

/**
 * Groups all the parameters required to add a reader to a ChannelHandler.
 * <p>
 * All parameters where grouped in this class so that if something needs to be
 * added or removed the impact is lessened. The class is immutable so that
 * the ChannelHandler can cache it for reference.
 *
 * @author carcassi
 */
public class ChannelHandlerReadSubscription {

    public ChannelHandlerReadSubscription(ReadCollector<?, ?> collector) {
        if (collector == null) {
            throw new NullPointerException("collector can't be null");
        }
        this.collector = collector;
    }
    
    private final ReadCollector<?, ?> collector;

    /**
     * The cache where to write the value.
     * 
     * @return never null
     */
    public ReadCollector<?, ?> getCollector() {
        return collector;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + (this.collector != null ? this.collector.hashCode() : 0);
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
        final ChannelHandlerReadSubscription other = (ChannelHandlerReadSubscription) obj;
        if (!collector.equals(other.collector)) {
            return false;
        }
        return true;
    }
    
}
