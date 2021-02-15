/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An iterator of {@code byte}s.
 *
 */
public abstract class IteratorByte implements IteratorNumber {

    public float nextFloat() {
        return (float) nextByte();
    }

    public double nextDouble() {
        return (double) nextByte();
    }

    public short nextShort() {
        return (short) nextByte();
    }

    public int nextInt() {
        return (int) nextByte();
    }

    public long nextLong() {
        return (long) nextByte();
    }

}
