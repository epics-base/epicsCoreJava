/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.util.array.mockapi;

import org.epics.util.array.ArrayInt;
import org.epics.util.array.ListInt;
import org.epics.util.array.ListNumber;

/**
 *
 * @author carcassi
 */
public class IntArrayField implements NumericArrayField {

    public IntArrayField(int[] backendArray) {
        this.backendArray = backendArray;
    }
    
    int[] backendArray;

    @Override
    public ListInt get() {
        return new ArrayInt(backendArray);
    }

    @Override
    public void put(int index, ListNumber data) {
        new ArrayInt(backendArray, false).setAll(index, data);
    }
}
