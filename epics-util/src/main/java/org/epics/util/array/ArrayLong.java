/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import java.io.Serializable;

/**
 * Non-resizable {@link ListLong} implementation backed by a {@code long[]}.
 */
public final class ArrayLong extends ListLong implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long[] array;
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
    public ArrayLong(CollectionNumber coll) {
        this(coll.toArray(new long[coll.size()]), 0, coll.size(), false);
    }

    /**
     * A new {@code ArrayLong} that wraps around the given array.
     *
     * @param array an array
     * @param startIndex first element
     * @param size number of elements
     * @param readOnly if false the wrapper allows writes to the array
     * @throws IndexOutOfBoundsException if startIndex and size are out of range
     *         (@code{startIndex < 0 || startIndex + size > array.length})
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

                    public boolean hasNext() {
                return index < startIndex + size;
            }

                    public long nextLong() {
                return array[index++];
            }
        };
    }

    public final int size() {
        return size;
    }

    public long getLong(int index) {
        if (checkBoundaries) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
        }
        return array[startIndex + index];
    }

    @Override
    public void setLong(int index, long value) {
        checkBounds(index, readOnly, checkBoundaries, size);
        array[startIndex + index] = value;
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

    int startIndex() {
        return startIndex;
    }

    boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Returns an unmodifiable {@link ArrayLong} wrapper for the given {@code long} array.
     *
     * @param values a primitive array.
     * @return an immutable wrapper.
     */
    public static ArrayLong of(long... values) {
        return CollectionNumbers.unmodifiableListLong(values);
    }
}
