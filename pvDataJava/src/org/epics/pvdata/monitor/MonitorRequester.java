/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.monitor;

import org.epics.pvdata.pv.Requester;
import org.epics.pvdata.pv.Status;
import org.epics.pvdata.pv.Structure;

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
     * @param monitor The monitor.
     */
    void monitorEvent(Monitor monitor);
    /**
     * The data source is no longer available.
     * @param monitor The monitor.
     */
    void unlisten(Monitor monitor);
}
