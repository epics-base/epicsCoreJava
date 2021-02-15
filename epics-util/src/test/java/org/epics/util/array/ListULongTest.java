/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
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

            public int size() {
                return 10;
            }

            public long getLong(int index) {
                return 1L;
            }
        };
    }

    @Override
    public ListULong createRampCollection() {
        return new ListULong() {

            public int size() {
                return 10;
            }

            public long getLong(int index) {
                return index;
            }
        };
    }

    @Override
    public ListULong createModifiableCollection() {
        return new ListULong() {

            private long[] array = new long[10];

            public long getLong(int index) {
                return array[index];
            }

            @Override
            public void setLong(int index, long value) {
                array[index] = value;
            }

            public int size() {
                return array.length;
            }
        };
    }

    @Override
    public ListULong createEmpty() {
        return new ListULong() {

            public int size() {
                return 0;
            }

            public long getLong(int index) {
                return 1L;
            }
        };
    }
}
