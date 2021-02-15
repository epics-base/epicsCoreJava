/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.number;

import java.math.BigInteger;

/**
 * Conversion utilities between unsigned primitives and standard Java
 * primitives.
 * <p>
 * Since we need all the combinations and since the standard libraries provide
 * only some, we chose to re-implement all the cases here for readability
 * and convenience. Note that the only conversions that are different
 * are the widening from unsigned to signed. All other conversions are
 * the same.
 *
 */
public class UnsignedConversions {

    private UnsignedConversions() {
        // Do not instantiate
    }

    /**
     * Converts an unsigned byte to a short.
     *
     * @param unsignedValue unsigned value
     * @return the converted value
     */
    public static short toShort(byte unsignedValue) {
        return (short) (unsignedValue & 0xff);
    }

    /**
     * Converts an unsigned byte to a int.
     *
     * @param unsignedValue unsigned value
     * @return the converted value
     */
    public static int toInt(byte unsignedValue) {
        return (int) (unsignedValue & 0xff);
    }

    /**
     * Converts an unsigned short to a int.
     *
     * @param unsignedValue unsigned value
     * @return the converted value
     */
    public static int toInt(short unsignedValue) {
        return (int) (unsignedValue & 0xffff);
    }

    /**
     * Converts an unsigned byte to a long.
     *
     * @param unsignedValue unsigned value
     * @return the converted value
     */
    public static long toLong(byte unsignedValue) {
        return unsignedValue & 0xffL;
    }

    /**
     * Converts an unsigned short to a long.
     *
     * @param unsignedValue unsigned value
     * @return the converted value
     */
    public static long toLong(short unsignedValue) {
        return unsignedValue & 0xffff;
    }

    /**
     * Converts an unsigned int to a long.
     *
     * @param unsignedValue unsigned value
     * @return the converted value
     */
    public static long toLong(int unsignedValue) {
        return unsignedValue & 0xffffffffL;
    }

    /**
     * Converts an unsigned byte to a float.
     *
     * @param unsignedValue unsigned value
     * @return the converted value
     */
    public static float toFloat(byte unsignedValue) {
        return (float) (unsignedValue & 0xff);
    }

    /**
     * Converts an unsigned short to a float.
     *
     * @param unsignedValue unsigned value
     * @return the converted value
     */
    public static float toFloat(short unsignedValue) {
        return (float) (unsignedValue & 0xffff);
    }

    /**
     * Converts an unsigned int to a float.
     *
     * @param unsignedValue unsigned value
     * @return the converted value
     */
    public static float toFloat(int unsignedValue) {
        return (float) (unsignedValue & 0xffffffffL);
    }

    /**
     * Converts an unsigned long to a float.
     *
     * @param unsignedValue unsigned value
     * @return the converted value
     */
    public static float toFloat(long unsignedValue) {
        float result = (float) (unsignedValue & 0x7fffffffffffffffL);
        if (unsignedValue < 0) {
          result += 0x1.0p63f;
        }
        return result;
    }

    /**
     * Converts an unsigned byte to a double.
     *
     * @param unsignedValue unsigned value
     * @return the converted value
     */
    public static double toDouble(byte unsignedValue) {
        return (double) (unsignedValue & 0xff);
    }

    /**
     * Converts an unsigned short to a double.
     *
     * @param unsignedValue unsigned value
     * @return the converted value
     */
    public static double toDouble(short unsignedValue) {
        return (double) (unsignedValue & 0xffff);
    }

    /**
     * Converts an unsigned int to a double.
     *
     * @param unsignedValue unsigned value
     * @return the converted value
     */
    public static double toDouble(int unsignedValue) {
        return (double) (unsignedValue & 0xffffffffL);
    }

    /**
     * Converts an unsigned long to a double.
     *
     * @param unsignedValue unsigned value
     * @return the converted value
     */
    public static double toDouble(long unsignedValue) {
        double result = (double) (unsignedValue & 0x7fffffffffffffffL);
        if (unsignedValue < 0) {
          result += 0x1.0p63;
        }
        return result;
    }

    private static final BigInteger LONG_BASE = BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE).multiply(BigInteger.valueOf(2));

    /**
     * Converts an unsigned long to a {@code BigInteger}.
     *
     * @param unsignedValue unsigned value
     * @return the converted value
     */
    public static BigInteger toBigInteger(long unsignedValue) {
        BigInteger result = BigInteger.valueOf(unsignedValue);
        if (unsignedValue < 0) {
            result = result.add(LONG_BASE);
        }
        return result;
    }
}
