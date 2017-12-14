/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype;

/**
 * Immutable Alarm implementation.
 *
 * @author carcassi
 */
class IAlarm extends Alarm {

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
