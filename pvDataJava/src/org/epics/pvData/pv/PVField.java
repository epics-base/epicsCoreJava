/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * PVField is the base class for each PVData field.
 * Each PVType has an interface that extends PVField.
 * @author mrk
 *
 */
public interface PVField extends Requester, Serializable {
    /**
     * Get offset of the PVField field within top level structure.
     * Every field within the PVStructure has a unique offset.
     * The top level structure has an offset of 0.
     * The first field within the structure has offset equal to 1.
     * The other offsets are determined by recursively traversing each structure of the tree.
     * @return The offset.
     */
    int getFieldOffset();
    /**
     * Get the next offset. If the field is a scalar or array field then this is just offset + 1.
     * If the field is a structure it is the offset of the next field after this structure.
     * Thus (nextOffset - offset) is always equal to the number of fields within the field.
     * @return The offset.
     */
    int getNextFieldOffset();
    /**
     * Get the total number of fields in this field.
     * This is equal to nextFieldOffset - fieldOffset.
     * @return The number of fields.
     */
    int getNumberFields();
    /**
     * Get the PVAuxInfo interface for the PVField.
     * @return The PVAuxInfo interface.
     */
    PVAuxInfo getPVAuxInfo();
    /**
     * Is the field immutable, i.e. does it not allow changes.
     * @return (false,true) if it (is not, is) immutable.
     */
    boolean isImmutable();
    /**
     * Set the field to be immutable, i. e. it can no longer be modified.
     * This is permanent, i.e. once done the field can onot be made mutable.
     */
    void setImmutable();
    /**
     * Get the fullFieldName, i.e. the complete hierarchy.
     * @return The name.
     */
    String getFullFieldName();
    /**
     * Get the full name, which is the recordName plus the fullFieldName
     * @return The name.
     */
    String getFullName();
    /**
     * Get the <i>Field</i> that describes the field.
     * @return Field, which is the reflection interface.
     */
    Field getField();
    /**
     * Get the parent of this field.
     * @return The parent interface or null if this is PVRecord
     */
    PVStructure getParent();
    /**
     * Get the PVRecordField for this PVField.
     * @return The interface or null if this field is not in a PVRecord.
     */
    PVRecordField getPVRecordField();
    /**
     * Replace the data implementation for a field.
     * @param newPVField The new implementation for this field.
     */
    void replacePVField(PVField newPVField);
    /**
     * Rename the field name.
     * @param newName The new name.
     */
    void renameField(String newName);
    /**
     * postPut. Called when the data for a field is updated by the implementation.
     */
    void postPut();
    /**
     * Convert the PVField to a string.
     * @return The string.
     */
    String toString();
    /**
     * Convert the PVField to a string.
     * Each line is indented.
     * @param indentLevel The indentation level.
     * @return The string.
     */
    String toString(int indentLevel);
}
