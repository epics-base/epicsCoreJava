/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.util.number;

/**
 * A wrapper for a unsigned int value.
 *
 * @author carcassi
 */
public final class UInteger extends Number {
    
    private final int unsignedValue;

    /**
     * Constructs a newly allocated UByte object that represent the specified unsigned int value.
     * 
     * @param unsignedValue the unsigned value
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
     * A wrapper for the given unsigned int.
     * 
     * @param unsignedValue an unsigned int
     * @return the new wrapper
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
        return Integer.toUnsignedString(unsignedValue, 10);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UInteger) {
            return unsignedValue == ((UInteger)obj).unsignedValue;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(unsignedValue);
    }

    @Override
    public String toString() {
        return UInteger.toString(unsignedValue);
    }
}
