/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;

/**
 * ThreadCreate : Create a thread.
 * This provides two features:
 * <ol>
 *    <li>The create does not return until the thread has started.</li>
 *    <li>Keeps a list of all active threads,
 *    i.e. threads for which the run method has not returned.</li>
 * </ol>
 * @author mrk
 *
 */
public interface ThreadCreate {
    /**
     * Create a new thread.
     * @param name The thread name.
     * @param priority The thread priority.
     * @param runnableReady An implementation of RunnableReady.
     * @return The newly created thread. Create does not return until ready has been called.
     */
    Thread create(String name, int priority, RunnableReady runnableReady);
    /**
     * Get an array of all the active threads.
     * @return The array.
     */
    Thread[] getThreads();
}
