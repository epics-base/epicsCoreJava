/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.ca.client;

import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.Status;

/**
 * @author mse
 *
 */
public interface QueryRequester {
    /**
     */
    void queryResult(Status status, Query query, PVField result);
}
