/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 * An iterator of {@code long}s.
 *
 * @author Gabriele Carcassi
 */
public interface IteratorLong extends IteratorNumber {

    @Override
    public default float nextFloat() {
        return (float) nextLong();
    }

    @Override
    public default double nextDouble() {
        return (double) nextLong();
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
