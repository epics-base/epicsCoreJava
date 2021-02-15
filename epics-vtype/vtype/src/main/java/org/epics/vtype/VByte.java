/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

/**
 * Scalar byte with alarm, timestamp, display and control information.
 * Auto-unboxing makes the extra method for the primitive type
 * unnecessary.
 *
 * @author carcassi
 */
public abstract class VByte extends VNumber {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract Byte getValue();

    /**
     * Creates a new VByte.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VByte of(final Byte value, final Alarm alarm, final Time time, final Display display) {
        return new IVByte(value, alarm, time, display);
    }

    /**
     * Creates a new VByte.
     *
     * @param value the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new value
     */
    public static VByte of(final Number value, final Alarm alarm, final Time time, final Display display) {
        return new IVByte(value.byteValue(), alarm, time, display);
    }
}
