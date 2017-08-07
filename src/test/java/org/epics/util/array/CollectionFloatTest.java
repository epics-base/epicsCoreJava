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
public class CollectionFloatTest {

    CollectionFloat floats = new CollectionFloat() {

        @Override
        public IteratorFloat iterator() {
            return new IteratorFloat() {

                int n=0;

                @Override
                public boolean hasNext() {
                    return n < 10;
                }

                @Override
                public float nextFloat() {
                    n++;
                    return (float) 1.0;
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
        CollectionTest.testIterationForAllTypes(floats);
    }

    @Test
    public void toArray() {
        CollectionTest.testToArrayForAllTypes(floats);
    }
    
    @Test(expected=ArrayStoreException.class)
    public void toArrayInvalid() {
        floats.toArray(new Object());
    }
}
