/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.epics.util.compat.legacy.lang.Objects;
import org.epics.util.stats.Range;

/**
 * Limit and unit information needed for display and control.
 * <p>
 * The numeric limits are given in double precision no matter which numeric
 * type. The unit is a simple String, which can be empty if no unit information
 * is provided. The number format can be used to convert the value to a String.
 *
 * @author carcassi
 */
public abstract class Display {

    /**
     * The range for the value when displayed.
     *
     * @return the display range; can be Range.UNDEFINED but not null
     */
    public abstract Range getDisplayRange();

    /**
     * The range for the alarm associated to the value.
     *
     * @return the alarm range; can be Range.UNDEFINED but not null
     */
    public abstract Range getAlarmRange();

    /**
     * The range for the warning associated to the value.
     *
     * @return the warning range; can be Range.UNDEFINED but not null
     */
    public abstract Range getWarningRange();

    /**
     * The range used for changing the value.
     *
     * @return the control range; can be Range.UNDEFINED but not null
     */
    public abstract Range getControlRange();

    /**
     * String representation of the unit using for all values.
     * Never null. If not available, returns the empty String.
     *
     * @return unit
     */
    public abstract String getUnit();

    /**
     * Returns a NumberFormat that creates a String with just the value (no units).
     * Format is locale independent and should be used for all values (values and
     * min/max of the ranges). Never null.
     *
     * @return the default format for all values
     */
    public abstract NumberFormat getFormat();


    /**
     * Alarm based on the value and the display ranges.
     *
     * @param value the value
     * @return the new alarm
     */
    public Alarm newAlarmFor(Number value) {
        double newValue = value.doubleValue();
        // Calculate new AlarmSeverity, using display ranges
        AlarmSeverity severity = AlarmSeverity.NONE;
        String status = "NONE";
        if (newValue <= getAlarmRange().getMinimum()) {
            return Alarm.lolo();
        } else if (newValue >= getAlarmRange().getMaximum()) {
            return Alarm.hihi();
        } else if (newValue <= getWarningRange().getMinimum()) {
            return Alarm.low();
        } else if (newValue >= getWarningRange().getMaximum()) {
            return Alarm.high();
        }

        return Alarm.none();
    }


    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

	if (obj instanceof Display) {
            Display other = (Display) obj;

            return Objects.equals(getFormat(), other.getFormat()) &&
                Objects.equals(getUnit(), other.getUnit()) &&
                Objects.equals(getDisplayRange(), other.getDisplayRange()) &&
                Objects.equals(getAlarmRange(), other.getAlarmRange()) &&
                Objects.equals(getWarningRange(), other.getWarningRange()) &&
                Objects.equals(getControlRange(), other.getControlRange());
        }

        return false;
    }

    @Override
    public final int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(getFormat());
        hash = 59 * hash + Objects.hashCode(getUnit());
        hash = 59 * hash + Objects.hashCode(getDisplayRange());
        hash = 59 * hash + Objects.hashCode(getAlarmRange());
        hash = 59 * hash + Objects.hashCode(getWarningRange());
        hash = 59 * hash + Objects.hashCode(getControlRange());
        return hash;
    }

    @Override
    public final String toString() {
        return "Display[units: " + getUnit() + " disp: " + getDisplayRange() + " alarm: " + getAlarmRange() + " warn: " + getWarningRange() + " ctrl: " + getControlRange() + " format: " + getFormat() + "]";
    }

    /**
     * Creates a new display.
     *
     * @param displayRange the display range
     * @param warningRange the warning range
     * @param alarmRange the alarm range
     * @param controlRange the control range
     * @param units the units
     * @param numberFormat the preferred number format
     * @return a new display
     */
    public static Display of(final Range displayRange, final Range alarmRange, final Range warningRange,
            final Range controlRange, final String units, final NumberFormat numberFormat) {
        return new IDisplay(displayRange, alarmRange, warningRange,
                controlRange, units, numberFormat);
    }

    // TODO: maybe this can be configured (injected through SPI?)
    private static final NumberFormat DEFAULT_NUMBERFORMAT = new DecimalFormat();

    private static final Display DISPLAY_NONE = of(Range.undefined(),
            Range.undefined(), Range.undefined(), Range.undefined(),
            defaultUnits(), DEFAULT_NUMBERFORMAT);

    /**
     * The default number format for number display.
     *
     * @return a number format
     */
    public static NumberFormat defaultNumberFormat() {
        return DEFAULT_NUMBERFORMAT;
    }

    /**
     * The default unit string.
     *
     * @return an empty string
     */
    public static String defaultUnits() {
        return "";
    }

    /**
     * Empty display information.
     *
     * @return no display
     */
    public static Display none() {
        return DISPLAY_NONE;
    }

    /**
     * Null and non-VType safe utility to extract display information.
     * <ul>
     * <li>If the value has a display, the associated display is returned.</li>
     * <li>If the value has no display, {@link #none()} is returned.</li>
     * <li>If the value is null, {@link #none()} is returned.</li>
     * </ul>
     *
     * @param value the value
     * @return the display information for the value
     */
    public static Display displayOf(Object value) {
        if (value instanceof DisplayProvider) {
            return ((DisplayProvider) value).getDisplay();
        }

        return none();
    }
}
