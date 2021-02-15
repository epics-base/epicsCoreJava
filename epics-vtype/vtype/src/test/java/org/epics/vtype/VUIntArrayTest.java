/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.array.ArrayUInteger;
import org.epics.util.array.ListInteger;
import org.epics.util.array.ListUInteger;

/**
 *
 * @author carcassi
 */
public class VUIntArrayTest extends FeatureTestVNumberArray<ListUInteger, VUIntArray> {

    @Override
    ListUInteger getData() {
        return ArrayUInteger.of(0,1,2,3,4,5,6,7,8,9);
    }

    @Override
    ListUInteger getOtherData() {
        return ArrayUInteger.of(0,-1,-2,-3,-4,-5,-6,-7,-8,-9);
    }

    @Override
    VUIntArray of(ListUInteger data, Alarm alarm, Time time, Display display) {
        return VUIntArray.of(data, alarm, time, display);
    }

    @Override
    VUIntArray of(ListUInteger data, ListInteger sizes, Alarm alarm, Time time, Display display) {
        return VUIntArray.of(data, sizes, alarm, time, display);
    }

    @Override
    String getToString() {
        // Modified precision of test to match joda time's millisecond precision
        return "VUIntArray[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9], size [5, 2], MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
