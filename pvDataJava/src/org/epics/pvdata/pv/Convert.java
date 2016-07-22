/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.pv;

/**
 * Convert between numeric types,convert any field to a string,
 * and convert from a string to a scalar field.
 * @author mrk
 * <p>Numeric conversions are between scalar numeric types or between arrays of
 * numeric types. It is not possible to convert between a scalar
 * and an array.
 * Numeric conversions are between types:
 * pvByte, pvShort, pvInt,
 * pvLong, pvFloat, or pvDouble.</p>
 * 
 * <p>getString converts any supported type to a String.
 * Code that implements a PVField interface can implement
 * method toString by calling this method.</p>
 *
 * <p>fromString converts a String to a scalar.
 * fromStringArray converts an array of String
 * to a pvArray, which must have a scaler element type.
 * A scalar field is a numeric field or pvBoolean or pvString.</p>
 * <p>All from methods put data into a PVField, e.g. from means where the PVField gets it's data.</p>
 *
 */
public interface Convert {
    /**
     * Get the full fieldName for the PVField and add to a StringBuilder.
     *
     * @param builder the StringBuilder that will have the result
     * @param pvField the PVField.
     */
    void getFullFieldName(StringBuilder builder, PVField pvField);

    /**
     * Convert a PVField to a String.
     * If a PVField is a structure or array be prepared for a very long string.
     *
     * @param buf the buffer for the result
     * @param pv the PVField to convert to a string
     * @param indentLevel the indentation level
     */
    void getString(StringBuilder buf, PVField pv, int indentLevel);

    /**
     * Convert a PVField to a string.
     * If the PVField is a structure or array be prepared for a very long string.
     *
     * @param buf the buffer for the result
     * @param pv the PVField to convert to a string
     */
    void getString(StringBuilder buf, PVField pv);

    /**
     * Convert from a String to a PVScalar
     *
     * @param pv the PVScalar to convert to
     * @param from the String value to convert from
     * @throws NumberFormatException if the String does not have a valid value
     */
    void fromString(PVScalar pv, String from);

    /**
     * Convert from a String to a PVScalarArray.
     * The String must be a comma separated set of values optionally
     * enclosed in [].
     *
     * @param pv the PVScalarArray to convert to
     * @param from the String value to convert from
     * @return the number of elements converted
     * @throws IllegalArgumentException if the element Type is not a scalar
     * @throws NumberFormatException if the String is not that of a valid set of array values
     * */
    int fromString(PVScalarArray pv, String from);
    /**
     * Convert from a String array and to a PVScalarArray array.
     *
     * @param pv the PVScalarArray to convert to
     * @param offset the element in the PVScalarArray at which to start putting the converted data
     * @param length the number of elements to convert
     * @param from the array of values to put into the PVScalarArray
     * @param fromOffset the starting element in the source array
     * @return the number of elements converted
     * @throws IllegalArgumentException if the element Type is not a scalar
     * @throws NumberFormatException if the String does not have a valid value
     */
    int fromStringArray(PVScalarArray pv, int offset, int length, String[] from, int fromOffset);

    /**
     * Convert from a PVScalarArray to a String array.
     *
     * @param pv the PVScalarArray
     * @param offset the starting element in the PVScalarArray
     * @param length the number of elements to convert to
     * @param to the array to receive the converted PVScalarArray data
     * @param toOffset the element in the destination array at which to start putting the converted data
     * @return the number of elements converted
     */
    int toStringArray(PVScalarArray pv, int offset, int length, String[] to, int toOffset);

    /**
     * Are from and to valid arguments to copy.
     * This first checks of both arguments have the same Type.
     * Then calls one of isCopyScalarCompatible,
     * isCopyArrayCompatible, or isCopyStructureCompatible.
     *
     * @param from the source
     * @param to the destination
     * @return (false,true) is the arguments (are not, are) compatible
     */
    boolean isCopyCompatible(Field from, Field to);

    /**
     * Copy from a PVField to another PVField.
     * This calls one of copyScalar, copyArray or copyStructure.
     * The two arguments must be compatible.
     *
     * @param from the source
     * @param to the destination
     * @throws IllegalArgumentException if the arguments are not compatible
     */
    void copy(PVField from,PVField to);

    /**
     * Are from and to the instrospection interfaces for valid arguments for copyScalar.
     * false will be returned if either argument is not a scalar as defined by Type.isScalar().
     * If both are scalars the return value is true if any of the following are true:
     * <ul>
     *   <li>Both arguments are numeric.</li>
     *   <li>Both arguments have the same type.</li>
     *   <li>Either argument is a string.</li>
     * </ul>
     *
     * @param from the introspection interface for the from data
     * @param to the introspection interface for the to data
     * @return (false,true) if the arguments (are not, are) compatible
     */
    boolean isCopyScalarCompatible(Scalar from, Scalar to);

