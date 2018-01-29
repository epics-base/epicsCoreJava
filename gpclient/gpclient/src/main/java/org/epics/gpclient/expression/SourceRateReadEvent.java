/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient.expression;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author carcassi
 */
public final class SourceRateReadEvent {
    public enum Type {READ_CONNECTION, WRITE_CONNECTION, VALUE, READ_EXCEPTION, WRITE_EXCEPTION, WRITE_SUCCEEDED, WRITE_FAILED};
    
    private final Set<Type> types;
    private final Exception exception;

    public SourceRateReadEvent(Exception ex, Type type) {
        this.types = Collections.unmodifiableSet(EnumSet.of(type));
        this.exception = ex;
    }

    public SourceRateReadEvent(Exception ex, Type type1, Type type2) {
        this.types = Collections.unmodifiableSet(EnumSet.of(type1, type2));
        this.exception = ex;
    }

    public Set<Type> getType() {
        return types;
    }

    public Exception getException() {
        return exception;
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
        final SourceRateReadEvent other = (SourceRateReadEvent) obj;
        if (!Objects.equals(this.types, other.types)) {
            return false;
        }
        if (!Objects.equals(this.exception, other.exception)) {
            return false;
        }
        return true;
    }
    
}
