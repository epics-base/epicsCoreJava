/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.datasource.ca;

import gov.aps.jca.Channel;
import gov.aps.jca.dbr.*;
import gov.aps.jca.event.MonitorEvent;

public class CAMessagePayload {

    private final DBR metadata;
    private final MonitorEvent event;

    public CAMessagePayload(DBR metadata, MonitorEvent event) {
        if (event != null) {
            // If we have a monitor event, it may be an "incomplete"
            // String event because of the RTYP support
            if (event.getDBR() instanceof DBR_String && !(event.getDBR() instanceof DBR_TIME_String)) {
                DBR_String originalValue = (DBR_String) event.getDBR();
                // Received only partial data. Filling in time and alarm
                DBR_TIME_String value = new DBR_TIME_String(originalValue.getStringValue());
                value.setSeverity(Severity.NO_ALARM);
                value.setStatus(Status.NO_ALARM);
                value.setTimeStamp(new TimeStamp());

                event = new MonitorEvent((Channel) event.getSource(), value, event.getStatus());
            }
        }
        this.metadata = metadata;
        this.event = event;
    }

    /**
     * The event returned by the monitor.
     *
     * @return the monitor event
     */
    public MonitorEvent getEvent() {
        return event;
    }

    /**
     * The data taken with a GET at connection time.
     *
     * @return the dbr type for the metadata
     */
    public DBR getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        DBR value = null;
        if (event != null) {
            value = event.getDBR();
        }
        return "Metadata " + metadata + " value " + value;
    }

}
