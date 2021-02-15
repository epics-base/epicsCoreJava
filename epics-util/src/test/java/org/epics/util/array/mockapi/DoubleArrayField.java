/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array.mockapi;

import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ListNumber;
import static org.epics.util.array.CollectionNumbers.*;

/**
 *
 * @author carcassi
 */
public class DoubleArrayField implements NumericArrayField {

    public DoubleArrayField(double[] backendArray) {
        this.backendArray = backendArray;
    }

    double[] backendArray;

    public ArrayDouble get() {
        return unmodifiableListDouble(backendArray);
    }

    public void put(int index, ListNumber data) {
        toList(backendArray).setAll(index, data);
    }

}
