/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.epics.util.number.UnsignedConversions;

/**
 * An iterator of unsigned {@code long}s.
 *
 */
public abstract class IteratorULong implements IteratorNumber {

    public  float nextFloat() {
        return UnsignedConversions.toFloat(nextLong());
    }

    public  double nextDouble() {
        return UnsignedConversions.toDouble(nextLong());
    }

    public  byte nextByte() {
        return (byte) nextLong();
    }

    public  short nextShort() {
        return (short) nextLong();
    }

    public  int nextInt() {
        return (int) nextLong();
    }

}
