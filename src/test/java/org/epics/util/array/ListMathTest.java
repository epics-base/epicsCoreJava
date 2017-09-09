/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.epics.util.array.CollectionNumbers.*;

/**
 *
 * @author carcassi
 */
public class ListMathTest {

    public ListMathTest() {
    }

    @Test
    public void rescale1() {
        ArrayDouble array1 = unmodifiableListDouble(0, 1, 2, 3, 4, 5);
        ListDouble rescaled = ListMath.rescale(array1, 2.5, -5.0);
        assertThat(rescaled, equalTo(unmodifiableListDouble(-5.0, -2.5, 0, 2.5, 5.0, 7.5)));
    }

    @Test
    public void rescaleWithfactor1() {
        ArrayDouble array1 = unmodifiableListDouble(0, 1, 2, 3, 4, 5);
        ListDouble rescaled = ListMath.rescale(array1, 1, 1);
        assertThat(rescaled, equalTo(unmodifiableListDouble(1.0, 2.0, 3.0, 4.0, 5.0, 6.0)));
    }

    @Test
    public void sum1() {
        ArrayDouble array1 = unmodifiableListDouble(0, 1, 2, 3, 4, 5);
        ListDouble summed = ListMath.add(array1, ListMath.rescale(array1, -1.0, 0.0));
        assertThat(summed, equalTo(unmodifiableListDouble(0, 0, 0, 0, 0, 0)));
    }

    @Test
    public void dft1() {
        ListDouble x = unmodifiableListDouble(0, 1.0, 0, -1.0, 0, 1, 0, -1);
        ListDouble y = unmodifiableListDouble(0, 0, 0, 0, 0, 0, 0, 0);
        List<ListNumber> res = ListMath.dft(x, y);
    }
}
