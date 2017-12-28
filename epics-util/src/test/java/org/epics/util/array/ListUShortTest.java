/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
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

            @Override
            public int size() {
                return 10;
            }

            @Override
            public short getShort(int index) {
                return 1;
            }
        };
    }

    @Override
    public ListUShort createRampCollection() {
        return new ListUShort() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public short getShort(int index) {
                return (short) index;
            }
        };
    }

    @Override
    public ListUShort createModifiableCollection() {
        return new ListUShort() {
            
            private short[] array = new short[10];
            
            @Override
            public short getShort(int index) {
                return array[index];
            }

            @Override
            public void setShort(int index, short value) {
                array[index] = value;
            }

            @Override
            public int size() {
                return array.length;
            }
        };
    }

    @Override
    public ListUShort createEmpty() {
        return new ListUShort() {

            @Override
            public int size() {
                return 0;
            }

            @Override
            public short getShort(int index) {
                return 1;
            }
        };
    }

}