    /**
     * Copy from a PVScalar to another PVScalar.
     *
     * @param from the source
     * @param to the destination
     * @throws IllegalArgumentException if the arguments are not compatible
     */
    void copyScalar(PVScalar from, PVScalar to);

    /**
     * Are from and to valid arguments to copyArray.
     * The results are like isCopyScalarCompatible except that the tests are made on the elementType.
     *
     * @param from the introspection interface for a source array
     * @param to the introspection interface for a destination array
     * @return (false,true) if the arguments (are not, are) compatible
     */
    boolean isCopyScalarArrayCompatible(ScalarArray from, ScalarArray to);

    /**
     * Copy from a source PVScalarArray to a destination PVScalarArray.
     *
     * @param from the source PVScalarArray
     * @param offset the starting element in the source
     * @param to the destination PVScalarArray
     * @param toOffset the element in the destination array at which to start putting the copied data
     * @param length the number of elements to copy
     * @return the number of elements converted
     * @throws IllegalArgumentException if the arguments are not compatible
     */
    int copyScalarArray(PVScalarArray from, int offset, PVScalarArray to, int toOffset, int length);

    /**
     * Are from and to the instrospection interfaces for valid arguments for copyStructure.
     * They are only compatible if they have the same Structure description.
     *
     * @param from the introspection interface for a source structure
     * @param to the introspection interface for a destination structure
     * @return (false,true) if the arguments (are not, are) compatible
     */
    boolean isCopyStructureCompatible(Structure from, Structure to);

    /**
     * Copy from a PVStructure to another PVStructure.
     * NOTE: Only compatible nodes are copied. This means:
     * <ul>
     *    <li>For scalar nodes this means that isCopyScalarCompatible is true.</li>
     *    <li>For array nodes this means that isCopyArrayCompatible is true.</li>
     *    <li>For structure nodes this means that isCopyStructureCompatible is true.</li>
     *    <li>Link nodes are not copied.</li>
     * </ul>
     *
     * @param from the source
     * @param to the destination
     * @throws IllegalArgumentException if the arguments are not compatible
     */
    void copyStructure(PVStructure from, PVStructure to);

    /**
     * Are from and to the instrospection interfaces for valid arguments for copyUnion.
     * They are only compatible if they have the same Union description.
     *
     * @param from the introspection interface for the source union
     * @param to the introspection interface for the destination union
     * @return (false,true) if the arguments (are not, are) compatible
     */
    boolean isCopyUnionCompatible(Union from, Union to);

    /**
     * Copy from a PVUnion to another PVUnion.
     *
     * @param from the source
     * @param to the destination
     * @throws IllegalArgumentException if the arguments are not compatible
     */
    void copyUnion(PVUnion from, PVUnion to);
    /**
     * Are from and to the instrospection interfaces for valid arguments for copyStructureArray.
     *
     * @param from the introspection interface for a source array
     * @param to the introspection interface for a destination array
     * @return (false,true) if the arguments (are not, are) compatible
     */
    boolean isCopyStructureArrayCompatible(StructureArray from, StructureArray to);

    /**
     * Are from and to the instrospection interfaces for valid arguments for copyUnionArray.
     *
     * @param from the from UnionArray
     * @param to the to UnionArray
     * @return (false,true) if the arguments (are not, are) compatible
     */
    boolean isCopyUnionArrayCompatible(UnionArray from, UnionArray to);

    /**
     * Copy from a PVStructureArray array to another PVStructureArray array.
     *
     * @param from the source array
     * @param to the destination array
     */
    void copyStructureArray(PVStructureArray from, PVStructureArray to);

    /**
     * Copy from a subset of a PVStructureArray to another PVStructureArray.
     *
     * @param from the source array
     * @param offset the element in the source array to start copying at
     * @param to the destination array
     * @param toOffset the element in the destination array at which to start
     *                 putting the copied data
     * @param length the number of elements to copy
     * @return the number of elements copied
     */
    int copyStructureArray(PVStructureArray from, int offset, PVStructureArray to, int toOffset, int length);

    /**
     * Copy from a PVUnionArray to another PVUnionArray.
     *
     * @param from the source array
     * @param to the destination array
     */
    void copyUnionArray(PVUnionArray from, PVUnionArray to);

