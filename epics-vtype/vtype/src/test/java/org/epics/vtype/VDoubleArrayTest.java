/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ListDouble;
import org.epics.util.array.ListInteger;

/**
 *
 * @author carcassi
 */
public class VDoubleArrayTest extends FeatureTestVNumberArray<ListDouble, VDoubleArray> {

    @Override
    ListDouble getData() {
        return ArrayDouble.of(0,1,2,3,4,5,6,7,8,9);
    }

    @Override
    ListDouble getOtherData() {
        return ArrayDouble.of(0,-1,-2,-3,-4,-5,-6,-7,-8,-9);
    }

    @Override
    VDoubleArray of(ListDouble data, Alarm alarm, Time time, Display display) {
        return VDoubleArray.of(data, alarm, time, display);
    }

    @Override
    VDoubleArray of(ListDouble data, ListInteger sizes, Alarm alarm, Time time, Display display) {
        return VDoubleArray.of(data, sizes, alarm, time, display);
    }

    @Override
    String getToString() {
        // Modified precision of test to match joda time's millisecond precision
        return "VDoubleArray[[0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0], size [5, 2], MINOR(DB) - LOW, 2012-12-05T14:57:21.521Z]";
    }

}
