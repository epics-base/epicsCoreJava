/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.util.number;

/**
 * A wrapper for a unsigned long value.
 *
 * @author carcassi
 */
public final class ULong extends Number {
    
    private final long unsignedValue;

    /**
     * Constructs a newly allocated UByte object that represent the specified unsigned long value.
     * 
     * @param unsignedValue the unsigned value
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
    
    /**
     * A wrapper for the given unsigned long.
     * 
     * @param unsignedValue an unsigned long
     * @return the new wrapper
     */
    public static ULong valueOf(long unsignedValue) {
        return new ULong(unsignedValue);
    }
}
