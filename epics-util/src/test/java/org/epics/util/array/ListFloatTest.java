/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 *
 * @author carcassi
 */
public class ListFloatTest extends FeatureTestListNumber {

    @Override
    public ListFloat createConstantCollection() {
        return new ListFloat() {

            public int size() {
                return 10;
            }

            public float getFloat(int index) {
                return 1.0F;
            }
        };
    }

    @Override
    public ListFloat createRampCollection() {
        return new ListFloat() {

            public int size() {
                return 10;
            }

            public float getFloat(int index) {
                return index;
            }
        };
    }

    @Override
    public ListNumber createModifiableCollection() {
        return new ListFloat() {

            private float[] array = new float[10];

            public float getFloat(int index) {
                return array[index];
            }

            @Override
            public void setFloat(int index, float value) {
                array[index] = value;
            }

            public int size() {
                return array.length;
            }
        };
    }

    @Override
    public ListNumber createEmpty() {
        return new ListFloat() {

            public int size() {
                return 0;
            }

            public float getFloat(int index) {
                return 1.0F;
            }
        };
    }
}
