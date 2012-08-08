/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.pv;

/**
 * Generate introspection object for standard fields.
 * @author mrk
 *
 */
public interface StandardField {
    /**
     * Create a Scalar field with additional properties.
     * @param scalarType The scalar type.
     * @param properties The list of additional properties which is some combination of alarm, timeStamp, display, control, and valueAlarm.
     * @return The Scalar.
     */
    Structure scalar(ScalarType scalarType,String properties);
    /**
     * Create a ScalarArray with additional properties.
     * @param elementType The scalar type for each element
     * @param properties The list of additional properties which is some combination of alarm, timeStamp, and display.
     * @return The ScalarArray.
     */
    Structure scalarArray(ScalarType elementType, String properties);
    /**
     * Create a StructureArray.
     * @param structure The introspection interface for each array element.
     * @param properties The list of additional properties which is some combination of alarm and timeStamp.
     * @return The StructureArray.
     */
    Structure structureArray(Structure structure,String properties);
    /**
     * Create an enumerated structure.
     * @return The Structure.
     */
    Structure enumerated();
    /**
     * Create a structure that has an enumerated structure with name value an additional properties.
     * @param properties The additional properties.
     * @return The structure.
     */
    Structure enumerated(String properties);
    /**
     * @return The Structure.
     */
    Structure alarm();
    /**
     * @return The Structure.
     */
    Structure timeStamp();
    /**
     * @return The Structure.
     */
    Structure display();
    /**
     * @return The Structure.
     */
    Structure control();
    /**
     * @return The Structure.
     */
    Structure booleanAlarm();
    /**
     * @return The Structure.
     */
    Structure byteAlarm();
    /**
     * @return The Structure.
     */
    Structure shortAlarm();
    /**
     * @return The Structure.
     */
    Structure intAlarm();
    /**
     * @return The Structure.
     */
    Structure longAlarm();
    /**
     * @return The Structure.
     */
    Structure floatAlarm();
    /**
     * @return The Structure.
     */
    Structure doubleAlarm();
    /**
     * @return The Structure.
     */
    Structure enumeratedAlarm();
}
