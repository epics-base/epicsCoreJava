/*
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import org.epics.pvdata.pv.Field;
import org.epics.pvdata.pv.Requester;
import org.epics.pvdata.pv.Status;

/**
 * Requester for a getStructure request.
 * @author mrk
 *
 */
public interface GetFieldRequester extends Requester {
    /**
     * The client and server have both completed the getStructure request.
     * @param status Completion status.
     * @param field The Structure for the request or <code>null</code> if the request failed.
     */
    void getDone(Status status, Field field);
}
