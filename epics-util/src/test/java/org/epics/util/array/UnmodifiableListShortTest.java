/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
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
