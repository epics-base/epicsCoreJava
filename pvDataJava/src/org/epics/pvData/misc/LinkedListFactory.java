/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;

/**
 * @author mrk
 *
 */
public class LinkedListFactory {
    
    public static LinkedList create() {
        return new LinkedListImpl();
    }
    
    public static ListNode createNode(Object object) {
        return new ListNodeImpl(object);
    }

    private static class LinkedListImpl implements LinkedList {
        
        private ListNodeImpl head = new ListNodeImpl(true);

        private LinkedListImpl() {}
        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#addHead(org.epics.ioc.util.ListNode)
         */
        public void addHead(ListNode temp) {
            ListNodeImpl node = (ListNodeImpl)temp;
            if(node.before!=null || node.after!=null) {
                throw new IllegalStateException("already on list");
            }
            node.after = head.after;
            node.before = head;
            head.after.before = node;
            head.after = node;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#addTail(org.epics.ioc.util.ListNode)
         */
        public void addTail(ListNode temp) {
            ListNodeImpl node = (ListNodeImpl)temp;
            if(node.before!=null || node.after!=null) {
                throw new IllegalStateException("already on list");
            }
            node.before = head.before;
            node.after = head;
            head.before.after = node;
            head.before = node;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#getHead()
         */
        public ListNode getHead() {
            if(head.after==head) return null;
            return head.after;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#getNext(org.epics.ioc.util.ListNode)
         */
        public ListNode getNext(ListNode temp) {
            ListNodeImpl node = (ListNodeImpl)temp;
            if(node.after==head) return null;
            return node.after;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#getPrev(org.epics.ioc.util.ListNode)
         */
        public ListNode getPrev(ListNode temp) {
            ListNodeImpl node = (ListNodeImpl)temp;
            if(node.before==head) return null;
            return node.before;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#getTail()
         */
        public ListNode getTail() {
            if(head.after==head) return null;
            return head.before;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#insertAfter(org.epics.ioc.util.ListNode, org.epics.ioc.util.ListNode)
         */
        public void insertAfter(ListNode node, ListNode addNode) {
            ListNodeImpl existingNode = (ListNodeImpl)node;
            ListNodeImpl newNode = (ListNodeImpl)addNode;
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
        }

        /* (non-Javadoc)
         * @see org.epics.pvData.misc.LinkedList#insertBefore(org.epics.pvData.misc.ListNode, org.epics.pvData.misc.ListNode)
         */
        public void insertBefore(ListNode node, ListNode addNode) {
            ListNodeImpl existingNode = (ListNodeImpl)node;
            ListNodeImpl newNode = (ListNodeImpl)addNode;
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
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#remove(org.epics.ioc.util.ListNode)
         */
        public void remove(ListNode temp) {
            ListNodeImpl node = (ListNodeImpl)temp;
            if(node.before==null || node.after==null) {
                throw new IllegalStateException("not on list");
            }
            ListNodeImpl prev = node.before;
            ListNodeImpl next = node.after;
            node.after = node.before = null;
            prev.after = next;
            next.before = prev;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#removeHead()
         */
        public ListNode removeHead() {
            if(head.after == head) return null;
            ListNodeImpl node = head.after;
            remove(head.after);
            return node;
        }

        /* (non-Javadoc)
         * @see org.epics.ioc.util.LinkedList#removeTail()
         */
        public ListNode removeTail() {
            if(head.after == head) return null;
            ListNodeImpl node = head.before;
            remove(head.before);
            return node;
        }

        /* (non-Javadoc)
         * @see org.epics.pvData.misc.LinkedList#isEmpty()
         */
        public boolean isEmpty() {
            if(head.after==head) return true;
            return false;
        }

        /* (non-Javadoc)
         * @see org.epics.pvData.misc.LinkedList#isOnList(org.epics.pvData.misc.ListNode)
         */
        public boolean isOnList(ListNode temp) {
            ListNodeImpl node = (ListNodeImpl)temp;
            if(node.before!=null || node.after!=null) return true;
            return false;
        }
    }
    
    private static class ListNodeImpl implements ListNode  {
        private Object object = null;
        private ListNodeImpl before = null;
        private ListNodeImpl after = null;
        
        public ListNodeImpl(Object object) {
            this.object = object;
        }
        private ListNodeImpl(boolean isHead) {
            before = after = this;
        }
        /* (non-Javadoc)
         * @see org.epics.ioc.util.ListNode#getObject()
         */
        public Object getObject() {
            return object;
        }
    }
}
