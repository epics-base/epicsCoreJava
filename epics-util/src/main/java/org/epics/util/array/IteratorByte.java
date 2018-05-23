/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An iterator of {@code byte}s.
 *
 */
public interface IteratorByte extends IteratorNumber {

    @Override
    public default float nextFloat() {
        return (float) nextByte();
    }

    @Override
    public default double nextDouble() {
        return (double) nextByte();
    }

    @Override
    public default short nextShort() {
        return (short) nextByte();
    }

    @Override
    public default int nextInt() {
        return (int) nextByte();
    }

    @Override
    public default long nextLong() {
        return (long) nextByte();
    }

}
