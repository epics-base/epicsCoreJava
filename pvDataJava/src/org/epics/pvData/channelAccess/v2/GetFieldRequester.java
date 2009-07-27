/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess.v2;

import org.epics.pvData.pv.Field;
import org.epics.pvData.pv.Requester;

/**
 * Requester for a getStructure request.
 * @author mrk
 *
 */
public interface GetFieldRequester extends Requester {
    /**
     * The client and server have both completed the getStructure request.
     * @param field The Structure for the request.
     */
    void getDone(Field field);
}
