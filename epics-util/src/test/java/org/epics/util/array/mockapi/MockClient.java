/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array.mockapi;

import java.util.List;

/**
 *
 * @author carcassi
 */
public class MockClient {

    public DoubleArrayField createDoubleArrayField(List<Double> initialValues) {
        int i = 0;
        double[] array = new double[initialValues.size()];

        for ( Double element : initialValues) {
            array[i++] = element;
        }
        return new DoubleArrayField(array);
    }

    public IntArrayField createArrayField(List<Integer> initialValues) {
        int i = 0;
        int[] array = new int[initialValues.size()];

        for ( Integer element : initialValues) {
            array[i++] = element;
        }
        return new IntArrayField(array);
    }
}
