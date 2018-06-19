/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.epics.util.number.UnsignedConversions;

/**
 * An iterator of unsigned {@code int}s.
 *
 */
public interface IteratorUInteger extends IteratorNumber {

    @Override
    public default float nextFloat() {
        return UnsignedConversions.toFloat(nextInt());
    }

    @Override
    public default double nextDouble() {
        return UnsignedConversions.toDouble(nextInt());
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
        return UnsignedConversions.toLong(nextInt());
    }

}
