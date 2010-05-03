/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.client;

import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.Requester;
import org.epics.pvData.pv.Status;

/**
 * Requester for a getStructure request.
 * @author mrk
 *
 */
public interface GetFieldRequester extends Requester {
    /**
     * The client and server have both completed the getStructure request.
     * @param status Completion status.
     * @param field The Structure for the request.
     */
    void getDone(Status status,Field field);
}
