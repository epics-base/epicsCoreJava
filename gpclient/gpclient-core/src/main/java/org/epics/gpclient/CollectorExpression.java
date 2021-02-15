/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

/**
 * A {@link ChannelExpression} that exposes its {@link ReadCollector} and {@link WriteCollector}.
 *
 * @author carcassi
 * @param <R> the type read by the GPClient
 * @param <C> the type collected into the collector
 * @param <W> the type written by the GPClient
 */
public class CollectorExpression<R, C ,W> extends ChannelExpression<R, W> {

    private boolean readConnected;
    private boolean writeConnected;

    /**
     * A new collector expression.
     *
     */
    CollectorExpression(ReadCollector<C, R> readCollector, WriteCollector<W> writeCollector) {
        super(readCollector, writeCollector);
    }

    @Override
    protected void connectRead(PVDirector pvDirector) {
        readConnected = true;
    }

    @Override
    protected void disconnectRead(PVDirector director) {
    }

    @Override
    protected void connectWrite(PVDirector pvDirector) {
        writeConnected = true;
    }

    @Override
    protected void disconnectWrite(PVDirector pvDirector) {
    }

    /**
     * Retrieves the collector that can be used to send updates to the reader.
     * Note that the PVReader must first be started.
     *
     * @return the read collector
     */
    @Override
    public ReadCollector<C, R> getReadCollector() {
        if (!readConnected) {
            throw new IllegalStateException("PVReader was not started");
        }
        return (ReadCollector<C, R>) super.getReadCollector();
    }

    /**
     * Retrieves the collector that can be used to send/receives updates
     * from the writer. Note that the PVWrite must first be started.
     *
     * @return the write collecto
     */
    @Override
    protected WriteCollector<W> getWriteCollector() {
        if (!writeConnected) {
            throw new IllegalStateException("PVWriter was not started");
        }
        return super.getWriteCollector();
    }

}
