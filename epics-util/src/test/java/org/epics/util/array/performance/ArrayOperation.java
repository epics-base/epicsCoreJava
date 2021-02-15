/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array.performance;

import org.epics.util.array.CollectionNumber;
import org.epics.util.array.IteratorNumber;

/**
 *
 * @author carcassi
 */
public abstract class ArrayOperation {
    public abstract double compute(CollectionNumber collection);

    public static final ArrayOperation average = new ArrayOperation() {

        @Override
        public double compute(CollectionNumber collection) {
            IteratorNumber iter = collection.iterator();
            double sum = 0;
            while (iter.hasNext()) {
                sum += iter.nextDouble();
            }
            sum /= collection.size();
            return sum;
        }
    };

    public static final ArrayOperation sum = new ArrayOperation() {

        @Override
        public double compute(CollectionNumber collection) {
            IteratorNumber iter = collection.iterator();
            double sum = 0;
            while (iter.hasNext()) {
                sum += iter.nextDouble();
            }
            return sum;
        }
    };



}
