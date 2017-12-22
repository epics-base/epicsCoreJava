/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class ArrayUIntegerTest extends FeatureTestListNumber {

    @Override
    public ArrayUInteger createConstantCollection() {
        return CollectionNumbers.unmodifiableListUInt(1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
    }

    @Override
    public ArrayUInteger createRampCollection() {
        return CollectionNumbers.unmodifiableListUInt(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Override
    public ArrayUInteger createModifiableCollection() {
        return CollectionNumbers.toListUInt(new int[10]);
    }

    @Override
    public ArrayUInteger createEmpty() {
        return CollectionNumbers.toListUInt(new int[0]);
    }

    @Test
    public void serialization1() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(buffer);
        ArrayUInteger array = createRampCollection();
        stream.writeObject(array);
        ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        ArrayUInteger read = (ArrayUInteger) inStream.readObject();
        assertThat(read, not(sameInstance(array)));
        assertThat(read, equalTo(array));
    }
}
