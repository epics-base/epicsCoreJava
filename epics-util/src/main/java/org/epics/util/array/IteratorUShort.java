/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.epics.util.number.UnsignedConversions;

/**
 * An iterator of unsigned {@code short}s.
 *
 */
public interface IteratorUShort extends IteratorNumber {

    @Override
    public default float nextFloat() {
        return UnsignedConversions.toFloat(nextShort());
    }

    @Override
    public default double nextDouble() {
        return UnsignedConversions.toDouble(nextShort());
    }

    @Override
    public default byte nextByte() {
        return (byte) nextShort();
    }

    @Override
    public default int nextInt() {
        return UnsignedConversions.toInt(nextShort());
    }

    @Override
    public default long nextLong() {
        return UnsignedConversions.toLong(nextShort());
    }

}
