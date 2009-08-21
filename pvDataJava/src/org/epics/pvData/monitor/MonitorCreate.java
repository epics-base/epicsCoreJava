/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;


import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pvCopy.PVCopy;

/**
 * An interface implemented by code that implements a monitor algorithm.
 * @author mrk
 *
 */
public interface MonitorCreate {
    /**
     * Get the name of the algorithm.
     * @return The name.
     */
    String getName();
    /**
     * Create a Monitor.
     * @param pvRecord The record;
     * @param MonitorRequester The requester.
     * @param pvOption Options for the algorithm.
     * @param pvCopy The PVCopy that maps to a subset of the fields in a PVRecord.
     * @param queueSize The queue size.
     * @return The Monitor interface.
     */
    Monitor create(
            PVRecord pvRecord,
            MonitorRequester monitorRequester,
            PVStructure pvOption,
            PVCopy pvCopy,
            int queueSize);
}
