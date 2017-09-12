/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import java.io.Serializable;

/**
 * Non-resizable {@link ListInt} implementation backed by a {@code int[]}.
 */
public final class ArrayInt extends ListInt implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int[] array;
    private final int startIndex;
    private final int size;
    private final boolean checkBoundaries;
    private final boolean readOnly;

    /**
     * Constructs a list containing the values provided by the specified collection
     * in the order returned by its iterator.
     * 
     * @param coll the collection whose values will be placed in this list
     */    
    public ArrayInt(CollectionNumber coll) {
        this(coll.toArray(new int[coll.size()]), 0, coll.size(), false);
    }

    /**
     * A new {@code ArrayDouble} that wraps around the given array.
     *
     * @param array an array
     * @param startIndex first element
     * @param size number of elements
     * @param readOnly if false the wrapper allows writes to the array
     * @throws IndexOutOfBoundsException if startIndex and size are out of range
     *         (@code{startIndex < 0 || startIndex + size > array.length})
     */
    ArrayInt(int[] array, int startIndex, int size, boolean readOnly) {
        if (startIndex < 0 || startIndex + size > array.length)
            throw new IndexOutOfBoundsException("Start index: "+startIndex+", Size: "+size+", Array length: "+array.length);
        this.array = array;
        this.readOnly = readOnly;
        this.startIndex = startIndex;
        this.size = size;
        this.checkBoundaries = startIndex != 0 || size != array.length;
    }

    @Override
    public final IteratorInt iterator() {
        return new IteratorInt() {

            private int index = startIndex;

            @Override
            public boolean hasNext() {
                return index < startIndex + size;
            }

            @Override
            public int nextInt() {
                return array[index++];
            }
        };
    }

    @Override
    public final int size() {
        return size;
    }

    @Override
    public int getInt(int index) {
        if (checkBoundaries) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
        }
        return array[startIndex + index];
    }

    @Override
    public void setInt(int index, int value) {
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
    public ArrayInt subList(int fromIndex, int toIndex) {
        return new ArrayInt(array, fromIndex + startIndex, toIndex - fromIndex, readOnly);
    }

    @Override
    public void setAll(int index, ListNumber list) {
        if (list instanceof ArrayInt) {
            if (readOnly) {
                throw new UnsupportedOperationException("Read only list.");
            }
            ArrayInt other = (ArrayInt) list;
            System.arraycopy(other.array, other.startIndex, array, startIndex + index, other.size);
        } else {
            super.setAll(index, list);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof ArrayInt) {
            ArrayInt other = (ArrayInt) obj;
            
            if ((array == other.array) && startIndex == other.startIndex && size == other.size)
                return true;
        }

        return super.equals(obj);
    }

    int[] wrappedArray() {
        return array;
    }
    
    int startIndex() {
        return startIndex;
    }
    
    boolean isReadOnly() {
        return readOnly;
    }
}
