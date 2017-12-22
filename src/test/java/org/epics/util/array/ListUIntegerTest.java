/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 *
 * @author carcassi
 */
public class ListUIntegerTest extends FeatureTestListNumber {

    @Override
    public ListUInteger createConstantCollection() {
        return new ListUInteger() {

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
    public ListUInteger createRampCollection() {
        return new ListUInteger() {

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
    public ListUInteger createModifiableCollection() {
        return new ListUInteger() {
            
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
    public ListUInteger createEmpty() {
        return new ListUInteger() {

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
