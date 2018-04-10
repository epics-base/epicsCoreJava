/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.gpclient;

/**
 * A Channel that uses the collectors to publish and receive data notifications.
 * <p>
 * An implementation of this class will need to setup the data communication
 * to and from the collectors, which are injected by the framework and retrievable
 * through the getters.
 *
 * @author carcassi
 * @param <R> the type of data read from the channel
 */
public abstract class DataChannel<R> {
    
    private final Class<R> readType;
    private volatile ReadCollector<R, ?> readCollector;

    /**
     * A new data channel.
     * 
     * @param readType the type to be read
     */
    public DataChannel(Class<R> readType) {
        if (readType == null) {
            throw new NullPointerException("The readType can't be null");
        }
        this.readType = readType;
    }

    /**
     * Injects the read collector.
     * 
     * @param readCollector the collector for the read communication
     */    
    void setCollector(ReadCollector<R, ?> readCollector) {
        this.readCollector = readCollector;
    }

    /**
     * The collector used by the framework to read incoming data.
     * 
     * @return the read collector
     */
    public ReadCollector<R, ?> getReadCollector() {
        return readCollector;
    }

    /**
     * The type read from the channel.
     * 
     * @return the read type
     */
    public Class<R> getReadType() {
        return readType;
    }
    
    /**
     * Instructs the data channel to activate the reading of the channel.
     * 
     * @param pvDirector the director
     */
    public abstract void startRead(PVDirector pvDirector);
    
    
    /**
     * Instructs the data channel to deactivate the reading of the channel.
     * 
     * @param pvDirector the director
     */
    public abstract void stopRead(PVDirector pvDirector);
}
