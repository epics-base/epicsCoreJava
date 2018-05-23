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
public class CollectionByteTest extends FeatureTestCollectionNumber {

    @Override
    public CollectionNumber createConstantCollection() {
        return new CollectionByte() {

            @Override
            public IteratorByte iterator() {
                return new IteratorByte() {

                    int n=0;

                    @Override
                    public boolean hasNext() {
                        return n < 10;
                    }

                    @Override
                    public byte nextByte() {
                        n++;
                        return (byte) 1;
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
