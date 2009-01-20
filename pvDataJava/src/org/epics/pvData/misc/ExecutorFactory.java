/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;

import java.util.ArrayList;
import java.util.List;
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
         * @see org.epics.pvData.misc.Executor#execute(java.lang.Runnable)
         */
        public void execute(Runnable command) {
            thread.add(command);
        }
    }
    
    static private ThreadCreate threadCreate = ThreadCreateFactory.getThreadCreate();
    
    static private class ThreadInstance implements RunnableReady {
        private List<Runnable> runList = new ArrayList<Runnable>();
        private ReentrantLock lock = new ReentrantLock();
        private Condition moreWork = lock.newCondition();
        private Thread thread = null;

        private ThreadInstance(String name,int priority) {
            thread = threadCreate.create(name, priority, this);
        } 
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.RunnableReady#run(org.epics.pvData.misc.ThreadReady)
         */
        public void run(ThreadReady threadReady) {
            boolean firstTime = true;
            try {
                while(true) {
                    Runnable runnable = null;
                    lock.lock();
                    try {
                        if(firstTime) {
                            firstTime = false;
                            threadReady.ready();
                        }
                        while(runList.isEmpty()) {
                            moreWork.await();
                        }
                        runnable = runList.remove(0);
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
        
        private void add(Runnable runnable) {
            lock.lock();
            try {
                if(runList.contains(runnable)) return;
                boolean isEmpty = runList.isEmpty();
                runList.add(runnable);
                if(isEmpty) moreWork.signal();
                return;
            } finally {
                lock.unlock();
            }
        }
    }
}
