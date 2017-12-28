/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
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

            @Override
            public int size() {
                return 10;
            }

            @Override
            public byte getByte(int index) {
                return 1;
            }
        };
    }

    @Override
    public ListUByte createRampCollection() {
        return new ListUByte() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public byte getByte(int index) {
                return (byte) index;
            }
        };
    }

    @Override
    public ListUByte createModifiableCollection() {
        return new ListUByte() {
            
            private byte[] array = new byte[10];
            
            @Override
            public byte getByte(int index) {
                return array[index];
            }

            @Override
            public void setByte(int index, byte value) {
                array[index] = value;
            }

            @Override
            public int size() {
                return array.length;
            }
        };
    }

    @Override
    public ListUByte createEmpty() {
        return new ListUByte() {

            @Override
            public int size() {
                return 0;
            }

            @Override
            public byte getByte(int index) {
                return 1;
            }
        };
    }
    
}
