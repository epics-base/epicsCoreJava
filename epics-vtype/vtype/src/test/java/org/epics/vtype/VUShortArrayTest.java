/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.array.ArrayUShort;
import org.epics.util.array.ListInteger;
import org.epics.util.array.ListUShort;

/**
 *
 * @author carcassi
 */
public class VUShortArrayTest extends FeatureTestVNumberArray<ListUShort, VUShortArray> {

    @Override
    ListUShort getData() {
        return ArrayUShort.of(new short[] {0,1,2,3,4,5,6,7,8,9});
    }

    @Override
    ListUShort getOtherData() {
        return ArrayUShort.of(new short[] {0,-1,-2,-3,-4,-5,-6,-7,-8,-9});
    }

    @Override
    VUShortArray of(ListUShort data, Alarm alarm, Time time, Display display) {
        return VUShortArray.of(data, alarm, time, display);
    }

    @Override
    VUShortArray of(ListUShort data, ListInteger sizes, Alarm alarm, Time time, Display display) {
        return VUShortArray.of(data, sizes, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VUShortArray[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9], size [5, 2], MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}
