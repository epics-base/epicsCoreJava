/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import org.epics.pvData.pv.Requester;

/**
 *  Requester for ChannelMonitor.
 * @author mrk
 *
 */
public interface ChannelMonitorRequester extends Requester{
    /**
     * The client and server have both completed the createMonitor request.
     * @param channelMonitor The channelMonitor interface or null if the request failed.
     */
    void channelMonitorConnect(ChannelMonitor channelMonitor);
    /**
     * A monitor event has occurrence.
     */
    void monitorEvent();
    /**
     * unlisten 
     */
    void unlisten();
}
