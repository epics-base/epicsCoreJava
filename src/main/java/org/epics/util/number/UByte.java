/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.util.number;

/**
 * A wrapper for a unsigned byte value.
 *
 * @author carcassi
 */
public final class UByte extends Number {
    
    private final byte unsignedValue;

    /**
     * Constructs a newly allocated UByte object that represent the specified unsigned byte value.
     * 
     * @param unsignedValue the unsigned value
     */
    public UByte(byte unsignedValue) {
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
     * A wrapper for the given unsigned byte.
     * 
     * @param unsignedValue an unsigned byte
     * @return the new wrapper
     */
    public static UByte valueOf(byte unsignedValue) {
        return new UByte(unsignedValue);
    }

    /**
     * Returns a new {@code String} object representing the
     * specified unsigned {@code byte}. The radix is assumed to be 10.
     *
     * @param unsignedByte the unsigned {@code byte} to be converted
     * @return the string representation of the specified unsigned byte {@code byte}
     */
    public static String toString(byte unsignedByte) {
        return Integer.toString(UnsignedConversions.toInt(unsignedByte), 10);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UByte) {
            return unsignedValue == ((UByte)obj).unsignedValue;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Byte.hashCode(unsignedValue);
    }

    @Override
    public String toString() {
        return UByte.toString(unsignedValue);
    }
}
