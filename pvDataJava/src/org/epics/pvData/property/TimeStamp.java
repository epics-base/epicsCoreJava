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
    /**
     * Get the number of seconds since the January 1, 1970, 00:00:00 UTC
     * @return The number of seconds.
     */
    public long getSecondsPastEpoch();
    /**
     * Get the number of nanoseconds within the second.
     * @return The number.
     */
    public int getNanoSeconds();
    /**
     * Put the time.
     * @param secondsPastEpoch The number of seconds since January 1, 1970, 00:00:00 UTC
     * @param nanoSeconds The number of nanoseconds within the second.
     */
    public void put(long secondsPastEpoch,int nanoSeconds);
    /**
     * Get the number of milliSeconds since the January 1, 1970, 00:00:00 UTC
     * @return The number.
     */
    public long getMilliSeconds();
    /**
     * Put the time in milliSeconds since January 1, 1970, 00:00:00 UTC
     * @param milliSeconds The number of milliSeconds.
     */
    public void put(long milliSeconds);
}
