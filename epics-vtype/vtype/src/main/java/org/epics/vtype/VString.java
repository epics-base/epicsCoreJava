/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.compat.legacy.lang.Objects;

/**
 * Scalar string with alarm and timestamp.
 *
 * @author carcassi
 */
public abstract class VString extends Scalar {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract String getValue();


    /**
     * Creates a new VString.
     *
     * @param value the string value
     * @param alarm the alarm
     * @param time the time
     * @return the new value
     */
    public static VString of(final String value, final Alarm alarm, final Time time) {
        return new IVString(value, alarm, time);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

	if (obj instanceof VString) {
            VString other = (VString) obj;

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
                .append("[\"")
                .append(getValue())
                .append("\", ")
                .append(getAlarm())
                .append(", ")
                .append(getTime())
                .append(']');
        return builder.toString();
    }

}
