/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import java.io.Serializable;

/**
 * Non-resizable {@link ListUInteger} implementation backed by a {@code int[]}.
 */
public final class ArrayUInteger extends ListUInteger implements Serializable {

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
    public ArrayUInteger(CollectionNumber coll) {
        this(coll.toArray(new int[coll.size()]), 0, coll.size(), false);
    }

    /**
     * A new {@code ArrayUInteger} that wraps around the given array.
     *
     * @param array      an array
     * @param startIndex first element
     * @param size       number of elements
     * @param readOnly   if false the wrapper allows writes to the array
     * @throws IndexOutOfBoundsException if startIndex and size are out of range
     *                                   (@code{startIndex < 0 || startIndex + size > array.length})
     */
    ArrayUInteger(int[] array, int startIndex, int size, boolean readOnly) {
        if (startIndex < 0 || startIndex + size > array.length)
            throw new IndexOutOfBoundsException("Start index: " + startIndex + ", Size: " + size + ", Array length: " + array.length);
        this.array = array;
        this.readOnly = readOnly;
        this.startIndex = startIndex;
        this.size = size;
        this.checkBoundaries = startIndex != 0 || size != array.length;
    }

    @Override
    public final IteratorUInteger iterator() {
        return new IteratorUInteger() {

            private int index = startIndex;

            public boolean hasNext() {
                return index < startIndex + size;
            }

            public int nextInt() {
                return array[index++];
            }
        };
    }

    public final int size() {
        return size;
    }

    public int getInt(int index) {
        if (checkBoundaries) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
        }
        return array[startIndex + index];
    }

    @Override
    public void setInt(int index, int value) {
        checkBounds(index, readOnly, checkBoundaries, size);
        array[startIndex + index] = value;
    }

    @Override
    public ArrayUInteger subList(int fromIndex, int toIndex) {
        return new ArrayUInteger(array, fromIndex + startIndex, toIndex - fromIndex, readOnly);
    }

    @Override
    public void setAll(int index, ListNumber list) {
        if (list instanceof ArrayUInteger) {
            if (readOnly) {
                throw new UnsupportedOperationException("Read only list.");
            }
            ArrayUInteger other = (ArrayUInteger) list;
            System.arraycopy(other.array, other.startIndex, array, startIndex + index, other.size);
        } else {
            super.setAll(index, list);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof ArrayUInteger) {
            ArrayUInteger other = (ArrayUInteger) obj;

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

    /**
     * Returns an unmodifiable {@link ArrayUInteger} wrapper for the given {@code int} array.
     *
     * @param values a primitive array.
     * @return an immutable wrapper.
     */
    public static ArrayUInteger of(int... values) {
        return CollectionNumbers.unmodifiableListUInt(values);
    }
}
