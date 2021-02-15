/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

/**
 * A writer of a channel expression created through the {@link GPClient}. The write payload is specified by the generic type,
 * and is changed by {@link #write(java.lang.Object)}. Changes in
 * values are notified through the {@link PVWriterListener}.
 * <p>
 * The write connection flag keeps track of whether the pv can be written to.
 * A write before the pv is connected will result in a failed write.
 * The different write methods allows to choose the type of call (e.g. synch or
 * asynch).
 *
 * @param <T> type of the write payload
 * @author carcassi
 */
public interface PVWriter<T> {

    /**
     * Writes a new value asynchronously using the default callback. The result
     * of the write will be notified through the {@link PVWriterListener}
     * registered at pv creation. Events are aggregated and throttled in the
     * usual way, so multiple writes may correspond to a single event if they
     * happen close to each other.
     *
     * @param newValue the new value
     */
    public void write(T newValue);

    /**
     * Writes a new value asynchronously using the given callback. The
     * {@link PVWriterListener} registered at pv creation will not be notified
     * for this write, only the given callback. The callback will receive one
     * event for each write that is either write succeeded or write failed, so
     * no event aggregation will happen.
     *
     * @param newValue the new value
     * @param callback the callback to be used for this write
     */
    public void write(T newValue, PVWriterListener<T> callback);

    /**
     * Writes synchronously. The
     * {@link PVWriterListener} registered at pv creation will not be notified
     * for this write. If the write is successful the method returns, otherwise
     * it will throw a runtime exception. The cause of the exception will be the
     * error that would have been returned with the asynchronous event.
     *
     * @param newValue the new value
     */
    public void writeAndWait(T newValue);

    /**
     * De-registers all listeners, stops all notifications and closes all
     * connections from the data sources needed by this. Once the PV
     * is closed, it can't be re-opened. Subsequent calls to close do not
     * do anything.
     */
    public void close();

    /**
     * True if no more notifications are going to be sent for this PV.
     *
     * @return true if closed
     */
    public boolean isClosed();

    /**
     * True if the writer is connected and ready to write.
     * <p>
     * False if the channel is not connected, or is connected but can't be
     * written to.
     *
     * @return true if can write
     */
    public boolean isWriteConnected();

}
