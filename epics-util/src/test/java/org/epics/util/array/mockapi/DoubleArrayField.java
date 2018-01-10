/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

    @Override
    public ArrayDouble get() {
        return unmodifiableListDouble(backendArray);
    }

    @Override
    public void put(int index, ListNumber data) {
        toList(backendArray).setAll(index, data);
    }
    
}
