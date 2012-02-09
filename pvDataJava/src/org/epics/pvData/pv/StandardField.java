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
     * Create a Scalar field.
     * @param fieldName The field name.
     * @param scalarType The scalar type. 
     * @return The Scalar.
     */
    Scalar scalar(String fieldName,ScalarType scalarType);
    /**
     * Create a Scalar field with additional properties.
     * @param fieldName The field name.
     * @param scalarType The scalar type.
     * @param properties The list of additional properties which is some combination of alarm, timeStamp, display, control, and valueAlarm.
     * @return The Scalar.
     */
    Structure scalar(String fieldName,ScalarType scalarType,String properties);
    /**
     * Create a ScalarArray.
     * @param fieldName The field name.
     * @param elementType The scalar type for each element
     * @return The ScalarArray
     */
    ScalarArray scalarArray(String fieldName,
        ScalarType elementType);
    /**
     * Create a ScalarArray with additional properties.
     * @param fieldName The field name.
     * @param elementType The scalar type for each element
     * @param properties The list of additional properties which is some combination of alarm, timeStamp, and display.
     * @return The ScalarArray.
     */
    Structure scalarArray(String fieldName,ScalarType elementType, String properties);
    /**
     * Create a StructureArray.
     * @param fieldName The field name.
     * @param structure The introspection interface for each array element.
     * @return The StructureArray
     */
    StructureArray structureArray(String fieldName,Structure structure);
    /**
     * Create a StructureArray.
     * @param fieldName The field name.
     * @param structure The introspection interface for each array element.
     * @param properties The list of additional properties which is some combination of alarm and timeStamp.
     * @return The StructureArray.
     */
    Structure structureArray(String fieldName,Structure structure,String properties);
    /**
     * Create a Structure.
     * @param fieldName The field name.
     * @param fields An array if fields for the structure.
     * @return The Structure.
     */
    Structure structure(String fieldName,Field[] fields);
    /**
     * Create and enumerated structure.
     * @param fieldName The field name.
     * @return The Structure.
     */
    Structure enumerated(String fieldName);
    /**
     * Create a structure that has an enumerated structure with name value an additional properties.
     * @param fieldName The field name.
     * @param properties The additional properties.
     * @return The structure.
     */
    Structure enumerated(String fieldName, String properties);
    /**
     * Create a Scalar with a field name of value.
     * @param type The scalarType.
     * @return The Scalar.
     */
    Scalar scalarValue(ScalarType type);
    /**
     * @param type
     * @param properties
     * @return
     */
    Structure scalarValue(ScalarType type,String properties);
    /**
     * @param elementType
     * @return
     */
    ScalarArray scalarArrayValue(ScalarType elementType);
    /**
     * @param elementType
     * @param properties
     * @return
     */
    Structure scalarArrayValue(ScalarType elementType,
        String properties);
    /**
     * @param structure
     * @return
     */
    StructureArray structureArrayValue(Structure structure);
    /**
     * @param structure
     * @param properties
     * @return
     */
    Structure structureArrayValue(Structure structure,
        String properties);
    /**
     * @param numFields
     * @param fields
     * @return
     */
    Structure structureValue(
        int numFields,Field[] fields);
    /**
     * @return
     */
    Structure enumeratedValue();
    /**
     * @param properties
     * @return
     */
    Structure enumeratedValue(String properties);
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
