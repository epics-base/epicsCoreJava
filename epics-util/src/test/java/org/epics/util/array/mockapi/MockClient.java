/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.util.array.mockapi;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 *
 * @author carcassi
 */
public class MockClient {
    
    public DoubleArrayField createArrayField(DoubleStream initialValues) {
        return new DoubleArrayField(initialValues.toArray());
    }
    
    public IntArrayField createArrayField(IntStream initialValues) {
        return new IntArrayField(initialValues.toArray());
    }
}
