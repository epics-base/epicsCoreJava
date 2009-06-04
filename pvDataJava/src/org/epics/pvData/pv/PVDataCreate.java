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
     * @return The PVArray implementation.
     */
    PVArray createPVArray(PVStructure parent,Array array);
    /**
     * Create an implementation for an array field. An Array introspection interface is created.
     * @param parent The parent interface.
     * @param fieldName The field name.
     * @param elementType The element type.
     * @return The PVArray implementation.
     */
    PVArray createPVArray(PVStructure parent,String fieldName,ScalarType elementType);
    /**
     * Create an implementation of an array field by cloning an existing PVArray.
     * The new PVArray will have the same value and auxInfo as the original.
     * @param parent The parent interface.
     * @param fieldName The field name.
     * @param arrayToClone The PVArray to clone.
     * @return The PVArray implementation.
     */
    PVArray createPVArray(PVStructure parent,String fieldName,PVArray arrayToClone);
    /**
     * Create implementation for PVStructure.
     * @param parent The parent interface.
     * @param fieldName The field name.
     * @param structure The introspection interface.
     * @return The PVStructure implementation
     */
    PVStructure createPVStructure(PVStructure parent,String fieldName,Structure structure);
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
     * Create implementation for PVStructure.
     * @param parent The parent interface.
     * @param fieldName The field name.
     * @param pvDatabase The database where structureName is located.
     * @param structureName The structure with this name is found in pvDatabase.
     * Each subfield and any auxInfo is cloned and added to the newly created structure.
     * @return The PVStructure implementation.
     */
    PVStructure createPVStructure(PVStructure parent,String fieldName,PVDatabase pvDatabase,String structureName);
    /**
     * Create a record instance.
     * @param recordName The instance name.
     * @param fields Array of reflection interfaces for the subFields.
     * @return The interface for accessing the record instance.
     */
    PVRecord createPVRecord(String recordName,Field[] fields);
    /**
     * Create a record instance.
     * @param recordName The instance name.
     * @param structToClone A structure. Each subfield and any auxInfo is cloned and added to the newly created structure.
     * @return The interface for accessing the record instance.
     */
    PVRecord createPVRecord(String recordName,PVStructure structToClone);
    /**
     * Create a record instance.
     * @param recordName The instance name.
     * @param pvDatabase The database where structureName is located.
     * @param structureName The structure with this name is located in pvDatabase.
     * Each subfield and any auxInfo is cloned and added to the newly created structure.
     * @return The interface for accessing the record instance.
     */
    PVRecord createPVRecord(String recordName,PVDatabase pvDatabase,String structureName);
    /**
     * Get a PVField[] that has all field of pvStructure in offset order.
     * @param pvStructure The structure.
     * @return The array of PVField in offset order.
     */
    PVField[] flattenPVStructure(PVStructure pvStructure);
}
