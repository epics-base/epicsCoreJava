/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.number;

/**
 * A wrapper for a unsigned {@code int} value.
 *
 * @author carcassi
 */
public final class UInteger extends Number {

    private final int unsignedValue;

    /**
     * Constructs a newly allocated {@code UInteger} object that represent
     * the specified unsigned {@code int} value.
     *
     * @param unsignedValue the value to be represented by the {@code UInteger}
     */
    public UInteger(int unsignedValue) {
        this.unsignedValue = unsignedValue;
    }

    @Override
    public int intValue() {
        return unsignedValue;
    }

    @Override
    public long longValue() {
        return UnsignedConversions.toLong(unsignedValue);
    }

    @Override
    public float floatValue() {
        return UnsignedConversions.toFloat(unsignedValue);
    }

    @Override
    public double doubleValue() {
        return UnsignedConversions.toDouble(unsignedValue);
    }

    /**
     * A wrapper for the given unsigned {@code int}.
     *
     * @param unsignedValue an unsigned {@code int} value
     * @return a {@code UInteger} instance representing {@code unsignedValue}
     */
    public static UInteger valueOf(int unsignedValue) {
        return new UInteger(unsignedValue);
    }

    /**
     * Returns a new {@code String} object representing the
     * specified unsigned {@code int}. The radix is assumed to be 10.
     *
     * @param unsignedValue the unsigned {@code int} to be converted
     * @return the string representation of the specified unsigned {@code int}
     */
    public static String toString(int unsignedValue) {
        return ULong.toString(toUnsignedLong(unsignedValue));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof UInteger) {
            return unsignedValue == ((UInteger)obj).unsignedValue;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return unsignedValue;
    }

    @Override
    public String toString() {
        return UInteger.toString(unsignedValue);
    }

    /**
     * From Java 12 implementation
     *
     * @param  x the value to convert to an unsigned {@code long}
     * @return the argument converted to {@code long} by an unsigned
     *         conversion
     * @since 1.8
     */
    public static long toUnsignedLong(int x) {
        return ((long) x) & 0xffffffffL;
    }

}
