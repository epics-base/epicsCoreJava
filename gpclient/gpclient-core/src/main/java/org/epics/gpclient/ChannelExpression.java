/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

/**
 * Represents a channel, which can be both read or written.
 *
 * @param <R> type of the read payload
 * @author carcassi
 */
abstract class ChannelExpression<R, W> extends Expression<R, W> {

    private final ReadCollector<?, R> readCollector;
    private final WriteCollector<W> writeCollector;

    ChannelExpression(ReadCollector<?, R> readCollector, WriteCollector<W> writeCollector) {
        super(null, readCollector.getReadFunction(), writeCollector.getWriteFunction());
        this.readCollector = readCollector;
        this.writeCollector = writeCollector;
    }

    protected ReadCollector<?, R> getReadCollector() {
        return readCollector;
    }

    protected WriteCollector<W> getWriteCollector() {
        return writeCollector;
    }

    @Override
    public final void startRead(PVDirector director) {
        director.registerCollector(readCollector);
        connectRead(director);
    }

    protected abstract void connectRead(PVDirector director);

    @Override
    public final void stopRead(PVDirector director) {
        director.deregisterCollector(readCollector);
        disconnectRead(director);
    }

    protected abstract void disconnectRead(PVDirector director);

    @Override
    public void startWrite(PVDirector director) {
        director.registerCollector(writeCollector);
        connectWrite(director);
    }

    protected abstract void connectWrite(PVDirector director);

    @Override
    public final void stopWrite(PVDirector director) {
        director.deregisterCollector(writeCollector);
        disconnectRead(director);
    }

    protected abstract void disconnectWrite(PVDirector director);



}
