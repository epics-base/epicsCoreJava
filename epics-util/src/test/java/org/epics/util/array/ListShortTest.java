/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 *
 * @author carcassi
 */
public class ListShortTest extends FeatureTestListNumber {

    @Override
    public ListShort createConstantCollection() {
        return new ListShort() {

            public int size() {
                return 10;
            }

            public short getShort(int index) {
                return 1;
            }
        };
    }

    @Override
    public ListShort createRampCollection() {
        return new ListShort() {

            public int size() {
                return 10;
            }

            public short getShort(int index) {
                return (short) index;
            }
        };
    }

    @Override
    public ListNumber createModifiableCollection() {
        return new ListShort() {

            private short[] array = new short[10];

            public short getShort(int index) {
                return array[index];
            }

            @Override
            public void setShort(int index, short value) {
                array[index] = value;
            }

            public int size() {
                return array.length;
            }
        };
    }

    @Override
    public ListNumber createEmpty() {
        return new ListShort() {

            public int size() {
                return 0;
            }

            public short getShort(int index) {
                return 1;
            }
        };
    }

}
