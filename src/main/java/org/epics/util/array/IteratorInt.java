/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 * An iterator of {@code int}s.
 *
 */
public interface IteratorInt extends IteratorNumber {

    @Override
    public default float nextFloat() {
        return (float) nextInt();
    }

    @Override
    public default double nextDouble() {
        return (double) nextInt();
    }

    @Override
    public default byte nextByte() {
        return (byte) nextInt();
    }

    @Override
    public default short nextShort() {
        return (short) nextInt();
    }

    @Override
    public default long nextLong() {
        return (long) nextInt();
    }

}
