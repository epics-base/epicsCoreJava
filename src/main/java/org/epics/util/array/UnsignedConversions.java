/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.util.array;

/**
 * Conversion utilities between unsigned primitives and standard Java
 * primitives.
 * <p>
 * Since we need all the combinations and since the standard libraries provide
 * only some, we chose to re-implement all the cases here for readability
 * and convenience.
 *
 */
public class UnsignedConversions {

    private UnsignedConversions() {
        // Do not instantiate
    }
    
    /**
     * Converts an unsigned byte to a short.
     * 
     * @param value unsigned value
     * @return the converted value
     */
    public static short toShort(byte value) {
        return (short) (value & 0xff);
    }
    
    /**
     * Converts an unsigned byte to a int.
     * 
     * @param value unsigned value
     * @return the converted value
     */
    public static int toInt(byte value) {
        return (int) (value & 0xff);
    }
    
    /**
     * Converts an unsigned short to a int.
     * 
     * @param value unsigned value
     * @return the converted value
     */
    public static int toInt(short value) {
        return (int) (value & 0xffff);
    }
    
    /**
     * Converts an unsigned byte to a long.
     * 
     * @param value unsigned value
     * @return the converted value
     */
    public static long toLong(byte value) {
        return value & 0xffL;
    }
    
    /**
     * Converts an unsigned short to a long.
     * 
     * @param value unsigned value
     * @return the converted value
     */
    public static long toLong(short value) {
        return value & 0xffff;
    }
    
    /**
     * Converts an unsigned int to a long.
     * 
     * @param value unsigned value
     * @return the converted value
     */
    public static long toLong(int value) {
        return value & 0xffffffffL;
    }
    
    /**
     * Converts an unsigned byte to a float.
     * 
     * @param value unsigned value
     * @return the converted value
     */
    public static float toFloat(byte value) {
        return (float) (value & 0xff);
    }
    
    /**
     * Converts an unsigned short to a float.
     * 
     * @param value unsigned value
     * @return the converted value
     */
    public static float toFloat(short value) {
        return (float) (value & 0xffff);
    }
    
    /**
     * Converts an unsigned int to a float.
     * 
     * @param value unsigned value
     * @return the converted value
     */
    public static float toFloat(int value) {
        return (float) (value & 0xffffffffL);
    }
    
    /**
     * Converts an unsigned long to a float.
     * 
     * @param value unsigned value
     * @return the converted value
     */
    public static float toFloat(long value) {
        float result = (float) (value & 0x7fffffffffffffffL);
        if (value < 0) {
          result += 0x1.0p63f;
        }
        return result;
    }
    
    /**
     * Converts an unsigned byte to a double.
     * 
     * @param value unsigned value
     * @return the converted value
     */
    public static double toDouble(byte value) {
        return (double) (value & 0xff);
    }
    
    /**
     * Converts an unsigned short to a double.
     * 
     * @param value unsigned value
     * @return the converted value
     */
    public static double toDouble(short value) {
        return (double) (value & 0xffff);
    }
    
    /**
     * Converts an unsigned int to a double.
     * 
     * @param value unsigned value
     * @return the converted value
     */
    public static double toDouble(int value) {
        return (double) (value & 0xffffffffL);
    }
    
    /**
     * Converts an unsigned long to a double.
     * 
     * @param value unsigned value
     * @return the converted value
     */
    public static double toDouble(long value) {
        double result = (double) (value & 0x7fffffffffffffffL);
        if (value < 0) {
          result += 0x1.0p63;
        }
        return result;
    }
}
