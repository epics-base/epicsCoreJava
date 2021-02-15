/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource;

import org.epics.gpclient.ReadCollector;
import org.epics.gpclient.WriteCollector;

import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Manages the connection of a channel for a data source.
 *
 * @author carcassi
 */
public abstract class ChannelHandler {

    private static final Logger log = Logger.getLogger(ChannelHandler.class.getName());
    private final String channelName;

    /**
     * Creates a new channel handler.
     *
     * @param channelName the name of the channel this handler will be responsible of
     */
    public ChannelHandler(String channelName) {
        if (channelName == null) {
            throw new NullPointerException("Channel name cannot be null");
        }
        this.channelName = channelName;
    }

    /**
     * Returns extra information about the channel, typically
     * useful for debugging.
     *
     * @return a property map
     */
    public Map<String, Object> getProperties() {
        return Collections.emptyMap();
    }

    /**
     * Returns the name of the channel.
     *
     * @return the channel name; can't be null
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * Returns how many readers or writers are open on
     * this channel.
     *
     * @return the number of open readers and writers
     */
    public abstract int getUsageCounter();

    /**
     * Returns how many readers are open on this channel.
     *
     * @return the number of open readers
     */
    public abstract int getReadUsageCounter();

    /**
     * Returns how many writers are open on this channel.
     *
     * @return the number of open writers
     */
    public abstract int getWriteUsageCounter();

    /**
     * Starts sending read notification to the given collector.
     *
     * @param collector the data collector
     */
    protected abstract void addReader(ReadCollector collector);

    /**
     * Stops sending read notification to the given collector.
     *
     * @param collector the data collector
     */
    protected abstract void removeReader(ReadCollector collector);

    /**
     * Starts sending/receiving write notification to the given collector.
     *
     * @param collector the data collector
     */
    protected abstract void addWriter(WriteCollector collector);

    /**
     * Stops sending/receiving write notification to the given collector.
     *
     * @param collector the data collector
     */
    protected abstract void removeWriter(WriteCollector collector);

    /**
     * Returns true if it is connected.
     *
     * @return true if underlying channel is connected
     */
    public abstract boolean isConnected();
}
