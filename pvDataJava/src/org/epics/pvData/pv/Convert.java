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
 * <i>pvByte</i>, <i>pvShort</i>, <i>pvInt</i>,
 * <i>pvLong</i>, <i>pvFloat</i>, or <i>pvDouble</i>.</p>
 * 
 * <p><i>getString</i> converts any supported type to a <i>String</i>.
 * Code that implements a PVField interface can implement
 * method <i>toString</i> by calling this method.</p>
 *
 * <p><i>fromString</i> converts a <i>String<i> to a scalar.
 * <i>fromStringArray</i> converts an array of <i>String</i>
 * to a <i>pvArray</i>, which must have a scaler element type.
 * A scalar field is a numeric field or <i>pvBoolean</i> or <i>pvString</i>.</p>
 * <p>All from methods put data into a PVField, e.g. from means where the PVField gets it's data.
 * All from methods call postPut after calling the appropriate put method.</p>
 * @author mrk
 *
 */
public interface Convert {
    /**
     * Convert a <i>PV</i> to a string.
     * @param pv a <i>PV</i> to convert to a string.
     * If a <i>PV</i> is a structure or array be prepared for a very long string.
     * @param indentLevel indentation level
     * @return value converted to string
     */
    String getString(PVField pv, int indentLevel);
    /**
     * Convert a <i>PV</i> to a string.
     * @param pv a <i>PV</i> to convert to a string.
     * If a <i>PV</i> is a structure or array be prepared for a very long string.
     * @return value converted to string
     */
    String getString(PVField pv);
    /**
     * Convert a <i>PV</i> from a <i>String</i>.
     * The <i>PV</i> must be a scaler.
     * @param pv The PV.
     * @param from The String value to convert and put into a PV.
     * @throws NumberFormatException if the String does not have a valid value.
     */
    void fromString(PVScalar pv,String from);
    /**
     * Convert a <i>PV</i> array from a <i>String</i> array.
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
    int fromStringArray(PVArray pv, int offset, int length, String[]from, int fromOffset);
    /**
     * Convert a <i>PV</i> array to a <i>String</i> array.
     * The <i>PV</i> array can have ant elementType.
     * @param pv The PV.
     * @param offset Starting element in the PV array.
     * param length Number of elements to convert to the string array.
     * @param to String array to receive the converted <i>PV</i> data.
     * @param toOffset Starting element in the string array.
     * @return Number of elements converted.
     */
    int toStringArray(PVArray pv, int offset, int length, String[]to, int toOffset);
    /**
     * Are <i>from</i> and <i>to</i> valid arguments to copyScalar.
     * <i>false</i> will be returned if either argument is not a scalar as defined by <i>Type.isScalar()</i>.
     * If both are scalars the return value is <i>true</i> if any of the following are true.
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
    boolean isCopyArrayCompatible(Array from, Array to);
    /**
     * Convert from a source <i>PV</i> array to a destination <i>PV</i> array.
     * @param from The source array.
     * @param offset Starting element in the source.
     * @param to The destination array.
     * @param toOffset Starting element in the array.
     * @param length Number of elements to transfer.
     * @return Number of elements converted.
     * @throws IllegalArgumentException if the arguments are not compatible.
     */
    int copyArray(PVArray from, int offset, PVArray to, int toOffset, int length);
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
     * NOTE: Only compatible fields are copied. This means:
     * <ul>
     *    <li>For scalar fields this means that isCopyScalarCompatible is true.</li>
     *    <li>For array fields this means that isCopyArrayCompatible is true.</li>
     *    <li>For structure fields this means that isCopyStructureCompatible is true.</li>
     *    <li>Link fields are not copied.</li>
     * </ul>
     * @param from The source.
     * @param to The destination.
     * @throws IllegalArgumentException if the arguments are not compatible.
     */
    void copyStructure(PVStructure from, PVStructure to);
    /**
     * Convert a <i>PV</i> to a <byte>.
     * @param pv a PV
     * @return converted value
     */
    byte toByte(PVScalar pv);
    /**
     * Convert a <i>PV</i> to a <i>short</i>.
     * @param pv a PV
     * @return converted value
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    short toShort(PVScalar pv);
    /**
     * Convert a <i>PV</i> to an <i>int</i>
     * @param pv a PV
     * @return converted value
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    int   toInt(PVScalar pv);
    /**
     * Convert a <i>PV</i> to a <i>long</i>
     * @param pv a PV
     * @return converted value
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    long  toLong(PVScalar pv);
    /**
     * Convert a <i>PV</i> to a <i>float</i>
     * @param pv a PV
     * @return converted value
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    float toFloat(PVScalar pv);
    /**
     * Convert a <i>PV</i> to a <i>double</i>
     * @param pv a PV
     * @return converted value
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    double toDouble(PVScalar pv);
    /**
     * Convert a <i>PV</i> from a <i>byte</i>
     * @param pv a PV
     * @param from value to put into PV
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    void fromByte(PVScalar pv, byte from);
    /**
     * Convert a <i>PV</i> from a <i>short</i>
     * @param pv a PV
     * @param from value to put into PV
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    void  fromShort(PVScalar pv, short from);
    /**
     * Convert a <i>PV</i> from an <i>int</i>
     * @param pv a PV
     * @param from value to put into PV
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    void  fromInt(PVScalar pv, int from);
    /**
     * Convert a <i>PV</i> from a <i>long</i>
     * @param pv a PV
     * @param from value to put into PV
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    void  fromLong(PVScalar pv, long from);
    /**
     * Convert a <i>PV</i> from a <i>float</i>
     * @param pv a PV
     * @param from value to put into PV
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    void  fromFloat(PVScalar pv, float from);
    /**
     * Convert a <i>PV</i> from a <i>double</i>
     * @param pv a PV
     * @param from value to put into PV
     * @throws IllegalArgumentException if the Type is not a numeric scalar
     */
    void  fromDouble(PVScalar pv, double from);
    /**
     * Convert a <i>PV</i> array to a <i>byte</i> array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param to where to put the <i>PV</i> data
     * @param toOffset starting element in the array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int toByteArray(PVArray pv, int offset, int length, byte[]to, int toOffset);
    /**
     * Convert a <i>PV</i> array to a <i>short</i> array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param to where to put the <i>PV</i> data
     * @param toOffset starting element in the array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int toShortArray(PVArray pv, int offset, int length, short[]to, int toOffset);
    /**
     * Convert a <i>PV</i> array to an <i>int</i> array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param to where to put the <i>PV</i> data
     * @param toOffset starting element in the array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int toIntArray(PVArray pv, int offset, int length, int[]to, int toOffset);
    /**
     * Convert a <i>PV</i> array to a <i>long</i> array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param to where to put the <i>PV</i> data
     * @param toOffset starting element in the array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int toLongArray(PVArray pv, int offset, int length, long[]to, int toOffset);
    /**
     * Convert a <i>PV</i> array to a <i>float</i> array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param to where to put the <i>PV</i> data
     * @param toOffset starting element in the array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int toFloatArray(PVArray pv, int offset, int length, float[]to, int toOffset);
    /**
     * Convert a <i>PV</i> array to a <i>double</i> array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param to where to put the <i>PV</i> data
     * @param toOffset starting element in the array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int toDoubleArray(PVArray pv, int offset, int length, double[]to, int toOffset);
    /**
     * Convert a <i>PV</i> array from a <i>byte</i> array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param from value to put into PV
     * @param fromOffset
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int fromByteArray(PVArray pv, int offset, int length, byte[]from, int fromOffset);
    /**
     * Convert a <i>PV</i> array from a <i>short</i> array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param from value to put into PV
     * @param fromOffset starting element in the source array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int fromShortArray(PVArray pv, int offset, int length, short[]from, int fromOffset);
    /**
     * Convert a <i>PV</i> array from an <i>int</i> array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param from value to put into PV
     * @param fromOffset starting element in the source array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int fromIntArray(PVArray pv, int offset, int length, int[]from, int fromOffset);
    /**
     * Convert a <i>PV</i> array from a <i>long</i> array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param from value to put into PV
     * @param fromOffset starting element in the source array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int fromLongArray(PVArray pv, int offset, int length, long[]from, int fromOffset);
    /**
     * Convert a <i>PV</i> array from a <i>float</i> array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param from value to put into PV
     * @param fromOffset starting element in the source array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int fromFloatArray(PVArray pv, int offset, int length, float[]from, int fromOffset);
    /**
     * Convert a <i>PV</i> array from a <i>double</i> array.
     * @param pv a PV
     * @param offset starting element in a PV
     * @param length number of elements to transfer
     * @param from value to put into PV
     * @param fromOffset starting element in the source array
     * @return number of elements converted
     * @throws IllegalArgumentException if the element type is not numeric
     */
    int fromDoubleArray(PVArray pv, int offset, int length, double[]from, int fromOffset);
    /**
     * Convenience method for implementing toString.
     * It generates a newline and inserts blanks at the beginning of the newline.
     * @param builder The StringBuilder being constructed.
     * @param indentLevel Indent level, Each level is four spaces.
     */
    void newLine(StringBuilder builder, int indentLevel);
}
