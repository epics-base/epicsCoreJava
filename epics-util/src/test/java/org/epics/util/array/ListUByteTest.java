/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 *
 * @author carcassi
 */
public class ListUByteTest extends FeatureTestListNumber {

    @Override
    public ListUByte createConstantCollection() {
        return new ListUByte() {

            public int size() {
                return 10;
            }

            public byte getByte(int index) {
                return 1;
            }
        };
    }

    @Override
    public ListUByte createRampCollection() {
        return new ListUByte() {

            public int size() {
                return 10;
            }

            public byte getByte(int index) {
                return (byte) index;
            }
        };
    }

    @Override
    public ListUByte createModifiableCollection() {
        return new ListUByte() {

            private byte[] array = new byte[10];

            public byte getByte(int index) {
                return array[index];
            }

            @Override
            public void setByte(int index, byte value) {
                array[index] = value;
            }

            public int size() {
                return array.length;
            }
        };
    }

    @Override
    public ListUByte createEmpty() {
        return new ListUByte() {

            public int size() {
                return 0;
            }

            public byte getByte(int index) {
                return 1;
            }
        };
    }

}