    /**
     * Copy from a subset of a PVUnionArray to another PVUnionArray.
     *
     * @param from the source array
     * @param offset the element in the source array to start copying at
     * @param to the destination array
     * @param toOffset the element in the destination array at which to start
     *                 putting the copied data
     * @param length the number of elements to copy
     * @return the number of elements copied
     */
    int copyUnionArray(PVUnionArray from,int offset, PVUnionArray to, int toOffset, int length);

    /**
     * Convert a PVScalar to a byte.
     *
     * @param pv the PVScalar to convert
     * @return converted value
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    byte toByte(PVScalar pv);

    /**
     * Convert a PVScalar to a short.
     *
     * @param pv the PVScalar to convert
     * @return converted value
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    short toShort(PVScalar pv);

    /**
     * Convert a PVScalar to an int
     *
     * @param pv the PVScalar to convert
     * @return converted value
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    int toInt(PVScalar pv);

    /**
     * Convert a PVScalar to a long
     * @param pv the PVScalar to convert
     * @return converted value
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    long toLong(PVScalar pv);

    /**
     * Convert a PVScalar to a float
     *
     * @param pv the PVScalar to convert
     * @return converted value
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    float toFloat(PVScalar pv);

    /**
     * Convert a PVScalar to a double
     *
     * @param pv the PVScalar to convert
     * @return converted value
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    double toDouble(PVScalar pv);

    /**
     * Convert a PVScalar to a String
     *
     * @param pv the PVScalar to convert
     * @return converted value
     */
    String toString(PVScalar pv);
    /**
     * Convert a PVScalar from a byte
     *
     * @param pv a PVScalar
     * @param from value to put into PVScalar
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    void fromByte(PVScalar pv, byte from);

    /**
     * Convert a PVScalar from a short
     *
     * @param pv a PVScalar
     * @param from value to put into PVScalar
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    void fromShort(PVScalar pv, short from);

    /**
     * Convert a PVScalar from an int
     *
     * @param pv a PVScalar
     * @param from the value to put into the PVScalar
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    void fromInt(PVScalar pv, int from);

    /**
     * Convert a PVScalar from a long
     *
     * @param pv a PVScalar
     * @param from the value to put into the PVScalar
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    void fromLong(PVScalar pv, long from);

    /**
     * Convert a PV from a byte interpreted as unsigned
     *
     * @param pv a PV
     * @param from the value to put into the PV
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    void fromUByte(PVScalar pv, byte from);

    /**
     * Convert a PV from a short interpreted as unsigned
     *
     * @param pv a PV
     * @param from the value to put into the PV
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    void fromUShort(PVScalar pv, short from);

    /**
     * Convert a PV from an int interpreted as unsigned
     *
     * @param pv a PV
     * @param from the value to put into the PV
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    void fromUInt(PVScalar pv, int from);

    /**
     * Convert a PV from a long interpreted as unsigned
     *
     * @param pv a PV
     * @param from the value to put into the PV
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    void fromULong(PVScalar pv, long from);

    /**
     * Convert a PV from a float.
     *
     * @param pv a PV
     * @param from the value to put into the PV
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    void fromFloat(PVScalar pv, float from);

    /**
     * Convert a PV from a double.
     *
     * @param pv a PV
     * @param from the value to put into the PV
     * @throws IllegalArgumentException if the ScalarType of pv is not a numeric scalar
     */
    void fromDouble(PVScalar pv, double from);

