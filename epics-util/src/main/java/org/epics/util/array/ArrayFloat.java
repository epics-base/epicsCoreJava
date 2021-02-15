/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import java.io.Serializable;

/**
 * Non-resizable {@link ListFloat} implementation backed by a {@code float[]}.
 */
public final class ArrayFloat extends ListFloat implements Serializable {

    private static final long serialVersionUID = 1L;

    private final float[] array;
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
    public ArrayFloat(ListNumber coll) {
        this(coll.toArray(new float[coll.size()]), 0, coll.size(), false);
    }

    /**
     * A new {@code ArrayFloat} that wraps around the given array.
     *
     * @param array      an array
     * @param startIndex first element
     * @param size       number of elements
     * @param readOnly   if false the wrapper allows writes to the array
     */
    ArrayFloat(float[] array, int startIndex, int size, boolean readOnly) {
        if (startIndex < 0 || startIndex + size > array.length)
            throw new IndexOutOfBoundsException("Start index: " + startIndex + ", Size: " + size + ", Array length: " + array.length);
        this.array = array;
        this.readOnly = readOnly;
        this.startIndex = startIndex;
        this.size = size;
        this.checkBoundaries = startIndex != 0 || size != array.length;
    }

    @Override
    public final IteratorFloat iterator() {
        return new IteratorFloat() {

            private int index = startIndex;

            public boolean hasNext() {
                return index < startIndex + size;
            }

            public float nextFloat() {
                return array[index++];
            }
        };
    }

    public final int size() {
        return size;
    }

    public float getFloat(int index) {
        if (checkBoundaries) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this.size);
        }
        return array[startIndex + index];
    }

    @Override
    public void setFloat(int index, float value) {
        checkBounds(index, readOnly, checkBoundaries, size);
        array[startIndex + index] = value;
    }

    public ArrayFloat subList(int fromIndex, int toIndex) {
        return new ArrayFloat(array, fromIndex + startIndex, toIndex - fromIndex, readOnly);
    }

    @Override
    public void setAll(int index, ListNumber list) {
        if (list instanceof ArrayFloat) {
            if (readOnly) {
                throw new UnsupportedOperationException("Read only list.");
            }
            ArrayFloat other = (ArrayFloat) list;
            System.arraycopy(other.array, other.startIndex, array, startIndex + index, other.size);
        } else {
            super.setAll(index, list);
        }
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof ArrayFloat) {
            ArrayFloat other = (ArrayFloat) obj;

            if ((array == other.array) && startIndex == other.startIndex && size == other.size)
                return true;
        }

        return super.equals(obj);
    }

    @Override
    public <T> T toArray(T array) {
        if (array instanceof float[]) {
            float[] floatArray;
            if (((float[]) array).length < size()) {
                floatArray = new float[size()];
            } else {
                floatArray = (float[]) array;
            }
            System.arraycopy(this.array, startIndex, floatArray, 0, size);
            return (T) floatArray;
        }
        return super.toArray(array);
    }

    float[] wrappedArray() {
        return array;
    }

    int startIndex() {
        return startIndex;
    }

    boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Returns an unmodifiable {@link ArrayFloat} wrapper for the given {@code float} array.
     *
     * @param values a primitive array.
     * @return an immutable wrapper.
     */
    public static ArrayFloat of(float... values) {
        return CollectionNumbers.unmodifiableListFloat(values);
    }
}
