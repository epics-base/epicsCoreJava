/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;

import java.util.concurrent.locks.ReentrantLock;

import org.epics.pvData.pv.MessageType;

/**
 * A factory that implements messageQueues.
 * @author mrk
 *
 */
public class MessageQueueFactory {
    /**
     * Create a messageQueue.
     * @param size The number of nodes. This can not be changed.
     * @return The interface for the messageQueue.
     */
    public static MessageQueue create(int size) {
        return new MessageQueueImpl(size);
    }
    
    private static class MessageQueueImpl implements MessageQueue {
        private ReentrantLock lock = new ReentrantLock();
        private MessageNode[] messageNodes;
        private int size;
        private int numFree;
        private int nextFree = 0;
        private int nextMessage = 0;
        private int numOverrun = 0;
        
        private MessageQueueImpl(int size) {
            messageNodes = new MessageNode[size];
            for(int i=0; i<size; i++) {
                messageNodes[i] = new MessageNode();
            }
            this.size = size;
            numFree = size;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.MessageQueue#lock()
         */
        public void lock() {
            lock.lock();
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.MessageQueue#unlock()
         */
        public void unlock() {
            lock.unlock();
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.MessageQueue#get()
         */
        public MessageNode get() {
            if(numFree==size) return null;
            MessageNode value = messageNodes[nextMessage++];
            if(nextMessage==size) nextMessage = 0;
            numFree++;
            return value;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.MessageQueue#put(java.lang.String, org.epics.pvData.pv.MessageType)
         */
        public boolean put(String message, MessageType messageType) {
            if(numFree==0) {
                numOverrun++;
                return false;
            }
            MessageNode messageNode = messageNodes[nextFree];
            nextFree++;
            if(nextFree==size) nextFree = 0;
            numFree--;
            messageNode.message = message;
            messageNode.messageType = messageType;
            return true;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.MessageQueue#isEmpty()
         */
        public boolean isEmpty() {
            return ((numFree==size) ? true : false);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.MessageQueue#isFull()
         */
        public boolean isFull() {
            return ((numFree==0) ? true : false);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.MessageQueue#replaceFirst(java.lang.String, org.epics.pvData.pv.MessageType)
         */
        public void replaceFirst(String message, MessageType messageType) {
            if(numFree!=0) {
                throw new IllegalStateException("Logic error. Must be called only if full");
            }
            MessageNode messageNode = messageNodes[nextMessage];
            numOverrun++;
            messageNode.message = message;
            messageNode.messageType = messageType;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.MessageQueue#replaceLast(java.lang.String, org.epics.pvData.pv.MessageType)
         */
        public void replaceLast(String message, MessageType messageType) {
            if(numFree!=0) {
                throw new IllegalStateException("Logic error. Must be called only if full");
            }
            int index = ((nextFree==0) ? size-1 : nextFree--) ;
            MessageNode messageNode = messageNodes[index];
            numOverrun++;
            messageNode.message = message;
            messageNode.messageType = messageType;
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.MessageQueue#getClearOverrun()
         */
        public int getClearOverrun() {
            int value = numOverrun;
            numOverrun = 0;
            return value;
        }
    }
}
