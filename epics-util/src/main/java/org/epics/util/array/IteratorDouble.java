/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An iterator of {@code double}s.
 *
 */
public abstract class IteratorDouble implements IteratorNumber {

    public float nextFloat() {
        return (float) nextDouble();
    }

    public byte nextByte() {
        return (byte) nextDouble();
    }

    public short nextShort() {
        return (short) nextDouble();
    }

    public int nextInt() {
        return (int) nextDouble();
    }

    public long nextLong() {
        return (long) nextDouble();
    }

}
