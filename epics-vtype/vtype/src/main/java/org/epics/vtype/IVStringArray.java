package org.epics.vtype;

import java.util.List;

import org.epics.util.array.ListInteger;

public class IVStringArray extends VStringArray {

    private final List<String> data;
    private final ListInteger sizes;
    private final Alarm alarm;
    private final Time time;

    IVStringArray(List<String> data, ListInteger sizes, Alarm alarm, Time time) {
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
    public List<String> getData() {
        return data;
    }

    public Alarm getAlarm() {
        return alarm;
    }

    public Time getTime() {
        return time;
    }

}
