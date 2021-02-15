/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.array.ArrayInteger;
import org.epics.util.array.ListInteger;

/**
 *
 * @author carcassi
 */
public class VIntArrayTest extends FeatureTestVNumberArray<ListInteger, VIntArray> {

    @Override
    ListInteger getData() {
        return ArrayInteger.of(new int[] {0,1,2,3,4,5,6,7,8,9});
    }

    @Override
    ListInteger getOtherData() {
        return ArrayInteger.of(new int[] {0,-1,-2,-3,-4,-5,-6,-7,-8,-9});
    }

    @Override
    VIntArray of(ListInteger data, Alarm alarm, Time time, Display display) {
        return VIntArray.of(data, alarm, time, display);
    }

    @Override
    VIntArray of(ListInteger data, ListInteger sizes, Alarm alarm, Time time, Display display) {
        return VIntArray.of(data, sizes, alarm, time, display);
    }

    @Override
    String getToString() {
        // Modified precision of test to match joda time's millisecond precision
        return "VIntArray[[0, 1, 2, 3, 4, 5, 6, 7, 8, 9], size [5, 2], MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
