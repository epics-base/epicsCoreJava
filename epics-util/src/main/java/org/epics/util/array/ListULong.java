/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.epics.util.number.ULong;
import org.epics.util.number.UnsignedConversions;

/**
 * An ordered collection of {@code long}s.
 *
 * @author Gabriele Carcassi
 */
public abstract class ListULong extends AbstractCollectionNumber implements ListNumber, CollectionULong {

    public IteratorULong iterator() {
        return new IteratorULong() {

            private int index;

            public boolean hasNext() {
                return index < size();
            }

            public long nextLong() {
                return getLong(index++);
            }
        };
    }

    public double getDouble(int index) {
        return UnsignedConversions.toDouble(getLong(index));
    }

    public float getFloat(int index) {
        return UnsignedConversions.toFloat(getLong(index));
    }

    public int getInt(int index) {
        return (int) getLong(index);
    }

    public short getShort(int index) {
        return (short) getLong(index);
    }

    public byte getByte(int index) {
        return (byte) getLong(index);
    }

    public void setDouble(int index, double value) {
        setLong(index, (long) value);
    }

    public void setFloat(int index, float value) {
        setLong(index, (long) value);
    }

    public void setLong(int index, long value) {
        throw new UnsupportedOperationException("Read only list.");
    }

    public void setInt(int index, int value) {
        setLong(index, (long) value);
    }

    public void setShort(int index, short value) {
        setLong(index, (long) value);
    }

    public void setByte(int index, byte value) {
        setLong(index, (long) value);
    }

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

        if (obj instanceof ListULong) {
            ListULong other = (ListULong) obj;

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
        if (size() == 0) return "[]";
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int i = 0;
        for (; i < size() - 1; i++) {
            builder.append(ULong.toString(getLong(i))).append(", ");
        }
        builder.append(ULong.toString(getLong(i))).append("]");
        return builder.toString();
    }

    public ListULong subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " toIndex: " + toIndex + ", size: " + size() );
        }
        final int size = toIndex - fromIndex;
        return new ListULong() {
            public long getLong(int index) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
                return ListULong.this.getLong(fromIndex + index);
            }

            @Override
            public void setLong(int index, long value) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
                ListULong.this.setLong(fromIndex + index, value);
            }

            public int size() {
                return size;
            }
        };
    }
}
