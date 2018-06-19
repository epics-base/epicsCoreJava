/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

/**
 * Immutable {@code VEnum} implementation.
 *
 * @author carcassi
 */
final class IVEnum extends VEnum {
    
    private final Alarm alarm;
    private final Time time;
    private final int index;
    private final EnumDisplay enumDisplay;

    IVEnum(int index, EnumDisplay enumDisplay, Alarm alarm, Time time) {
        VType.argumentNotNull("enumDisplay", enumDisplay);
        VType.argumentNotNull("alarm", alarm);
        VType.argumentNotNull("time", time);
        if (index < 0 || index >= enumDisplay.getChoices().size()) {
            throw new IndexOutOfBoundsException("VEnum index must be within the label range");
        }
        this.index = index;
        this.enumDisplay = enumDisplay;
        this.alarm = alarm;
        this.time = time;
    }

    @Override
    public String getValue() {
        return enumDisplay.getChoices().get(index);
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public EnumDisplay getDisplay() {
        return enumDisplay;
    }

    @Override
    public Alarm getAlarm() {
        return alarm;
    }

    @Override
    public Time getTime() {
        return time;
    }

}
