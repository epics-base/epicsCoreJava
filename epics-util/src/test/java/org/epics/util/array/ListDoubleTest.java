/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 *
 * @author carcassi
 */
public class ListDoubleTest extends FeatureTestListNumber {

    @Override
    public ListDouble createConstantCollection() {
        return new ListDouble() {
            public int size() {
                return 10;
            }

            public double getDouble(int index) {
                return 1.0;
            }
        };
    }

    @Override
    public ListDouble createRampCollection() {
        return new ListDouble() {
            public int size() {
                return 10;
            }

            public double getDouble(int index) {
                return index;
            }
        };
    }

    @Override
    public ListNumber createModifiableCollection() {
        return new ListDouble() {

            private double[] array = new double[10];

            public double getDouble(int index) {
                return array[index];
            }

            public void setDouble(int index, double value) {
                array[index] = value;
            }

            public int size() {
                return array.length;
            }
        };
    }

    @Override
    public ListNumber createEmpty() {
        return new ListDouble() {
            public int size() {
                return 0;
            }

            public double getDouble(int index) {
                return 1.0;
            }
        };
    }
}
