/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import java.text.NumberFormat;
import org.epics.util.stats.Range;

/**
 * Immutable {@code Display} implementation.
 *
 * @author carcassi
 */
final class IDisplay extends Display {

    private final Range displayRange;
    private final Range alarmRange;
    private final Range warningRange;
    private final Range controlRange;
    private final String unit;
    private final NumberFormat format;

    public IDisplay(Range displayRange, Range alarmRange, Range warningRange,
            Range controlRange, String unit, NumberFormat format) {
        VType.argumentNotNull("displayRange", displayRange);
        VType.argumentNotNull("warningRange", warningRange);
        VType.argumentNotNull("alarmRange", alarmRange);
        VType.argumentNotNull("controlRange", controlRange);
        VType.argumentNotNull("unit", unit);
        VType.argumentNotNull("format", format);
        this.displayRange = displayRange;
        this.warningRange = warningRange;
        this.alarmRange = alarmRange;
        this.controlRange = controlRange;
        this.unit = unit;
        this.format = format;
    }

    @Override
    public Range getDisplayRange() {
        return displayRange;
    }

    @Override
    public Range getWarningRange() {
        return warningRange;
    }

    @Override
    public Range getAlarmRange() {
        return alarmRange;
    }

    @Override
    public Range getControlRange() {
        return controlRange;
    }

    @Override
    public String getUnit() {
        return unit;
    }

    @Override
    public NumberFormat getFormat() {
        return format;
    }

}
