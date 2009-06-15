/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;

/**
 * Requester for a createPVStructure request.
 * @author mrk
 *
 */
public interface CreatePVStructureRequester extends Requester {
    /**
     * The client and server have both completed the createPVStructure request.
     * @param pvStructure The PVStructure for the request.
     */
    void createDone(PVStructure pvStructure);
}
