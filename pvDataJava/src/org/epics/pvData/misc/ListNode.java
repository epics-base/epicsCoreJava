/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;

/**
 * Interface for a ListNode.
 * Code that uses LinkedList must supply a node by calling LinkedListFactory.crateNode().
 * @author mrk
 *
 */
public interface ListNode {
    /**
     * Get the object passed to LinkedListFactory.createNode();
     * @return the object.
     */
    public Object getObject();
}
