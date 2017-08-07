/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class CollectionDoubleTest {

    CollectionDouble doubles = new CollectionDouble() {

        @Override
        public IteratorDouble iterator() {
            return new IteratorDouble() {

                int n=0;

                @Override
                public boolean hasNext() {
                    return n < 10;
                }

                @Override
                public double nextDouble() {
                    n++;
                    return 1.0;
                }
            };
        }

        @Override
        public int size() {
            return 10;
        }
    };

    @Test
    public void iteration() {
        CollectionTest.testIterationForAllTypes(doubles);
    }

    @Test
    public void toArray() {
        CollectionTest.testToArrayForAllTypes(doubles);
    }
    
    @Test(expected=ArrayStoreException.class)
    public void toArrayInvalid() {
        doubles.toArray(new Object());
    }
}
