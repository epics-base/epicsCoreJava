/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 * A collection of unsigned {@code int}s.
 *
 */
public interface CollectionUInt extends CollectionNumber {

    @Override
    IteratorUInt iterator();

}
