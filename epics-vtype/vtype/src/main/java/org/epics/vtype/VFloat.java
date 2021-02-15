/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

/**
 * Scalar float with alarm, timestamp, display and control information.
 * Auto-unboxing makes the extra method for the primitive type
 * unnecessary.
 *
 * @author carcassi
 */
public abstract class VFloat extends VNumber {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract Float getValue();

    /**
     * Creates a new VFloat.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VFloat of(final Float value, final Alarm alarm, final Time time, final Display display) {
        return new IVFloat(value, alarm, time, display);
    }

    /**
     * Creates a new VFloat.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VFloat of(final Number value, final Alarm alarm, final Time time, final Display display) {
        return new IVFloat(value.floatValue(), alarm, time, display);
    }
}
