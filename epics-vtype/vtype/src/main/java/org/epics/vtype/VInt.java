/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

/**
 * Scalar integer with alarm, timestamp, display and control information.
 * Auto-unboxing makes the extra method for the primitive type
 * unnecessary.
 *
 * @author carcassi
 */
public abstract class VInt extends VNumber {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract Integer getValue();

    /**
     * Creates a new VInt.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VInt of(final Integer value, final Alarm alarm, final Time time, final Display display) {
        return new IVInt(value, alarm, time, display);
    }

    /**
     * Creates a new VInt.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VInt of(final Number value, final Alarm alarm, final Time time, final Display display) {
        return new IVInt(value.intValue(), alarm, time, display);
    }
}
