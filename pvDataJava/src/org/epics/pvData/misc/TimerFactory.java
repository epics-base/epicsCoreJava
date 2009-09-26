/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;

import java.util.Comparator;
import java.util.PriorityQueue;

import org.epics.pvData.misc.Timer.TimerCallback;
import org.epics.pvData.misc.Timer.TimerNode;

/**
 * Factory that creates a Timer.
 * This implementation of a Timer provides the following features not provided by java.util.Timer
 * 1) Timer thread has a priority and 2) A TimerNode can be reused.
 * @author mrk
 *
 */
public class TimerFactory {
    /**
     * Create and return a Timer.
     * @param threadName The thread name for the timer thread.
     * @param priority The priority for the timer thread.
     * @return The new timer.
     */
    static public Timer create(String threadName, ThreadPriority priority) {
        return new TimerInstance(threadName,priority);
    }
    /**
     * Create a node that can be passed to the schedule methods..
     * @param timerCallback The interface implemented by the user.
     * @return Interface.
     */
    static public TimerNode createNode(TimerCallback timerCallback) {
        return new TimerNodeImpl(timerCallback);
    }
    
    static private final ThreadCreate threadCreate = ThreadCreateFactory.getThreadCreate();

    static private class TimerInstance implements Timer, Comparator<TimerNodeImpl> {
        private PriorityQueue<TimerNodeImpl> queue = new PriorityQueue<TimerNodeImpl>(16,this);
        private ThreadInstance thread;
        private volatile boolean isStopped = false;

        private TimerInstance(String threadName, ThreadPriority priority) {
            thread = new ThreadInstance(threadName,priority.getJavaPriority());
        }
        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(TimerNodeImpl arg0, TimerNodeImpl arg1) {
            long a = arg0.timeToRun;
            long b = arg1.timeToRun;
            return (a < b)? -1 : ((a == b)? 0 : 1);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.Timer#scheduleAfterDelay(org.epics.pvData.misc.Timer.TimerNode, double)
         */
        public void scheduleAfterDelay(TimerNode timerNode, double delay) {
            schedulePeriodic(timerNode,delay,-.1);
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.Timer#schedulePeriodic(org.epics.pvData.misc.Timer.TimerNode, double, double)
         */
        public void schedulePeriodic(TimerNode timerNode, double delayDouble,double periodDouble) {
            long delay = (long)(delayDouble*1000.0);
            long period = (long)(periodDouble*1000.0);
            TimerNodeImpl timerNodeImpl = (TimerNodeImpl)timerNode;
            if(timerNodeImpl.isQueued) {
                throw new IllegalStateException("already scheduled");
            }
            if(isStopped) {
                timerNodeImpl.timerCallback.timerStopped();
                return;
            }
            timerNodeImpl.isCanceled = false;
            if(delay<0) delay = 0;
            timerNodeImpl.timeToRun = System.currentTimeMillis() + delay;
            if(period<0) period = 0;
            timerNodeImpl.period = period;
            boolean isFirst = false;
            synchronized(queue) {
                if(!timerNodeImpl.isCanceled) {
                    timerNodeImpl.isQueued = true;
                    timerNodeImpl.timerInstance = this;
                    queue.add(timerNodeImpl);
                    TimerNodeImpl first = queue.peek();
                    if(first==timerNodeImpl) isFirst = true;
                }
            }
            if(isFirst) thread.wakeUp();
        }
        /* (non-Javadoc)
         * @see org.epics.pvData.misc.Executor#stop()
         */
        public void stop() {
            isStopped = true;
            thread.stop();
            TimerCallback timerCallback = null;
            while(true) {
                synchronized(queue) {
                    TimerNodeImpl timerNode = queue.poll();
                    if(timerNode==null) return;
                    timerCallback = timerNode.timerCallback;
                }
                timerCallback.timerStopped();
            }
        }


        private class ThreadInstance implements RunnableReady {

            private volatile boolean alive = true;
            private Thread thread = null;

            private ThreadInstance(String name,int priority) {
                thread = threadCreate.create(name, priority, this);
            } 
            /* (non-Javadoc)
             * @see org.epics.pvData.misc.RunnableReady#run(org.epics.pvData.misc.ThreadReady)
             */
            public void run(ThreadReady threadReady) {
                threadReady.ready();
                while(alive) {
                    try {
                        long currentTime = System.currentTimeMillis();
                        long timeToRun = currentTime + 1000000;
                        long period = 0;
                        TimerNodeImpl nodeToCall = null;
                        synchronized(queue) {
                            TimerNodeImpl timerNode = queue.peek();
                            if(timerNode!=null) {
                                timeToRun = timerNode.timeToRun;
                                if(timeToRun<=currentTime) {
                                    if(!timerNode.isCanceled) {
                                        nodeToCall = timerNode;
                                    }
                                    queue.poll();
                                    period = timerNode.period;
                                    if(period>0 && !timerNode.isCanceled) {
                                        timerNode.timeToRun = currentTime + period;
                                        queue.add(timerNode);
                                    } else {
                                        timerNode.isQueued = false;
                                        timerNode.timerInstance = null;
                                    }
                                    timerNode = queue.peek();
                                    if(timerNode!=null) {
                                        timeToRun = timerNode.timeToRun;
                                    } else {
                                        timeToRun = currentTime + 1000000;
                                    }
                                }
                            }
                        }
                        if(nodeToCall!=null) {
                            nodeToCall.timerCallback.callback();
                        }
                        long delay = timeToRun - currentTime;
                        if(delay>0) {
                            Thread.sleep(delay);
                        }
                    }catch(InterruptedException e) {}
                } 
            }

            private void wakeUp() {
                thread.interrupt();
            }

            private void stop() {
                alive = false;
                thread.interrupt();
            }
        }
    }

    private static class TimerNodeImpl implements Timer.TimerNode {
        private TimerInstance timerInstance = null;
        private TimerCallback timerCallback;
        private long timeToRun = 0;
        private long period = 0;
        private volatile boolean isQueued = false;
        private volatile boolean isCanceled = false;

        private TimerNodeImpl(TimerCallback timerCallback) {
            this.timerCallback = timerCallback;
        }

        /* (non-Javadoc)
         * @see org.epics.pvData.misc.Timer.TimerNode#cancel()
         */
        public void cancel() {
            isCanceled = true;
            TimerInstance timerInstance = this.timerInstance;
            if(timerInstance!=null) {
                synchronized(timerInstance.queue) {
                    timerInstance.queue.remove(this);
                    isQueued = false;
                }
            }
        }

		/* (non-Javadoc)
		 * @see org.epics.pvData.misc.Timer.TimerNode#isScheduled()
		 */
		@Override
		public boolean isScheduled() {
			return isQueued;
		}
        
        
    }
}
