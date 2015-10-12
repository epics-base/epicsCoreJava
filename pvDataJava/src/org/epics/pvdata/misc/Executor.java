/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvdata.misc;

/**
 * Schedule a command to be executed via a thread.
 * An Executor is created via ExecutorFactory.
 * @author mrk
 *
 */
public interface Executor {
    /**
     * Create a node that can be passed to execute.
     * @param command the command to be executed
     * @return the ExecutorNode created
     */
    ExecutorNode createNode(Runnable command);
    /**
     * Execute a command via a thread.
     * @param executorNode The ExecutorNode created by createNode.
     */
    void execute(ExecutorNode executorNode);
    /**
     * Stop the executor.
     */
    void stop();
}
