/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * Convert between numeric types,  convert any field to a string,
 *  or convert from a string to a scalar field.
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
 * @author mrk
 *
 */
public interface Convert {
	/**
	 * Get the full fieldName for the pvField.
	 * @param pvField The pvField.
	 * @return The full field name.
	 */
	String getFullFieldName(PVField pvField);
    /**
     * Convert a PVField to a string.
     * @param pv a PVField to convert to a string.
     * If a PVField is a structure or array be prepared for a very long string.
     * @param indentLevel indentation level
     * @return value converted to string
     */
    String getString(PVField pv, int indentLevel);
    /**
     * Convert a PVField to a string.
     * @param pv The PVField to convert to a string.
     * If the PVField is a structure or array be prepared for a very long string.
     * @return value converted to string
     */
    String getString(PVField pv);
    /**
     * Convert from a String to a PVScalar
     * @param pv The PV.
     * @param from The String value to convert and put into a PV.
     * @throws NumberFormatException if the String does not have a valid value.
     */
    void fromString(PVScalar pv,String from);
    /**
     * Convert  from a String to a PVScalarArray.
     * The String must be a comma separated set of values optionally enclosed in []
     * @param pv The PV.
     * @param from The String value to convert and put into a PV.
     * @return The number of elements converted.
     * @throws IllegalArgumentException if the element Type is not a scalar.
     * @throws NumberFormatException if the String does not have a valid array values.
     * */
    int fromString(PVScalarArray pv,String from);
    /**
     * Convert a PVScalarArray from a String array.
     * The array element type must be a scalar.
     * @param pv The PV.
     * @param offset Starting element in a PV.
     * @param length The number of elements to transfer.
     * @param from The array of values to put into the PV.
     * @param fromOffset Starting element in the source array.
     * @return The number of elements converted.
     * @throws IllegalArgumentException if the element Type is not a scalar.
     * @throws NumberFormatException if the String does not have a valid value.
     */
    int fromStringArray(PVScalarArray pv, int offset, int length, String[]from, int fromOffset);
    /**
     * Convert a PVScalarArray to a String array.
     * @param pv The PV.
     * @param offset Starting element in the PV array.
     * @param length Number of elements to convert to the string array.
     * @param to String array to receive the converted PV data.
     * @param toOffset Starting element in the string array.
     * @return Number of elements converted.
     */
    int toStringArray(PVScalarArray pv, int offset, int length, String[]to, int toOffset);
    /**
     * Are from and to valid arguments to copy.
     * This first checks of both arguments have the same Type.
     * Then calls one of isCopyScalarCompatible,
     * isCopyArrayCompatible, or isCopyStructureCompatible.
     * @param from The source.
     * @param to The destination.
     * @return (false,true) is the arguments (are not, are) compatible.
     */
    boolean isCopyCompatible(Field from, Field to);
    /**
     * Copy from a PVField to another PVField.
     * This calls one on copyScalar, copyArray, copyStructure.
     * The two arguments must be compatible.
     * @param from The source.
     * @param to The destination
     * @throws IllegalArgumentException if the arguments are not compatible.
     */
    void copy(PVField from,PVField to);
    /**
     * Are from and to valid arguments to copyScalar.
     * false will be returned if either argument is not a scalar as defined by Type.isScalar().
     * If both are scalars the return value is true if any of the following are true.
     * <ul>
     *   <li>Both arguments are numeric.</li>
     *   <li>Both arguments have the same type.</li>
     *   <li>Either argument is a string.</li>
     * </ul>
     * @param from The introspection interface for the from data.
     * @param to The introspection interface for the to data..
     * @return (false,true) If the arguments (are not, are) compatible.
     */
    boolean isCopyScalarCompatible(Scalar from, Scalar to);
    /**
     * Copy from a scalar pv to another scalar pv.
     * @param from the source.
     * @param to the destination.
     * @throws IllegalArgumentException if the arguments are not compatible.
     */
    void copyScalar(PVScalar from, PVScalar to);
    /**
     * Are from and to valid arguments to copyArray.
     * The results are like isCopyScalarCompatible except that the tests are made on the elementType.
     * @param from The from array.
     * @param to The to array.
     * @return (false,true) If the arguments (are not, are) compatible.
     */
    boolean isCopyScalarArrayCompatible(ScalarArray from, ScalarArray to);
    /**
     * Convert from a source PV array to a destination PV array.
     * @param from The source array.
     * @param offset Starting element in the source.
     * @param to The destination array.
     * @param toOffset Starting element in the array.
     * @param length Number of elements to transfer.
     * @return Number of elements converted.
     * @throws IllegalArgumentException if the arguments are not compatible.
     */
    int copyScalarArray(PVScalarArray from, int offset, PVScalarArray to, int toOffset, int length);
    /**
     * Are from and to valid arguments for copyStructure.
     * They are only compatible if they have the same Structure description.
     * @param from from structure.
     * @param to structure.
     * @return (false,true) If the arguments (are not, are) compatible.
     */
    boolean isCopyStructureCompatible(Structure from, Structure to);
    /**
     * Copy from a structure pv to another structure pv.
     * NOTE: Only compatible nodes are copied. This means:
     * <ul>
     *    <li>For scalar nodes this means that isCopyScalarCompatible is true.</li>
     *    <li>For array nodes this means that isCopyArrayCompatible is true.</li>
     *    <li>For structure nodes this means that isCopyStructureCompatible is true.</li>
     *    <li>Link nodes are not copied.</li>
     * </ul>
     * @param from The source.
     * @param to The destination.
     * @throws IllegalArgumentException if the arguments are not compatible.
     */
    void copyStructure(PVStructure from, PVStructure to);
    /**
     * Are from and to valid for copyStructureArray.
     * @param from The from StructureArray.
     * @param to The to StructureArray.
     * @return (false,true) If the arguments (are not, are) compatible.
     */
    boolean isCopyStructureArrayCompatible(StructureArray from, StructureArray to);
    /**
     * Copy from a structure array to another structure array.
     * @param from The source array.
     * @param to The destination array.
     */
    void copyStructureArray(PVStructureArray from, PVStructureArray to);
    /**
     * Convert a PV to a <byte>.
     * @param pv a PV
     * @return converted value
     */
    byte toByte(PVScalar pv);
    /**
     * Convert a PV to a short.
     * @param pv a PV
     * @return converted value
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    short toShort(PVScalar pv);
    /**
     * Convert a PV to an int
     * @param pv a PV
     * @return converted value
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    int toInt(PVScalar pv);
    /**
     * Convert a PV to a long
     * @param pv a PV
     * @return converted value
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    long toLong(PVScalar pv);
    /**
     * Convert a PV to a float
     * @param pv a PV
     * @return converted value
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    float toFloat(PVScalar pv);
    /**
     * Convert a PV to a double
     * @param pv a PV
     * @return converted value
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    double toDouble(PVScalar pv);
    /**
     * Convert a PV from a byte
     * @param pv a PV
     * @param from value to put into PV
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    void fromByte(PVScalar pv, byte from);
    /**
     * Convert a PV from a short
     * @param pv a PV
     * @param from value to put into PV
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    void  fromShort(PVScalar pv, short from);
    /**
     * Convert a PV from an int
     * @param pv a PV
     * @param from value to put into PV
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    void  fromInt(PVScalar pv, int from);
    /**
     * Convert a PV from a long
     * @param pv a PV
     * @param from value to put into PV
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    void  fromLong(PVScalar pv, long from);
    /**
     * Convert a PV from a float
     * @param pv a PV
     * @param from value to put into PV
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    void  fromFloat(PVScalar pv, float from);
    /**
     * Convert a PV from a double
     * @param pv a PV
     * @param from value to put into PV
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    void  fromDouble(PVScalar pv, double from);
    /**
     * Convert a PV array to a byte array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param to where to put the PV data
     * @param toOffset starting element in the array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int toByteArray(PVScalarArray pv, int offset, int length, byte[]to, int toOffset);
    /**
     * Convert a PV array to a short array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param to where to put the PV data
     * @param toOffset starting element in the array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int toShortArray(PVScalarArray pv, int offset, int length, short[]to, int toOffset);
    /**
     * Convert a PV array to an int array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param to where to put the PV data
     * @param toOffset starting element in the array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int toIntArray(PVScalarArray pv, int offset, int length, int[]to, int toOffset);
    /**
     * Convert a PV array to a long array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param to where to put the PV data
     * @param toOffset starting element in the array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int toLongArray(PVScalarArray pv, int offset, int length, long[]to, int toOffset);
    /**
     * Convert a PV array to a float array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param to where to put the PV data
     * @param toOffset starting element in the array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int toFloatArray(PVScalarArray pv, int offset, int length, float[]to, int toOffset);
    /**
     * Convert a PV array to a double array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param to where to put the PV data
     * @param toOffset starting element in the array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int toDoubleArray(PVScalarArray pv, int offset, int length, double[]to, int toOffset);
    /**
     * Convert a PV array from a byte array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param from value to put into PV
     * @param fromOffset
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int fromByteArray(PVScalarArray pv, int offset, int length, byte[]from, int fromOffset);
    /**
     * Convert a PV array from a short array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param from value to put into PV
     * @param fromOffset starting element in the source array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int fromShortArray(PVScalarArray pv, int offset, int length, short[]from, int fromOffset);
    /**
     * Convert a PV array from an int array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param from value to put into PV
     * @param fromOffset starting element in the source array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int fromIntArray(PVScalarArray pv, int offset, int length, int[]from, int fromOffset);
    /**
     * Convert a PV array from a long array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param from value to put into PV
     * @param fromOffset starting element in the source array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int fromLongArray(PVScalarArray pv, int offset, int length, long[]from, int fromOffset);
    /**
     * Convert a PV array from a float array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param from value to put into PV
     * @param fromOffset starting element in the source array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int fromFloatArray(PVScalarArray pv, int offset, int length, float[]from, int fromOffset);
    /**
     * Convert a PV array from a double array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param from value to put into PV
     * @param fromOffset starting element in the source array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int fromDoubleArray(PVScalarArray pv, int offset, int length, double[]from, int fromOffset);
    /**
     * Convenience method for implementing toString.
     * It generates a newline and inserts blanks at the beginning of the newline.
     * @param builder The StringBuilder being constructed.
     * @param indentLevel Indent level, Each level is four spaces.
     */
    void newLine(StringBuilder builder, int indentLevel);
}
