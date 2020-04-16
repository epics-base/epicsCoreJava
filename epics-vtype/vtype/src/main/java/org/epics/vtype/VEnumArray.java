package org.epics.vtype;

import java.util.List;

import org.epics.util.array.ListInteger;
import org.epics.util.array.ListNumber;
import org.epics.util.array.ListShort;

/**
 * Scalar enum array with alarm, timestamp, and display information.
 */
public abstract class VEnumArray extends Array implements AlarmProvider, TimeProvider {

    /**
     * Return the enum label values
     */
    @Override
    public abstract List<String> getData();

    /**
     * Returns the indexes instead of the labels.
     *
     * @return an array of indexes
     */
    public abstract ListNumber getIndexes();

    /** @return the enum display information, i.e. choices */
    public abstract EnumDisplay getDisplay();

    /**
     * Return an instance of the VEnumArray
     * @param data the indices
     * @param enumDisplay the enum display labels
     * @param alarm the alarm
     * @param time new time
     * @return {@link VEnumArray} instance of VEnumArray
     */
    public static VEnumArray of(ListInteger data, EnumDisplay enumDisplay, Alarm alarm, Time time) {
        return new IVEnumArray(data, enumDisplay, alarm, time);
    }

    /**
     * Return an instance of the VEnumArray
     * @param data the indices
     * @param enumDisplay the enum display labels
     * @param alarm the alarm
     * @param time new time
     * @return {@link VEnumArray} instance of VEnumArray
     */
    public static VEnumArray of(ListShort data, EnumDisplay enumDisplay, Alarm alarm, Time time) {
        return new IVEnumArray(data, enumDisplay, alarm, time);
    }
}
