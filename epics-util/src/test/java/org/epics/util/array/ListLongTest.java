/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 *
 * @author carcassi
 */
public class ListLongTest extends FeatureTestListNumber {

    @Override
    public ListLong createConstantCollection() {
        return new ListLong() {

            public int size() {
                return 10;
            }

            public long getLong(int index) {
                return 1L;
            }
        };
    }

    @Override
    public ListLong createRampCollection() {
        return new ListLong() {

            public int size() {
                return 10;
            }

            public long getLong(int index) {
                return index;
            }
        };
    }

    @Override
    public ListNumber createModifiableCollection() {
        return new ListLong() {

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
    public ListNumber createEmpty() {
        return new ListLong() {

            public int size() {
                return 0;
            }

            public long getLong(int index) {
                return 1L;
            }
        };
    }
}
