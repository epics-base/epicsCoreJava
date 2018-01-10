/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

import org.epics.util.array.ArrayShort;
import org.epics.util.array.ListShort;
import org.epics.util.array.ListInteger;

/**
 *
 * @author carcassi
 */
public class VShortArrayTest extends FeatureTestVNumberArray<ListShort, VShortArray> {

    @Override
    ListShort getData() {
        return ArrayShort.of(new short[]{0,1,2,3,4,5,6,7,8,9});
    }

    @Override
    ListShort getOtherData() {
        return ArrayShort.of(new short[] {0,-1,-2,-3,-4,-5,-6,-7,-8,-9});
    }

    @Override
    VShortArray of(ListShort data, Alarm alarm, Time time, Display display) {
        return VShortArray.of(data, alarm, time, display);
    }

    @Override
    VShortArray of(ListShort data, ListInteger sizes, Alarm alarm, Time time, Display display) {
        return VShortArray.of(data, sizes, alarm, time, display);
    }

    @Override
    String getToString() {
        return "VShortArray[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9], size [5, 2], MINOR(DB) - LOW, 2012-12-05T14:57:21.521786982Z]";
    }
    
}
