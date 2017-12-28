/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
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
