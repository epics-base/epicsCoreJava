/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.array.ArrayLong;
import org.epics.util.array.ListLong;
import org.epics.util.array.ListInt;

/**
 *
 * @author carcassi
 */
public class VLongArrayTest extends FeatureTestVNumberArray<ListLong, VLongArray> {

    @Override
    ListLong getData() {
        return ArrayLong.of(0,1,2,3,4,5,6,7,8,9);
    }

    @Override
    VLongArray of(ListLong data, Alarm alarm, Time time, Display display) {
        return VLongArray.of(data, alarm, time, display);
    }

    @Override
    VLongArray of(ListLong data, ListInt sizes, Alarm alarm, Time time, Display display) {
        return VLongArray.of(data, sizes, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VLongArray[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9], size [5, 2], MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}
