/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.array;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 *
 * @author carcassi
 */
public class ArrayULongTest extends FeatureTestListNumber {

    @Override
    public ArrayULong createConstantCollection() {
        return CollectionNumbers.unmodifiableListULong(1, 1, 1, 1, 1, 1, 1, 1, 1, 1);
    }

    @Override
    public ArrayULong createRampCollection() {
        return CollectionNumbers.unmodifiableListULong(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Override
    public ArrayULong createModifiableCollection() {
        return CollectionNumbers.toListULong(new long[10]);
    }

    @Override
    public ArrayULong createEmpty() {
        return CollectionNumbers.toListULong(new long[0]);
    }

    @Test
    public void serialization1() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(buffer);
        ArrayULong array = createRampCollection();
        stream.writeObject(array);
        ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        ArrayULong read = (ArrayULong) inStream.readObject();
        assertThat(read, not(sameInstance(array)));
        assertThat(read, equalTo(array));
    }

    @Test
    public void toStringOverflow() {
        ListNumber list = ArrayULong.of(new long[] {-1, 0, 1});
        assertThat(list.toString(), equalTo("[18446744073709551615, 0, 1]"));
    }
}
