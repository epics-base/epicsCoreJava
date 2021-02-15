/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

/**
 * Scalar short with alarm, timestamp, display and control information.
 * Auto-unboxing makes the extra method for the primitive type
 * unnecessary.
 *
 * @author carcassi
 */
public abstract class VShort extends VNumber {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract Short getValue();

    /**
     * Creates a new VShort.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VShort of(final Short value, final Alarm alarm, final Time time, final Display display) {
        return new IVShort(value, alarm, time, display);
    }

    /**
     * Creates a new VShort.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VShort of(final Number value, final Alarm alarm, final Time time, final Display display) {
        return new IVShort(value.shortValue(), alarm, time, display);
    }
}
