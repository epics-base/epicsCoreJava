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
     * 
     * @param type The scalarType
     * @param properties the list of additional properties, which is some
     *                   combination of the strings alarm, timeStamp, display,
     *                   control and valueAlarm separated by commas
     * @return the PVStructure with fields value plus the specified properties
     */
    PVStructure scalar(ScalarType type, String properties);

    /**
     * Create a PVStructure with a scalarArray value field.
     * 
     * @param elementType the scalarType for each element.
     * @param properties the list of additional properties, which is some
     *                   combination of the strings alarm, timeStamp, display,
     *                   control and valueAlarm separated by commas
     * @return the PVStructure with fields value plus the specified properties.
     */
    PVStructure scalarArray(ScalarType elementType, String properties);

    /**
     * Create a PVStructure with a structureArray value field.
     * 
     * @param properties Some combination of alarm,timeStamp
     * @param properties the list of additional properties, which is some
     *                   combination of the strings alarm and timeStamp
     *                   separated by commas
     * @param structure the Structure of the StructureArray value field
     * @return The PVStructure with fields value plus the specified properties.
     */
    PVStructure structureArray(Structure structure,String properties);

    /**
     * Create a PVStructure with an enumerated value field
     * 
     * @param choices the array of choices.
     * @return the PVStructure with field value and choices field containing
     *         the supplied choices
     */
    PVStructure enumerated(String[] choices);

    /**
     * Create a PVStructure with an enumerated value field
     * 
     * @param choices the array of choices.
     * @param properties the list of additional properties, which is some
     *                   combination of the strings alarm and timeStamp
     *                   separated by commas
     * @return the PVStructure with field value with choices field containing
     *         the supplied choices, plus the specified properties
     */
    PVStructure enumerated(String[] choices,String properties);
}
