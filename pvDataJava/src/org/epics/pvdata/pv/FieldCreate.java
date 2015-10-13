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
     * Create a new instance of in-line <code>Field</code> builder.
     *
     * @return a new instance of a <code>FieldBuilder</code>
     */
    FieldBuilder createFieldBuilder();
    
    /**
     * Create a <i>ScalarField</i>.
     *
     * @param scalarType the scalar type
     * @return a <i>Scalar</i> interface for the newly created object
     * @throws IllegalArgumentException if an illegal type is specified
     */
    Scalar createScalar(ScalarType scalarType);

    /**
     * Create a <i>BoundedString</i>.
     *
     * @param maxLength the maximum string length
     * @return a <i>BoundedScalar</i> interface for the newly created object
     * @throws IllegalArgumentException if maxLength &lt;= 0
     */
    BoundedString createBoundedString(int maxLength);

    /**
     * Create an <i>Array</i> field, variable size array
     *
     * @param elementType the <i>scalarType</i> for array elements
     * @return an <i>Array</i> interface for the newly created object
     */
    ScalarArray createScalarArray(ScalarType elementType);

    /**
     * Create an <i>Array</i> field, fixed size array.
     *
     * @param elementType The <i>scalarType</i> for array elements
     * @param size the fixed array size
     * @return an <i>Array</i> interface for the newly created object
     */
    ScalarArray createFixedScalarArray(ScalarType elementType, int size);

    /**
     * Create an <i>Array</i> field, bounded size array.
     *
     * @param elementType The <i>scalarType</i> for array elements
     * @param bound the array maximum capacity (bound)
     * @return an <i>Array</i> interface for the newly created object
     */
    ScalarArray createBoundedScalarArray(ScalarType elementType, int bound);

    /**
     * Create an <i>Array</i> field that is has element type <i>Structure</i>
     *
     * @param elementStructure the <i>Structure</i> for each array element
     * @return an <i>Array</i> interface for the newly created object
     */
    StructureArray createStructureArray(Structure elementStructure);

    /**
     * Create an <i>Array</i> field that is has element type <i>Union</i>
     *
     * @param elementUnion the <i>Union</i> for each array element
     * @return an <i>Array</i> interface for the newly created object
     */
    UnionArray createUnionArray(Union elementUnion);

    /**
     * Create a variant <i>UnionArray</i> (aka any type) field.
     *
     * @return a <i>UnionArray</i> interface for the newly created object
     */
    UnionArray createVariantUnionArray();

    /**
     * Create a <i>Structure</i> field.
     *
     * @param fieldNames the array of field names for the structure
     * @param fields the array of fields for the structure
     * @return a <i>Structure</i> interface for the newly created object
     */
    Structure createStructure(String[] fieldNames, Field[] fields);

    /**
     * Create a <i>Structure</i> field with an identification.
     *
     * @param id the identification string for the structure
     * @param fieldNames the array of field names for the structure
     * @param fields the array of fields for the structure
     * @return a <i>Structure</i> interface for the newly created object
     */
    Structure createStructure(String id, String[] fieldNames, Field[] fields);

    /**
     * Append a field to a structure.
     *
     * @param structure the structure to which the field is appended
     * @param fieldName the name for the appended field
     * @param field the appended field
     * @return The new structure
     */
    Structure appendField(Structure structure, String fieldName, Field field);

    /**
     * Append  fields to a structure.
     *
     * @param structure the structure to which the fields are appended
     * @param fieldNames the names for the appended fields
     * @param fields the appended fields
     * @return the new structure
     */
    Structure appendFields(Structure structure, String[] fieldNames, Field[] fields);

    /**
     * Create a <i>Structure</i> field.
     *
     * @param structToClone the structure to clone
     * @return a <i>Structure</i> interface for the newly created object
     */
    Structure createStructure(Structure structToClone);

    /**
     * Create a variant <i>Union</i> (aka any type) field.
     *
     * @return a <i>Union</i> interface for the newly created object
     */
    Union createVariantUnion();

    /**
     * Create an <i>Union</i> field.
     *
     * @param fieldNames the array of field names for the union
     * @param fields the array of fields for the union
     * @return a <i>Union</i> interface for the newly created object
     */
    Union createUnion(String[] fieldNames, Field[] fields);

    /**
     * Create an <i>Union</i> field with an identification.
     *
     * @param id the identification string for the union
     * @param fieldNames the array of field names for the union
     * @param fields the array of fields for the union
     * @return a <i>Union</i> interface for the newly created object
     */
    Union createUnion(String id, String[] fieldNames, Field[] fields);

    /**
     * Deserialize <i>Field</i> instance from given byte buffer
     *
     * @param buffer the buffer containing serialized <i>Field</i> instance
     * @param control the deserialization control instance
     * @return a deserialized <i>Field</i> instance
     */
    Field deserialize(ByteBuffer buffer, DeserializableControl control);

}

