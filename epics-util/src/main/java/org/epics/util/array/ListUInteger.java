/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.epics.util.number.UnsignedConversions;

/**
 * An ordered collection of unsigned {@code int}s.
 *
 * @author Gabriele Carcassi
 */
public abstract class ListUInteger extends AbstractCollectionNumber implements ListNumber {

    public IteratorUInteger iterator() {
        return new IteratorUInteger() {

            private int index;

            public boolean hasNext() {
                return index < size();
            }

            public int nextInt() {
                return getInt(index++);
            }
        };
    }

    public double getDouble(int index) {
        return UnsignedConversions.toDouble(getInt(index));
    }

    public float getFloat(int index) {
        return UnsignedConversions.toFloat(getInt(index));
    }

    public long getLong(int index) {
        return UnsignedConversions.toLong(getInt(index));
    }

    public short getShort(int index) {
        return (short) getInt(index);
    }

    public byte getByte(int index) {
        return (byte) getInt(index);
    }

    public void setDouble(int index, double value) {
        setInt(index, (int) value);
    }

    public void setFloat(int index, float value) {
        setInt(index, (int) value);
    }

    public void setLong(int index, long value) {
        setInt(index, (int) value);
    }

    public void setInt(int index, int value) {
        throw new UnsupportedOperationException("Read only list.");
    }

    public void setShort(int index, short value) {
        setInt(index, (int) value);
    }

    public void setByte(int index, byte value) {
        setInt(index, (int) value);
    }

    public void setAll(int index, ListNumber list) {
        if ((index + list.size()) > size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Elements: " + list.size() + ", Size: " + size());
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
            builder.append(getLong(i)).append(", ");
        }
        builder.append(getLong(i)).append("]");
        return builder.toString();
    }

    public ListUInteger subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " toIndex: " + toIndex + ", size: " + size());
        }
        final int size = toIndex - fromIndex;
        return new ListUInteger() {
            public int getInt(int index) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
                return ListUInteger.this.getInt(fromIndex + index);
            }

            @Override
            public void setInt(int index, int value) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
                ListUInteger.this.setInt(fromIndex + index, value);
            }

            public int size() {
                return size;
            }
        };
    }
}
