/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype;

import org.joda.time.Instant;

import org.epics.util.compat.legacy.lang.Objects;

/**
 * Time information.
 *
 * @author carcassi
 */
public abstract class Time {

    /**
     * The timestamp of the value, typically indicating when it was
     * generated. If disconnected, it returns the
     * time at which the disconnection was detected.
     *
     * @return the timestamp; not null
     */
    public abstract Instant getTimestamp();

    /**
     * Returns a user defined tag, that can be used to store extra
     * time information, such as beam shot.
     *
     * @return the user tag
     */
    public abstract Integer getUserTag();

    /**
     * Returns a data source specific flag to indicate whether the time
     * information should be trusted. Typical cases are when records
     * were not processes and the timestamp has a zero time.
     *
     * @return true if the time information is valid
     */
    public abstract boolean isValid();

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

	if (obj instanceof Time) {
            Time other = (Time) obj;

            return Objects.equals(getTimestamp(), other.getTimestamp()) &&
                Objects.equals(getUserTag(), other.getUserTag()) &&
                isValid() == other.isValid();
        }

        return false;
    }

    @Override
    public final int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(getTimestamp());
        hash = 59 * hash + Objects.hashCode(getUserTag());
        hash = 59 * hash + (isValid() ? 1 : 0);
        return hash;
    }

    @Override
    public final String toString() {
        if (getUserTag() == null) {
            return getTimestamp().toString();
        } else {
            return getTimestamp().toString() + "(" + getUserTag()+ ")";
        }
    }

    /**
     * Creates a new time.
     *
     * @param timestamp the timestamp
     * @param userTag the user tag
     * @param valid whether the time is valid
     * @return the new time
     */
    public static Time of(final Instant timestamp, final Integer userTag, final boolean valid) {
        return new ITime(timestamp, userTag, valid);
    }

    /**
     * New time, with no user tag and valid data.
     *
     * @param timestamp the timestamp
     * @return the new time
     */
    public static Time of(final Instant timestamp) {
        return Time.of(timestamp, null, true);
    }

    /**
     * New time with the current timestamp, no user tag and valid data.
     *
     * @return the new time
     */
    public static Time now() {
        return Time.of(Instant.now(), null, true);
    }

    /**
     * New time with the current timestamp, no user tag and invalid data.
     *
     * @return the new time
     */
    public static Time nowInvalid() {
        return Time.of(Instant.now(), null, false);
    }

    /**
     * Null and non-VType safe utility to extract time information.
     * <ul>
     * <li>If the value has a time, the associated time is returned.</li>
     * <li>If the value has no time, {@link #nowInvalid()} is returned.</li>
     * <li>If the value is null, {@link #nowInvalid()} is returned.</li>
     * </ul>
     *
     * @param value the value
     * @return the time information for the value
     */
    public static Time timeOf(Object value) {
        if (value instanceof TimeProvider) {
            return ((TimeProvider) value).getTime();
        }

        return nowInvalid();
    }

}
