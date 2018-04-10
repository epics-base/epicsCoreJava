/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A notification event for a pv. This may represent an aggregated event of multiple events
 * coming at a faster rate, of the same channel or of multiple channels.
 * <p>
 * Implementation notes. This class is used for both source rate events and
 * desired rate events. The class allows to combine events together into aggregated
 * events.
 *
 * @author carcassi
 */
public final class PVEvent {
    public enum Type {READ_CONNECTION, WRITE_CONNECTION, VALUE, EXCEPTION, WRITE_SUCCEEDED, WRITE_FAILED};
    
    private final List<Type> types;
    private final Exception exception;
    private final Exception writeError;

    private PVEvent(Exception ex, Exception writeError, List<Type> types) {
        this.types = Collections.unmodifiableList(types);
        this.exception = ex;
        this.writeError = writeError;
    }

    private PVEvent(Type type) {
        this(null, null, Collections.singletonList(type));
    }
    
    public boolean isType(Type type) {
        return types.contains(type);
    }

    public List<Type> getType() {
        return types;
    }

    public Exception getException() {
        return exception;
    }

    public Exception getWriteError() {
        return writeError;
    }
    
    public PVEvent addEvent(PVEvent event) {
        List<Type> newTypes = new ArrayList<>(getType());
        for (Type type : event.getType()) {
            newTypes.remove(type);
            newTypes.add(type);
        }
        Exception newException = (event.getException() != null) ? event.getException() : getException();
        Exception newWriteError = (event.getWriteError() != null) ? event.getWriteError() : getWriteError();
        return new PVEvent(newException, newWriteError, newTypes);
    }
    
    PVEvent removeType(Type type) {
        List<Type> newTypes = new ArrayList<>(getType());
        newTypes.remove(type);
        Exception newException = (type == Type.EXCEPTION) ? null : getException();
        Exception newWriteError = (type == Type.WRITE_FAILED) ? null : getWriteError();
        return new PVEvent(newException, newWriteError, newTypes);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.types);
        hash = 29 * hash + Objects.hashCode(this.exception);
        hash = 29 * hash + Objects.hashCode(this.writeError);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PVEvent other = (PVEvent) obj;
        if (!Objects.equals(this.types, other.types)) {
            return false;
        }
        if (!Objects.equals(this.exception, other.exception)) {
            return false;
        }
        if (!Objects.equals(this.writeError, other.writeError)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        if (exception != null) {
            return "{Type: " + types + " - ex: " + exception.getMessage() + "}";
        } else {
            return "{Type: " + types + "}";
        }
    }
    
    // Cache events that don't have an exception to save memory creation/collection
    private static final PVEvent READ_CONNECTION_EVENT = new PVEvent(Type.READ_CONNECTION);
    private static final PVEvent VALUE_EVENT = new PVEvent( Type.VALUE);
    private static final PVEvent READ_CONNECTION_VALUE_EVENT = new PVEvent(null, null, Arrays.asList(Type.READ_CONNECTION, Type.VALUE));
    private static final PVEvent WRITE_CONNECTION_EVENT = new PVEvent(Type.WRITE_CONNECTION);
    private static final PVEvent WRITE_SUCCEEDED_EVENT = new PVEvent(Type.WRITE_SUCCEEDED);
    
    public static PVEvent readConnectionEvent() {
        return READ_CONNECTION_EVENT;
    }
    
    public static PVEvent valueEvent() {
        return VALUE_EVENT;
    }
    
    public static PVEvent readConnectionValueEvent() {
        return READ_CONNECTION_VALUE_EVENT;
    }
    
    public static PVEvent exceptionEvent(Exception ex) {
        return new PVEvent(ex, null, Collections.singletonList(Type.EXCEPTION));
    }
    
    public static PVEvent writeConnectionEvent() {
        return WRITE_CONNECTION_EVENT;
    }
    
    public static PVEvent writeSucceededEvent() {
        return WRITE_SUCCEEDED_EVENT;
    }
    
    public static PVEvent writeFailedEvent(Exception writeError) {
        return new PVEvent(null, writeError, Collections.singletonList(Type.WRITE_FAILED));
    }
}
