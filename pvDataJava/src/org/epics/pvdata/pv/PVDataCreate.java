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
     *
     * @param field the introspection data to be used to create PVField
     * @return the PVField implementation
     */
    PVField createPVField(Field field);

    /**
     * Create a PVField using given a PVField to clone.
     * This method calls the appropriate createPVScalar, createPVArray, or createPVStructure.
     *
     * @param fieldToClone the field to clone
     * @return the PVField implementation
     */
    PVField createPVField(PVField fieldToClone);

    /**
     * Create an implementation of a scalar field reusing the Scalar introspection interface.
     *
     * @param scalar the introspection interface
     * @return the PVScalar implementation
     */
    PVScalar createPVScalar(Scalar scalar);

    /**
     * Create an implementation of a scalar field. A Scalar introspection interface is created.
     *
     * @param fieldType the field type
     * @return the PVScalar implementation
     */
    PVScalar createPVScalar(ScalarType fieldType);

    /**
     * Create an implementation of a scalar field by cloning an existing PVScalar.
     * The new PVScalar will have the same value and auxInfo as the original.
     *
     * @param scalarToClone the PVScalar to clone
     * @return the PVScalar implementation
     */
    PVScalar createPVScalar(PVScalar scalarToClone);

    /**
     * Create an implementation of an array field reusing the Array introspection interface.
     * @param array the introspection interface
     * @return the PVScalarArray implementation
     */
    PVScalarArray createPVScalarArray(ScalarArray array);

    /**
     * Create an implementation for an array field. An Array introspection interface is created.
     *
     * @param elementType the element type
     * @return the PVScalarArray implementation
     */
    PVScalarArray createPVScalarArray(ScalarType elementType);
    /**
     * Create an implementation of an array field by cloning an existing PVArray.
     * The new PVArray will have the same value and auxInfo as the original.
     *
     * @param arrayToClone the PVScalarArray to clone
     * @return the PVScalarArray implementation
     */
    PVScalarArray createPVScalarArray(PVScalarArray arrayToClone);

    /**
     * Create an implementation of an array with structure elements.
     *
     * @param structureArray the introspection interface.
     * All elements share the same introspection interface.
     * @return the PVStructureArray implementation
     */
    PVStructureArray createPVStructureArray(StructureArray structureArray);

    /**
     * Create an implementation of an array with union elements.
     *
     * @param unionArray The introspection interface.
     * All elements share the same introspection interface
     * @return the PVUnionArray implementation
     */
    PVUnionArray createPVUnionArray(UnionArray unionArray);

    /**
     * Create implementation for PVStructure.
     *
     * @param structure the introspection interface
     * @return the PVStructure implementation
     */
    PVStructure createPVStructure(Structure structure);

    /**
     * Create implementation for PVUnion.
     *
     * @param union The introspection interface
     * @return the PVUnion implementation
     */
    PVUnion createPVUnion(Union union);

    /**
     * Create implementation.
     *
     * @param fieldNames the array of fieldNames
     * @param pvFields the array of PVField
     * @return the PVStructure implementation
     */
    PVStructure createPVStructure(String[] fieldNames,PVField[] pvFields);

    /**
     * Create variant union implementation.
     *
     * @return the variant PVUnion implementation. 
     */
    PVUnion createPVVariantUnion();

    /**
     * Create variant union array implementation.
     *
     * @return the variant PVUnionArray implementation.
     */
    PVUnionArray createPVVariantUnionArray();

    /**
     * Clone an existing PVStructure.
     *
     * @param structToClone the PVStructure to clone. Each subfield and any auxInfo is cloned and added to the newly created structure.
     * @return the PVStructure implementation
     */
    PVStructure createPVStructure(PVStructure structToClone);

    /**
     * Clone an existing PVUnion.
     *
     * @param unionToClone the PVUnion to clone
     * @return the new PVUnion
     */
    PVUnion createPVUnion(PVUnion unionToClone);

    /**
     * Get a PVField[] that has all fields of pvStructure in offset order.
     *
     * @param pvStructure the PVStructure
     * @return the array of PVField in offset order
     */
    PVField[] flattenPVStructure(PVStructure pvStructure);
    
    <T extends PVScalar, TA extends PVScalarArray> T createPVScalar(PVScalarType<T, TA> scalarType);

    <T extends PVScalar, TA extends PVScalarArray> TA createPVScalarArray(PVScalarType<T, TA> elementType);
    
    PVStructureArray createPVStructureArray(Structure structure);
    PVUnionArray createPVUnionArray(Union union);

}
