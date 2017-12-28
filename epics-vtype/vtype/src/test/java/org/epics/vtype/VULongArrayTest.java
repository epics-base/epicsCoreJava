/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.array.ArrayULong;
import org.epics.util.array.ListInteger;
import org.epics.util.array.ListULong;

/**
 *
 * @author carcassi
 */
public class VULongArrayTest extends FeatureTestVNumberArray<ListULong, VULongArray> {

    @Override
    ListULong getData() {
        return ArrayULong.of(new long[] {0,1,2,3,4,5,6,7,8,9});
    }

    @Override
    VULongArray of(ListULong data, Alarm alarm, Time time, Display display) {
        return VULongArray.of(data, alarm, time, display);
    }

    @Override
    VULongArray of(ListULong data, ListInteger sizes, Alarm alarm, Time time, Display display) {
        return VULongArray.of(data, sizes, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VULongArray[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9], size [5, 2], MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}
