/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
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
     * @return the object.
     */
    T getObject();
    /**
     * Is this node on a list?
     * @return (false,true) if node (is not,is) on a list.
     */
    boolean isOnList();
}
