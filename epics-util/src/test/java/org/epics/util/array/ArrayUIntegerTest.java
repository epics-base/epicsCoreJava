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

    @Test
    public void toStringOverflow() {
        ListNumber list = ArrayUInteger.of(new int[] {-1, 0, 1});
        assertThat(list.toString(), equalTo("[4294967295, 0, 1]"));
    }
}
