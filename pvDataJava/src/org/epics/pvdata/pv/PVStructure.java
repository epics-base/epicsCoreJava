/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;



/**
 * PVStructure interface.
 * @author mrk
 *
 */
public interface PVStructure extends PVField, BitSetSerializable {
    /**
     * Get the Structure introspection interface.
     *
     * @return the introspection interface
     */
    Structure getStructure();

    /**
     * Get the <i>PVField</i> array for the nodes of the structure.
     *
     * @return array of PVField, one for each field
     */
    PVField[] getPVFields();

    /**
     * Get the PVField subfield with name fieldName.
     * <p>fieldName is the name of the field relative to this PVStructure.
     * So it consists of one or more strings separated by periods (.s).
     * The last of these strings is the name of the field. The other
     * strings are the names of the structure subfields containing the requested
     * field starting with the highest-level subfield and descending to the leaf
     * field, e.g. current.alarm.status.
     *
     * @param fieldName the fieldName
     * @return the PVField or null if the subfield does not exist
     */
    PVField getSubField(String fieldName);

    /**
     * Get the PVField with the specified offset.
     *
     * @param fieldOffset the offset
     * @return the PVField or null if the offset is not part of this structure
     */
    PVField getSubField(int fieldOffset);
    
    /**
     * Get the PVField subfield with name fieldName.
     * <p>fieldName is the name of the field relative to this PVStructure.
     * So it consists of one or more strings separated by periods (.s).
     * The last of these strings is the name of the field. The other
     * strings are the names of the structure subfields containing the requested
     * field starting with the highest-level subfield and descending to the leaf
     * field, e.g. current.alarm.status.
     *
     * @param <T> the expected type of the PVField of the requested field
     * @param c class object modeling the class T of expected type of the requested field
     * @param fieldName the fieldName.
     * @return the PVField or null if the subfield does not exist, or the field is not of type <code>T</code>
     */
    <T extends PVField> T getSubField(Class<T> c, String fieldName);

    /**
     * Get the PVField with the specified offset.
     *
     * @param <T> the expected type of the PVField of the requested field
     * @param c class object modeling the class T of expected type of the requested field
     * @param fieldOffset the offset
     * @return the PVField or null if the offset is not part of this structure, or the field is not of type <code>T</code>
     */
    <T extends PVField> T getSubField(Class<T> c, int fieldOffset);

    /**
     * Find a boolean subfield with the specified fieldName.
     * <p>fieldName is the full name of the field relative to this PVStructure.
     *
     * @param fieldName the name of the field to find
     * @return the interface if the field of the correct type is found or null otherwise
     */
    //@Deprecated
    PVBoolean getBooleanField(String fieldName);

    /**
     * Find a byte subfield with the specified fieldName.
     * <p>fieldName is the full name of the field relative to this PVStructure.
     *
     * @param fieldName the name of the field to find
     * @return the interface if the field of the correct type is found or null if not found
     */
    //@Deprecated
    PVByte getByteField(String fieldName);

    /**
     * Find a short subfield with the specified fieldName.
     * <p>fieldName is the full name of the field relative to this PVStructure.
     *
     * @param fieldName the name of the field to find.
     * @return the interface if the field of the correct type is found or null otherwise
     */
    //@Deprecated
    PVShort getShortField(String fieldName);

    /**
     * Find an int subfield with the specified fieldName.
     * <p>fieldName is the full name of the field relative to this PVStructure.
     *
     * @param fieldName the name of the field to find
     * @return the interface if the field of the correct type is found or null if not found
     */
    //@Deprecated
    PVInt getIntField(String fieldName);

    /**
     * Find a long subfield with the specified fieldName.
     * <p>fieldName is the full name of the field relative to this PVStructure.
     *
     * @param fieldName the name of the field to find.
     * @return the interface if the field of the correct type is found or null otherwise
     */
    //@Deprecated
    PVLong getLongField(String fieldName);

    /**
     * Find a float subfield with the specified fieldName.
     * <p>fieldName is the full name of the field relative to this PVStructure.
     *
     * @param fieldName the name of the field to find
     * @return the interface if the field of the correct type is found or null otherwise
     */
    //@Deprecated
    PVFloat getFloatField(String fieldName);

    /**
     * Find a double subfield with the specified fieldName.
     * <p>fieldName is the full name of the field relative to this PVStructure.
     *
     * @param fieldName the name of the field to find
     * @return the interface if the field of the correct type is found or null otherwise
     */
    //@Deprecated
    PVDouble getDoubleField(String fieldName);

    /**
     * Find a string subfield with the specified fieldName.
     * <p>fieldName is the full name of the field relative to this PVStructure.
     *
     * @param fieldName the name of the field to find
     * @return the interface if the field of the correct type is found or null otherwise
     */
    //@Deprecated
    PVString getStringField(String fieldName);

    /**
     * Find a structure subfield with the specified fieldName
     * <p>fieldName is the full name of the field relative to this PVStructure.
     *
     * @param fieldName The field name to find.
     * @return the interface if the field of the correct type is found or null otherwise
     */
    //@Deprecated
    PVStructure getStructureField(String fieldName);
    /**
     * Find an array subfield with the specified fieldName and elementType.
     * <p>fieldName is the full name of the field relative to this PVStructure.
     *
     * @param fieldName the name of the field to find
     * @param elementType the ScalarType of an element of the ScalarArray field
     * @return the interface if the field of the correct type is found or null otherwise
     */
    //@Deprecated
    PVScalarArray getScalarArrayField(String fieldName,ScalarType elementType);

    /**
     * Find a structureArray subfield with the specified fieldName.
     * <p>fieldName is the full name of the field relative to this PVStructure.
     *
     * @param fieldName the name of the field to find
     * @return the interface if the field of the correct type is found or null if not found
     */
    //@Deprecated
    PVStructureArray getStructureArrayField(String fieldName);

    /**
     * Find a union subfield with the specified fieldName.
     * <p>fieldName is the full name of the field relative to this PVStructure.
     *
     * @param fieldName the field name to find
     * @return the interface if the field of the correct type is found or null otherwise
     */
    //@Deprecated
    PVUnion getUnionField(String fieldName);

    /**
     * Find a unionArray subfield with the specified fieldName.
     * <p>fieldName is the full name of the field relative to this PVStructure.
     *
     * @param fieldName The field name to find
     * @return the interface if the field of the correct type is found or null otherwise
     */
    //@Deprecated
    PVUnionArray getUnionArrayField(String fieldName);

    /**
     * Check if PVStructure and sub fields are valid.
     *
     * @return (true,false) if (OK, problems found)
     */
    public boolean checkValid();
}
