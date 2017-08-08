/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.util.array;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class ListFloatTest extends ListNumberTestBase<ListFloat> {

    public ListFloatTest() {
        super(new ListFloat() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public float getFloat(int index) {
                return 1.0F;
            }
        },
        new ListFloat() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public float getFloat(int index) {
                return index;
            }
        },
        new ArrayFloat(new float[] {0,1,2,3,4,5,6,7,8,9}));
    }

    @Test
    public void toString1() {
        assertThat(incrementCollection.toString(), equalTo("[0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0]"));
    }

}
