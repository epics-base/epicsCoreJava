/**
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
public class CollectionLongTest extends FeatureTestCollectionNumber {

    @Override
    public CollectionNumber createConstantCollection() {
        return new CollectionLong() {

            @Override
            public IteratorLong iterator() {
                return new IteratorLong() {

                    int n=0;

                    @Override
                    public boolean hasNext() {
                        return n < 10;
                    }

                    @Override
                    public long nextLong() {
                        n++;
                        return 1L;
                    }
                };
            }

            @Override
            public int size() {
                return 10;
            }
        };
    }
}
