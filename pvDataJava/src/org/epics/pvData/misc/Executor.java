/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS pvData is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.misc;

/**
 * Schedule a command to be executed via a thread.
 * An IOCExecutor is created via IOCExecutorFactory.
 * @author mrk
 *
 */
public interface Executor {
    /**
     * Execute a command via a thread.
     * @param command The interface for the command.
     */
    void execute(Runnable command);
    /**
     * Stop the executor.
     */
    void stop();
}
