/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca;

import org.epics.pvaccess.client.Channel;
import org.epics.pvaccess.client.ChannelGet;
import org.epics.pvaccess.client.ChannelPut;
import org.epics.pvdata.monitor.Monitor;


/**
 * Channel interface for communicating with V3 IOCs.
 * @author mrk
 *
 */
public interface V3Channel extends Channel
{
    /**
     * Add a channelGet
     * @param channelGet The channelGet to add.
     * @return (false,true) if the channelGet (was not, was) added.
     */
    boolean add(ChannelGet channelGet);
    /**
     * Add a channelPut
     * @param channelPut The channelPut to add.
     * @return (false,true) if the channelPut (was not, was) added.
     */
    boolean add(ChannelPut channelPut);
    /**
     * Add a monitor
     * @param monitor The monitor to add.
     * @return (false,true) if the monitor (was not, was) added.
     */
    boolean add(Monitor monitor);
    /**
     * Remove a ChannelGet
     * @param channelGet The channelGet to remove.
     * @return (false,true) if the channelGet (was not, was) removed.
     */
    boolean remove(ChannelGet channelGet);
    /**
     * Remove a ChannelPut
     * @param channelPut The channelPut to remove.
     * @return (false,true) if the channelPut (was not, was) removed.
     */
    boolean remove(ChannelPut channelPut);
    /**
     * Remove a Monitor
     * @param monitor The monitor to remove.
     * @return (false,true) if the monitor (was not, was) removed.
     */
    boolean remove(Monitor monitor);
    /**
     * Get the JCA Channel.
     * @return The interface.
     */
    gov.aps.jca.Channel getJCAChannel();
}
