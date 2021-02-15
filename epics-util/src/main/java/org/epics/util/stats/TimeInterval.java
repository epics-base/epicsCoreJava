/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.util.stats;

import org.joda.time.Duration;
import org.joda.time.Instant;

/**
 * A period of time that spans two instants (included) at the nanosecond
 * precision.
 *
 * @author carcassi
 */
public class TimeInterval {

    private final Instant start;
    private final Instant end;

    private TimeInterval(Instant start, Instant end) {
        this.start = start;
        this.end = end;
    }

    /**
     * True if the given time stamp is inside the interval.
     *
     * @param instant a time stamp
     * @return true if inside the interval
     */
    public boolean contains(Instant instant) {
        return (start == null || start.compareTo(instant) <= 0) && (end == null || end.compareTo(instant) >= 0);
    }

    /**
     * Returns the interval between the given timestamps.
     *
     * @param start the beginning of the interval
     * @param end   the end of the interval
     * @return a new interval
     */
    public static TimeInterval between(Instant start, Instant end) {
        return new TimeInterval(start, end);
    }

    /**
     * Returns a new interval shifted backward in time by the given duration.
     *
     * @param duration a time duration
     * @return the new shifted interval
     */
    public TimeInterval minus(Duration duration) {
        return between(start.minus(duration), end.minus(duration));
    }

    /**
     * Initial value of the interval.
     *
     * @return the initial instant
     */
    public Instant getStart() {
        return start;
    }

    /**
     * Final value of the interval.
     *
     * @return the final instant
     */
    public Instant getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return start + " - " + end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimeInterval) {
            TimeInterval other = (TimeInterval) obj;
            boolean startEqual = (getStart() == other.getStart()) || (getStart() != null && getStart().equals(other.getStart()));
            boolean endEqual = (getEnd() == other.getEnd()) || (getEnd() != null && getEnd().equals(other.getEnd()));
            return startEqual && endEqual;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + (this.start != null ? this.start.hashCode() : 0);
        hash = 29 * hash + (this.end != null ? this.end.hashCode() : 0);
        return hash;
    }

}
