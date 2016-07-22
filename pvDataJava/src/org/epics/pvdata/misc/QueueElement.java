/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.misc;

/**
 * Interface for a QueueElement.
 * Code that uses Queue must supply a node by calling QueueCreate.crateElement().
 * @author mrk
 *
 */
public interface QueueElement<T> {
    /**
     * Get the object passed to QueueCreate.createElement().
     *
     * @return the object
     */
    public T getObject();
}
