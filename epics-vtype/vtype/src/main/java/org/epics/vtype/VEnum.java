/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.compat.legacy.lang.Objects;

/**
 * Scalar enum with alarm and timestamp.
 * Given that enumerated values are of very limited use without
 * the labels, and that the current label is the data most likely used, the
 * enum is scalar of type {@link String}. The index is provided as an extra field, and
 * the list of all possible values is always provided.
 *
 * @author carcassi
 */
public abstract class VEnum extends Scalar {

    /**
     * {@inheritDoc }
     */
    @Override
    public abstract String getValue();

    /**
     * Return the index of the value in the list of labels.
     *
     * @return the current index
     */
    public abstract int getIndex();

    /**
     * Returns the display information, including all possible choice names.
     *
     * @return the enum display
     */
    public abstract EnumDisplay getDisplay();

    /**
     * Create a new VEnum.
     *
     * @param index the index in the label array
     * @param metaData the metadata
     * @param alarm the alarm
     * @param time the time
     * @return the new value
     */
    public static VEnum of(int index, EnumDisplay metaData, Alarm alarm, Time time) {
        return new IVEnum(index, metaData, alarm, time);
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

	if (obj instanceof VEnum) {
            VEnum other = (VEnum) obj;

            return getIndex() == other.getIndex() &&
                    getDisplay().equals(other.getDisplay()) &&
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
