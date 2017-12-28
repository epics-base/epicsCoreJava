/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 *
 * @author carcassi
 */
public class UnmodifiableListShortTest extends ListShortTest {

    @Override
    public ListShort createConstantCollection() {
        return CollectionNumbers.unmodifiableList(super.createConstantCollection());
    }

    @Override
    public ListShort createRampCollection() {
        return CollectionNumbers.unmodifiableList(super.createRampCollection());
    }
}
