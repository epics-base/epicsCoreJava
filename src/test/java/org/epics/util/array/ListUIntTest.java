/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 *
 * @author carcassi
 */
public class ListUIntTest extends FeatureTestListNumber {

    @Override
    public ListUInt createConstantCollection() {
        return new ListUInt() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public int getInt(int index) {
                return 1;
            }
        };
    }

    @Override
    public ListUInt createRampCollection() {
        return new ListUInt() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public int getInt(int index) {
                return index;
            }
        };
    }

    @Override
    public ListUInt createModifiableCollection() {
        return new ListUInt() {
            
            private int[] array = new int[10];
            
            @Override
            public int getInt(int index) {
                return array[index];
            }

            @Override
            public void setInt(int index, int value) {
                array[index] = value;
            }

            @Override
            public int size() {
                return array.length;
            }
        };
    }

    @Override
    public ListUInt createEmpty() {
        return new ListUInt() {

            @Override
            public int size() {
                return 0;
            }

            @Override
            public int getInt(int index) {
                return 1;
            }
        };
    }
}
