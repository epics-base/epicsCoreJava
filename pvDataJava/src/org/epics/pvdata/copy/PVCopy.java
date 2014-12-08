/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.copy;

import org.epics.pvdata.misc.BitSet;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Structure;



/**
 * 
 * @author mrk
 *
 * This interface maps between a master PVStructure and a PVStructure that holds a copy of a subset
 * of the data in the master.
 */
public interface PVCopy {
    /**
     * Get the master PVStructure to which this PVCopy is attached. 
     * @return The interface.
     */
    PVStructure getPVMaster();
    /**
     * Traverse all the fields in master.
     * @param callback This is called for each field on master.
     */
    void traverseMaster(PVCopyTraverseMasterCallback callback);
    /**
     * Get the introspection interface for the set of fields describing the subset
     * of the fields from PVRecord which a PVStructure contains.
     * @return The introspection interface.
     */
    Structure getStructure();
    /**
     * Create a PVStructure which can hold a subset of the data from the master.
     * A client may require multiple PVStructures. For example if a monitor request supports
     * a queue than a PVStructure is required for each queue element.
     * @return The interface.
     */
    PVStructure createPVStructure();
    /**
     * Given a field the master. return the offset in copy for the same field.
     * A value of -1 that the copy does not have this field.
     * @param masterPVField The field in master.
     * @return The offset or -1 if the field in the record does not have a copy in the PVStructure.
     */
    int getCopyOffset(PVField masterPVField);
    /**
     * Given a field within master, return the offset in copy for the same field.
     *A value of -1 that the copy does not have this field.
     * @param masterPVStructure A structure in master that has masterPVField.
     * @param masterPVField The field in master.
     * @return The offset or -1 if the field in the record does not have a copy in the PVStructure.
     */
    int getCopyOffset(PVStructure masterPVStructure,PVField masterPVField);
    /**
     * Given a offset in the copy get the corresponding field in master.
     * @param structureOffset The offset in the copy.
     * @return The interface within master.
     */
    PVField getMasterPVField(int structureOffset);
    /**
     * Initialize PVStructure with the current data from the PVRecord.
     * The bitSet will have offset 0 set to 1 and all other bits set to 0. 
     * @param copyPVStructure The PVStructure.
     * @param bitSet The bitSet for PVStructure.
     */
    void initCopy(PVStructure copyPVStructure, BitSet bitSet);
    /**
     * Update PVStructure from master. The BitSet shows which fields in PVStructure have changed.
     * @param copyPVStructure The PVStructure.
     * @param bitSet The BitSet which shows the fields that were modified.
     */
    void updateCopySetBitSet(PVStructure copyPVStructure,BitSet bitSet);
    /**
     * Update PVStructure from the bitSet. Thus each PVField of PVStructure for which
     * bitSet.get(pvField.getOffset) is true is updated with the data from the PVRecord.
     * @param copyPVStructure The PVStructure.
     * @param bitSet The bitSet which shows which fields should be updated.
     */
    void updateCopyFromBitSet(PVStructure copyPVStructure,BitSet bitSet);
    /**
     * Update the fields in master with data from PVStructure. Only fields
     * that have the offset in bitSet set to true are modified.
     * @param copyPVStructure The PVStructure.
     * @param bitSet The offsets within PVStructure that have new data.
     */
    void updateMaster(PVStructure copyPVStructure,BitSet bitSet);
     /**
     * Get options for a field in a PVStructure created by pvCopy
     * @param fieldOffset The field offset.
     * @return The pvStructure containing the options or null if the field did not have options.
     */
    PVStructure getOptions(int fieldOffset);
    /**
     * Dump the internal pvCopy nodes.
     * @return The nodes.
     */
    String dump();
}
