/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An ordered collection of {@code int}s.
 *
 * @author Gabriele Carcassi
 */
public abstract class ListInteger extends AbstractCollectionNumber implements ListNumber, CollectionInteger {

    public IteratorInteger iterator() {
        return new IteratorInteger() {

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
        return (double) getInt(index);
    }

    public float getFloat(int index) {
        return (float) getInt(index);
    }

    public long getLong(int index) {
        return (long) getInt(index);
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

        if (obj instanceof ListInteger) {
            ListInteger other = (ListInteger) obj;

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

    public ListInteger subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " toIndex: " + toIndex + ", size: " + size() );
        }
        final int size = toIndex - fromIndex;
        return new ListInteger() {
                    public int getInt(int index) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
                return ListInteger.this.getInt(fromIndex + index);
            }

            @Override
            public void setInt(int index, int value) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
                ListInteger.this.setInt(fromIndex + index, value);
            }

                    public int size() {
                return size;
            }
        };
    }
}
