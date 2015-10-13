/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.misc;

/**
 * Convert a LinkedList to a LinkedListNode array.
 * LinkedListCreate provides an efficient implementation.
 * @author mrk
 *
 */
public interface LinkedListArray<T> {
    /**
     * Set the LinkNode array from linkedList.
     * If the current capacity of the array is less than the number of elements in the current array a new array
     * is allocated. The values returned by getNodes and getLength are determined by the argument passed to setNodes.
     * The linkedList is traversed by getHead and getNext.
     * 
     * @param linkedList the list
     */
    void setNodes(LinkedList<T> linkedList);

    /**
     * The LinkedListNode array as set by the last call to setNodes.
     * 
     * @return the LinkedListNode array. The first getLength() elements are not null and the remaining elements are all null.
     */
    LinkedListNode<T>[] getNodes();

    /**
     * The number of non-null elements in the array returned by getNodes.
     * 
     * @return the length
     */
    int getLength();

    /**
     * Clear the array.
     * The array is set to null.
     * 
     */
    void clear();
}
