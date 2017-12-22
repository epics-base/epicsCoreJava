/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.array.ArrayUInt;
import org.epics.util.array.ListInt;
import org.epics.util.array.ListUInt;

/**
 *
 * @author carcassi
 */
public class VUIntArrayTest extends FeatureTestVNumberArray<ListUInt, VUIntArray> {

    @Override
    ListUInt getData() {
        return ArrayUInt.of(new int[] {0,1,2,3,4,5,6,7,8,9});
    }

    @Override
    VUIntArray of(ListUInt data, Alarm alarm, Time time, Display display) {
        return VUIntArray.of(data, alarm, time, display);
    }

    @Override
    VUIntArray of(ListUInt data, ListInt sizes, Alarm alarm, Time time, Display display) {
        return VUIntArray.of(data, sizes, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VUIntArray[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9], size [5, 2], MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}