    /**
     * Convert a PVScalarArray to a byte array.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray to start converting at
     * @param length the number of elements to convert
     * @param to the array to put the converted PVScalarArray data into
     * @param toOffset the element in the destination array at which to start putting the converted data
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int toByteArray(PVScalarArray pv, int offset, int length, byte[] to, int toOffset);

    /**
     * Convert a PVScalarArray to a short array.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray to start converting at
     * @param length the number of elements to convert
     * @param to the array to put the converted PVScalarArray data into
     * @param toOffset the element in the destination array at which to start putting the converted data
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int toShortArray(PVScalarArray pv, int offset, int length, short[] to, int toOffset);

    /**
     * Convert a PVScalarArray array to an int array.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray to start converting at
     * @param length the number of elements to convert
     * @param to the array to put the converted PVScalarArray data into
     * @param toOffset the element in the destination array at which to start putting the converted data
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int toIntArray(PVScalarArray pv, int offset, int length, int[] to, int toOffset);

    /**
     * Convert a PVScalarArray to a long array.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray to start converting at
     * @param length the number of elements to convert
     * @param to the array to put the converted PVScalarArray data into
     * @param toOffset the element in the destination array at which to start putting the converted data
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int toLongArray(PVScalarArray pv, int offset, int length, long[] to, int toOffset);

    /**
     * Convert a PVScalarArray to a float array.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray to start converting at
     * @param length the number of elements to convert
     * @param to the array to put the converted PVScalarArray data into
     * @param toOffset the element in the destination array at which to start putting the converted data
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int toFloatArray(PVScalarArray pv, int offset, int length, float[] to, int toOffset);

    /**
     * Convert a PVScalarArray to a double array.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray to start converting at
     * @param length the number of elements to convert
     * @param to the array to put the converted PVScalarArray data into
     * @param toOffset the element in the destination array at which to start putting the converted data
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int toDoubleArray(PVScalarArray pv, int offset, int length, double[] to, int toOffset);

    /**
     * Convert to a PVScalarArray from a byte array.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray at which to start putting the converted data
     * @param length the number of elements to convert
     * @param from the source array to put into the PVScalarArray
     * @param fromOffset the element in the source array to start converting at
     * @return the number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int fromByteArray(PVScalarArray pv, int offset, int length, byte[] from, int fromOffset);

    /**
     * Convert to a PVScalarArray from a short array.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray at which to start putting the converted data
     * @param length the number of elements to convert
     * @param from the source array to put into the PVScalarArray
     * @param fromOffset the element in the source array to start converting at
     * @return the number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int fromShortArray(PVScalarArray pv, int offset, int length, short[] from, int fromOffset);

    /**
     * Convert to a PVScalarArray from an int array.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray at which to start putting the converted data
     * @param length the number of elements to convert
     * @param from the source array to put into the PVScalarArray
     * @param fromOffset the element in the source array to start converting at
     * @return the number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int fromIntArray(PVScalarArray pv, int offset, int length, int[] from, int fromOffset);

    /**
     * Convert to a PVScalarArray from a long array.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray at which to start putting the converted data
     * @param length the number of elements to convert
     * @param from the source array to put into the PVScalarArray
     * @param fromOffset the element in the source array to start converting at
     * @return the number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int fromLongArray(PVScalarArray pv, int offset, int length, long[] from, int fromOffset);

    /**
     * Convert to a PVScalarArray from a byte array interpreted as unsigned.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray at which to start putting the converted data
     * @param length the number of elements to convert
     * @param from the source array to put into the PVScalarArray
     * @param fromOffset the element in the source array to start converting at
     * @return the number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int fromUByteArray(PVScalarArray pv, int offset, int length, byte[]from, int fromOffset);

    /**
     * Convert to a PVScalarArray from a short array interpreted as unsigned.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray at which to start putting the converted data
     * @param length the number of elements to convert
     * @param from the source array to put into the PVScalarArray
     * @param fromOffset the element in the source array to start converting at
     * @return the number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int fromUShortArray(PVScalarArray pv, int offset, int length, short[]from, int fromOffset);

    /**
     * Convert to a PVScalarArray from an int array interpreted as unsigned.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray at which to start putting the converted data
     * @param length the number of elements to convert
     * @param from the source array to put into the PVScalarArray
     * @param fromOffset the element in the source array to start converting at
     * @return the number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int fromUIntArray(PVScalarArray pv, int offset, int length, int[]from, int fromOffset);

    /**
     * Convert to a PVScalarArray from a long array interpreted as unsigned.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray at which to start putting the converted data
     * @param length the number of elements to convert
     * @param from the source array to put into the PVScalarArray
     * @param fromOffset the element in the source array to start converting at
     * @return the number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int fromULongArray(PVScalarArray pv, int offset, int length, long[]from, int fromOffset);

    /**
     * Convert to a PVScalarArray from a float array.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray at which to start
     *               putting the converted data
     * @param length the number of elements to convert
     * @param from the source array to put into the PVScalarArray
     * @param fromOffset the element in the source array to start converting at
     * @return the number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int fromFloatArray(PVScalarArray pv, int offset, int length, float[]from, int fromOffset);

    /**
     * Convert to a PVScalarArray from a double array.
     *
     * @param pv a PVScalarArray
     * @param offset the element in the PVScalarArray at which to start
     *               putting the converted data
     * @param length the number of elements to convert
     * @param from the source array to put into the PVScalarArray
     * @param fromOffset the element in the source array to start converting at
     * @return the number of elements converted
     * @throws IllegalArgumentException if the element type of pv is not numeric
     */
    int fromDoubleArray(PVScalarArray pv, int offset, int length, double[]from, int fromOffset);

    /**
     * Convenience method for implementing toString.
     * It generates a newline and inserts blanks at the beginning of the newline.
     *
     * @param builder the StringBuilder being constructed
     * @param indentLevel indentation level. Each level is four spaces.
     */
    void newLine(StringBuilder builder, int indentLevel);
}
