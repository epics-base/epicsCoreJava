/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.number.UInteger;

/**
 * Scalar unsigned int with alarm, timestamp, display and control information.
 *
 * @author carcassi
 */
public abstract class VUInt extends VNumber {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract UInteger getValue();

    /**
     * Creates a new VUShort.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VUInt of(final UInteger value, final Alarm alarm, final Time time, final Display display) {
        return new IVUInt(value, alarm, time, display);
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
    public static VUInt of(final Number value, final Alarm alarm, final Time time, final Display display) {
        return new IVUInt(new UInteger(value.intValue()), alarm, time, display);
    }
}
