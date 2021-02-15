/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.epics.util.number.UnsignedConversions;

/**
 * An iterator of unsigned {@code int}s.
 *
 */
public abstract class IteratorUInteger implements IteratorNumber {

    public  float nextFloat() {
        return UnsignedConversions.toFloat(nextInt());
    }

    public  double nextDouble() {
        return UnsignedConversions.toDouble(nextInt());
    }

    public  byte nextByte() {
        return (byte) nextInt();
    }

    public  short nextShort() {
        return (short) nextInt();
    }

    public  long nextLong() {
        return UnsignedConversions.toLong(nextInt());
    }

}
