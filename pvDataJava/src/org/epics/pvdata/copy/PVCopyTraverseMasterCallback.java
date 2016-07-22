/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.copy;

import org.epics.pvdata.pv.PVField;



/**
 * This interface maps between a PVRecord and a PVStructure that holds a copy of a subset
 * of the data in the PVRecord.
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
