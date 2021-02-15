/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 *
 * @author carcassi
 */
public class ListUShortTest extends FeatureTestListNumber {

    @Override
    public ListUShort createConstantCollection() {
        return new ListUShort() {

            public int size() {
                return 10;
            }

            public short getShort(int index) {
                return 1;
            }
        };
    }

    @Override
    public ListUShort createRampCollection() {
        return new ListUShort() {

            public int size() {
                return 10;
            }

            public short getShort(int index) {
                return (short) index;
            }
        };
    }

    @Override
    public ListUShort createModifiableCollection() {
        return new ListUShort() {

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
    public ListUShort createEmpty() {
        return new ListUShort() {

            public int size() {
                return 0;
            }

            public short getShort(int index) {
                return 1;
            }
        };
    }

}
