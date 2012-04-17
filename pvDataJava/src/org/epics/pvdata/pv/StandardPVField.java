/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */

package org.epics.pvdata.pv;

/**
 * @author mrk
 *
 */
public interface StandardPVField {
    /**
     * @param parent
     * @param fieldName
     * @param type
     * @param properties
     * @return
     */
    PVStructure scalar(PVStructure parent,String fieldName,ScalarType type,String properties);
    /**
     * @param parent
     * @param fieldName
     * @param elementType
     * @param properties
     * @return
     */
    PVStructure scalarArray(PVStructure parent,String fieldName,ScalarType elementType, String properties);
    /**
     * @param parent
     * @param fieldName
     * @param structure
     * @param properties
     * @return
     */
    PVStructure structureArray(PVStructure parent,String fieldName,Structure structure,String properties);
    /**
     * @param parent
     * @param choices
     * @param number
     * @return
     */
    PVStructure enumerated(PVStructure parent,String[] choices);
    /**
     * @param parent
     * @param fieldName
     * @param choices
     * @param number
     * @param properties
     * @return
     */
    PVStructure enumerated(PVStructure parent,String fieldName,String[] choices,String properties);
}
