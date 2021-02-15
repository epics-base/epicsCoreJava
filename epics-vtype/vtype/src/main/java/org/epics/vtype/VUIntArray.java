/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.array.ArrayInteger;
import org.epics.util.array.ListInteger;
import org.epics.util.array.ListUInteger;

/**
 * Scalar unsigned byte array with alarm, timestamp, display and control information.
 *
 * @author carcassi
 */
public abstract class VUIntArray extends VNumberArray {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract ListUInteger getData();

    /**
     * Creates a new VUIntArray.
     *
     * @param data the value
     * @param sizes the sizes
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VUIntArray of(final ListUInteger data, final ListInteger sizes, final Alarm alarm, final Time time, final Display display) {
        return new IVUIntArray(data, sizes, alarm, time, display);
    }

    /**
     * Creates a new VUIntArray.
     *
     * @param data the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VUIntArray of(final ListUInteger data, final Alarm alarm, final Time time, final Display display) {
        return of(data, ArrayInteger.of(data.size()), alarm, time, display);
    }
}
