/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;


/**
 * Create PVField field implementations.
 * @author mrk
 *
 */
public interface PVDataCreate {
    /**
     * Create a PVField using given Field introspection data.
     * @param parent The parent interface.
     * @param field The introspection data to be used to create PVField.
     * @return The PVField implementation.
     */
    PVField createPVField(PVStructure parent, Field field);
    /**
     * Create a PVField using given a PVField to clone.
     * This method calls the appropriate createPVScalar, createPVArray, or createPVStructure.
     * @param parent The parent interface.
     * @param fieldToClone The field to clone.
     * @return The PVField implementation
     */
    PVField createPVField(PVStructure parent,String fieldName,PVField fieldToClone);
    /**
     * Create an implementation of a scalar field reusing the Scalar introspection interface.
     * @param parent The parent.
     * @param scalar The introspection interface.
     * @return The PVScalar implementation.
     */
    PVScalar createPVScalar(PVStructure parent,Scalar scalar);
    /**
     * Create an implementation of a scalar field. A Scalar introspection interface is created.
     * @param parent The parent interface.
     * @param fieldName The field name.
     * @param fieldType The field type.
     * @return The PVScalar implementation.
     */
    PVScalar createPVScalar(PVStructure parent,String fieldName,ScalarType fieldType);
    /**
     * Create an implementation of a scalar field by cloning an existing PVScalar.
     * The new PVScalar will have the same value and auxInfo as the original.
     * @param parent The parent interface.
     * @param fieldName The field name.
     * @param scalarToClone The PVScalar to clone.
     * @return The PVScalar implementation.
     */
    PVScalar createPVScalar(PVStructure parent,String fieldName,PVScalar scalarToClone);
    /**
     * Create an implementation of an array field reusing the Array introspection interface.
     * @param parent The parent interface.
     * @param array The introspection interface.
     * @return The PVScalarArray implementation.
     */
    PVScalarArray createPVScalarArray(PVStructure parent,ScalarArray array);
    /**
     * Create an implementation for an array field. An Array introspection interface is created.
     * @param parent The parent interface.
     * @param fieldName The field name.
     * @param elementType The element type.
     * @return The PVScalarArray implementation.
     */
    PVScalarArray createPVScalarArray(PVStructure parent,String fieldName,ScalarType elementType);
    /**
     * Create an implementation of an array field by cloning an existing PVArray.
     * The new PVArray will have the same value and auxInfo as the original.
     * @param parent The parent interface.
     * @param fieldName The field name.
     * @param arrayToClone The PVScalarArray to clone.
     * @return The PVScalarArray implementation.
     */
    PVScalarArray createPVScalarArray(PVStructure parent,String fieldName,PVScalarArray arrayToClone);
    /**
     * Create an implementation of an array with structure elements.
     * @param parent The parent interface.
     * @param structureArray The introspection interface.
     * All elements share the same introspection interface.
     * @return The PVStructureArray implementation.
     */
    PVStructureArray createPVStructureArray(PVStructure parent,StructureArray structureArray);
    /**
     * Create implementation for PVStructure.
     * @param parent The parent interface.
     * @param structure The introspection interface.
     * @return The PVStructure implementation
     */
    PVStructure createPVStructure(PVStructure parent,Structure structure);
    /**
     * Create implementation for PVStructure.
     * @param parent The parent interface.
     * @param fieldName The field name.
     * @param fields Array of reflection interfaces for the subFields.
     * @return The PVStructure implementation
     */
    PVStructure createPVStructure(PVStructure parent,String fieldName,Field[] fields);
    /**
     * Create implementation for PVStructure.
     * @param parent The parent interface.
     * @param fieldName The field name.
     * @param structToClone A structure. Each subfield and any auxInfo is cloned and added to the newly created structure.
     * @return The PVStructure implementation.
     */
    PVStructure createPVStructure(PVStructure parent,String fieldName,PVStructure structToClone);
    /**
     * Get a PVField[] that has all field of pvStructure in offset order.
     * @param pvStructure The structure.
     * @return The array of PVField in offset order.
     */
    PVField[] flattenPVStructure(PVStructure pvStructure);
}
