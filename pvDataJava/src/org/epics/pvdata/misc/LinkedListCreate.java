/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.misc;
/**
 * Create LinkedList, LinkedListNode, and LinkedListArray.
 * Also factory that implements LinkedList, LinkedListNode, and LinkedListArray.
 * @author mrk
 *
 */
public class LinkedListCreate<T> {
    /**
     * Create a Linked List.
     * 
     * @return the interface
     */
    public LinkedList<T> create() {
        return new LinkedListImpl<T>();
    }
    /**
     * Create a Linked List Node.
     * 
     * @param object the object for the node.
     * @return the interface.
     */
    public LinkedListNode<T> createNode(T object) {
        return new LinkedListNodeImpl<T>(object);
    }
    /**
     * Create a Linked List Array.
     * 
     * @return the interface
     */
    public LinkedListArray<T> createArray() {
        return new LinkedListArrayImpl<T>();
    }
    

    private static class LinkedListImpl<T> implements LinkedList<T> {
        private LinkedListNodeImpl<T> head = new LinkedListNodeImpl<T>(true);
        private int length = 0;

        private LinkedListImpl() {}
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.LinkedList#getLength()
         */
        public int getLength() {
            return length;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#addHead(org.epics.ioc.util.ListNode)
         */
        public void addHead(LinkedListNode<T> temp) {
            LinkedListNodeImpl<T> node = (LinkedListNodeImpl<T>)temp;
            if(node.before!=null || node.after!=null) {
                throw new IllegalStateException("already on list");
            }
            node.after = head.after;
            node.before = head;
            head.after.before = node;
            head.after = node;
            ++length;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#addTail(org.epics.ioc.util.ListNode)
         */
        public void addTail(LinkedListNode<T> temp) {
            LinkedListNodeImpl<T> node = (LinkedListNodeImpl<T>)temp;
            if(node.before!=null || node.after!=null) {
                throw new IllegalStateException("already on list");
            }
            node.before = head.before;
            node.after = head;
            head.before.after = node;
            head.before = node;
            ++length;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#getHead()
         */
        public LinkedListNode<T> getHead() {
            if(head.after==head) return null;
            return head.after;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#getNext(org.epics.ioc.util.ListNode)
         */
        public LinkedListNode<T> getNext(LinkedListNode<T> temp) {
            LinkedListNodeImpl<T> node = (LinkedListNodeImpl<T>)temp;
            if(node.after==head) return null;
            return node.after;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#getPrev(org.epics.ioc.util.ListNode)
         */
        public LinkedListNode<T> getPrev(LinkedListNode<T> temp) {
            LinkedListNodeImpl<T> node = (LinkedListNodeImpl<T>)temp;
            if(node.before==head) return null;
            return node.before;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#getTail()
         */
        public LinkedListNode<T> getTail() {
            if(head.after==head) return null;
            return head.before;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#insertAfter(org.epics.ioc.util.ListNode, org.epics.ioc.util.ListNode)
         */
        public void insertAfter(LinkedListNode<T> node, LinkedListNode<T> addNode) {
            LinkedListNodeImpl<T> existingNode = (LinkedListNodeImpl<T>)node;
            LinkedListNodeImpl<T> newNode = (LinkedListNodeImpl<T>)addNode;
            if(existingNode.after==null || existingNode.before==null) {
                throw new IllegalStateException("listNode is not  on list");
            }
            if(newNode.before!=null || newNode.after!=null) {
                throw new IllegalStateException("addNode is already on list");
            }
            newNode.after = existingNode.after;
            newNode.before = existingNode;
            existingNode.after.before = newNode;
            existingNode.after = newNode;
            length++;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.LinkedList#insertBefore(org.epics.pvdata.misc.LinkedListNode, org.epics.pvdata.misc.LinkedListNode)
         */
        public void insertBefore(LinkedListNode<T> node, LinkedListNode<T> addNode) {
            LinkedListNodeImpl<T> existingNode = (LinkedListNodeImpl<T>)node;
            LinkedListNodeImpl<T> newNode = (LinkedListNodeImpl<T>)addNode;
            if(existingNode.after==null || existingNode.before==null) {
                throw new IllegalStateException("listNode is not  on list");
            }
            if(newNode.before!=null || newNode.after!=null) {
                throw new IllegalStateException("addNode is already on list");
            }
            newNode.after = existingNode;
            newNode.before = existingNode.before;
            existingNode.before.after = newNode;
            existingNode.before = newNode;
            length++;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#remove(org.epics.ioc.util.ListNode)
         */
        public void remove(LinkedListNode<T> temp) {
            LinkedListNodeImpl<T> node = (LinkedListNodeImpl<T>)temp;
            if(node.before==null || node.after==null) {
                throw new IllegalStateException("not on list");
            }
            LinkedListNodeImpl<T> prev = node.before;
            LinkedListNodeImpl<T> next = node.after;
            node.after = node.before = null;
            prev.after = next;
            next.before = prev;
            length--;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#removeHead()
         */
        public LinkedListNode<T> removeHead() {
            if(head.after == head) return null;
            LinkedListNodeImpl<T> node = head.after;
            remove(head.after);
            return node;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#removeTail()
         */
        public LinkedListNode<T> removeTail() {
            if(head.after == head) return null;
            LinkedListNodeImpl<T> node = head.before;
            remove(head.before);
            return node;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.LinkedList#remove(java.lang.Object)
         */
        public void remove(T object) {
            LinkedListNode<T> node = getHead();
            while(node!=null) {
                if(node.getObject()==object) {
                    remove(node);
                    return;
                }
                node = getNext(node);
            }
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.LinkedList#isEmpty()
         */
        public boolean isEmpty() {
            if(head.after==head) return true;
            return false;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.LinkedList#contains(java.lang.Object)
         */
        public boolean contains(T object) {
            LinkedListNode<T> node = getHead();
            while(node!=null) {
                if(node.getObject()==object) {
                    return true;
                }
                node = getNext(node);
            }
            return false;
        }
    }
    
    private static class LinkedListNodeImpl<T> implements LinkedListNode<T>  {
        private T object = null;
        private LinkedListNodeImpl<T> before = null;
        private LinkedListNodeImpl<T> after = null;
        
        public LinkedListNodeImpl(T object) {
            this.object = object;
        }
        private LinkedListNodeImpl(boolean isHead) {
            before = after = this;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.util.ListNode#getObject()
         */
        public T getObject() {
            return object;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.LinkedListNode#isOnList()
         */
        public boolean isOnList() {
            if(before==null && after==null) return false;
            return true;
        }
    }
    
    private static class LinkedListArrayImpl<T> implements LinkedListArray<T> {
        private LinkedListArrayImpl() {}
        
        private LinkedListNode<T>[] listNodes = null;
        private int length = 0;
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.LinkedListArray#getLength()
         */
        public int getLength() {
            return length;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.LinkedListArray#getNodes()
         */
        public LinkedListNode<T>[] getNodes() {
            return (LinkedListNode<T>[])listNodes;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.LinkedListArray#setNodes(org.epics.pvdata.misc.LinkedList)
         */
        public void setNodes(LinkedList<T> linkedList) {
            int lengthNow = linkedList.getLength();
            if(listNodes==null || lengthNow>listNodes.length) {
                listNodes = new LinkedListNode[lengthNow];
            }
            LinkedListNode<T> node = linkedList.getHead();
            int index = 0;
            while(node!=null) {
                listNodes[index++] = node;
                node = linkedList.getNext(node);
            }
            for(int i=index; i<listNodes.length; i++) listNodes[i] = null;
            length = lengthNow;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.LinkedListArray#clear()
         */
        public void clear() {
            length = 0;
            listNodes = null;
        }
    }
}
