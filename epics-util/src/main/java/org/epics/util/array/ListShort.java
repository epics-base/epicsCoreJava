/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An ordered collection of {@code short}s.
 *
 * @author Gabriele Carcassi
 */
public abstract class ListShort extends AbstractCollectionNumber implements ListNumber, CollectionShort {

    public IteratorShort iterator() {
        return new IteratorShort() {

            private int index;

            public boolean hasNext() {
                return index < size();
            }

            public short nextShort() {
                return getShort(index++);
            }
        };
    }

    public double getDouble(int index) {
        return (double) getShort(index);
    }

    public float getFloat(int index) {
        return (float) getShort(index);
    }

    public long getLong(int index) {
        return (long) getShort(index);
    }

    public int getInt(int index) {
        return (int) getShort(index);
    }

    public byte getByte(int index) {
        return (byte) getShort(index);
    }

    public void setDouble(int index, double value) {
        setShort(index, (short) value);
    }

    public void setFloat(int index, float value) {
        setShort(index, (short) value);
    }

    public void setLong(int index, long value) {
        setShort(index, (short) value);
    }

    public void setInt(int index, int value) {
        setShort(index, (short) value);
    }

    public void setShort(int index, short value) {
        throw new UnsupportedOperationException("Read only list.");
    }

    public void setByte(int index, byte value) {
        setShort(index, (short) value);
    }

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

        if (obj instanceof ListShort) {
            ListShort other = (ListShort) obj;

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

    public ListShort subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " toIndex: " + toIndex + ", size: " + size() );
        }
        final int size = toIndex - fromIndex;
        return new ListShort() {
            public short getShort(int index) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
                return ListShort.this.getShort(fromIndex + index);
            }

            @Override
            public void setShort(int index, short value) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
                ListShort.this.setShort(fromIndex + index, value);
            }

            public int size() {
                return size;
            }
        };
    }
}
