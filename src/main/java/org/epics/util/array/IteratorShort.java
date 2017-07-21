/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 * An iterator of {@code short}s.
 *
 * @author Gabriele Carcassi
 */
public interface IteratorShort extends IteratorNumber {

    @Override
    public default float nextFloat() {
        return (float) nextShort();
    }

    @Override
    public default double nextDouble() {
        return (double) nextShort();
    }

    @Override
    public default byte nextByte() {
        return (byte) nextShort();
    }

    @Override
    public default int nextInt() {
        return (int) nextShort();
    }

    @Override
    public default long nextLong() {
        return (long) nextShort();
    }

}
