/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.epics.util.compat.legacy.lang.Objects;
import org.epics.util.array.ListByte;
import org.epics.util.array.ListDouble;
import org.epics.util.array.ListFloat;
import org.epics.util.array.ListInteger;
import org.epics.util.array.ListLong;
import org.epics.util.array.ListNumber;
import org.epics.util.array.ListShort;
import org.epics.util.array.ListUByte;
import org.epics.util.array.ListUInteger;
import org.epics.util.array.ListULong;
import org.epics.util.array.ListUShort;

/**
 * Numeric array with alarm, timestamp, display and control information.
 * <p>
 * This class allows to use any numeric array (i.e. {@link VIntArray} or
 * {@link VDoubleArray}) through the same interface.
 *
 * @author carcassi
 */
public abstract class VNumberArray extends Array implements AlarmProvider, TimeProvider, DisplayProvider {

    /**
     * The numeric value.
     *
     * @return the value
     */
    @Override
    public abstract ListNumber getData();

    /**
     * Default toString implementation for VNumberArray.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Class type = typeOf(this);
        builder.append(type.getSimpleName())
                .append("[");
        builder.append(getData());
        builder.append(", size ")
                .append(getSizes())
                .append(", ")
                .append(getAlarm())
                .append(", ")
                .append(getTime())
                .append(']');
        return builder.toString();
    }

    /**
     * Creates a new {@code VNumberArray} based on the type of the data
     *
     * @param data the value
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new number array
     */
    public static VNumberArray of(ListNumber data, Alarm alarm, Time time, Display display){
        if (data instanceof ListDouble) {
            return VDoubleArray.of((ListDouble) data, alarm, time, display);
        } else if (data instanceof ListFloat) {
            return VFloatArray.of((ListFloat) data, alarm, time, display);
        } else if (data instanceof ListULong) {
            return VULongArray.of((ListULong) data, alarm, time, display);
        } else if (data instanceof ListLong) {
            return VLongArray.of((ListLong) data, alarm, time, display);
        } else if (data instanceof ListUInteger) {
            return VUIntArray.of((ListUInteger) data, alarm, time, display);
        } else if (data instanceof ListInteger) {
            return VIntArray.of((ListInteger) data, alarm, time, display);
        } else if (data instanceof ListUShort) {
            return VUShortArray.of((ListUShort) data, alarm, time, display);
        } else if (data instanceof ListShort) {
            return VShortArray.of((ListShort) data, alarm, time, display);
        } else if (data instanceof ListUByte) {
            return VUByteArray.of((ListUByte) data, alarm, time, display);
        } else if (data instanceof ListByte) {
            return VByteArray.of((ListByte) data, alarm, time, display);
        }
	throw new UnsupportedOperationException();
    }

    /**
     * Creates a new {@code VNumberArray} based on the type of the data
     *
     * @param data the value
     * @param sizes the sizes
     * @param alarm the alarm
     * @param time the time
     * @param display the display
     * @return the new number array
     */
    public static VNumberArray of(ListNumber data, ListInteger sizes, Alarm alarm, Time time, Display display){
        if (data instanceof ListDouble) {
            return VDoubleArray.of((ListDouble) data, sizes, alarm, time, display);
        } else if (data instanceof ListFloat) {
            return VFloatArray.of((ListFloat) data, sizes, alarm, time, display);
        } else if (data instanceof ListULong) {
            return VULongArray.of((ListULong) data, sizes, alarm, time, display);
        } else if (data instanceof ListLong) {
            return VLongArray.of((ListLong) data, sizes, alarm, time, display);
        } else if (data instanceof ListUInteger) {
            return VUIntArray.of((ListUInteger) data, sizes, alarm, time, display);
        } else if (data instanceof ListInteger) {
            return VIntArray.of((ListInteger) data, sizes, alarm, time, display);
        } else if (data instanceof ListUShort) {
            return VUShortArray.of((ListUShort) data, sizes, alarm, time, display);
        } else if (data instanceof ListShort) {
            return VShortArray.of((ListShort) data, sizes, alarm, time, display);
        } else if (data instanceof ListUByte) {
            return VUByteArray.of((ListUByte) data, sizes, alarm, time, display);
        } else if (data instanceof ListByte) {
            return VByteArray.of((ListByte) data, sizes, alarm, time, display);
        }
	throw new UnsupportedOperationException();
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

	if (obj instanceof VNumberArray) {
            VNumberArray other = (VNumberArray) obj;

            return getClass().equals(other.getClass()) &&
                    getData().equals(other.getData()) &&
                    getAlarm().equals(other.getAlarm()) &&
                    getTime().equals(other.getTime()) &&
                    getDisplay().equals(other.getDisplay());
        }

        return false;
    }

    @Override
    public final int hashCode() {
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(getData());
        hash = 23 * hash + Objects.hashCode(getAlarm());
        hash = 23 * hash + Objects.hashCode(getTime());
        hash = 23 * hash + Objects.hashCode(getDisplay());
        return hash;
    }

}
