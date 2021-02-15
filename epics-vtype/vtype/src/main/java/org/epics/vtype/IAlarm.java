/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

/**
 * Immutable {code Alarm} implementation.
 *
 * @author carcassi
 */
final class IAlarm extends Alarm {

    private final AlarmSeverity severity;
    private final AlarmStatus status;
    private final String name;

    IAlarm(AlarmSeverity severity, AlarmStatus status, String name) {
        VType.argumentNotNull("severity", severity);
        VType.argumentNotNull("status", status);
        VType.argumentNotNull("name", name);
        this.severity = severity;
        this.name = name;
        this.status = status;
    }

    @Override
    public AlarmSeverity getSeverity() {
        return severity;
    }

    @Override
    public AlarmStatus getStatus() {
        return status;
    }

    @Override
    public String getName() {
        return name;
    }

}
