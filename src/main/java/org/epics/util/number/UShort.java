/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.util.number;

/**
 * A wrapper for a unsigned short value.
 *
 * @author carcassi
 */
public final class UShort extends Number {
    
    private final short unsignedValue;

    /**
     * Constructs a newly allocated UByte object that represent the specified unsigned short value.
     * 
     * @param unsignedValue the unsigned value
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
     * A wrapper for the given unsigned short.
     * 
     * @param unsignedValue an unsigned short
     * @return the new wrapper
     */
    public static UShort valueOf(short unsignedValue) {
        return new UShort(unsignedValue);
    }
}
