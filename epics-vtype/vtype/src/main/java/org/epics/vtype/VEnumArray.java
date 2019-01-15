package org.epics.vtype;

import java.util.List;

import org.epics.util.array.ListInteger;

public abstract class VEnumArray extends Array implements AlarmProvider, TimeProvider {

    /**
     * 
     */
    @Override
    public abstract List<String> getData();

    /**
     * Returns the indexes instead of the labels.
     *
     * @return an array of indexes
     */
    public abstract ListInteger getIndexes();

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
}
