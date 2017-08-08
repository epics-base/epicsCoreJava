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
public class ListLongTest extends ListNumberTestBase<ListLong> {

    public ListLongTest() {
        super(new ListLong() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public long getLong(int index) {
                return 1L;
            }
        },
        new ListLong() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public long getLong(int index) {
                return index;
            }
        },
        new ArrayLong(new long[] {0,1,2,3,4,5,6,7,8,9}));
    }

    @Test
    public void toString1() {
        assertThat(incrementCollection.toString(), equalTo("[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]"));
    }

}
