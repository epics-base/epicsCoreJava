/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isWriteConnected() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
