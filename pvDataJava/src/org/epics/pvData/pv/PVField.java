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
public interface PVField extends Requester {
    
    /**
     * Get the PVAuxInfo interface for the PVField.
     * @return The PVAuxInfo interface.
     */
    PVAuxInfo getPVAuxInfo();
    /**
     * Can the data for the field be modified?
     * @return If it can be modified
     */
    boolean isMutable();
    /**
     * Specify if the data for the field can be modified
     * @param value (false,true) if the data (can not, can) be modified
     */
    void setMutable(boolean value);
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
     * Get the record.
     * @return The record interface.
     */
    PVRecord getPVRecord();
    /**
     * Replace the data implementation for a field.
     * @param newPVField The new implementation for this field.
     */
    void replacePVField(PVField newPVField);
    /**
     * Add a listener to this field.
     * @param pvListener The pvListener to add to list for postPut notification.
     * @return (false,true) if the pvListener (was not,was) added.
     * If the listener was already in the list false is returned.
     */
    boolean addListener(PVListener pvListener);
    /**
     * remove a pvListener.
     * @param pvListener The listener to remove.
     */
    void removeListener(PVListener pvListener);
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
