/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.util.array.mockapi;

import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ListDouble;
import org.epics.util.array.ListNumber;

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
    public ListDouble get() {
        return new ArrayDouble(backendArray);
    }

    @Override
    public void put(int index, ListNumber data) {
        new ArrayDouble(backendArray).setAll(index, data);
    }
    
}
