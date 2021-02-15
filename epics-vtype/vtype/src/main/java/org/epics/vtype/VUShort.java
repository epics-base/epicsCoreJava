/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.number.UShort;

/**
 * Scalar unsigned short with alarm, timestamp, display and control information.
 *
 * @author carcassi
 */
public abstract class VUShort extends VNumber {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract UShort getValue();

    /**
     * Creates a new VUShort.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VUShort of(final UShort value, final Alarm alarm, final Time time, final Display display) {
        return new IVUShort(value, alarm, time, display);
    }

    /**
     * Creates a new VUShort.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VUShort of(final Number value, final Alarm alarm, final Time time, final Display display) {
        return new IVUShort(new UShort(value.shortValue()), alarm, time, display);
    }
}
