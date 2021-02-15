/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array.mockapi;

import org.epics.util.array.ArrayInteger;
import org.epics.util.array.ListNumber;
import static org.epics.util.array.CollectionNumbers.*;

/**
 *
 * @author carcassi
 */
public class IntArrayField implements NumericArrayField {

    public IntArrayField(int[] backendArray) {
        this.backendArray = backendArray;
    }

    int[] backendArray;

    public ArrayInteger get() {
        return unmodifiableListInt(backendArray);
    }

    public void put(int index, ListNumber data) {
        toListInt(backendArray).setAll(index, data);
    }
}
