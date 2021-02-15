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
public class CollectionULongTest extends FeatureTestCollectionNumber {

    @Override
    public CollectionNumber createConstantCollection() {
        return new AbstractCollectionULong() {

            public IteratorULong iterator() {
                return new IteratorULong() {

                    int n=0;

                    public boolean hasNext() {
                        return n < 10;
                    }

                    public long nextLong() {
                        n++;
                        return 1L;
                    }
                };
            }

            public int size() {
                return 10;
            }
        };
    }
}
