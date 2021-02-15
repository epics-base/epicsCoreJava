/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 *
 * @author carcassi
 */
public class CollectionUByteTest extends FeatureTestCollectionNumber {

    @Override
    public CollectionNumber createConstantCollection() {
        return new AbstractCollectionUByte() {

            public IteratorUByte iterator() {
                return new IteratorUByte() {

                    int n=0;

                    public boolean hasNext() {
                        return n < 10;
                    }

                    public byte nextByte() {
                        n++;
                        return (byte) 1;
                    }
                };
            }

            public int size() {
                return 10;
            }
        };
    }
}
