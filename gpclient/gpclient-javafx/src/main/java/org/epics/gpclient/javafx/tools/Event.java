/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.javafx.tools;

import org.joda.time.Instant;
import org.epics.gpclient.PVEvent;


/**
 *
 * @author carcassi
 */
public class Event {
    private final Instant timestamp;
    private final PVEvent event;
    private final boolean connected;
    private final boolean writeConnected;
    private final Object value;

    public Event(Instant timestamp, PVEvent event, boolean connected, boolean writeConnected, Object value) {
        this.timestamp = timestamp;
        this.event = event;
        this.connected = connected;
        this.writeConnected = writeConnected;
        this.value = value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public PVEvent getEvent() {
        return event;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isWriteConnected() {
        return writeConnected;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return event.toString();
    }

}
