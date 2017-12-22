/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.epics.util.number.UnsignedConversions;

/**
 * An ordered collection of unsigned {@code int}s.
 *
 * @author Gabriele Carcassi
 */
public abstract class ListUInteger implements ListNumber, CollectionUInteger {

    @Override
    public IteratorUInteger iterator() {
        return new IteratorUInteger() {

            private int index;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public int nextInt() {
                return getInt(index++);
            }
        };
    }

    @Override
    public double getDouble(int index) {
        return UnsignedConversions.toDouble(getInt(index));
    }

    @Override
    public float getFloat(int index) {
        return UnsignedConversions.toFloat(getInt(index));
    }

    @Override
    public long getLong(int index) {
        return UnsignedConversions.toLong(getInt(index));
    }

    @Override
    public short getShort(int index) {
        return (short) getInt(index);
    }

    @Override
    public byte getByte(int index) {
        return (byte) getInt(index);
    }
    @Override
    public void setDouble(int index, double value) {
        setInt(index, (int) value);
    }

    @Override
    public void setFloat(int index, float value) {
        setInt(index, (int) value);
    }

    @Override
    public void setLong(int index, long value) {
        setInt(index, (int) value);
    }

    @Override
    public void setInt(int index, int value) {
        throw new UnsupportedOperationException("Read only list.");
    }

    @Override
    public void setShort(int index, short value) {
        setInt(index, (int) value);
    }

    @Override
    public void setByte(int index, byte value) {
        setInt(index, (int) value);
    }

    @Override
    public void setAll(int index, ListNumber list) {
        if ((index+list.size()) > size()) {
            throw new IndexOutOfBoundsException("Index: "+index+", Elements: "+list.size()+", Size: "+size());
        }
        for (int i = 0; i < list.size(); i++) {
            setInt(index + i, list.getInt(i));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof ListUInteger) {
            ListUInteger other = (ListUInteger) obj;

            if (size() != other.size())
                return false;

            for (int i = 0; i < size(); i++) {
                if (getInt(i) != other.getInt(i))
                    return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < size(); i++) {
            result = 31 * result + getInt(i);
        }
        return result;
    }

    @Override
    public String toString() {
        if (size() == 0) return "[]";
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (; i < size() - 1; i++) {
            builder.append(getInt(i)).append(", ");
        }
        builder.append(getInt(i)).append("]");
        return builder.toString();
    }
    
    @Override
    public ListUInteger subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " toIndex: " + toIndex + ", size: " + size() );
        }
        final int size = toIndex - fromIndex;
        return new ListUInteger() {
            @Override
            public int getInt(int index) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
                return ListUInteger.this.getInt(fromIndex + index);
            }

            @Override
            public void setInt(int index, int value) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
                ListUInteger.this.setInt(fromIndex + index, value);
            }

            @Override
            public int size() {
                return size;
            }
        };
    }
}
