/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

/**
 * Scalar double with alarm, timestamp, display and control information.
 * Auto-unboxing makes the extra method for the primitive type
 * unnecessary.
 *
 * @author carcassi
 */
public abstract class VDouble extends VNumber {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract Double getValue();

    /**
     * Creates a new VDouble.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VDouble of(final Double value, final Alarm alarm, final Time time, final Display display) {
        return new IVDouble(value, alarm, time, display);
    }

    /**
     * Creates a new VDouble.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VDouble of(final Number value, final Alarm alarm, final Time time, final Display display) {
        return new IVDouble(value.doubleValue(), alarm, time, display);
    }
}
