/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient.expression;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.epics.gpclient.expression.LatestValueCollector;
import org.epics.gpclient.expression.LatestValueCollector;
import org.epics.gpclient.expression.ReadCollector;
import org.epics.gpclient.expression.ReadCollector;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVEvent;
import org.epics.gpclient.PVEventRecorder;

/**
 *
 * @author carcassi
 */
public class ProbeCollector<T> {
    
    private final PVEventRecorder recorder;
    private final ReadCollector<T, T> collector;

    public ProbeCollector(Class<T> type, PrintStream out) {
        this.collector = new LatestValueCollector<>(type);
        this.recorder = new PVEventRecorder() {
            @Override
            protected void onEvent(PVEvent event) {
                if (out != null) {
                    if (event.getType().contains(PVEvent.Type.READ_CONNECTION)) {
                        out.println("CONN: " + collector.getConnection());
                    }
                    if (event.getType().contains(PVEvent.Type.VALUE)) {
                        out.println("VAL: " + collector.get());
                    }
                    if (event.getType().contains(PVEvent.Type.READ_EXCEPTION)) {
                        out.println("ERR: " + event.getException().getMessage());
                    }
                }
            }
            
        };
        this.collector.setUpdateListener(this.recorder);
    }
    
    public boolean getConnection() {
        return collector.getConnection();
    }
    
    public T getValue() {
        return collector.get();
    }

    public ReadCollector<T, T> getCollector() {
        return collector;
    }

    public PVEventRecorder getRecorder() {
        return recorder;
    }
    
    public static ProbeCollector<?> create() {
        return new ProbeCollector<>(Object.class, null);
    }
    
}
