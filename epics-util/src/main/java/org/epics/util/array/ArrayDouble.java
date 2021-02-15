/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import java.io.Serializable;

/**
 * Non-resizable {@link ListDouble} implementation backed by a {@code double[]}.
 */
public final class ArrayDouble extends ListDouble implements Serializable {

    private static final long serialVersionUID = 1L;

    private final double[] array;
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
    public ArrayDouble(CollectionNumber coll) {
        this(coll.toArray(new double[coll.size()]), 0, coll.size(), false);
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
    ArrayDouble(double[] array, int startIndex, int size, boolean readOnly) {
        if (startIndex < 0 || startIndex + size > array.length)
            throw new IndexOutOfBoundsException("Start index: " + startIndex + ", Size: " + size + ", Array length: " + array.length);
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

            public boolean hasNext() {
                return index < startIndex + size;
            }

            public double nextDouble() {
                return array[index++];
            }
        };
    }

    public final int size() {
        return size;
    }

    public double getDouble(int index) {
        if (checkBoundaries) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
        }
        return array[startIndex + index];
    }

    @Override
    public void setDouble(int index, double value) {
        checkBounds(index, readOnly, checkBoundaries, size);
        array[startIndex + index] = value;
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
        if (obj == this)
            return true;

        if (obj instanceof ArrayDouble) {
            ArrayDouble other = (ArrayDouble) obj;

            if ((array == other.array) && startIndex == other.startIndex && size == other.size)
                return true;
        }

        return super.equals(obj);
    }

    public <T> T toArray(T array) {
        if (array instanceof double[]) {
            double[] doubleArray;
            if (((double[]) array).length < size()) {
                doubleArray = new double[size()];
            } else {
                doubleArray = (double[]) array;
            }
            System.arraycopy(this.array, startIndex, doubleArray, 0, size);
            return (T) doubleArray;
        }
        return super.toArray(array);
    }

    double[] wrappedArray() {
        return array;
    }

    int startIndex() {
        return startIndex;
    }

    boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Returns an unmodifiable {@link ArrayDouble} wrapper for the given {@code double} array.
     *
     * @param values a primitive array.
     * @return an immutable wrapper.
     */
    public static ArrayDouble of(double... values) {
        return CollectionNumbers.unmodifiableListDouble(values);
    }
}
