/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import java.io.Serializable;

/**
 * Wraps a {@code byte[]} into a {@link ListByte}.
 *
 * @author Gabriele Carcassi
 */
public final class ArrayByte extends ListByte implements Serializable {

    private static final long serialVersionUID = 7493025761455302916L;

    private final byte[] array;
    private final int startIndex;
    private final int size;
    private final boolean checkBoundaries;
    private final boolean readOnly;
    
    public ArrayByte(ListNumber array) {
        this(array.toArray(new byte[array.size()]), false);
    }

    /**
     * A new read-only {@code ArrayByte} that wraps around the given array.
     *
     * @param array an array
     */
    public ArrayByte(byte... array) {
        this(array, true);
    }

    /**
     * A new {@code ArrayByte} that wraps around the given array.
     *
     * @param array an array
     * @param readOnly if false the wrapper allows writes to the array
     */
    public ArrayByte(byte[] array, boolean readOnly) {
        this(array, 0, array.length, readOnly);
    }

    /**
     * A new {@code ArrayByte} that wraps around the given array.
     *
     * @param array an array
     * @param startIndex first element
     * @param size number of elements
     * @param readOnly if false the wrapper allows writes to the array
     */
    ArrayByte(byte[] array, int startIndex, int size, boolean readOnly) {
        if (startIndex < 0 || startIndex + size > array.length)
            throw new IndexOutOfBoundsException("Start index: "+startIndex+", Size: "+size+", Array length: "+array.length);
        this.array = array;
        this.readOnly = readOnly;
        this.startIndex = startIndex;
        this.size = size;
        this.checkBoundaries = startIndex != 0 || size != array.length;
    }

    @Override
    public final IteratorByte iterator() {
        return new IteratorByte() {

            private int index = startIndex;

            @Override
            public boolean hasNext() {
                return index < startIndex + size;
            }

            @Override
            public byte nextByte() {
                return array[index++];
            }
        };
    }

    @Override
    public final int size() {
        return size;
    }

    @Override
    public final byte getByte(int index) {
        if (checkBoundaries) {
            if (index < 0 || index >= this.size)
                throw new IndexOutOfBoundsException("Index: "+index+", Size: "+this.size);
        }
        return array[index];
    }

    @Override
    public void setByte(int index, byte value) {
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
    public ArrayByte subList(int fromIndex, int toIndex) {
        return new ArrayByte(array, fromIndex + startIndex, toIndex - fromIndex, readOnly);
    }

    @Override
    public void setAll(int index, ListNumber list) {
        if (list instanceof ArrayByte) {
            if (readOnly) {
                throw new UnsupportedOperationException("Read only list.");
            }
            ArrayByte other = (ArrayByte) list;
            System.arraycopy(other.array, other.startIndex, array, startIndex + index, other.size);
        } else {
            super.setAll(index, list);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof ArrayByte) {
            ArrayByte other = (ArrayByte) obj;
            
            if ((array == other.array) && startIndex == other.startIndex && size == other.size)
                return true;
        }

        return super.equals(obj);
    }

    @Override
    public <T> T toArray(T array) {
        if (array instanceof byte[]) {
            byte[] byteArray;
            if (((byte[]) array).length < size()) {
                byteArray = new byte[size()];
            } else {
                byteArray = (byte[]) array;
            }
            System.arraycopy(this.array, startIndex, byteArray, 0, size);
            return (T) byteArray;
        }        
        return super.toArray(array);
    }

    byte[] wrappedArray() {
        return array;
    }
    
    int startIndex() {
        return startIndex;
    }
}
