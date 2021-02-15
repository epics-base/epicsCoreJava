/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An iterator of {@code int}s.
 */
public abstract class IteratorInteger implements IteratorNumber {

    public float nextFloat() {
        return (float) nextInt();
    }

    public double nextDouble() {
        return (double) nextInt();
    }

    public byte nextByte() {
        return (byte) nextInt();
    }

    public short nextShort() {
        return (short) nextInt();
    }

    public long nextLong() {
        return (long) nextInt();
    }
}
