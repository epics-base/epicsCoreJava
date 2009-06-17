/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pvCopy;
import java.util.BitSet;

import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVRecord;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Structure;


/**
 * This interface maps between a PVRecord and a PVStructure that holds a copy of a subset
 * of the data in the PVRecord.
 * @author mrk
 *
 */
public interface PVCopy {
    /**
     * Get the PVRecord to which this PVCopy is attached. 
     * @return The interface.
     */
    PVRecord getPVRecord();
    /**
     * Get the introspection interface for the set of fields describing the subset
     * of the fields from PVRecord which a PVStructure contains.
     * @return The introspection interface.
     */
    Structure getStructure();
    /**
     * Create a PVStructure which can hold a subset of the data from the PVRecord.
     * A client may require multiple PVStructures. For example if a monitor request supports
     * a queue than a PVStructure is required for each queue element.
     * @return The interface.
     */
    PVStructure createPVStructure();
    /**
     * Given a PVField from the record determine the offset within the PVStructure where the copy of the data is located.
     * PVStructure.getSubField(offset) can be called to locate the PVField within the PVStructure.
     * @param recordPVField The PVField within PVRecord.
     * @return The offset or -1 if the field in the record does not have a copy in the PVStructure.
     */
    int getCopyOffset(PVField recordPVField);
    /**
     * Given a recordPVField within a recordPVStructure determine the offset within the PVStructure where the copy of the data is located.
     * PVStructure.getSubField(offset) can be called to locate the PVField within the PVStructure.
     * @param recordPVStructure The PVStructure within the PVRecord.
     * @param recordPVField The PVField within the recordPVStructure.
     * @return The offset or -1 if the field in the record does not have a copy in the PVStructure.
     */
    int getCopyOffset(PVStructure recordPVStructure,PVField recordPVField);
    /**
     * Given an offset within a PVStructure return the corresponding PVField in the PVREcord.
     * @param structureOffset The offset within the PVStructure.
     * @return The interface within the PVRecord.
     */
    PVField getRecordPVField(int structureOffset);
    /**
     * Initialize PVStructure with the current data from the PVRecord.
     * The bitSet will have offset 0 set to 1 and all other bits set to 0. 
     * @param copyPVStructure The PVStructure.
     * @param bitSet The bitSet for PVStructure.
     * @param lockRecord lock the record while initializing.
     */
    void initCopy(PVStructure copyPVStructure, BitSet bitSet, boolean lockRecord);
    /**
     * Update PVStructure from PVRecord. The BitSet shows which fields in PVStructure have changed.
     * @param copyPVStructure The PVStructure.
     * @param bitSet The BitSet which shows the fields that were modified.
     * @param lockRecord lock the record while updating.
     */
    void updateCopySetBitSet(PVStructure copyPVStructure,BitSet bitSet,boolean lockRecord);
    /**
     * Update PVStructure from the bitSet. Thus each PVField of PVStructure for which
     * bitSet.get(pvField.getOffset) is true is updated with the data from the PVRecord.
     * @param copyPVStructure The PVStructure.
     * @param bitSet The bitSet which shows which fields should be updated.
     * @param lockRecord Should the pvRecord be locked while updating.
     */
    void updateCopyFromBitSet(PVStructure copyPVStructure,BitSet bitSet,boolean lockRecord);
    /**
     * Update the fields in PVRecord with data from PVStructure. Only fields
     * that have the offset in bitSet set to true are modified.
     * @param copyPVStructure The PVStructure.
     * @param bitSet The offsets within PVStructure that have new data.
     * @param lockRecord lock the record while updating.
     */
    void updateRecord(PVStructure copyPVStructure,BitSet bitSet,boolean lockRecord);
    /**
     * Create a PVCopyMonitor.
     * @param pvCopyMonitorRequester The PVCopyMonitorRequester.
     * @return The PVCopyMonitor.
     */
    PVCopyMonitor createPVCopyMonitor(PVCopyMonitorRequester pvCopyMonitorRequester);
}
