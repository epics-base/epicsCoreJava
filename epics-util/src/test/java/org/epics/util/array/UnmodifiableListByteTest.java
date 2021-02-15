/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

/**
 *
 * @author carcassi
 */
public class UnmodifiableListByteTest extends ListByteTest {

    @Override
    public ListByte createConstantCollection() {
        return CollectionNumbers.unmodifiableList(super.createConstantCollection());
    }

    @Override
    public ListByte createRampCollection() {
        return CollectionNumbers.unmodifiableList(super.createRampCollection());
    }
}
