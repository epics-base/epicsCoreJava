/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
     * Get the object passed to QueueCreate.createElement();
     * @return the object.
     */
    public T getObject();
}
