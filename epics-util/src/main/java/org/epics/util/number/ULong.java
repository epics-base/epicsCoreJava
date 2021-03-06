/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.number;

import java.math.BigInteger;

/**
 * A wrapper for a unsigned {@code long} value.
 *
 * @author carcassi
 */
public final class ULong extends Number {
    
    private final long unsignedValue;

    /**
     * Constructs a newly allocated {@code ULong} object that represent
     * the specified unsigned {@code long} value.
     * 
     * @param unsignedValue the value to be represented by the {@code ULong}
     */
    public ULong(long unsignedValue) {
        this.unsignedValue = unsignedValue;
    }

    @Override
    public int intValue() {
        return (int) unsignedValue;
    }

    @Override
    public long longValue() {
        return unsignedValue;
    }

    @Override
    public float floatValue() {
        return UnsignedConversions.toFloat(unsignedValue);
    }

    @Override
    public double doubleValue() {
        return UnsignedConversions.toDouble(unsignedValue);
    }

    public BigInteger bigIntegerValue() {
        return UnsignedConversions.toBigInteger(unsignedValue);
    }
    
    /**
     * A wrapper for the given unsigned {@code long}.
     * 
     * @param unsignedValue an unsigned {@code long} value
     * @return a {@code ULong} instance representing {@code unsignedValue}
     */
    public static ULong valueOf(long unsignedValue) {
        return new ULong(unsignedValue);
    }

    /**
     * Returns a new {@code String} object representing the
     * specified unsigned {@code long}. The radix is assumed to be 10.
     *
     * @param unsignedValue the unsigned {@code long} to be converted
     * @return the string representation of the specified unsigned {@code long}
     */
    public static String toString(long unsignedValue) {
        return Long.toUnsignedString(unsignedValue, 10);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ULong) {
            return unsignedValue == ((ULong)obj).unsignedValue;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(unsignedValue);
    }

    @Override
    public String toString() {
        return ULong.toString(unsignedValue);
    }
}
