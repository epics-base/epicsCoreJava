/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Create an IOCExecutor.
 * @author mrk
 *
 */
public class ExecutorFactory {
    /**
     * Create a new set of threads.
     * @param threadName The name for the set of threads.
     * @param priority The ScanPriority for the thread.
     * @return The IOCExecutor interface.
     */
    static public Executor create(String threadName, ThreadPriority priority) {
        return new ExecutorInstance(threadName,priority);
    }
    
    static private class ExecutorInstance implements Executor {
        private ThreadInstance thread;

        private ExecutorInstance(String threadName, ThreadPriority priority) {
            thread = new ThreadInstance(threadName,priority.getJavaPriority());
        }
        
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.Executor#createNode(java.lang.Runnable)
         */
        public ExecutorNode createNode(Runnable command) {
            return new ExecutorNodeImpl(command);
        }

        /* (non-Javadoc)
         * @see org.epics.pvData.misc.Executor#execute(org.epics.pvData.misc.ExecutorNode)
         */
        public void execute(ExecutorNode executorNode) {
            ExecutorNodeImpl impl = (ExecutorNodeImpl)executorNode;
            thread.add(impl.listNode);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.Executor#stop()
         */
        public void stop() {
            thread.stop();
        }
    }
    
    static private ThreadCreate threadCreate = ThreadCreateFactory.getThreadCreate();
    static private LinkedListCreate<ExecutorNodeImpl> linkedListCreate = new LinkedListCreate<ExecutorNodeImpl>();
    
    static private class ThreadInstance implements RunnableReady {
        
        private LinkedList<ExecutorNodeImpl> runList = linkedListCreate.create();
        private ReentrantLock lock = new ReentrantLock();
        private Condition moreWork = lock.newCondition();
        private boolean alive = true;

        private ThreadInstance(String name,int priority) {
            threadCreate.create(name, priority, this);
        } 
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.RunnableReady#run(org.epics.pvData.misc.ThreadReady)
         */
        public void run(ThreadReady threadReady) {
            boolean firstTime = true;
            try {
                while(alive) {
                    Runnable runnable = null;
                    lock.lock();
                    try {
                        if(firstTime) {
                            firstTime = false;
                            threadReady.ready();
                        }
                        while(alive && runList.isEmpty()) {
                            moreWork.await();
                        }
                        if(!runList.isEmpty()) {
                            LinkedListNode<ExecutorNodeImpl> listNode = runList.removeHead();
                            ExecutorNodeImpl impl = listNode.getObject();
                            runnable = impl.command;
                        }
                    }finally {
                        lock.unlock();
                    }
                    if(runnable!=null) {
                        runnable.run();
                    }
                }
            } catch(InterruptedException e) {
                
            }
        }
        
        private void add(LinkedListNode<ExecutorNodeImpl> listNode) {
            lock.lock();
            try {
                if(!alive || listNode.isOnList()) return;
                boolean isEmpty = runList.isEmpty();
                runList.addTail(listNode);
                if(isEmpty) moreWork.signal();
                return;
            } finally {
                lock.unlock();
            }
        }
        
        private void stop() {
            lock.lock();
            try {
                alive = false;
                moreWork.signal();
                return;
            } finally {
                lock.unlock();
            }
        }
    }
    
    private static class ExecutorNodeImpl implements ExecutorNode {
        private LinkedListNode<ExecutorNodeImpl> listNode;
        private Runnable command;
        
        private ExecutorNodeImpl(Runnable command) {
            this.command = command;
            listNode = linkedListCreate.createNode(this);
        }
    }
        
}
