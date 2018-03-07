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
    public enum Type {READ_CONNECTION, WRITE_CONNECTION, VALUE, READ_EXCEPTION, WRITE_EXCEPTION, WRITE_SUCCEEDED, WRITE_FAILED};
    
    private final List<Type> types;
    private final Exception exception;

    private PVEvent(Exception ex, List<Type> types) {
        this.types = Collections.unmodifiableList(types);
        this.exception = ex;
    }

    private PVEvent(Exception ex, Type type) {
        this.types = Collections.unmodifiableList(Arrays.asList(type));
        this.exception = ex;
    }

    private PVEvent(Exception ex, Type type1, Type type2) {
        this.types = Collections.unmodifiableList(Arrays.asList(type1, type2));
        this.exception = ex;
    }

    public List<Type> getType() {
        return types;
    }

    public Exception getException() {
        return exception;
    }
    
    public PVEvent addEvent(PVEvent event) {
        List<Type> newTypes = new ArrayList<>(getType());
        for (Type type : event.getType()) {
            newTypes.remove(type);
            newTypes.add(type);
        }
        Exception newException = getException();
        if (event.getException() != null) {
            newException = event.getException();
        }
        return new PVEvent(exception, newTypes);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(this.types);
        hash = 29 * hash + Objects.hashCode(this.exception);
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
    private static final PVEvent CONNECTION_EVENT = new PVEvent(null, Type.READ_CONNECTION);
    private static final PVEvent VALUE_EVENT = new PVEvent(null, Type.VALUE);
    private static final PVEvent CONNECTION_VALUE_EVENT = new PVEvent(null, Type.READ_CONNECTION, Type.VALUE);
    
    public static PVEvent connectionEvent() {
        return CONNECTION_EVENT;
    }
    
    public static PVEvent valueEvent() {
        return VALUE_EVENT;
    }
    
    public static PVEvent connectionValueEvent() {
        return CONNECTION_VALUE_EVENT;
    }
    
    public static PVEvent exceptionEvent(Exception ex) {
        return new PVEvent(ex, Type.READ_EXCEPTION);
    }
}
