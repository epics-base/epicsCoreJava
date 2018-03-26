/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.datasource;

import org.epics.gpclient.ReadCollector;

/**
 * The recipe for the read connection of a single channel.
 * <p>
 * The recipe is made up of two parts to make it easy to forward
 * the request to a channel with a different name.
 *
 * @author carcassi
 */
public class ChannelReadRecipe {
    private final String channelName;
    private final ReadCollector<?,?> readCollector;

    /**
     * Creates a new read recipe for the given channel.
     *
     * @param channelName the name of the channel to connect to
     * @param readCollector the collector for the read operations
     */
    public ChannelReadRecipe(String channelName, ReadCollector<?,?> readCollector) {
        this.channelName = channelName;
        this.readCollector = readCollector;
    }
    
    /**
     * The name of the channel to read.
     *
     * @return the channel name
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * The collector associated with the channel.
     *
     * @return the read subscription parameters
     */
    public ReadCollector<?,?> getReadSubscription() {
        return readCollector;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + (this.channelName != null ? this.channelName.hashCode() : 0);
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
        final ChannelReadRecipe other = (ChannelReadRecipe) obj;
        if (this.readCollector != other.readCollector && (this.readCollector == null || !this.readCollector.equals(other.readCollector))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[ChannelReadRecipe for " + channelName + ": " + readCollector + "]";
    }
    
}
