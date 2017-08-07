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
public class CollectionLongTest extends CollectionNumberTestBase<CollectionLong> {

    public CollectionLongTest() {
        super(new CollectionLong() {

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
        });
    }
}
