/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 * A collection of unisgned {@code long}s.
 *
 */
public interface CollectionULong extends CollectionNumber {

    @Override
    IteratorULong iterator();

}
