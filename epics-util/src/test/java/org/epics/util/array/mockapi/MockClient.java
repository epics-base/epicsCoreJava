/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
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
