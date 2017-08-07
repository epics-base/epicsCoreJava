/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array.mockapi;

import java.util.Random;
import org.epics.util.array.*;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class MockApiTest {

    public MockApiTest() {
    }

    @Test
    public void readDoubles() {
        MockClient client = new MockClient();
        DoubleArrayField field = client.createArrayField(new Random(0).doubles(100, 0, 1.0));
        assertThat(field.get().size(), equalTo(100));
        assertThat(field.get().getDouble(0), equalTo(0.730967787376657));
        assertThat(UnsafeUnwrapper.wrappedDoubleArray(field.get()), sameInstance(field.backendArray));
    }

    @Test
    public void readInts() {
        MockClient client = new MockClient();
        IntArrayField field = client.createArrayField(new Random(0).ints(100, 0, 100));
        assertThat(field.get().size(), equalTo(100));
        assertThat(field.get().getInt(0), equalTo(60));
        assertThat(UnsafeUnwrapper.wrappedIntArray(field.get()), sameInstance(field.backendArray));
    }

    @Test
    public void readNumbers() {
        MockClient client = new MockClient();
        NumericArrayField field = client.createArrayField(new Random(0).ints(100, 0, 100));
        assertThat(field.get().size(), equalTo(100));
        assertThat(field.get().getInt(0), equalTo(60));
    }

    @Test
    public void writeDoubles() {
        MockClient client = new MockClient();
        DoubleArrayField field = client.createArrayField(new Random(0).doubles(10, 0, 1.0));
        ArrayDouble doubles = new ArrayDouble(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);
        field.put(0, doubles);
        assertThat(field.get(), equalTo(doubles));
    }

    @Test
    public void writeIntsOverDoubles() {
        MockClient client = new MockClient();
        DoubleArrayField field = client.createArrayField(new Random(0).doubles(10, 0, 1.0));
        ArrayInt ints = new ArrayInt(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        field.put(0, ints);
        assertThat(field.get(), equalTo(new ArrayDouble(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0)));
    }

    @Test
    public void writeInts() {
        MockClient client = new MockClient();
        IntArrayField field = client.createArrayField(new Random(0).ints(10, 0, 100));
        ArrayInt ints = new ArrayInt(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        field.put(0, ints);
        assertThat(field.get(), equalTo(ints));
    }

    @Test
    public void writeDoublesOverInts() {
        MockClient client = new MockClient();
        IntArrayField field = client.createArrayField(new Random(0).ints(10, 0, 100));
        ArrayDouble doubles = new ArrayDouble(0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0);
        field.put(0, doubles);
        assertThat(field.get(), equalTo(new ArrayInt(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)));
    }

    @Test
    public void readAndWriteDoubles1() {
        MockClient client = new MockClient();
        DoubleArrayField field = client.createArrayField(new Random(0).doubles(10, 0, 1.0));
        assertThat(field.get(), equalTo(new ArrayDouble(0.730967787376657, 0.24053641567148587, 0.6374174253501083, 0.5504370051176339, 0.5975452777972018, 0.3332183994766498, 0.3851891847407185, 0.984841540199809, 0.8791825178724801, 0.9412491794821144)));
        ListDouble doubles = new ArrayDouble(field.get());
        doubles.setDouble(5, 1.0 + doubles.getDouble(5));
        field.put(0, doubles);
        assertThat(field.get(), equalTo(new ArrayDouble(0.730967787376657, 0.24053641567148587, 0.6374174253501083, 0.5504370051176339, 0.5975452777972018, 1.3332183994766498, 0.3851891847407185, 0.984841540199809, 0.8791825178724801, 0.9412491794821144)));
    }

    @Test
    public void readAndWriteDoubles2() {
        MockClient client = new MockClient();
        DoubleArrayField field = client.createArrayField(new Random(0).doubles(10, 0, 1.0));
        assertThat(field.get(), equalTo(new ArrayDouble(0.730967787376657, 0.24053641567148587, 0.6374174253501083, 0.5504370051176339, 0.5975452777972018, 0.3332183994766498, 0.3851891847407185, 0.984841540199809, 0.8791825178724801, 0.9412491794821144)));
        ListDouble doubles = new ArrayDouble(field.get().subList(5, 6));
        doubles.setDouble(0, 1.0 + doubles.getDouble(0));
        field.put(5, doubles);
        assertThat(field.get(), equalTo(new ArrayDouble(0.730967787376657, 0.24053641567148587, 0.6374174253501083, 0.5504370051176339, 0.5975452777972018, 1.3332183994766498, 0.3851891847407185, 0.984841540199809, 0.8791825178724801, 0.9412491794821144)));
    }
}
