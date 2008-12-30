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
     * @param parent The parent interface.
     * @param fieldName The field name.
     * @param fieldType The field type.
     * @return The PVScalar implementation.
     */
    public PVScalar createPVScalar(PVStructure parent,String fieldName,ScalarType fieldType);
    
    /**
     * Create an implementation for an array field.
     * @param parent The parent interface.
     * @param fieldName The field name.
     * @param elementType The element type.
     * @return The PVArray implementation.
     */
    public PVArray createPVArray(PVStructure parent,String fieldName,ScalarType elementType);
    /**
     * Create implementation for PVStructure.
     * @param parent The parent interface.
     * @param fieldName The field name.
     * @param fields Array of reflection interfaces for the subFields.
     * @return The PVStructure implementation
     */
    public PVStructure createPVStructure(PVStructure parent,String fieldName,Field[] fields);
    /**
     * Create implementation for PVStructure.
     * @param parent The parent interface.
     * @param fieldName The field name.
     * @param structToClone A structure. Each subfield and any auxInfo is cloned and added to the newly created structure.
     * @return The PVStructure implementation.
     */
    public PVStructure createPVStructure(PVStructure parent,String fieldName,PVStructure structToClone);
    /**
     * Create implementation for PVStructure.
     * @param parent The parent interface.
     * @param fieldName The field name.
     * @param pvDatabase The database where structureName is located.
     * @param structureName The structure with this name is found in pvDatabase.
     * Each subfield and any auxInfo is cloned and added to the newly created structure.
     * @return The PVStructure implementation.
     */
    public PVStructure createPVStructure(PVStructure parent,String fieldName,PVDatabase pvDatabase,String structureName);
    /**
     * Create a record instance.
     * @param recordName The instance name.
     * @param fields Array of reflection interfaces for the subFields.
     * @return The interface for accessing the record instance.
     */
    public PVRecord createPVRecord(String recordName,Field[] fields);
    /**
     * Create a record instance.
     * @param recordName The instance name.
     * @param structToClone A structure. Each subfield and any auxInfo is cloned and added to the newly created structure.
     * @return The interface for accessing the record instance.
     */
    public PVRecord createPVRecord(String recordName,PVStructure structToClone);
    /**
     * Create a record instance.
     * @param recordName The instance name.
     * @param pvDatabase The database where structureName is located.
     * @param structureName The structure with this name is found in pvDatabase.
     * Each subfield and any auxInfo is cloned and added to the newly created structure.
     * @return The interface for accessing the record instance.
     */
    public PVRecord createPVRecord(String recordName,PVDatabase pvDatabase,String structureName);
}
