/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import org.epics.util.compat.legacy.lang.Objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
    /**
     * The type of the event.
     */
    public enum Type {
        /**
         * The read connection value has changed. For example, the network
         * channel was connected/disconnected, the read access rights were
         * changed, ...
         */
        READ_CONNECTION,

        /**
         * The write connection value has changed. For example, the network
         * channel was connected/disconnected, the write access rights were
         * changed, ...
         */
        WRITE_CONNECTION,

        /**
         * The value has changed. For example, a new value was posted for
         * this channel, the metadata associated with it changed, ...
         */
        VALUE,

        /**
         * An error was posted on this channel. For example, a timeout expired,
         * the channel was not found, the value did not match the type requested, .,..
         */
        EXCEPTION,

        /**
         * The write was successful. The exact meaning will depend on the
         * channel. For example, one channel may return successful if the value
         * was posted successfully, one may return successful only if the value
         * was processed correctly, ...
         */
        WRITE_SUCCEEDED,

        /**
         * The write was not successful. The exact meaning will depend on the
         * channel. For example, one channel may deem the write failed even
         * if the value was received correctly, but its processing caused
         * an error.
         */
        WRITE_FAILED};

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

    /**
     * Checks whether the type of this event contains the given type.
     *
     * @param type an event type
     * @return true if this event is of that type
     */
    public boolean isType(Type type) {
        return types.contains(type);
    }

    /**
     * All the types of this event. An event may be aggregated and can, therefore,
     * map to more than one type.
     *
     * @return the type list
     */
    public List<Type> getType() {
        return types;
    }

    /**
     * If the type is {@link Type#EXCEPTION}, it contains the exception. Null
     * otherwise.
     *
     * @return the error associated with the event
     */
    public Exception getException() {
        return exception;
    }

    /**
     * If the type is {@link Type#WRITE_FAILED}, it contains the error. Null
     * otherwise.
     *
     * @return the error associated with the event
     */
    public Exception getWriteError() {
        return writeError;
    }

    /**
     * Returns a new event that aggregates this event with the given event.
     *
     * @param event the event to aggregate to this
     * @return the new aggregated event
     */
    public PVEvent addEvent(PVEvent event) {
        List<Type> newTypes = new ArrayList<Type>(getType());
        for (Type type : event.getType()) {
            newTypes.remove(type);
            newTypes.add(type);
        }
        Exception newException = (event.getException() != null) ? event.getException() : getException();
        Exception newWriteError = (event.getWriteError() != null) ? event.getWriteError() : getWriteError();
        return new PVEvent(newException, newWriteError, newTypes);
    }

    PVEvent removeType(Type type) {
        List<Type> newTypes = new ArrayList<Type>(getType());
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
        StringBuilder sb = new StringBuilder();
        sb.append("{Type: ").append(types);
        if (exception != null) {
            sb.append(" - ex: ").append(exception.getMessage());
        }
        if (writeError != null) {
            sb.append(" - wrEx: ").append(writeError.getMessage());
        }
        sb.append("}");

        return sb.toString();
    }

    // Cache events that don't have an exception to save memory creation/collection
    private static final PVEvent READ_CONNECTION_EVENT = new PVEvent(Type.READ_CONNECTION);
    private static final PVEvent VALUE_EVENT = new PVEvent( Type.VALUE);
    private static final PVEvent READ_CONNECTION_VALUE_EVENT = new PVEvent(null, null, Arrays.asList(Type.READ_CONNECTION, Type.VALUE));
    private static final PVEvent WRITE_CONNECTION_EVENT = new PVEvent(Type.WRITE_CONNECTION);
    private static final PVEvent WRITE_SUCCEEDED_EVENT = new PVEvent(Type.WRITE_SUCCEEDED);

    /**
     * A read connection event.
     *
     * @return an event
     */
    public static PVEvent readConnectionEvent() {
        return READ_CONNECTION_EVENT;
    }

    /**
     * A value event.
     *
     * @return an event
     */
    public static PVEvent valueEvent() {
        return VALUE_EVENT;
    }

    /**
     * A read connection and value event.
     *
     * @return an event
     */
    public static PVEvent readConnectionValueEvent() {
        return READ_CONNECTION_VALUE_EVENT;
    }

    /**
     * An exception event.
     *
     * @param ex the exception to associate with the event
     * @return an event
     */
    public static PVEvent exceptionEvent(Exception ex) {
        return new PVEvent(ex, null, Collections.singletonList(Type.EXCEPTION));
    }

    /**
     * A write connection event.
     *
     * @return an event
     */
    public static PVEvent writeConnectionEvent() {
        return WRITE_CONNECTION_EVENT;
    }

    /**
     * A write succeeded event.
     *
     * @return an event
     */
    public static PVEvent writeSucceededEvent() {
        return WRITE_SUCCEEDED_EVENT;
    }

    /**
     * A write failed event.
     *
     * @param writeError the error associated with the write
     * @return an event
     */
    public static PVEvent writeFailedEvent(Exception writeError) {
        return new PVEvent(null, writeError, Collections.singletonList(Type.WRITE_FAILED));
    }
}
