/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import java.util.Collection;

/**
 * A collection of numeric (primitive) elements. It provides basis iteration and
 * copy to array.
 * <p>
 * The method names are taken from {@link Collection}, though not all
 * methods are specified. At this moment, the class is read-only. If in the
 * future the class is extended, the new methods should match the names from
 * {@link Collection}.
 *
 * @author George McIntyre. 15-Feb-2021, SLAC
 */
public abstract class AbstractCollectionNumber extends AbstractCollection implements CollectionNumber {
    /**
     * @param <T>   the type of the array
     * @param array the array into which the elements of this list are to
     *              be stored, if it is big enough; otherwise, a new array of the
     *              same runtime type is allocated for this purpose.
     * @return an array containing the elements
     * @throws ArrayStoreException  if the type of the specified array
     *                              is not a primitive number array
     * @throws NullPointerException if the specified array is null
     */
    public <T> T toArray(T array) {
        return CollectionNumbers.defaultToArray(this, array);
    }
}
