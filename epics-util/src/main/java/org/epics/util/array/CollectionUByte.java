/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 * A collection of unsigned {@code byte}s.
 *
 */
public interface CollectionUByte extends CollectionNumber {

    @Override
    IteratorUByte iterator();

}
