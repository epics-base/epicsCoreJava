/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.epics.util.number.UnsignedConversions;

/**
 * An iterator of unsigned {@code byte}s.
 *
 */
public interface IteratorUByte extends IteratorNumber {

    @Override
    public default float nextFloat() {
        return UnsignedConversions.toFloat(nextByte());
    }

    @Override
    public default double nextDouble() {
        return UnsignedConversions.toDouble(nextByte());
    }

    @Override
    public default short nextShort() {
        return UnsignedConversions.toShort(nextByte());
    }

    @Override
    public default int nextInt() {
        return UnsignedConversions.toInt(nextByte());
    }

    @Override
    public default long nextLong() {
        return UnsignedConversions.toLong(nextByte());
    }

}
