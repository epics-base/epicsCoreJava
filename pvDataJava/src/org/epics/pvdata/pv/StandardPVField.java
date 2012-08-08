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
     * Create a PVStructure with a scalar value field.
     * @param type The scalarType
     * @param properties Some combination of alarm,timeStamp,display,control,valueAlarm
     * @return The PVStructure with fields value plus the specified properties.
     */
    PVStructure scalar(ScalarType type,String properties);
    /**
     * Create a PVStructure with a scalarArray value field.
     * @param elementType The scalarType for each element.
     * @param properties Some combination of alarm,timeStamp,display,control,valueAlarm
     * @return The PVStructure with fields value plus the specified properties.
     */
    PVStructure scalarArray(ScalarType elementType, String properties);
    /**
     * Create a PVStructure with a structureArray value field.
     * @param properties Some combination of alarm,timeStamp
     * @return The PVStructure with fields value plus the specified properties.
     */
    PVStructure structureArray(Structure structure,String properties);
    /**
     * Create a PVStructure with an enumerated value field
     * @param choices The array of choices.
     * @return The PVStructure with field value.
     */
    PVStructure enumerated(String[] choices);
    /**
     * Create a PVStructure with an enumerated value field
     * @param choices The array of choices.
     * @param properties Some combination of alarm,timeStamp
     * @return The PVStructure with field value plus the specified properties.
     */
    PVStructure enumerated(String[] choices,String properties);
}
