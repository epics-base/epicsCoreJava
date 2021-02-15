/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.compat.legacy.lang.Objects;

/**
 * Scalar boolean with alarm and timestamp.
 *
 * @author carcassi
 */
public abstract class VBoolean extends Scalar {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract Boolean getValue();


    /**
     * Creates a new VBoolean.
     *
     * @param value the boolean value
     * @param alarm the alarm
     * @param time the time
     * @return the new value
     */
    public static VBoolean of(final Boolean value, final Alarm alarm, final Time time) {
        return new IVBoolean(value, alarm, time);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

	if (obj instanceof VBoolean) {
            VBoolean other = (VBoolean) obj;

            return getValue().equals(other.getValue()) &&
                    getAlarm().equals(other.getAlarm()) &&
                    getTime().equals(other.getTime());
        }

        return false;
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(getValue());
        hash = 23 * hash + Objects.hashCode(getAlarm());
        hash = 23 * hash + Objects.hashCode(getTime());
        return hash;
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        Class type = typeOf(this);
        builder.append(type.getSimpleName())
                .append("[")
                .append(getValue())
                .append(", ")
                .append(getAlarm())
                .append(", ")
                .append(getTime())
                .append(']');
        return builder.toString();
    }

}
