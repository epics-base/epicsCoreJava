package org.epics.vtype;

import org.epics.util.array.ArrayInteger;
import org.epics.util.array.ListBoolean;

/**
 * Boolean array with alarm, timestamp, display and control information.
 *
 * @author carcassi
 */
public abstract class VBooleanArray extends Array implements AlarmProvider, TimeProvider {

    /**
     * {@inheritDoc }
     * @return the data
     */
    @Override
    public abstract ListBoolean getData();
    

    /**
     * Creates a new VBooleanArray.
     * 
     * @param data the value
     * @param alarm the alarm
     * @param time the time
     * @return the new value
     */
    public static VBooleanArray of(final ListBoolean data, final Alarm alarm, final Time time) {
        return new IVBooleanArray(data, ArrayInteger.of(data.size()), alarm, time);
    }
}
