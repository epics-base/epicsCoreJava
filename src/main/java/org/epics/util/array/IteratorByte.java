/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 * An iterator of {@code byte}s.
 *
 * @author Gabriele Carcassi
 */
public interface IteratorByte extends IteratorNumber {

    @Override
    public default float nextFloat() {
        return (float) nextByte();
    }

    @Override
    public default double nextDouble() {
        return (double) nextByte();
    }

    @Override
    public default short nextShort() {
        return (short) nextByte();
    }

    @Override
    public default int nextInt() {
        return (int) nextByte();
    }

    @Override
    public default long nextLong() {
        return (long) nextByte();
    }

}
