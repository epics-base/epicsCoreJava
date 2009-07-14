/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;

/**
 * Interface implemented by the requester of a PVCopyMonitor.
 * @author mrk
 *
 */
public interface PVCopyMonitorRequester {
    /**
     * Data being monitored has changed.
     */
    void dataChanged();
    /**
     * PVCopyMonitor has been told to unlisten. No more monitors will occur.
     */
    void unlisten();
}
