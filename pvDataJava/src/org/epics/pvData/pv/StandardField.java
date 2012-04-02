/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

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
     * @return
     */
    Structure alarm();
    /**
     * @return
     */
    Structure timeStamp();
    /**
     * @return
     */
    Structure display();
    /**
     * @return
     */
    Structure control();
    /**
     * @return
     */
    Structure booleanAlarm();
    /**
     * @return
     */
    Structure byteAlarm();
    /**
     * @return
     */
    Structure shortAlarm();
    /**
     * @return
     */
    Structure intAlarm();
    /**
     * @return
     */
    Structure longAlarm();
    /**
     * @return
     */
    Structure floatAlarm();
    /**
     * @return
     */
    Structure doubleAlarm();
    /**
     * @return
     */
    Structure enumeratedAlarm();
}
