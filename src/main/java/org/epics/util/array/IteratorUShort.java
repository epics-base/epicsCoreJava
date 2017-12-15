/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
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
