/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An iterator of {@code long}s.
 *
 */
public abstract class IteratorLong implements IteratorNumber {

    public  float nextFloat() {
        return (float) nextLong();
    }

    public  double nextDouble() {
        return (double) nextLong();
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
