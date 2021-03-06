/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 * An ordered collection of {@code float}s.
 *
 * @author Gabriele Carcassi
 */
public abstract class ListFloat implements ListNumber, CollectionFloat {

    @Override
    public IteratorFloat iterator() {
        return new IteratorFloat() {

            private int index;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public float nextFloat() {
                return getFloat(index++);
            }
        };
    }

    @Override
    public double getDouble(int index) {
        return (double) getFloat(index);
    }

    @Override
    public long getLong(int index) {
        return (long) getFloat(index);
    }

    @Override
    public int getInt(int index) {
        return (int) getFloat(index);
    }

    @Override
    public short getShort(int index) {
        return (short) getFloat(index);
    }

    @Override
    public byte getByte(int index) {
        return (byte) getFloat(index);
    }

    @Override
    public void setDouble(int index, double value) {
        setFloat(index, (float) value);
    }

    @Override
    public void setFloat(int index, float value) {
        throw new UnsupportedOperationException("Read only list.");
    }

    @Override
    public void setLong(int index, long value) {
        setFloat(index, (float) value);
    }

    @Override
    public void setInt(int index, int value) {
        setFloat(index, (float) value);
    }

    @Override
    public void setShort(int index, short value) {
        setFloat(index, (float) value);
    }

    @Override
    public void setByte(int index, byte value) {
        setFloat(index, (float) value);
    }

    @Override
    public void setAll(int index, ListNumber list) {
        if ((index+list.size()) > size()) {
            throw new IndexOutOfBoundsException("Index: "+index+", Elements: "+list.size()+", Size: "+size());
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
    
    @Override
    public ListFloat subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + " toIndex: " + toIndex + ", size: " + size() );
        }
        final int size = toIndex - fromIndex;
        return new ListFloat() {
            @Override
            public float getFloat(int index) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
                return ListFloat.this.getFloat(fromIndex + index);
            }

            @Override
            public void setFloat(int index, float value) {
                if (index < 0 || index >= size)
                    throw new IndexOutOfBoundsException("Index: "+index+", Size: "+size);
                ListFloat.this.setFloat(fromIndex + index, value);
            }

            @Override
            public int size() {
                return size;
            }
        };
    }
}
