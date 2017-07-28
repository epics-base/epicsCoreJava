/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import java.util.Collection;

/**
 * A collection of numeric (primitive) elements. It provides a size and
 * can be iterated more than once.
 * <p>
 * The method names are taken from {@link Collection}, though not all
 * methods are specified. At this moment, the class is read-only. If in the
 * future the class is extended, the new methods should match the names from
 * {@link Collection}.
 *
 * @author Gabriele Carcassi
 */
public interface CollectionNumber {

    /**
     * Returns an iterator over the elements of the collection.
     *
     * @return a new iterator
     */
    IteratorNumber iterator();

    /**
     * Returns the number of elements in the collection.
     *
     * @return the number of elements in the collection
     */
    int size();

    /**
     * 
     * @param <T> the type of the array
     * @param array the array into which the elements of this list are to
     *          be stored, if it is big enough; otherwise, a new array of the
     *          same runtime type is allocated for this purpose.
     * @throws ArrayStoreException if the type of the specified array
     *         is not a primitive number array
     * @throws NullPointerException if the specified array is null
     * @return an array containing the elements
     */
    default <T> T toArray(T array) {
        return CollectionNumbers.defaultToArray(this, array);
    }
}
