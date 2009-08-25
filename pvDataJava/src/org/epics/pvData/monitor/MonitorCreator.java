/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;

import org.epics.pvData.pv.Requester;

/**
 *  Requester for ChannelMonitor.
 * @author mrk
 *
 */
public interface MonitorCreator extends Requester{
    /**
     * A monitor is being destroyed.
     */
    void remove(Monitor monitor);
}
