/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An iterator of {@code double}s.
 *
 */
public interface IteratorDouble extends IteratorNumber {

    @Override
    public default float nextFloat() {
        return (float) nextDouble();
    }

    @Override
    public default byte nextByte() {
        return (byte) nextDouble();
    }

    @Override
    public default short nextShort() {
        return (short) nextDouble();
    }

    @Override
    public default int nextInt() {
        return (int) nextDouble();
    }

    @Override
    public default long nextLong() {
        return (long) nextDouble();
    }

}
