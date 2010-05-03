/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.monitor;


import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;

/**
 * An interface implemented by code that implements a monitor algorithm.
 * @author mrk
 *
 */
public interface MonitorAlgorithmCreate {
    /**
     * Get the name of the algorithm.
     * @return The name.
     */
    String getAlgorithmName();
    /**
     * Create a MonitorAlgorithm.
     * @param pvRecord The record;
     * @param monitorRequester The requester.
     * @param fromPVRecord The field in the PVRecord for the algorithm.
     * @param pvOptions The options for the client.
     * @return The MonitorAlgorithm interface.
     */
    MonitorAlgorithm create(
            PVRecord pvRecord,
            MonitorRequester monitorRequester,
            PVField fromPVRecord,
            PVStructure pvOptions);
}
