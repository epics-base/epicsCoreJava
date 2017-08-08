/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.epics.util.array.ArrayByte;
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
public class ArrayByteTest extends FeatureTestListByte {

    @Override
    public ArrayByte createConstantCollection() {
        return new ArrayByte(new byte[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
    }

    @Override
    public ArrayByte createRampCollection() {
        return new ArrayByte(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    }

    @Override
    public ArrayByte createModifiableCollection() {
        return new ArrayByte(new byte[10], false);
    }

    @Test
    public void serialization1() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(buffer);
        ArrayByte array = new ArrayByte(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        stream.writeObject(array);
        ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        ArrayByte read = (ArrayByte) inStream.readObject();
        assertThat(read, not(sameInstance(array)));
        assertThat(read, equalTo(array));
    }
}
