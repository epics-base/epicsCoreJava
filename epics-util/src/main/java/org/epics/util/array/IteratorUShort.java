/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.epics.util.number.UnsignedConversions;

/**
 * An iterator of unsigned {@code short}s.
 *
 */
public abstract class IteratorUShort implements IteratorNumber {

    public  float nextFloat() {
        return UnsignedConversions.toFloat(nextShort());
    }

    public  double nextDouble() {
        return UnsignedConversions.toDouble(nextShort());
    }

    public  byte nextByte() {
        return (byte) nextShort();
    }

    public  int nextInt() {
        return UnsignedConversions.toInt(nextShort());
    }

    public  long nextLong() {
        return UnsignedConversions.toLong(nextShort());
    }

}
