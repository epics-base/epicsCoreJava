/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Structure;

/**
 * Requester for a getStructure request.
 * @author mrk
 *
 */
public interface GetStructureRequester extends Requester {
    /**
     * The client and server have both completed the getStructure request.
     * @param structure The Structure for the request.
     */
    void getDone(Structure structure);
}
