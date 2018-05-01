/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author carcassi
 */
class PVImpl<R, W> implements PV<R, W>{
    
    private final Object lock = new Object();
    private final PVListener<R, W> listener;

    // No need to be guarded: the director is used only in response to actions
    // started by the user. The user recevies the pv only after the
    // the director is set. Whatever other method of publishing the pv
    // itself will take care of synchronizing the director
    private PVDirector<R, W> director = null;

    
    // Guarded by the lock
    private boolean connected = false;
    private boolean writeConnected = false;
    private R value = null;
    private boolean paused = false;
    private boolean closed = false;

    public PVImpl(PVListener<R, W> listener) {
        this.listener = listener;
    }

    void setDirector(PVDirector<R, W> director) {
        this.director = director;
    }
    
    void fireEvent(PVEvent event) {
        listener.pvChanged(event, this);
    }
    
    void fireValueUpdate(PVEvent event, R value) {
        synchronized(lock) {
            this.value = value;
        }
        listener.pvChanged(event, this);
    }
    
    void fireConnectionUpdate(PVEvent event, boolean connected) {
        synchronized(lock) {
            this.connected = connected;
        }
        listener.pvChanged(event, this);
    }
    
    void fireConnectionValueUpdate(PVEvent event, boolean connected, R value) {
        synchronized(lock) {
            this.value = value;
            this.connected = connected;
        }
        listener.pvChanged(event, this);
    }
    
    void fireEvent(PVEvent event, boolean connected, boolean writeConnected, R value) {
        synchronized(lock) {
            if (event.isType(PVEvent.Type.VALUE)) {
                this.value = value;
            }
            if (event.isType(PVEvent.Type.READ_CONNECTION)) {
                this.connected = connected;
            }
            if (event.isType(PVEvent.Type.WRITE_CONNECTION)) {
                this.writeConnected = writeConnected;
            }
        }
        listener.pvChanged(event, this);
    }

    @Override
    public R getValue() {
        synchronized(lock) {
            return value;
        }
    }
    @Override
    public void close() {
        synchronized(lock) {
            director.close();
            closed = true;
        }
    }

    @Override
    public boolean isClosed() {
        synchronized(lock) {
            return closed;
        }
    }

    @Override
    public void setPaused(boolean paused) {
        synchronized(lock) {
            if (this.paused == paused){
                return;
            }
            this.paused = paused;
        }
        if (paused) {
            director.pause();
        } else {
            director.resume();
        }
    }

    @Override
    public boolean isPaused() {
        synchronized(lock) {
            return paused;
        }
    }

    @Override
    public boolean isConnected() {
        synchronized(lock) {
            return connected;
        }
    }
    
    private void checkWriteable() {
        synchronized(lock) {
            if (!writeConnected) {
                throw new IllegalStateException("The pv is not write connected");
            }
        }
    }

    @Override
    public void write(W newValue) {
        checkWriteable();
        director.submitWrite(newValue, null);
    }

    @Override
    public void write(W newValue, PVWriterListener<W> callback) {
        checkWriteable();
        director.submitWrite(newValue, (PVEvent event) -> {
            callback.pvChanged(event, this);
        });
    }

    @Override
    public void writeAndWait(W newValue) {
        checkWriteable();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<PVEvent> response = new AtomicReference<>();
        director.submitWrite(newValue, (PVEvent event) -> {
            response.set(event);
            latch.countDown();
        });
        
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("WriteAndWait interrupted", ex);
        }
        
        PVEvent event = response.get();
        if (event == null) {
            Logger.getLogger(PVImpl.class.getName()).log(Level.SEVERE, "Synch write did not return event");
            throw new IllegalStateException("No event was given");
        }
        
        if (event.isType(PVEvent.Type.WRITE_SUCCEEDED)) {
            return;
        } else if (event.isType(PVEvent.Type.WRITE_FAILED)) {
            throw new RuntimeException("Write failed", event.getWriteError());
        } else {
            Logger.getLogger(PVImpl.class.getName()).log(Level.SEVERE, "Synch write result in a wrong event type: {0}", event);
            throw new IllegalStateException("Event type mismatch");
        }
    }

    @Override
    public boolean isWriteConnected() {
        synchronized(lock) {
            return writeConnected;
        }
    }
    
}
