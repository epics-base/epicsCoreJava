/*
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import java.io.PrintStream;

/**
 * Utility class used to test the communication between the collectors and the
 * data providers.
 *
 * @author carcassi
 */
public class ProbeCollector<T> {

    private final PVEventRecorder recorder;
    private final ReadCollector<T, T> readCollector;
    private final WriteCollector<T> writeCollector;

    /**
     * Creates a probe collector that reads/writes the given type and that
     * streams events to the given output.
     *
     * @param type the type of objects to read/write
     * @param out where to stream the log
     */
    public ProbeCollector(Class<T> type, final PrintStream out) {
        this.readCollector = new LatestValueCollector<T>(type);
        this.writeCollector = new WriteCollector<T>();
        this.recorder = new PVEventRecorder() {
            @Override
            protected void onEvent(PVEvent event) {
                if (out != null) {
                    if (event.getType().contains(PVEvent.Type.READ_CONNECTION)) {
                        out.println("CONN: " + readCollector.getConnection());
                    }
                    if (event.getType().contains(PVEvent.Type.VALUE)) {
                        out.println("VAL: " + readCollector.getValue());
                    }
                    if (event.getType().contains(PVEvent.Type.EXCEPTION)) {
                        out.println("ERR: " + event.getException().getMessage());
                    }
                }
            }

        };
        this.readCollector.setUpdateListener(this.recorder);
        this.writeCollector.setUpdateListener(this.recorder);
    }

    /**
     * The current connection value in the collector.
     *
     * @return the connection value
     */
    public boolean getConnection() {
        return readCollector.getConnection();
    }

    /**
     * The current value in the collector.
     *
     * @return the value
     */
    public T getValue() {
        return readCollector.getValue();
    }

    /**
     * The read collector to send events to.
     *
     * @return the read collector
     */
    public ReadCollector<T, T> getReadCollector() {
        return readCollector;
    }

    /**
     * The write collector to send events to.
     *
     * @return the write collector
     */
    public WriteCollector<T> getWriteCollector() {
        return writeCollector;
    }

    /**
     * Write a value to the collector.
     *
     * @param value the value to be written
     */
    public void writeValue(T value) {
        writeCollector.prepareWrite(1);
        writeCollector.queueValue(value);
        writeCollector.sendWriteRequest(0, recorder);
    }

    /**
     * The recorder that gives access to all the accumulated events.
     *
     * @return the event log
     */
    public PVEventRecorder getRecorder() {
        return recorder;
    }

    /**
     * Creates a simple probe collector that does not write a log.
     *
     * @return a probe collector
     */
    public static ProbeCollector<?> create() {
        return new ProbeCollector<Object>(Object.class, null);
    }

}
