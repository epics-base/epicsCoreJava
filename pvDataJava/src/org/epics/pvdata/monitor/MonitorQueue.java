/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.monitor;


/**
 * A queue for monitors.
 * None of the methods are synchronized. The caller must perform synchronization.
 * @author mrk
 *
 */
public interface MonitorQueue {
    /**
     * Set all elements free.
     */
    void clear();
    /**
     * Get the number of free queue elements.
     * @return The number.
     */
    int getNumberFree();
    /**
     * Get the queue capacity.
     * @return The capacity.
     */
    int capacity();
    /**
     * Get the next free element.
     * @return The next free element or null if no free elements.
     */
    MonitorElement getFree();
    /**
     * Set the getFree element to used;
     * @param monitorElement The monitorElement, which must be the
     * element returned by the oldest call to getFree that was not setUsed.
     * @throws IllegalStateException if monitorElement is not the element
     * returned by the oldest call to getFree that was not setUsed.
     */
    void setUsed(MonitorElement monitorElement);
    /**
     * Get the oldest used element.
     * @return The next used element or null if no used elements.
     */
    MonitorElement getUsed();
    /**
     * Release the getUsed structure.
     * @param monitorElement The monitorElement, which must be the
     * element returned by the most recent call to getUsed.
     * @throws IllegalStateException if monitorElement is not the element
     * returned by the most recent call to getUsed.
     */
    void releaseUsed(MonitorElement monitorElement);
}
