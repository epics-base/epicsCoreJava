/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.number;

/**
 * A wrapper for a unsigned {@code short} value.
 *
 * @author carcassi
 */
public final class UShort extends Number {

    private final short unsignedValue;

    /**
     * Constructs a newly allocated {@code UShort} object that represent
     * the specified unsigned {@code short} value.
     *
     * @param unsignedValue the value to be represented by the {@code UShort}
     */
    public UShort(short unsignedValue) {
        this.unsignedValue = unsignedValue;
    }

    @Override
    public int intValue() {
        return UnsignedConversions.toInt(unsignedValue);
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
     * A wrapper for the given unsigned {@code short}.
     *
     * @param unsignedValue an unsigned {@code short} value
     * @return a {@code UShort} instance representing {@code unsignedValue}
     */
    public static UShort valueOf(short unsignedValue) {
        return new UShort(unsignedValue);
    }

    /**
     * Returns a new {@code String} object representing the
     * specified unsigned {@code short}. The radix is assumed to be 10.
     *
     * @param unsignedValue the unsigned {@code short} to be converted
     * @return the string representation of the specified unsigned {@code short}
     */
    public static String toString(short unsignedValue) {
        return Integer.toString(UnsignedConversions.toInt(unsignedValue), 10);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UShort)) return false;

        UShort uShort = (UShort) o;

        return unsignedValue == uShort.unsignedValue;
    }

    @Override
    public int hashCode() {
        return unsignedValue;
    }

    @Override
    public String toString() {
        return UShort.toString(unsignedValue);
    }
}
