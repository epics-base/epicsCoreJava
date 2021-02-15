/*
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
     * <p>
     * Implementation adapted from Java 12
     *
     * @param unsignedValue the unsigned {@code long} to be converted
     * @return the string representation of the specified unsigned {@code long}
     */
    public static String toString(long unsignedValue) {
        if (unsignedValue >= 0)
            return Long.toString(unsignedValue, 10);
        else {/*
         * We can get the effect of an unsigned division by 10
         * on a long value by first shifting right, yielding a
         * positive value, and then dividing by 5.  This
         * allows the last digit and preceding digits to be
         * isolated more quickly than by an initial conversion
         * to BigInteger.
         */
            long quot = (unsignedValue >>> 1) / 5;
            long rem = unsignedValue - quot * 10;
            return toString(quot) + rem;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof ULong) {
            return unsignedValue == ((ULong) obj).unsignedValue;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (int) (unsignedValue ^ (unsignedValue >>> 32));
    }

    @Override
    public String toString() {
        return ULong.toString(unsignedValue);
    }
}
