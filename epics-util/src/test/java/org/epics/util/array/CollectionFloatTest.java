/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class CollectionFloatTest extends FeatureTestCollectionNumber {

    @Override
    public CollectionNumber createConstantCollection() {
        return new AbstractCollectionFloat() {

            public IteratorFloat iterator() {
                return new IteratorFloat() {

                    int n=0;

                    public boolean hasNext() {
                        return n < 10;
                    }

                    public float nextFloat() {
                        n++;
                        return (float) 1.0;
                    }
                };
            }

            public int size() {
                return 10;
            }
        };
    }
}
