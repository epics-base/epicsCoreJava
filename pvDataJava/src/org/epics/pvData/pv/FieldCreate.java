/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Interface for creating introspection interfaces.
 * @author mrk
 *
 */
public interface FieldCreate {
    /**
     * Create a new Field like an existing field but with a different name.
     * @param fieldName The field name.
     * @param field An existing field
     * @return a <i>Field</i> interface for the newly created object.
     */
    Field create(String fieldName,Field field);
    /**
     * Create a <i>ScalarField</i>.
     * @param fieldName The field name.
     * @param scalarType The scalar type.
     * @return a <i>Scalar</i> interface for the newly created object.
     * @throws An <i>IllegalArgumentException</i> if an illegal type is specified.
     */
    Scalar createScalar(String fieldName,ScalarType scalarType);
    /**
     * Create an <i>Array</i> field.
     * @param fieldName The field name
     * @param elementType The <i>scalarType</i> for array elements
     * @return An <i>Array</i> Interface for the newly created object.
     */
    ScalarArray createScalarArray(String fieldName,ScalarType elementType);
    /**
     * Create an <i>Array</i> field that is has element type <i>Structure</i>
     * @param fieldName The field name
     * @param elementStructure The <i>Structure</i> for each array element.
     * @return An <i>Array</i> Interface for the newly created object.
     */
    StructureArray createStructureArray(String fieldName,Structure elementStructure);
    /**
     * Create a <i>Structure</i> field.
     * @param fieldName The field name
     * @param fields The array of <i>Field</i>s for the structure.
     * @return a <i>Structure</i> interface for the newly created object.
     */
    Structure createStructure(String fieldName, Field[] fields);
}
