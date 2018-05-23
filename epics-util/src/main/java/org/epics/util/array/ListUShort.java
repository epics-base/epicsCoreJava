/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.epics.util.number.UnsignedConversions;

/**
 * An ordered collection of {@code short}s.
 *
 * @author Gabriele Carcassi
 */
public abstract class ListUShort implements ListNumber, CollectionUShort {

    @Override
    public IteratorUShort iterator() {
        return new IteratorUShort() {

            private int index;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public short nextShort() {
                return getShort(index++);
            }
        };
    }

    @Override
    public double getDouble(int index) {
        return UnsignedConversions.toDouble(getShort(index));
    }

    @Override
    public float getFloat(int index) {
        return UnsignedConversions.toFloat(getShort(index));
    }

    @Override
    public long getLong(int index) {
        return UnsignedConversions.toLong(getShort(index));
    }

    @Override
    public int getInt(int index) {
        return UnsignedConversions.toInt(getShort(index));
    }

    @Override
    public byte getByte(int index) {
        return (byte) getShort(index);
    }

    @Override
    public void setDouble(int index, double value) {
        setShort(index, (short) value);
    }

    @Override
    public void setFloat(int index, float value) {
        setShort(index, (short) value);
    }

    @Override
    public void setLong(int index, long value) {
        setShort(index, (short) value);
    }

    @Override
    public void setInt(int index, int value) {
        setShort(index, (short) value);
    }

    @Override
    public void setShort(int index, short value) {
        throw new UnsupportedOperationException("Read only list.");
    }

    @Override
    public void setByte(int index, byte value) {
        setShort(index, (short) value);
    }

    @Override
    public void setAll(int index, ListNumber list) {
        if ((index+list.size()) > size()) {
            throw new IndexOutOfBoundsException("Index: "+index+", Elements: "+list.size()+", Size: "+size());
        }
        for (int i = 0; i < list.size(); i++) {
            setShort(index + i, list.getShort(i));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof ListUShort) {
            ListUShort other = (ListUShort) obj;

            if (size() != other.size())
                return false;

            for (int i = 0; i < size(); i++) {
                if (getShort(i) != other.getShort(i))
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
            builder.append(getShort(i)).append(", ");
        }
        builder.append(getShort(i)).append("]");
        return builder.toString();
    }
    
    @Override
    public ListUShort subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " toIndex: " + toIndex + ", size: " + size() );
        }
        final int size = toIndex - fromIndex;
        return new ListUShort() {
            @Override
            public short getShort(int index) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
                return ListUShort.this.getShort(fromIndex + index);
            }

            @Override
            public void setShort(int index, short value) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
                ListUShort.this.setShort(fromIndex + index, value);
            }

            @Override
            public int size() {
                return size;
            }
        };
    }
}
