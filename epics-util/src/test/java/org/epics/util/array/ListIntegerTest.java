/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 *
 * @author carcassi
 */
public class ListIntegerTest extends FeatureTestListNumber {

    @Override
    public ListInteger createConstantCollection() {
        return new ListInteger() {

            public int size() {
                return 10;
            }

            public int getInt(int index) {
                return 1;
            }
        };
    }

    @Override
    public ListInteger createRampCollection() {
        return new ListInteger() {

            public int size() {
                return 10;
            }

            public int getInt(int index) {
                return index;
            }
        };
    }

    @Override
    public ListNumber createModifiableCollection() {
        return new ListInteger() {

            private int[] array = new int[10];

            public int getInt(int index) {
                return array[index];
            }

            @Override
            public void setInt(int index, int value) {
                array[index] = value;
            }

            public int size() {
                return array.length;
            }
        };
    }

    @Override
    public ListNumber createEmpty() {
        return new ListInteger() {

            public int size() {
                return 0;
            }

            public int getInt(int index) {
                return 1;
            }
        };
    }
}
