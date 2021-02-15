/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import org.joda.time.Duration;
import java.util.concurrent.Executor;
import org.epics.util.compat.legacy.functional.Consumer;
import org.epics.gpclient.datasource.DataSource;

/**
 * An expression used to set the final parameters on how the pv expression
 * should be read.
 *
 * @param <T> the type of the expression
 * @author carcassi
 */
public interface PVReaderConfiguration<T> {

    /**
     * Defines which DataSource should be used to read/write the data.
     *
     * @param dataSource the data source to access the data
     * @return this
     */
    public PVReaderConfiguration<T> from(DataSource dataSource);

    /**
     * Defines on which thread to notify the client.
     *
     * @param onThread the thread on which to notify
     * @return this
     */
    public PVReaderConfiguration<T> notifyOn(Executor onThread);

    /**
     * Sends a {@link TimeoutException} if the expression does not connect
     * within the given duration.
     *
     * @param timeout time to wait before the timeout
     * @return this
     */
    public PVReaderConfiguration<T> connectionTimeout(Duration timeout);

    /**
     * Sends a {@link TimeoutException} with the given message if the expression
     * does not connect within the given duration.
     *
     * @param timeout time to wait before the timeout
     * @param timeoutMessage the exception message
     * @return this
     */
    public PVReaderConfiguration<T> connectionTimeout(Duration timeout, String timeoutMessage);

    /**
     * The maximum rate of notifications for this reader/writer.
     *
     * @param maxRate the maximum time between notifications
     * @return this
     */
    public PVReaderConfiguration<T> maxRate(Duration maxRate);

    /**
     * Adds a read listener for the expression.
     *
     * @param listener a new listener
     * @return this
     */
    public PVReaderConfiguration<T> addReadListener(PVReaderListener<T> listener);

    /**
     * Adds a listener for the expression.
     *
     * @param listener a new listener
     * @return this
     */
    public PVReaderConfiguration<T> addListener(Consumer<PVEvent> listener);

    /**
     * Starts processing events for the expression.
     *
     * @return the new pv
     */
    public PVReader<T> start();
}
