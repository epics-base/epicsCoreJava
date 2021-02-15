/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An ordered collection of {@code byte}s.
 *
 * @author Gabriele Carcassi
 */
public abstract class ListByte extends AbstractCollectionNumber implements ListNumber, CollectionByte {

    public IteratorByte iterator() {
        return new IteratorByte() {

            private int index;

            public boolean hasNext() {
                return index < size();
            }

            public byte nextByte() {
                return getByte(index++);
            }
        };
    }

    public double getDouble(int index) {
        return (double) getByte(index);
    }

    public float getFloat(int index) {
        return (float) getByte(index);
    }

    public long getLong(int index) {
        return (long) getByte(index);
    }

    public int getInt(int index) {
        return (int) getByte(index);
    }

    public short getShort(int index) {
        return (short) getByte(index);
    }

    public void setDouble(int index, double value) {
        setByte(index, (byte) value);
    }

    public void setFloat(int index, float value) {
        setByte(index, (byte) value);
    }

    public void setLong(int index, long value) {
        setByte(index, (byte) value);
    }

    public void setInt(int index, int value) {
        setByte(index, (byte) value);
    }

    public void setShort(int index, short value) {
        setByte(index, (byte) value);
    }

    public void setByte(int index, byte value) {
        throw new UnsupportedOperationException("Read only list.");
    }

    public void setAll(int index, ListNumber list) {
        if ((index + list.size()) > size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Elements: " + list.size() + ", Size: " + size());
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

    public ListByte subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " toIndex: " + toIndex + ", size: " + size());
        }
        final int size = toIndex - fromIndex;
        return new ListByte() {
            public byte getByte(int index) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
                return ListByte.this.getByte(fromIndex + index);
            }

            @Override
            public void setByte(int index, byte value) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
                ListByte.this.setByte(fromIndex + index, value);
            }

            public int size() {
                return size;
            }
        };
    }
}
