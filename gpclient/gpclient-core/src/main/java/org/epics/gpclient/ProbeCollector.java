/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient;

import java.io.PrintStream;

/**
 *
 * @author carcassi
 */
public class ProbeCollector<T> {
    
    private final PVEventRecorder recorder;
    private final ReadCollector<T, T> readCollector;
    private final WriteCollector<T> writeCollector;

    public ProbeCollector(Class<T> type, PrintStream out) {
        this.readCollector = new LatestValueCollector<>(type);
        this.writeCollector = new WriteCollector<>();
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
    
    public boolean getConnection() {
        return readCollector.getConnection();
    }
    
    public T getValue() {
        return readCollector.getValue();
    }

    public ReadCollector<T, T> getReadCollector() {
        return readCollector;
    }

    public WriteCollector<T> getWriteCollector() {
        return writeCollector;
    }
    
    public void writeValue(T value) {
        writeCollector.prepareWrite(1);
        writeCollector.queueValue(value);
        writeCollector.sendWriteRequest(0, recorder);
    }

    public PVEventRecorder getRecorder() {
        return recorder;
    }
    
    public static ProbeCollector<?> create() {
        return new ProbeCollector<>(Object.class, null);
    }
    
}
