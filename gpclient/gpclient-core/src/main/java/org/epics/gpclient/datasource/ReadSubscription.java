/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource;

import org.epics.gpclient.ReadCollector;

/**
 * The information for a read subscription to a datasource channel. It consists
 * of a channel name, which identifies the channel, and a collector, which
 * is used to update the data.
 *
 * @author carcassi
 */
public class ReadSubscription {
    private final String channelName;
    private final ReadCollector<?,?> readCollector;

    /**
     * Creates a new read subscription for the given channel and collector.
     *
     * @param channelName the name of the channel to connect to
     * @param readCollector the collector for the read operations
     */
    public ReadSubscription(String channelName, ReadCollector<?,?> readCollector) {
        this.channelName = channelName;
        this.readCollector = readCollector;
    }

    /**
     * The name of the channel to read from.
     *
     * @return the channel name
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * The collector to be connected to the channel.
     *
     * @return the read collector
     */
    public ReadCollector<?,?> getCollector() {
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
        final ReadSubscription other = (ReadSubscription) obj;
        if (this.readCollector != other.readCollector && (this.readCollector == null || !this.readCollector.equals(other.readCollector))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "[ReadSubscription for " + channelName + ": " + readCollector + "]";
    }

}
