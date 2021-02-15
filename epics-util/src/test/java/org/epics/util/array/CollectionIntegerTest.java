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
public class CollectionIntegerTest extends FeatureTestCollectionNumber {

    @Override
    public CollectionNumber createConstantCollection() {
        return new AbstractCollectionInteger() {

            public IteratorInteger iterator() {
                return new IteratorInteger() {

                    int n=0;

                    public boolean hasNext() {
                        return n < 10;
                    }

                    public int nextInt() {
                        n++;
                        return 1;
                    }
                };
            }

            public int size() {
                return 10;
            }
        };
    }
}
