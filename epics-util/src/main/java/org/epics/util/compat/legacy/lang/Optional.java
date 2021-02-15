package org.epics.util.compat.legacy.lang;

import java.util.NoSuchElementException;

/**
 * Implementation of Java Optional class
 *
 * @param <T> the type for the optional object
 * @author George McIntyre. 15-Feb-2021, SLAC
 */
public final class Optional<T> {
    private static final Optional<?> EMPTY = new Optional<Object>();

    private final T value;

    private Optional() {
        this.value = null;
    }

    public static <T> Optional<T> empty() {
        @SuppressWarnings("unchecked")
        Optional<T> t = (Optional<T>) EMPTY;
        return t;
    }

    private Optional(T value) {
        this.value = Objects.requireNonNull(value, "Null object passed to nonNullable Optional");
    }

    public static <T> Optional<T> of(T value) {
        return new Optional<T>(value);
    }

    public static <T> Optional<T> ofNullable(T value) {
        //noinspection unchecked
        return value == null ? (Optional<T>) empty() : of(value);
    }

    public T get() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public T orElse(T other) {
        return value != null ? value : other;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Optional)) {
            return false;
        }

        Optional<?> other = (Optional<?>) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public String toString() {
        return value != null
                ? String.format("Optional[%s]", value)
                : "Optional.empty";
    }
}
