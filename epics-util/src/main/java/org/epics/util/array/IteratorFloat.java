/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An iterator of {@code float}s.
 *
 */
public interface IteratorFloat extends IteratorNumber {

    @Override
    public default double nextDouble() {
        return (double) nextFloat();
    }

    @Override
    public default byte nextByte() {
        return (byte) nextFloat();
    }

    @Override
    public default short nextShort() {
        return (short) nextFloat();
    }

    @Override
    public default int nextInt() {
        return (int) nextFloat();
    }

    @Override
    public default long nextLong() {
        return (long) nextFloat();
    }

}
