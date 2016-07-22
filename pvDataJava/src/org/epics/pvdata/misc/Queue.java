/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.misc;

/**
 * A finite Queue.
 * @author mrk
 *
 */
public interface Queue<T> {
    
    /**
     * Set all elements free.
     */
    void clear();

    /**
     * Get the number of free queue elements.
     *
     * @return The number.
     */
    int getNumberFree();

    /**
     * Get the queue capacity.
     *
     * @return the capacity
     */
    int capacity();

    /**
     * Get the next free element.
     *
     * @return the next free element or null if no free elements
     */
    QueueElement<T> getFree();

    /**
     * Set the getFree element to used.
     *
     * @param queueElement the queueElement, which must be the
     * element returned by the oldest call to getFree that was not setUsed
     * @throws IllegalStateException if queueElement is not the element
     * returned by the oldest call to getFree that was not setUsed
     */
    void setUsed(QueueElement<T> queueElement);
    /**
     * Get the oldest used element.
     *
     * @return The next used element or null if no used elements
     */
    QueueElement<T> getUsed();

    /**
     * Release the getUsed structure.
     *
     * @param queueElement the queueElement, which must be the
     * element returned by the most recent call to getUsed
     * @throws IllegalStateException if queueElement is not the element
     * returned by the most recent call to getUsed
     */
    void releaseUsed(QueueElement<T> queueElement);
}
