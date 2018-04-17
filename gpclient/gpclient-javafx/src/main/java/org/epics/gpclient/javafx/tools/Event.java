/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.javafx.tools;

import java.time.Instant;
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
