/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.property;

/**
 * Interface TimeStamp.
 * @author mrk
 *
 */
public interface TimeStamp {
    static final long milliSecPerSec = 1000;
    static final long microSecPerSec = milliSecPerSec*milliSecPerSec;
    static final long nanoSecPerSec = milliSecPerSec*microSecPerSec;
    static final long  posixEpochAtEpicsEpoch = 631152000;
    /**
     * Adjust secs and nanoseconds so that 0&lt;=nanoseconds&lt;nanoSecPerSec
     */
    void normalize();

    /**
     * Get the number of seconds since the January 1, 1970, 00:00:00 UTC
     *
     * @return the number of seconds
     */
    long getSecondsPastEpoch();

    /**
     * Get the number of seconds since the January 1, 1990, 00:00:00 UTC
     *
     * @return the number of seconds
     */
    long getEpicsSecondsPastEpoch();

    /**
     * Get the number of nanoseconds within the second.
     *
     * @return the number
     */
    int getNanoseconds();

    /**
     * Get the userTag.
     *
     * @return the userTag
     */
    int getUserTag();

    /**
     * set the userTag.
     *
     * @param userTag the value for the userTag
     */
    void setUserTag(int userTag);

    /**
     * Put the time.
     *
     * @param secondsPastEpoch the number of seconds since January 1, 1970, 00:00:00 UTC
     * @param nanoseconds the number of nanoseconds within the second
     */
    void put(long secondsPastEpoch,int nanoseconds);

    /**
     * Get the number of milliSeconds since the January 1, 1970, 00:00:00 UTC
     *
     * @return the number
     */
    long getMilliSeconds();

    /**
     * Put the time in milliSeconds since January 1, 1970, 00:00:00 UTC
     *
     * @param milliSeconds the number of milliSeconds
     */
    void put(long milliSeconds);

    /**
     * Get the current time.
     */
    void getCurrentTime();

    /**
     * Does this timeStamp have the same time as the other?
     *
     * @param other the other timeStamp
     * @return (false,true) if (not same, same)
     */
    boolean equals(TimeStamp other);

    /**
     * Is the time for this timeStamp earlier than for the other?
     *
     * @param other the other timeStamp
     * @return (false,true) if (not less, less)
     */
    boolean lt(TimeStamp other);

    /**
     * Is the time for this timeStamp earlier than or equal to the other.
     *
     * @param other the other timeStamp
     * @return (false,true) if (not less or equal, less or equal))
     */
    boolean le(TimeStamp other);

    /**
     * Add the number of seconds.
     *
     * @param seconds the number of seconds to add
     */
    void add(long seconds);

    /**
     * Add the number of seconds.
     *
     * @param seconds the number of seconds to add
     */
    void add(double seconds);

    /**
     * Get the difference, i.e. a-b
     *
     * @param a the first value
     * @param b the second value
     * @return a-b
     */
    double diff(TimeStamp a, TimeStamp b);
}
