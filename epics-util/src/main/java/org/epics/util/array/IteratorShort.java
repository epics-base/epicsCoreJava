/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An iterator of {@code short}s.
 *
 */
public abstract class IteratorShort implements IteratorNumber {

    public  float nextFloat() {
        return (float) nextShort();
    }

    public  double nextDouble() {
        return (double) nextShort();
    }

    public  byte nextByte() {
        return (byte) nextShort();
    }

    public  int nextInt() {
        return (int) nextShort();
    }

    public  long nextLong() {
        return (long) nextShort();
    }

}
