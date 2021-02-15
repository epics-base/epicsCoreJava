/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.number.UByte;

/**
 * Scalar unsigned byte with alarm, timestamp, display and control information.
 *
 * @author carcassi
 */
public abstract class VUByte extends VNumber {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract UByte getValue();

    /**
     * Creates a new VUByte.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VUByte of(final UByte value, final Alarm alarm, final Time time, final Display display) {
        return new IVUByte(value, alarm, time, display);
    }

    /**
     * Creates a new VUByte.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VUByte of(final Number value, final Alarm alarm, final Time time, final Display display) {
        return new IVUByte(new UByte(value.byteValue()), alarm, time, display);
    }
}
