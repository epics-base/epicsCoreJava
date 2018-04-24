/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.gpclient;

/**
 *
 * @author carcassi
 */
class PVImpl<R, W> implements PV<R, W>{
    
    private final Object lock = new Object();
    private final PVListener<R, W> listener;
    
    // Guarded by the lock
    private PVDirector director = null;
    private boolean connected = false;
    private boolean writeConnected = false;
    private R value = null;
    private boolean paused = false;
    private boolean closed = false;

    public PVImpl(PVListener<R, W> listener) {
        this.listener = listener;
    }

    void setDirector(PVDirector director) {
        synchronized(this) {
            this.director = director;
        }
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

    @Override
    public void write(W newValue) {
        director.submitWrite(newValue, null);
    }

    @Override
    public boolean isWriteConnected() {
        synchronized(lock) {
            return writeConnected;
        }
    }
    
}
