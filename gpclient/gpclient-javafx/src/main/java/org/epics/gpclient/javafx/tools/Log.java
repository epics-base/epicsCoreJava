/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient.javafx.tools;

import java.io.PrintStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVReader;
import org.epics.gpclient.PVReaderListener;

/**
 *
 * @author carcassi
 */
public class Log {
    
    private final Runnable callback;
    private final List<Event> events = Collections.synchronizedList(new ArrayList<Event>());

    public Log(Runnable callback) {
        this.callback = callback;
    }
    
    public <T> PVReaderListener<T> createReadListener() {
        return new org.epics.gpclient.PVReaderListener<T>() {
            @Override
            public void pvChanged(PVEvent event, PVReader<T> pvReader) {
                events.add(new Event(Instant.now(), event, pvReader.isConnected(), pvReader.getValue()));
                callback.run();
            }
        };
    }
    
    public List<Event> getEvents() {
        return events;
    }
    
//    private TimestampFormat format = new TimestampFormat("ss.NNNNNNNNN");
    
    public void print(PrintStream out) {
        for (Event event : events) {
            Event readEvent = (Event) event;
            // TODO: format the timestamp
            out.append(readEvent.getTimestamp().toString())
                    .append(" R(");
            if (readEvent.getEvent().isType(PVEvent.Type.READ_CONNECTION)) {
                out.append("C");
            }
            if (readEvent.getEvent().isType(PVEvent.Type.VALUE)) {
                out.append("V");
            }
            if (readEvent.getEvent().isType(PVEvent.Type.READ_EXCEPTION)) {
                out.append("E");
            }
            out.append(")");
            if (readEvent.isConnected()) {
                out.append(" CONN ");
            } else {
                out.append(" DISC ");
            }
            out.append(Objects.toString(readEvent.getValue()));
            if (readEvent.getEvent().getException() != null) {
                out.append(" ").append(readEvent.getEvent().getException().getClass().getName())
                        .append(":").append(readEvent.getEvent().getException().getMessage());
            } else {
                out.append(" NoException");
            }
            out.append("\n");
        }
        out.flush();
    }
}
