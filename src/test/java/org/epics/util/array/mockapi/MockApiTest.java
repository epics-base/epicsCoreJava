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
}
