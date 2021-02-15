/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
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
public class ArrayUShortTest extends FeatureTestListNumber {

    @Override
    public ArrayUShort createConstantCollection() {
        return CollectionNumbers.unmodifiableListUShort(new short[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
    }

    @Override
    public ArrayUShort createRampCollection() {
        return CollectionNumbers.unmodifiableListUShort(new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    }

    @Override
    public ArrayUShort createModifiableCollection() {
        return CollectionNumbers.toListUShort(new short[10]);
    }

    @Override
    public ArrayUShort createEmpty() {
        return CollectionNumbers.toListUShort(new short[0]);
    }

    @Test
    public void serialization1() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(buffer);
        ArrayUShort array = createRampCollection();
        stream.writeObject(array);
        ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        ArrayUShort read = (ArrayUShort) inStream.readObject();
        assertThat(read, not(sameInstance(array)));
        assertThat(read, equalTo(array));
    }

    @Test
    public void toStringOverflow() {
        ListNumber list = ArrayUShort.of(new short[] {-1, 0, 1});
        assertThat(list.toString(), equalTo("[65535, 0, 1]"));
    }
}
