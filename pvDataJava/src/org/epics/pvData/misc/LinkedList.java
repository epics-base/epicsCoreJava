/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;

/**
 * Linked List with user supplied storage for node.
 * The user must create nodes by calling LinkedListCreate.createNode.
 * A node can be put on any LinkedList but only on one list at a time.
 * It is the users responsibility to synchronize access to the list.
 * @author mrk
 *
 */
public interface LinkedList<T> {
    int getLength();
    /**
     * Add a node to the end of the list.
     * @param listNode The node to add to the list.
     * @throws IllegalStateException if the node is already on the list.
     */
    void addTail(LinkedListNode<T> listNode);
    /**
     * Add a node to the beginning of the list.
     * @param listNode The node to add to the list.
     * @throws IllegalStateException if the node is already on the list.
     */
    void addHead(LinkedListNode<T> listNode);
    /**
     * Add a node after a node that is already on the list.
     * @param listNode The node that is on the list.
     * @param addNode The node to add.
     * @throws IllegalStatexception if listNode is not on list or addNode is already on list.
     */
    void insertAfter(LinkedListNode<T> listNode,LinkedListNode<T> addNode);
    /**
     * Add a node before a node that is already on the list.
     * @param listNode The node that is on the list.
     * @param addNode The node to add.
     * @throws IllegalStatexception if listNode is not on list or addNode is already on list.
     */
    void insertBefore(LinkedListNode<T> listNode,LinkedListNode<T> addNode);   
    /**
     * Remove and return the LinkedListNode that is at the end of the list.
     * @return The listNode or null if the list is empty.
     */
    LinkedListNode<T> removeTail();
    /**
     * Remove and return the LinkedListNode that is at the head of the list.
     * @return The listNode or null if the list is empty.
     */
    LinkedListNode<T> removeHead();
    /**
     * Remove the listNode from the list.
     * @param listNode The node to remove.
     */
    void remove(LinkedListNode<T> listNode);
    /**
     * Remove the object from the list.
     * @param object The object to remove.
     */
    void remove(T object);
    /**
     * Get the node at the head of the list.
     * The node is not removed from the list.
     * @return The node or null if the list is empty.
     */
    LinkedListNode<T> getHead();
    /**
     * Get the node at the end of the list.
     * The node is not removed from the list.
     * @return The node or null if the list is empty.
     */
    LinkedListNode<T> getTail();
    /**
     * Get the node after listNode.
     * @param listNode The current node.
     * @return The node or null if listNode is the last node on the list.
     * @throws IllegalStateException if listNode is not on the list.
     */
    LinkedListNode<T> getNext(LinkedListNode<T> listNode);
    /**
     * Get the node before listNode.
     * @param listNode The current node.
     * @return The node or null if listNode is the first node on the list.
     * @throws IllegalStateException if listNode is not on the list.
     */
    LinkedListNode<T> getPrev(LinkedListNode<T> listNode);
    /**
     * Is the list empty?
     * @return (false,true) if the list (is not,is) empty.
     */
    boolean isEmpty();
    /**
     * Is the object on the list?
     * @param object The object.
     * @return (false,true) if the node (is not, is on the list).
     */
    boolean contains(T object);
}
