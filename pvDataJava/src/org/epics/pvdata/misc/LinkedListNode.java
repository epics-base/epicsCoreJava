/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE that is included with the distribution
 */
package org.epics.pvdata.misc;

/**
 * Interface for a LinkedListNode.
 * Code that uses LinkedList must supply a node by calling LinkedListCreate.crateNode().
 * @author mrk
 *
 */
public interface LinkedListNode<T> {
    /**
     * Get the object passed to LinkedListCreate.createNode();
     * 
     * @return the object
     */
    T getObject();
    /**
     * Is this node on a list?
     *
     * @return (false,true) if node (is not,is) on a list
     */
    boolean isOnList();
}
