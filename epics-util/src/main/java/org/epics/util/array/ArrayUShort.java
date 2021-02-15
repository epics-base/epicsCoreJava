/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import java.io.Serializable;

/**
 * Non-resizable {@link ListUShort} implementation backed by a {@code short[]}.
 */
public final class ArrayUShort extends ListUShort implements Serializable {

    private static final long serialVersionUID = 1L;

    private final short[] array;
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
    public ArrayUShort(CollectionNumber coll) {
        this(coll.toArray(new short[coll.size()]), 0, coll.size(), false);
    }

    /**
     * A new {@code ArrayUShort} that wraps around the given array.
     *
     * @param array an array
     * @param startIndex first element
     * @param size number of elements
     * @param readOnly if false the wrapper allows writes to the array
     * @throws IndexOutOfBoundsException if startIndex and size are out of range
     *         (@code{startIndex < 0 || startIndex + size > array.length})
     */
    ArrayUShort(short[] array, int startIndex, int size, boolean readOnly) {
        if (startIndex < 0 || startIndex + size > array.length)
            throw new IndexOutOfBoundsException("Start index: "+startIndex+", Size: "+size+", Array length: "+array.length);
        this.array = array;
        this.readOnly = readOnly;
        this.startIndex = startIndex;
        this.size = size;
        this.checkBoundaries = startIndex != 0 || size != array.length;
    }

    @Override
    public final IteratorUShort iterator() {
        return new IteratorUShort() {

            private int index = startIndex;

                    public boolean hasNext() {
                return index < startIndex + size;
            }

                    public short nextShort() {
                return array[index++];
            }
        };
    }

    public final int size() {
        return size;
    }

    public short getShort(int index) {
        if (checkBoundaries) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
        }
        return array[startIndex + index];
    }

    @Override
    public void setShort(int index, short value) {
        checkBounds(index, readOnly, checkBoundaries, size);
        array[startIndex + index] = value;
    }

    @Override
    public ArrayUShort subList(int fromIndex, int toIndex) {
        return new ArrayUShort(array, fromIndex + startIndex, toIndex - fromIndex, readOnly);
    }

    @Override
    public void setAll(int index, ListNumber list) {
        if (list instanceof ArrayUShort) {
            if (readOnly) {
                throw new UnsupportedOperationException("Read only list.");
            }
            ArrayUShort other = (ArrayUShort) list;
            System.arraycopy(other.array, other.startIndex, array, startIndex + index, other.size);
        } else {
            super.setAll(index, list);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof ArrayUShort) {
            ArrayUShort other = (ArrayUShort) obj;

            if ((array == other.array) && startIndex == other.startIndex && size == other.size)
                return true;
        }

        return super.equals(obj);
    }

    @Override
    public <T> T toArray(T array) {
        if (array instanceof short[]) {
            short[] shortArray;
            if (((short[]) array).length < size()) {
                shortArray = new short[size()];
            } else {
                shortArray = (short[]) array;
            }
            System.arraycopy(this.array, startIndex, shortArray, 0, size);
            return (T) shortArray;
        }
        return super.toArray(array);
    }

    short[] wrappedArray() {
        return array;
    }

    int startIndex() {
        return startIndex;
    }

    boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Returns an unmodifiable {@link ArrayUShort} wrapper for the given {@code short} array.
     *
     * @param values a primitive array.
     * @return an immutable wrapper.
     */
    public static ArrayUShort of(short... values) {
        return CollectionNumbers.unmodifiableListUShort(values);
    }
}
