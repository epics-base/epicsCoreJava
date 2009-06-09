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
     * Create a PVStructure which can hold a subset od the data from the PVRecord.
     * A client may require multiple PVStructures. For example if a monitor request supports
     * a queue than a PVStructure is required for each queue element.
     * @return The interface.
     */
    PVStructure createPVStructure();
    /**
     * Given a PVField from the record determine the offset within the PVStructure where copy of the data is located.
     * PVStructure.getSubField(offset) can be called to locate the PVField within the PVStructure.
     * @param recordPVField The PVField within PVREcord.
     * @return The offset or -1 if the field in the record does not have a copy in the PVStructure.
     */
    int getCopyOffset(PVField recordPVField);
    /**
     * Given a recordPVField within a recordPVStructure determine the offset within the PVStructure where copy of the data is located.
     * PVStructure.getSubField(offset) can be called to locate the PVField within the PVStructure.
     * @param recordPVStructure The PVStructure within the PVRecord.
     * @param recordPVField The PVField within the recordPVStructure.
     * @return The offset or -1 if the field in the record does not have a copy in the PVStructure.
     */
    int getCopyOffset(PVStructure recordPVStructure,PVField recordPVField);
    /**
     * Given an offset within a PVStructure return the corresponding PVField in the PVREcord.
     * @param structureOffset The offset withinh the PVStructure.
     * @return The interface within the PVRecord.
     */
    PVField getRecordPVField(int structureOffset);
    /**
     * Initialize PVStructure with then current data from the PVRecord.
     * The bitSet will have offset 0 set to 1 and all other bits set to 0. 
     * @param copyPVStructure The PVStructure.
     * @param bitSet The bitSet for PVStructure.
     */
    void initCopy(PVStructure copyPVStructure, BitSet bitSet);
    /**
     * Update PVStructure from PVRecord. The BitSet will show which fields in PVStructure have changed.
     * @param copyPVStructure The PVStructure.
     * @param bitSet The BitSet which shows the fields that were modified.
     * @return (false,true) if (no fields, at least one) field was modified.
     */
    boolean updateCopy(PVStructure copyPVStructure,BitSet bitSet);
    /**
     * Update the fields in PVRecord with data from PVStructure. Only fields
     * that have the offset in bitSet set to true are modified.
     * @param copyPVStructure The PVStructure.
     * @param bitSet The offsets within PVStructure that have new data.
     * @return false,true) if (no fields, at least one) field was modified.
     */
    boolean updateRecord(PVStructure copyPVStructure,BitSet bitSet);
    /**
     * Check the bits in BitSet.
     * If any PVStructure has all it;s fields modified then the offset for the structure itself is
     * set to true and the offset for ALL the fields within the structure are set false.
     * @param bitSet The bitSet
     * @return (false,true) if any offsets are true.
     */
    boolean checkBitSet(BitSet bitSet);
}
