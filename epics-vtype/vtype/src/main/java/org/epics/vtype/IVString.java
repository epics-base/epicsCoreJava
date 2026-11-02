/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import java.util.Optional;

/**
 * Immutable {@code VString} implementation.
 *
 * @author carcassi
 */
final class IVString extends VString {
    
    private final String value;
    private final Alarm alarm;
    private final Time time;
    private final Optional<String> description;

    IVString(String value, Alarm alarm, Time time, Optional<String> description) {
        VType.argumentNotNull("value", value);
        VType.argumentNotNull("alarm", alarm);
        VType.argumentNotNull("time", time);
        VType.argumentNotNull("description", description);
        this.value = value;
        this.alarm = alarm;
        this.time = time;
        this.description = description;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Alarm getAlarm() {
        return alarm;
    }

    @Override
    public Time getTime() {
        return time;
    }

    @Override
    public Optional<String> getDescription() {
        return description;
    }
}
