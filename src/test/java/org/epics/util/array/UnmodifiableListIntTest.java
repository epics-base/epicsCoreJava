/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

/**
 *
 * @author carcassi
 */
public class UnmodifiableListIntTest extends ListIntTest {

    @Override
    public ListInt createConstantCollection() {
        return CollectionNumbers.unmodifiableList(super.createConstantCollection());
    }

    @Override
    public ListInt createRampCollection() {
        return CollectionNumbers.unmodifiableList(super.createRampCollection());
    }
}
