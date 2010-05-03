/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;

import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;
/**
 * A Factory that creates a PVCopy interface which describes a subset of the fields
 * within a PVRecord. It can be used by Channel Access servers.
 * @author mrk
 *
 */
public class PVCopyFactory {
    /**
     * Map a subset of the fields within a PVRecord.
     * @param pvRecord The PVRecord.
     * @param pvRequest A PVStructure which describes the set of fields of PVRecord that
     * should be mapped. See the packaged overview for details.
     * PVRecord of should it keep a separate copy.
     * @param structureName Must be one of null, "field", "putField", or "getField".
     * @return The PVCopy interface.
     */
    public static PVCopy create(PVRecord pvRecord,PVStructure pvRequest,String structureName) {
    	return PVCopyImpl.create(pvRecord, pvRequest,structureName);
    }
    /**
     * Create a request structure for the create calls in Channel.
     * See the package overview documentation for details.
     * @param request The field request. See the package overview documentation for details.
     * @param requester The requester;
     * @return The request structure if an invalid request was given. 
     */
    public static PVStructure createRequest(String request,Requester requester) {
    	return CreateRequestImpl.createRequest(request,requester);
    }
}
