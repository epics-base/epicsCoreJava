/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An iterator of {@code short}s.
 *
 */
public interface IteratorShort extends IteratorNumber {

    @Override
    public default float nextFloat() {
        return (float) nextShort();
    }

    @Override
    public default double nextDouble() {
        return (double) nextShort();
    }

    @Override
    public default byte nextByte() {
        return (byte) nextShort();
    }

    @Override
    public default int nextInt() {
        return (int) nextShort();
    }

    @Override
    public default long nextLong() {
        return (long) nextShort();
    }

}
