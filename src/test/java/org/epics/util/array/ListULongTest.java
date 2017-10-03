/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 *
 * @author carcassi
 */
public class ListULongTest extends FeatureTestListNumber {

    @Override
    public ListULong createConstantCollection() {
        return new ListULong() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public long getLong(int index) {
                return 1L;
            }
        };
    }

    @Override
    public ListULong createRampCollection() {
        return new ListULong() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public long getLong(int index) {
                return index;
            }
        };
    }

    @Override
    public ListULong createModifiableCollection() {
        return new ListULong() {
            
            private long[] array = new long[10];
            
            @Override
            public long getLong(int index) {
                return array[index];
            }

            @Override
            public void setLong(int index, long value) {
                array[index] = value;
            }

            @Override
            public int size() {
                return array.length;
            }
        };
    }

    @Override
    public ListULong createEmpty() {
        return new ListULong() {

            @Override
            public int size() {
                return 0;
            }

            @Override
            public long getLong(int index) {
                return 1L;
            }
        };
    }
}
