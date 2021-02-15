/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient.javafx.tools;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.epics.gpclient.PV;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVListener;
import org.epics.util.compat.legacy.lang.Objects;
import org.joda.time.Instant;

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

    public <R, W> PVListener<R, W> createReadListener() {
        return new org.epics.gpclient.PVListener<R, W>() {
            public void pvChanged(PVEvent event, PV<R, W> pv) {
                events.add(new Event(Instant.now(), event, pv.isConnected(), pv.isWriteConnected(), pv.getValue()));
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
            if (readEvent.getEvent().isType(PVEvent.Type.WRITE_CONNECTION)) {
                out.append("c");
            }
            if (readEvent.getEvent().isType(PVEvent.Type.VALUE)) {
                out.append("V");
            }
            if (readEvent.getEvent().isType(PVEvent.Type.EXCEPTION)) {
                out.append("E");
            }
            if (readEvent.getEvent().isType(PVEvent.Type.WRITE_SUCCEEDED)) {
                out.append("S");
            }
            if (readEvent.getEvent().isType(PVEvent.Type.WRITE_FAILED)) {
                out.append("F");
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
