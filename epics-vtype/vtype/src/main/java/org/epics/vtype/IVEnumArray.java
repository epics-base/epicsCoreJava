package org.epics.vtype;

import java.util.ArrayList;
import java.util.List;

import org.epics.util.array.ArrayInteger;
import org.epics.util.array.ListInteger;
import org.epics.util.array.ListNumber;

/**
 * Immutable {@code VEnumArray} implementation.
 *
 * @author carcassi, shroffk, kasemir
 */
public class IVEnumArray extends VEnumArray {

    private final Alarm alarm;
    private final Time time;
    private final ListNumber indices;
    private final EnumDisplay enumDisplay;

    private final List<String> labels;

    IVEnumArray(ListNumber indices, EnumDisplay enumDisplay, Alarm alarm, Time time) {
        VType.argumentNotNull("enumDisplay", enumDisplay);
        this.enumDisplay = enumDisplay;

        VType.argumentNotNull("alarm", alarm);
        VType.argumentNotNull("time", time);

        labels = new ArrayList<String>(indices.size());

        for (int i = 0; i < indices.size(); i++) {
            int index = indices.getInt(i);
            if (index < 0 || index >= enumDisplay.getChoices().size()) {
                labels.add("VEnumArray element " + i + " has index " + index + " outside of permitted options "
                        + enumDisplay.getChoices());
            }
            labels.add(enumDisplay.getChoices().get(index));
        }
        this.indices = indices;
        this.alarm = alarm;
        this.time = time;
    }

    @Override
    public EnumDisplay getDisplay() {
        return enumDisplay;
    }

    public Alarm getAlarm() {
        return alarm;
    }

    public Time getTime() {
        return time;
    }

    @Override
    public List<String> getData() {
        return labels;
    }

    @Override
    public ListNumber getIndexes() {
        return indices;
    }


    @Override
    public ListInteger getSizes() {
        return ArrayInteger.of(labels.size());
    }

}
