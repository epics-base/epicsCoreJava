/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.epics.util.number.UnsignedConversions;

/**
 * An iterator of unsigned {@code byte}s.
 *
 */
public abstract class IteratorUByte implements IteratorNumber {

    public  float nextFloat() {
        return UnsignedConversions.toFloat(nextByte());
    }

    public  double nextDouble() {
        return UnsignedConversions.toDouble(nextByte());
    }

    public  short nextShort() {
        return UnsignedConversions.toShort(nextByte());
    }

    public  int nextInt() {
        return UnsignedConversions.toInt(nextByte());
    }

    public  long nextLong() {
        return UnsignedConversions.toLong(nextByte());
    }

}
