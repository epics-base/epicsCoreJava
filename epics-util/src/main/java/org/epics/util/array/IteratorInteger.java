/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An iterator of {@code int}s.
 *
 */
public interface IteratorInteger extends IteratorNumber {

    @Override
    public default float nextFloat() {
        return (float) nextInt();
    }

    @Override
    public default double nextDouble() {
        return (double) nextInt();
    }

    @Override
    public default byte nextByte() {
        return (byte) nextInt();
    }

    @Override
    public default short nextShort() {
        return (short) nextInt();
    }

    @Override
    public default long nextLong() {
        return (long) nextInt();
    }

}
