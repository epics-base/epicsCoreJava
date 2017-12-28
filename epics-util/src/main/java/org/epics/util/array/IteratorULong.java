/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
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
