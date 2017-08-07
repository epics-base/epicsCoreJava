/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Wraps a {@code double[]} into a {@link ListDouble}.
 *
 * @author Gabriele Carcassi
 */
public final class ArrayDouble extends ListDouble implements Serializable {

    private static final long serialVersionUID = 7493025761455302917L;

    private final double[] array;
    private final int startIndex;
    private final int size;
    private final boolean checkBoundaries;
    private final boolean readOnly;

    /**
     * A new read-only {@code ArrayDouble} that wraps around the given array.
     *
     * @param array an array
     */
    public ArrayDouble(double... array) {
        this(array, true);
    }

    /**
     * A new {@code ArrayDouble} that wraps around the given array.
     *
     * @param array an array
     * @param readOnly if false the wrapper allows writes to the array
     */
    public ArrayDouble(double[] array, boolean readOnly) {
        this(array, 0, array.length, readOnly);
    }

    /**
     * A new {@code ArrayDouble} that wraps around the given array.
     *
     * @param array an array
     * @param readOnly if false the wrapper allows writes to the array
     */
    public ArrayDouble(double[] array, int startIndex, int size, boolean readOnly) {
        if (startIndex < 0 || startIndex + size > array.length)
            throw new IndexOutOfBoundsException("Start index: "+startIndex+", Size: "+size+", Array length: "+array.length);
        this.array = array;
        this.readOnly = readOnly;
        this.startIndex = startIndex;
        this.size = size;
        this.checkBoundaries = startIndex != 0 || size != array.length;
    }

    @Override
    public final IteratorDouble iterator() {
        return new IteratorDouble() {

            private int index = startIndex;

            @Override
            public boolean hasNext() {
                return index < startIndex + size;
            }

            @Override
            public double nextDouble() {
                return array[index++];
            }
        };
    }

    @Override
    public final int size() {
        return size;
    }

    @Override
    public double getDouble(int index) {
        if (checkBoundaries) {
            if (index < 0 || index > this.size)
                throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
        }
        return array[startIndex + index];
    }

    @Override
    public void setDouble(int index, double value) {
        if (!readOnly) {
            if (checkBoundaries) {
                if (index < 0 || index > this.size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
            }
            array[startIndex + index] = value;
        } else {
            throw new UnsupportedOperationException("Read only list.");
        }
    }

    @Override
    public ArrayDouble subList(int fromIndex, int toIndex) {
        return new ArrayDouble(array, fromIndex + startIndex, toIndex - fromIndex, readOnly);
    }

    @Override
    public void setAll(int index, ListNumber list) {
        if (list instanceof ArrayDouble) {
            if (readOnly) {
                throw new UnsupportedOperationException("Read only list.");
            }
            ArrayDouble other = (ArrayDouble) list;
            System.arraycopy(other.array, other.startIndex, array, startIndex + index, other.size);
        } else {
            super.setAll(index, list);
        }
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof ArrayDouble) {
            ArrayDouble other = (ArrayDouble) obj;
            return Arrays.equals(array, other.array) && startIndex == other.startIndex && size == other.size;
        }

        return super.equals(obj);
    }

    double[] wrappedArray() {
        return array;
    }
}
