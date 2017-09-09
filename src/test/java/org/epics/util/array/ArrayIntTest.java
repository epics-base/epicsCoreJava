/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.epics.util.array.ArrayInt;
import org.epics.util.array.CollectionNumbers;
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
public class ArrayIntTest extends FeatureTestListNumber {

    @Override
    public ArrayInt createConstantCollection() {
        return new ArrayInt(new int[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
    }

    @Override
    public ArrayInt createRampCollection() {
        return new ArrayInt(new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    }

    @Override
    public ArrayInt createModifiableCollection() {
        return new ArrayInt(new int[10], false);
    }

    @Test
    public void serialization1() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(buffer);
        ArrayInt array = createRampCollection();
        stream.writeObject(array);
        ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        ArrayInt read = (ArrayInt) inStream.readObject();
        assertThat(read, not(sameInstance(array)));
        assertThat(read, equalTo(array));
    }
}
