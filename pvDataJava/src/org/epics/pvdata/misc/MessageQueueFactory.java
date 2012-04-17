/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.misc;

import org.epics.pvdata.pv.MessageType;

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
         * @see org.epics.pvdata.misc.MessageQueue#get()
         */
        public MessageNode get() {
            if(numFree==size) return null;
            MessageNode value = messageNodes[nextMessage++];
            if(nextMessage==size) nextMessage = 0;
            numFree++;
            return value;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.MessageQueue#put(java.lang.String, org.epics.pvdata.pv.MessageType, boolean)
         */
        public boolean put(String message, MessageType messageType,boolean replaceLast) {
            MessageNode messageNode = null;
            boolean ok = true;
            if(numFree==0) {
                int index = ((nextFree==0) ? size-1 : nextFree--) ;
                messageNode = messageNodes[index];
                numOverrun++;
                ok = false;
            } else {
                messageNode = messageNodes[nextFree];
                nextFree++;
                if(nextFree==size) nextFree = 0;
                numFree--;
            }
            messageNode.message = message;
            messageNode.messageType = messageType;
            return ok;
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.MessageQueue#isEmpty()
         */
        public boolean isEmpty() {
            return ((numFree==size) ? true : false);
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.MessageQueue#isFull()
         */
        public boolean isFull() {
            return ((numFree==0) ? true : false);
        }
        /* (non-Javadoc)
         * @see org.epics.pvdata.misc.MessageQueue#getClearOverrun()
         */
        public int getClearOverrun() {
            int value = numOverrun;
            numOverrun = 0;
            return value;
        }
    }
}
