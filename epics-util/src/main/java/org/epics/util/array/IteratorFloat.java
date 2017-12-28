/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
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
