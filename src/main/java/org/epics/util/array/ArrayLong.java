/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Wraps a {@code long[]} into a {@link ListLong}.
 *
 * @author Gabriele Carcassi
 */
public final class ArrayLong extends ListLong implements Serializable {

    private static final long serialVersionUID = 7493025761455302920L;

    private final long[] array;
    private final int startIndex;
    private final int size;
    private final boolean checkBoundaries;
    private final boolean readOnly;
    
    public ArrayLong(ListNumber array) {
        this(array.toArray(new long[array.size()]), false);
    }

    /**
     * A new {@code ArrayLong} that wraps around the given array.
     *
     * @param array an array
     */
    public ArrayLong(long... array) {
        this(array, true);
    }

    /**
     * A new {@code ArrayLong} that wraps around the given array.
     *
     * @param array an array
     * @param readOnly if false the wrapper allows writes to the array
     */
    public ArrayLong(long[] array, boolean readOnly) {
        this(array, 0, array.length, readOnly);
    }

    /**
     * A new {@code ArrayLong} that wraps around the given array.
     *
     * @param array an array
     * @param startIndex first element
     * @param size number of elements
     * @param readOnly if false the wrapper allows writes to the array
     */
    ArrayLong(long[] array, int startIndex, int size, boolean readOnly) {
        if (startIndex < 0 || startIndex + size > array.length)
            throw new IndexOutOfBoundsException("Start index: "+startIndex+", Size: "+size+", Array length: "+array.length);
        this.array = array;
        this.readOnly = readOnly;
        this.startIndex = startIndex;
        this.size = size;
        this.checkBoundaries = startIndex != 0 || size != array.length;
    }

    @Override
    public final IteratorLong iterator() {
        return new IteratorLong() {

            private int index = startIndex;

            @Override
            public boolean hasNext() {
                return index < startIndex + size;
            }

            @Override
            public long nextLong() {
                return array[index++];
            }
        };
    }

    @Override
    public final int size() {
        return size;
    }

    @Override
    public long getLong(int index) {
        if (checkBoundaries) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
        }
        return array[startIndex + index];
    }

    @Override
    public void setLong(int index, long value) {
        if (!readOnly) {
            if (checkBoundaries) {
                if (index < 0 || index >= this.size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
            }
            array[startIndex + index] = value;
        } else {
            throw new UnsupportedOperationException("Read only list.");
        }
    }

    @Override
    public ArrayLong subList(int fromIndex, int toIndex) {
        return new ArrayLong(array, fromIndex + startIndex, toIndex - fromIndex, readOnly);
    }

    @Override
    public void setAll(int index, ListNumber list) {
        if (list instanceof ArrayLong) {
            if (readOnly) {
                throw new UnsupportedOperationException("Read only list.");
            }
            ArrayLong other = (ArrayLong) list;
            System.arraycopy(other.array, other.startIndex, array, startIndex + index, other.size);
        } else {
            super.setAll(index, list);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof ArrayLong) {
            ArrayLong other = (ArrayLong) obj;
            
            if ((array == other.array) && startIndex == other.startIndex && size == other.size)
                return true;
        }

        return super.equals(obj);
    }

    @Override
    public <T> T toArray(T array) {
        if (array instanceof long[]) {
            long[] longArray;
            if (((long[]) array).length < size()) {
                longArray = new long[size()];
            } else {
                longArray = (long[]) array;
            }
            System.arraycopy(this.array, startIndex, longArray, 0, size);
            return (T) longArray;
        }        
        return super.toArray(array);
    }

    long[] wrappedArray() {
        return array;
    }
}
