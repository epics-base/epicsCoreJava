/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An ordered collection of {@code float}s.
 *
 * @author Gabriele Carcassi
 */
public abstract class ListFloat extends AbstractCollectionNumber implements ListNumber, CollectionFloat {

    public IteratorFloat iterator() {
        return new IteratorFloat() {

            private int index;

            public boolean hasNext() {
                return index < size();
            }

            public float nextFloat() {
                return getFloat(index++);
            }
        };
    }

    public double getDouble(int index) {
        return (double) getFloat(index);
    }

    public long getLong(int index) {
        return (long) getFloat(index);
    }

    public int getInt(int index) {
        return (int) getFloat(index);
    }

    public short getShort(int index) {
        return (short) getFloat(index);
    }

    public byte getByte(int index) {
        return (byte) getFloat(index);
    }

    public void setDouble(int index, double value) {
        setFloat(index, (float) value);
    }

    public void setFloat(int index, float value) {
        throw new UnsupportedOperationException("Read only list.");
    }

    public void setLong(int index, long value) {
        setFloat(index, (float) value);
    }

    public void setInt(int index, int value) {
        setFloat(index, (float) value);
    }

    public void setShort(int index, short value) {
        setFloat(index, (float) value);
    }

    public void setByte(int index, byte value) {
        setFloat(index, (float) value);
    }

    public void setAll(int index, ListNumber list) {
        if ((index + list.size()) > size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Elements: " + list.size() + ", Size: " + size());
        }
        for (int i = 0; i < list.size(); i++) {
            setFloat(index + i, list.getFloat(i));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof ListFloat) {
            ListFloat other = (ListFloat) obj;

            if (size() != other.size())
                return false;

            for (int i = 0; i < size(); i++) {
                if (Float.floatToIntBits(getFloat(i)) != Float.floatToIntBits(other.getFloat(i)))
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
            result = 31 * result + Float.floatToIntBits(getFloat(i));
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
            builder.append(getFloat(i)).append(", ");
        }
        builder.append(getFloat(i)).append("]");
        return builder.toString();
    }

    public ListFloat subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " toIndex: " + toIndex + ", size: " + size());
        }
        final int size = toIndex - fromIndex;
        return new ListFloat() {
            public float getFloat(int index) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
                return ListFloat.this.getFloat(fromIndex + index);
            }

            public void setFloat(int index, float value) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
                ListFloat.this.setFloat(fromIndex + index, value);
            }

            public int size() {
                return size;
            }
        };
    }
}
