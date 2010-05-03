/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;



/**
 * PVStructure interface.
 * @author mrk
 *
 */
public interface PVStructure extends PVField, BitSetSerializable {
    /**
     * Get the Structure introspection interface.
     * @return The introspection interface.
     */
    Structure getStructure();
    /**
     * Get the <i>PVField</i> array for the nodes of the structure.
     * @return array of PVField. One for each field.
     */
    PVField[] getPVFields();
    /**
     * Get the PVField subfield with name fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The fieldName.
     * @return The PVField or null if the subfield does not exist.
     */
    PVField getSubField(String fieldName);
    /**
     * Get the PVField with the specified offset.
     * @param fieldOffset The offset.
     * @return The PVField or null if the offset is not part of this structure.
     */
    PVField getSubField(int fieldOffset);
    /**
     * Replace a field of a structure..
     * For a javaIOC record. This should only be called when a record is in the readyForInitialization state.
     * @param fieldName The field name.
     * @param newPVField The new field.
     */
    void replacePVField(String fieldName,PVField newPVField);
    /**
     * Append a new PVField to this structure.
     * For a javaIOC record. This should only be called when a record is in the readyForInitialization state.
     * @param pvField The field to append.
     */
    void appendPVField(PVField pvField);
    /**
     * Remove a field.
     * For a javaIOC record. This should only be called when a record is in the readyForInitialization state.
     * @param fieldName The name of the field to remove.
     */
    void removePVField(String fieldName);
    /**
     * One or more of the PVField[] of the PVStructure has been replaced.
     * Update the implementation.
     */
    void updateInternal();
    /**
     * Find a boolean subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find.
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVBoolean getBooleanField(String fieldName);
    /**
     * Find a byte subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find.
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVByte getByteField(String fieldName);
    /**
     * Find a short subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find.
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVShort getShortField(String fieldName);
    /**
     * Find an int subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find.
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVInt getIntField(String fieldName);
    /**
     * Find a long subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find.
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVLong getLongField(String fieldName);
    /**
     * Find a float subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find.
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVFloat getFloatField(String fieldName);
    /**
     * Find a double subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find.
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVDouble getDoubleField(String fieldName);
    /**
     * Find a string subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find.
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVString getStringField(String fieldName);
    /**
     * Find a structureScalar subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find.
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVStructureScalar getStructureScalarField(String fieldName);
    /**
     * Find a structure subfield with the specified fieldName
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find.
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVStructure getStructureField(String fieldName);
    /**
     * Find an array subfield with the specified fieldName and elementType.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find.
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVArray getArrayField(String fieldName,ScalarType elementType);
    /**
     * Find a structureArray subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find.
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVStructureArray getStructureArrayField(String fieldName);
    /**
     * Get the name of structure that this structure extends.
     * @return The name or null if this structure does not extend a structure.
     */
    String getExtendsStructureName();
    /**
     * Specify the structure that this structure extends.
     * The call fails if a previous call was successful.
     * @param extendsStructureName The structure this structure extends.
     * @return (false,true) if the call (succeeds,fails).
     * It succeeds only if extendPVStructure is not null and no previous call was successful.
     */
    boolean putExtendsStructureName(String extendsStructureName);
}
