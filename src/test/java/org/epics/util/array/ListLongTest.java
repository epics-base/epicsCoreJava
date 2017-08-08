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
public class ListLongTest extends FeatureTestListLong {

    @Override
    public ListLong createConstantCollection() {
        return new ListLong() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public long getLong(int index) {
                return 1L;
            }
        };
    }

    @Override
    public ListLong createRampCollection() {
        return new ListLong() {

            @Override
            public int size() {
                return 10;
            }

            @Override
            public long getLong(int index) {
                return index;
            }
        };
    }

    @Override
    public ListNumber createModifiableCollection() {
        return new ListLong() {
            
            private long[] array = new long[10];
            
            @Override
            public long getLong(int index) {
                return array[index];
            }

            @Override
            public void setLong(int index, long value) {
                array[index] = value;
            }

            @Override
            public int size() {
                return array.length;
            }
        };
    }
}
