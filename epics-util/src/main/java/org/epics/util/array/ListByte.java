/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 * An ordered collection of {@code byte}s.
 *
 * @author Gabriele Carcassi
 */
public abstract class ListByte implements ListNumber, CollectionByte {

    @Override
    public IteratorByte iterator() {
        return new IteratorByte() {

            private int index;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public byte nextByte() {
                return getByte(index++);
            }
        };
    }

    @Override
    public double getDouble(int index) {
        return (double) getByte(index);
    }

    @Override
    public float getFloat(int index) {
        return (float) getByte(index);
    }

    @Override
    public long getLong(int index) {
        return (long) getByte(index);
    }

    @Override
    public int getInt(int index) {
        return (int) getByte(index);
    }

    @Override
    public short getShort(int index) {
        return (short) getByte(index);
    }

    @Override
    public void setDouble(int index, double value) {
        setByte(index, (byte) value);
    }

    @Override
    public void setFloat(int index, float value) {
        setByte(index, (byte) value);
    }

    @Override
    public void setLong(int index, long value) {
        setByte(index, (byte) value);
    }

    @Override
    public void setInt(int index, int value) {
        setByte(index, (byte) value);
    }

    @Override
    public void setShort(int index, short value) {
        setByte(index, (byte) value);
    }

    @Override
    public void setByte(int index, byte value) {
        throw new UnsupportedOperationException("Read only list.");
    }

    @Override
    public void setAll(int index, ListNumber list) {
        if ((index+list.size()) > size()) {
            throw new IndexOutOfBoundsException("Index: "+index+", Elements: "+list.size()+", Size: "+size());
        }
        for (int i = 0; i < list.size(); i++) {
            setByte(index + i, list.getByte(i));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof ListByte) {
            ListByte other = (ListByte) obj;

            if (size() != other.size())
                return false;

            for (int i = 0; i < size(); i++) {
                if (getByte(i) != other.getByte(i))
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
            result = 31 * result + getShort(i);
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
            builder.append(getByte(i)).append(", ");
        }
        builder.append(getByte(i)).append("]");
        return builder.toString();
    }
    
    @Override
    public ListByte subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " toIndex: " + toIndex + ", size: " + size() );
        }
        final int size = toIndex - fromIndex;
        return new ListByte() {
            @Override
            public byte getByte(int index) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
                return ListByte.this.getByte(fromIndex + index);
            }

            @Override
            public void setByte(int index, byte value) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
                ListByte.this.setByte(fromIndex + index, value);
            }

            @Override
            public int size() {
                return size;
            }
        };
    }
}
