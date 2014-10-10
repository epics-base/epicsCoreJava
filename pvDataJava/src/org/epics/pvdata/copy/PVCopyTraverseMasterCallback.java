/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.copy;

import org.epics.pvdata.pv.PVField;



/**
 * This interface maps between a PVRecord and a PVStructure that holds a copy of a subset
 * of the data in the PVRecord.
 * @author mrk
 *
 */
/**
 * @author mrk
 *
 */
public interface PVCopyTraverseMasterCallback {
    /**
     * Called once for each field in the copy. 
     * @param pvField The field in the master.
     */
    void nextMasterPVField(PVField pvField);
}
