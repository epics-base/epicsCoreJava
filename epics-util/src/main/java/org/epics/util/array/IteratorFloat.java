/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An iterator of {@code float}s.
 *
 */
public abstract class IteratorFloat implements IteratorNumber {

    public  double nextDouble() {
        return (double) nextFloat();
    }

    public  byte nextByte() {
        return (byte) nextFloat();
    }

    public  short nextShort() {
        return (short) nextFloat();
    }

    public  int nextInt() {
        return (int) nextFloat();
    }

    public  long nextLong() {
        return (long) nextFloat();
    }

}
