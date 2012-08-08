/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;


/**
 * Create PVField field implementations.
 * @author mrk
 *
 */
public interface PVDataCreate {
    /**
     * Create a PVField using given Field introspection data.
     * @param field The introspection data to be used to create PVField.
     * @return The PVField implementation.
     */
    PVField createPVField(Field field);
    /**
     * Create a PVField using given a PVField to clone.
     * This method calls the appropriate createPVScalar, createPVArray, or createPVStructure.
     * @param fieldToClone The field to clone.
     * @return The PVField implementation
     */
    PVField createPVField(PVField fieldToClone);
    /**
     * Create an implementation of a scalar field reusing the Scalar introspection interface.
     * @param scalar The introspection interface.
     * @return The PVScalar implementation.
     */
    PVScalar createPVScalar(Scalar scalar);
    /**
     * Create an implementation of a scalar field. A Scalar introspection interface is created.
     * @param fieldType The field type.
     * @return The PVScalar implementation.
     */
    PVScalar createPVScalar(ScalarType fieldType);
    /**
     * Create an implementation of a scalar field by cloning an existing PVScalar.
     * The new PVScalar will have the same value and auxInfo as the original.
     * @param scalarToClone The PVScalar to clone.
     * @return The PVScalar implementation.
     */
    PVScalar createPVScalar(PVScalar scalarToClone);
    /**
     * Create an implementation of an array field reusing the Array introspection interface.
     * @param array The introspection interface.
     * @return The PVScalarArray implementation.
     */
    PVScalarArray createPVScalarArray(ScalarArray array);
    /**
     * Create an implementation for an array field. An Array introspection interface is created.
     * @param elementType The element type.
     * @return The PVScalarArray implementation.
     */
    PVScalarArray createPVScalarArray(ScalarType elementType);
    /**
     * Create an implementation of an array field by cloning an existing PVArray.
     * The new PVArray will have the same value and auxInfo as the original.
     * @param arrayToClone The PVScalarArray to clone.
     * @return The PVScalarArray implementation.
     */
    PVScalarArray createPVScalarArray(PVScalarArray arrayToClone);
    /**
     * Create an implementation of an array with structure elements.
     * @param structureArray The introspection interface.
     * All elements share the same introspection interface.
     * @return The PVStructureArray implementation.
     */
    PVStructureArray createPVStructureArray(StructureArray structureArray);
    /**
     * Create implementation for PVStructure.
     * @param structure The introspection interface.
     * @return The PVStructure implementation
     */
    PVStructure createPVStructure(Structure structure);
    /**
     * Create implementation.
     * @param fieldNames The array of fieldNames.
     * @param pvFields The array of PVField.
     * @return The PVStructure implementation. 
     */
    PVStructure createPVStructure(String[] fieldNames,PVField[] pvFields);
    /**
     * Create implementation for PVStructure.
     * @param structToClone A structure. Each subfield and any auxInfo is cloned and added to the newly created structure.
     * @return The PVStructure implementation.
     */
    PVStructure createPVStructure(PVStructure structToClone);
    /**
     * Get a PVField[] that has all field of pvStructure in offset order.
     * @param pvStructure The structure.
     * @return The array of PVField in offset order.
     */
    PVField[] flattenPVStructure(PVStructure pvStructure);
}
