/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import java.util.AbstractList;
import java.util.List;

/**
 * Source of the alarm.
 * <p>
 * Values are provided in order of increasing severity, so you can rely on
 * {@link #ordinal() } and {@code #compareTo(java.lang.Enum) } for comparison
 * and ordering. In case additional AlarmStatus values are added in the future,
 * which is very unlikely, they will be added in order as well.
 * <p>
 * TODO: re-adding this because it's in EPICSv4. But wasn't this problematic because
 * an alarm can have more than one source?
 *
 * @author carcassi
 */
public enum AlarmStatus {
    /**
     * The current value is valid, and there is no alarm.
     */
    NONE,

    DEVICE,

    DRIVER,

    RECORD,

    DB,

    CONF,

    UNDEFINED,

    CLIENT;

    private static final List<String> labels = new AbstractList<String>() {
        @Override
        public String get(int index) {
            return AlarmStatus.values()[index].name();
        }

        @Override
        public int size() {
            return AlarmStatus.values().length;
        }
    };

    /**
     * Returns the list of labels for the status.
     * <p>
     * This is useful to create VEnums containing severities.
     *
     * @return an immutable list with the labels
     */
    public static List<String> labels() {
        return labels;
    }
}
