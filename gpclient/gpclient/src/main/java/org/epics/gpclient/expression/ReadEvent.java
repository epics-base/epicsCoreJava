/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient.expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author carcassi
 */
public final class ReadEvent {
    public enum Type {READ_CONNECTION, WRITE_CONNECTION, VALUE, READ_EXCEPTION, WRITE_EXCEPTION, WRITE_SUCCEEDED, WRITE_FAILED};
    
    private final List<Type> types;
    private final Exception exception;

    private ReadEvent(Exception ex, List<Type> types) {
        this.types = Collections.unmodifiableList(types);
        this.exception = ex;
    }

    public ReadEvent(Exception ex, Type type) {
        this.types = Collections.unmodifiableList(Arrays.asList(type));
        this.exception = ex;
    }

    public ReadEvent(Exception ex, Type type1, Type type2) {
        this.types = Collections.unmodifiableList(Arrays.asList(type1, type2));
        this.exception = ex;
    }

    public List<Type> getType() {
        return types;
    }

    public Exception getException() {
        return exception;
    }
    
    public ReadEvent addEvent(ReadEvent event) {
        List<Type> newTypes = new ArrayList<>(getType());
        for (Type type : event.getType()) {
            newTypes.remove(type);
            newTypes.add(type);
        }
        Exception newException = getException();
        if (event.getException() != null) {
            newException = event.getException();
        }
        return new ReadEvent(exception, newTypes);
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
        final ReadEvent other = (ReadEvent) obj;
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
    
    
}
