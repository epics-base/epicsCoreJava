/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
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
        // Modified precision of test to match joda time's millisecond precision
        return "VUShortArray[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9], size [5, 2], MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
