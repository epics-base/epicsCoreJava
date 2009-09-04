/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;

import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Status;
import org.epics.pvData.pv.Structure;

/**
 *  Requester for ChannelMonitor.
 * @author mrk
 *
 */
public interface MonitorRequester extends Requester{
    /**
     * The client and server have both completed the createMonitor request.
     * @param status Completion status.
     * @param monitor The monitor
     * @param structure The structure defining the data.
     */
    void monitorConnect(Status status, Monitor monitor, Structure structure);
    /**
     * A monitor event has occurred. The requester must call Monitor.poll to get data.
     */
    void monitorEvent(Monitor monitor);
    /**
     * unlisten 
     */
    void unlisten();
}
