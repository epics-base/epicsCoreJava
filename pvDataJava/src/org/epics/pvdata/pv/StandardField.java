/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
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
     * 
     * @param scalarType the scalar type
     * @param properties the list of additional properties, which is some
     *                   combination of the strings alarm, timeStamp, display,
     *                   control and valueAlarm separated by commas
     * @return the Scalar
     */
    Structure scalar(ScalarType scalarType,String properties);

    /**
     * Create a ScalarArray with additional properties.
     * 
     * @param elementType the scalar type for each element
     * @param properties the list of additional properties, which is some
     *                   combination of the strings alarm, timeStamp, display,
     *                   control and valueAlarm separated by commas
     * @return the ScalarArray
     */
    Structure scalarArray(ScalarType elementType, String properties);

    /**
     * Create a StructureArray.
     *
     * @param structure the introspection interface for each array element.
     * @param properties the list of additional properties, which is some
     *                   combination of the strings alarm and timeStamp
     *                   separated by commas
     * @return the StructureArray
     */
    Structure structureArray(Structure structure,String properties);

    /**
     * Create an enumerated structure.
     *
     * @return the enumerated type Structure
     */
    Structure enumerated();

    /**
     * Create a structure that has an enumerated structure with name value an additional properties.
     *
     * @param properties the additional properties
     * @return a structure that has an enumerated structure field with name
     *         value and the additional properties
     */
    Structure enumerated(String properties);

    /**
     * @return the Structure object of an alarm field
     */
    Structure alarm();

    /**
     * @return the Structure object of a time stamp field
     */
    Structure timeStamp();

    /**
     * @return the Structure object of a display field
     */
    Structure display();

    /**
     * @return the Structure object of a control field 
     */
    Structure control();

    /**
     * @return the Structure object of a boolean value alarm limit field
     */
    Structure booleanAlarm();

    /**
     * @return the Structure object of a  byte value alarm limit field
     */
    Structure byteAlarm();

    /**
     * @return the Structure object of a short value alarm limit field
     */
    Structure shortAlarm();

    /**
     * @return the Structure object of an int value alarm limit field
     */
    Structure intAlarm();

    /**
     * @return the Structure object of a long value alarm limit field
     */
    Structure longAlarm();

    /**
     * @return the Structure object of a float value alarm limit field
     */
    Structure floatAlarm();

    /**
     * @return the Structure object of a double value alarm limit field
     */
    Structure doubleAlarm();

    /**
     * @return the Structure object of an enumerated value alarm limit field
     */
    Structure enumeratedAlarm();

}
