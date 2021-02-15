/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import java.io.Serializable;

/**
 * Non-resizable {@link ListInteger} implementation backed by a {@code int[]}.
 */
public final class ArrayInteger extends ListInteger implements Serializable {

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
    public ArrayInteger(CollectionNumber coll) {
        this(coll.toArray(new int[coll.size()]), 0, coll.size(), false);
    }

    /**
     * A new {@code ArrayDouble} that wraps around the given array.
     *
     * @param array      an array
     * @param startIndex first element
     * @param size       number of elements
     * @param readOnly   if false the wrapper allows writes to the array
     * @throws IndexOutOfBoundsException if startIndex and size are out of range
     *                                   (@code{startIndex < 0 || startIndex + size > array.length})
     */
    ArrayInteger(int[] array, int startIndex, int size, boolean readOnly) {
        if (startIndex < 0 || startIndex + size > array.length)
            throw new IndexOutOfBoundsException("Start index: " + startIndex + ", Size: " + size + ", Array length: " + array.length);
        this.array = array;
        this.readOnly = readOnly;
        this.startIndex = startIndex;
        this.size = size;
        this.checkBoundaries = startIndex != 0 || size != array.length;
    }

    public final IteratorInteger iterator() {
        return new IteratorInteger() {

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

    public void setInt(int index, int value) {
        checkBounds(index, readOnly, checkBoundaries, size);
        array[startIndex + index] = value;
    }

    public ArrayInteger subList(int fromIndex, int toIndex) {
        return new ArrayInteger(array, fromIndex + startIndex, toIndex - fromIndex, readOnly);
    }

    @Override
    public void setAll(int index, ListNumber list) {
        if (list instanceof ArrayInteger) {
            if (readOnly) {
                throw new UnsupportedOperationException("Read only list.");
            }
            ArrayInteger other = (ArrayInteger) list;
            System.arraycopy(other.array, other.startIndex, array, startIndex + index, other.size);
        } else {
            super.setAll(index, list);
        }
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof ArrayInteger) {
            ArrayInteger other = (ArrayInteger) obj;

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
     * Returns an unmodifiable {@link ArrayInteger} wrapper for the given {@code int} array.
     *
     * @param values a primitive array.
     * @return an immutable wrapper.
     */
    public static ArrayInteger of(int... values) {
        return CollectionNumbers.unmodifiableListInt(values);
    }
}
