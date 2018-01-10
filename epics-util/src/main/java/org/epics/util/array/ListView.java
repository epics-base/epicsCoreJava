/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 * Provides a view of a wrapped list that only exposes the elements with
 * the given indexes.
 *
 * @author carcassi
 */
class ListView {

    /**
     * A ListView implementation for doubles.
     */
    static class Double extends ListDouble {
        private final ListDouble list;
        private final ListInteger indexes;

        public Double(ListDouble list, ListInteger indexes) {
            this.list = list;
            this.indexes = indexes;
        }

        @Override
        public double getDouble(int index) {
            return list.getDouble(indexes.getInt(index));
        }

        @Override
        public int size() {
            return indexes.size();
        }

    }

    /**
     * A ListView implementation for floats.
     */
    static class Float extends ListFloat {
        private final ListFloat list;
        private final ListInteger indexes;

        public Float(ListFloat list, ListInteger indexes) {
            this.list = list;
            this.indexes = indexes;
        }

        @Override
        public float getFloat(int index) {
            return list.getFloat(indexes.getInt(index));
        }

        @Override
        public int size() {
            return indexes.size();
        }

    }

    /**
     * A ListView implementation for longs.
     */
    static class Long extends ListLong {
        private final ListLong list;
        private final ListInteger indexes;

        public Long(ListLong list, ListInteger indexes) {
            this.list = list;
            this.indexes = indexes;
        }

        @Override
        public long getLong(int index) {
            return list.getLong(indexes.getInt(index));
        }

        @Override
        public int size() {
            return indexes.size();
        }

    }

    /**
     * A ListView implementation for ints.
     */
    static class Int extends ListInteger {
        private final ListInteger list;
        private final ListInteger indexes;

        public Int(ListInteger list, ListInteger indexes) {
            this.list = list;
            this.indexes = indexes;
        }

        @Override
        public int getInt(int index) {
            return list.getInt(indexes.getInt(index));
        }

        @Override
        public int size() {
            return indexes.size();
        }

    }

    /**
     * A ListView implementation for shorts.
     */
    static class Short extends ListShort {
        private final ListShort list;
        private final ListInteger indexes;

        public Short(ListShort list, ListInteger indexes) {
            this.list = list;
            this.indexes = indexes;
        }

        @Override
        public short getShort(int index) {
            return list.getShort(indexes.getInt(index));
        }

        @Override
        public int size() {
            return indexes.size();
        }

    }

    /**
     * A ListView implementation for bytes.
     */
    static class Byte extends ListByte {
        private final ListByte list;
        private final ListInteger indexes;

        public Byte(ListByte list, ListInteger indexes) {
            this.list = list;
            this.indexes = indexes;
        }

        @Override
        public byte getByte(int index) {
            return list.getByte(indexes.getInt(index));
        }

        @Override
        public int size() {
            return indexes.size();
        }

    }
}
