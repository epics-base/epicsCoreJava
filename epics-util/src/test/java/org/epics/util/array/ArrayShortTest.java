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
public class ArrayShortTest extends FeatureTestListNumber {

    @Override
    public ArrayShort createConstantCollection() {
        return CollectionNumbers.unmodifiableListShort(new short[] {1, 1, 1, 1, 1, 1, 1, 1, 1, 1});
    }

    @Override
    public ArrayShort createRampCollection() {
        return CollectionNumbers.unmodifiableListShort(new short[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
    }

    @Override
    public ArrayShort createModifiableCollection() {
        return CollectionNumbers.toListShort(new short[10]);
    }

    @Override
    public ListNumber createEmpty() {
        return CollectionNumbers.toListShort(new short[0]);
    }

    @Test
    public void serialization1() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(buffer);
        ArrayShort array = createRampCollection();
        stream.writeObject(array);
        ObjectInputStream inStream = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()));
        ArrayShort read = (ArrayShort) inStream.readObject();
        assertThat(read, not(sameInstance(array)));
        assertThat(read, equalTo(array));
    }

    @Test
    public void toStringOverflow() {
        ListNumber list = ArrayShort.of(new short[] {-1, 0, 1});
        assertThat(list.toString(), equalTo("[-1, 0, 1]"));
    }
}
