/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource;

import org.epics.gpclient.WriteCollector;

/**
 * The information for a write subscription to a datasource channel. It consists
 * of a channel name, which identifies the channel, and a collector, which
 * is used to update the data.
 *
 * @author carcassi
 */
public class WriteSubscription {
    private final String channelName;
    private final WriteCollector writeCollector;

    /**
     * Creates a new write subscription for the given channel and collector.
     *
     * @param channelName the name of the channel to connect to
     * @param writeCollector the collector for the write operations
     */
    public WriteSubscription(String channelName, WriteCollector writeCollector) {
        this.channelName = channelName;
        this.writeCollector = writeCollector;
    }

    /**
     * The name of the channel to write to.
     *
     * @return the channel name
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * The collector to be connected to the channel.
     *
     * @return the write collector
     */
    public WriteCollector getCollector() {
        return writeCollector;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 71 * hash + (this.channelName != null ? this.channelName.hashCode() : 0);
        hash = 71 * hash + (this.writeCollector != null ? this.writeCollector.hashCode() : 0);
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
        final WriteSubscription other = (WriteSubscription) obj;
        if ((this.channelName == null) ? (other.channelName != null) : !this.channelName.equals(other.channelName)) {
            return false;
        }
        if (this.writeCollector != other.writeCollector && (this.writeCollector == null || !this.writeCollector.equals(other.writeCollector))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[WriteSubscription for " + channelName + ": " + writeCollector + "]";
    }

}
