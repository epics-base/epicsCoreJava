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
public interface PVStructure extends PVField {
    void postPut(PVField subField);
    /**
     * Get the Structure introspection interface.
     * @return The introspection interface.
     */
    Structure getStructure();
    /**
     * Get the <i>PVField</i> array for the fields of the structure.
     * @return array of PVField. One for each field.
     */
    PVField[] getPVFields();
    /**
     * Get the PVField subfield with name fieldName.
     * @param fieldName The fieldName.
     * @return The PVField or null if the subfield does not exist.
     */
    PVField getSubField(String fieldName);
    /**
     * Replace a field of the structure that is itself a structure.
     * For an ioc record. This should only be called when a record is in the readyForInitialization state.
     * @param fieldName The field name.
     * @param structure The replacement structure.
     * @return (false,true) if the field (was not,was) replaced.
     */
    boolean replaceStructureField(String fieldName,Structure structure);
    /**
     * Append a new PVField to this structure.
     * @param pvField The field to append.
     */
    void appendPVField(PVField pvField);
    /**
     * Find a boolean subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVBoolean getBooleanField(String fieldName);
    /**
     * Find a byte subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVByte getByteField(String fieldName);
    /**
     * Find a short subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVShort getShortField(String fieldName);
    /**
     * Find an int subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVInt getIntField(String fieldName);
    /**
     * Find a long subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVLong getLongField(String fieldName);
    /**
     * Find a float subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVFloat getFloatField(String fieldName);
    /**
     * Find a double subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVDouble getDoubleField(String fieldName);
    /**
     * Find a string subfield with the specified fieldName.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVString getStringField(String fieldName);
    /**
     * Find a structure subfield with the specified fieldName
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVStructure getStructureField(String fieldName);
    /**
     * Find an array subfield with the specified fieldName and elementType.
     * The fieldName is of the form name.name...
     * @param fieldName The field name to find
     * @return The interface if the field of the correct type is found or null if not found.
     */
    PVArray getArrayField(String fieldName,ScalarType elementType);
}
