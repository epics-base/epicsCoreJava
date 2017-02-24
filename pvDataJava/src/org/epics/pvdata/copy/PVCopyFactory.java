/*
 * The License for this software can be found in the file LICENSE that is included with the distribution.
 */
package org.epics.pvdata.copy;

import org.epics.pvdata.pv.PVStructure;
/**
 * A Factory that creates a PVCopy interface which describes a subset of the fields
 * within a PVRecord. It can be used by Channel Access servers.
 * @author mrk
 *
 */
public class PVCopyFactory {
	private static boolean firstTime = true;
    /**
     * Map a subset of the fields within a PVRecord.
     * @param pvMaster The master PVStructure.
     * @param pvRequest A PVStructure which describes the set of fields of PVRecord that
     * should be mapped. See the packaged overview for details.
     * PVRecord of should it keep a separate copy.
     * @param structureName Must be one of null, "field", "putField", or "getField".
     * @return The PVCopy interface.
     */
    public static PVCopy create(PVStructure pvMaster,PVStructure pvRequest,String structureName) {
    	if(firstTime) {
    		firstTime = false;
    		new org.epics.pvdata.copy.arrayPlugin.ArrayPlugin();
    		new org.epics.pvdata.copy.timestampPlugin.TimestampPlugin();
    	}
    	return PVCopyImpl.create(pvMaster, pvRequest,structureName);
    }
}
