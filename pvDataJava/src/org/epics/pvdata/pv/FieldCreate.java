/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

import java.nio.ByteBuffer;

/**
 * Interface for creating introspection interfaces.
 * @author mrk
 *
 */
public interface FieldCreate {
    /**
     * Create a <i>ScalarField</i>.
     * @param scalarType The scalar type.
     * @return a <i>Scalar</i> interface for the newly created object.
     * @throws An <i>IllegalArgumentException</i> if an illegal type is specified.
     */
    Scalar createScalar(ScalarType scalarType);
    /**
     * Create an <i>Array</i> field.
     * @param elementType The <i>scalarType</i> for array elements
     * @return An <i>Array</i> Interface for the newly created object.
     */
    ScalarArray createScalarArray(ScalarType elementType);
    /**
     * Create an <i>Array</i> field that is has element type <i>Structure</i>
     * @param elementStructure The <i>Structure</i> for each array element.
     * @return An <i>Array</i> Interface for the newly created object.
     */
    StructureArray createStructureArray(Structure elementStructure);
    /**
     * Create a <i>Structure</i> field.
     * @param fieldNames The array of field names for the structure.
     * @param fields The array of fields for the structure.
     * @return a <i>Structure</i> interface for the newly created object.
     */
    Structure createStructure(String[] fieldNames, Field[] fields);
    /**
     * Create a <i>Structure</i> field with an identification.
     * @param id The identification string for the structure.
     * @param fieldNames The array of field names for the structure.
     * @param fields The array of fields for the structure.
     * @return a <i>Structure</i> interface for the newly created object.
     */
    Structure createStructure(String id, String[] fieldNames, Field[] fields);
    /**
     * Append a field to a structure,
     * @param structure The structure to which the field is appended.
     * @param fieldName the name for the appended field.
     * @param field The appended field.
     * @return The new structure.
     */
    Structure appendField(Structure structure,String fieldName, Field field);
    /**
     * Append  fields to a structure,
     * @param structure The structure to which the fields are appended.
     * @param fieldNames the names for the appended fields.
     * @param fields The appended fields.
     * @return The new structure.
     */
    Structure appendFields(Structure structure,String[] fieldNames, Field[] fields);
    /**
     * Create a <i>Structure</i> field.
     * @param structToClone The structure to clone.
     * @return a <i>Structure</i> interface for the newly created object.
     */
    Structure createStructure(Structure structToClone);
    /**
     * Deserialize <i>Field</i> instance from given byte buffer.
     * @param buffer Buffer containing serialized <i>Field</i> instance. 
     * @param control Deserialization control instance.
     * @return a deserialized <i>Field</i> instance.
     */
    Field deserialize(ByteBuffer buffer, DeserializableControl control);

}

