/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;

import org.epics.pvData.misc.BitSet;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;

/**
 *  Requester for ChannelMonitor.
 * @author mrk
 *
 */
public interface MonitorRequester extends Requester{
    /**
     * The client and server have both completed the createMonitor request.
     * @param channelMonitor The channelMonitor interface or null if the request failed.
     */
    void monitorConnect(Monitor monitor);
    /**
     * The get request is done. The requester must call Monitor.poll to get data.
     * @param monitor
     */
    void getDone(Monitor monitor);
    /**
     * A monitor event has occurred. The requester must call Monitor.poll to get data.
     */
    void monitorEvent(Monitor monitor);
    /**
     * unlisten 
     */
    void unlisten();
}
