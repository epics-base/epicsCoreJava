package org.epics.vtype;

import org.epics.util.array.ListBoolean;
import org.epics.util.array.ListInteger;

public class IVBooleanArray extends VBooleanArray {

    private final ListBoolean data;
    private final Alarm alarm;
    private final Time time;
    private final ListInteger sizes;

    IVBooleanArray(ListBoolean data, ListInteger sizes, Alarm alarm, Time time) {
        VType.argumentNotNull("data", data);
        VType.argumentNotNull("sizes", sizes);
        VType.argumentNotNull("alarm", alarm);
        VType.argumentNotNull("time", time);
        this.data = data;
        this.alarm = alarm;
        this.time = time;
        this.sizes = sizes;
    }

    @Override
    public ListInteger getSizes() {
        return sizes;
    }

    @Override
    public ListBoolean getData() {
        return data;
    }

    public Alarm getAlarm() {
        return alarm;
    }

    public Time getTime() {
        return time;
    }

}
