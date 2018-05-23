/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.epics.util.number.UnsignedConversions;

/**
 * An iterator of unsigned {@code long}s.
 *
 */
public interface IteratorULong extends IteratorNumber {

    @Override
    public default float nextFloat() {
        return UnsignedConversions.toFloat(nextLong());
    }

    @Override
    public default double nextDouble() {
        return UnsignedConversions.toDouble(nextLong());
    }

    @Override
    public default byte nextByte() {
        return (byte) nextLong();
    }

    @Override
    public default short nextShort() {
        return (short) nextLong();
    }

    @Override
    public default int nextInt() {
        return (int) nextLong();
    }

}
