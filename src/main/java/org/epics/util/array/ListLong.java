/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 * An ordered collection of {@code long}s.
 *
 * @author Gabriele Carcassi
 */
public abstract class ListLong implements ListNumber, CollectionLong {

    @Override
    public IteratorLong iterator() {
        return new IteratorLong() {

            private int index;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public long nextLong() {
                return getLong(index++);
            }
        };
    }

    @Override
    public double getDouble(int index) {
        return (double) getLong(index);
    }

    @Override
    public float getFloat(int index) {
        return (float) getLong(index);
    }

    @Override
    public int getInt(int index) {
        return (int) getLong(index);
    }

    @Override
    public short getShort(int index) {
        return (short) getLong(index);
    }

    @Override
    public byte getByte(int index) {
        return (byte) getLong(index);
    }

    @Override
    public void setDouble(int index, double value) {
        setLong(index, (long) value);
    }

    @Override
    public void setFloat(int index, float value) {
        setLong(index, (long) value);
    }

    @Override
    public void setLong(int index, long value) {
        throw new UnsupportedOperationException("Read only list.");
    }

    @Override
    public void setInt(int index, int value) {
        setLong(index, (long) value);
    }

    @Override
    public void setShort(int index, short value) {
        setLong(index, (long) value);
    }

    @Override
    public void setByte(int index, byte value) {
        setLong(index, (long) value);
    }

    @Override
    public void setAll(int index, ListNumber list) {
        if ((index+list.size()) > size()) {
            throw new IndexOutOfBoundsException("Index: "+index+", Elements: "+list.size()+", Size: "+size());
        }
        for (int i = 0; i < list.size(); i++) {
            setLong(index + i, list.getLong(i));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof ListLong) {
            ListLong other = (ListLong) obj;

            if (size() != other.size())
                return false;

            for (int i = 0; i < size(); i++) {
                if (getLong(i) != other.getLong(i))
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
            long element = getLong(i);
            int elementHash = (int)(element ^ (element >>> 32));
            result = 31 * result + elementHash;
        }
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (; i < size() - 1; i++) {
            builder.append(getLong(i)).append(", ");
        }
        builder.append(getLong(i)).append("]");
        return builder.toString();
    }
}
