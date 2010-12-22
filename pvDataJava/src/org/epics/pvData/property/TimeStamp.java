/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.property;

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
     * Adjust secs and nanoSeconds so that 0<=nanoSeconds<nanoSecPerSec
     */
    void normalize();
    /**
     * Get the number of seconds since the January 1, 1970, 00:00:00 UTC
     * @return The number of seconds.
     */
    long getSecondsPastEpoch();
    /**
     * Get the number of seconds since the January 1, 1990, 00:00:00 UTC
     * @return The number of seconds.
     */
    long getEpicsSecondsPastEpoch();
    /**
     * Get the number of nanoseconds within the second.
     * @return The number.
     */
    int getNanoSeconds();
    /**
     * Put the time.
     * @param secondsPastEpoch The number of seconds since January 1, 1970, 00:00:00 UTC
     * @param nanoSeconds The number of nanoseconds within the second.
     */
    void put(long secondsPastEpoch,int nanoSeconds);
    /**
     * Get the number of milliSeconds since the January 1, 1970, 00:00:00 UTC
     * @return The number.
     */
    long getMilliSeconds();
    /**
     * Put the time in milliSeconds since January 1, 1970, 00:00:00 UTC
     * @param milliSeconds The number of milliSeconds.
     */
    void put(long milliSeconds);
    /**
     * Get the current time.
     */
    void getCurrentTime();
    /**
     * Does this timeStamp have the same time as the other.
     * @param other The other timeStamp.
     * @return (false,true) if (not same, same)
     */
    boolean equals(TimeStamp other);
    /**
     * Is the time for this timeStamp earlier than for the other.
     * @param other The other timeStamp.
     * @return (false,true) if (not less, less))
     */
    boolean lt(TimeStamp other);
    /**
     * Is the time for this timeStamp earlier than or equal to the other.
     * @param other The other timeStamp.
     * @return (false,true) if (not less or equal, less or equal))
     */
    boolean le(TimeStamp other);
    /**
     * Add the number of seconds.
     * @param seconds The value.
     */
    void add(long seconds);
    /**
     * Add the number of seconds.
     * @param seconds The value.
     */
    void add(double seconds);
    /**
     * Get the difference, i.e. a-b
     * @param a The first value.
     * @param b The second value.
     * @return a-b
     */
    double diff(TimeStamp a,TimeStamp b);
}
