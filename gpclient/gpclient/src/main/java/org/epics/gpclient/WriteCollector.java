/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.gpclient;

import java.util.Collection;
import java.util.function.Supplier;

/**
 * @author carcassi
 */
public abstract class WriteCollector<T> {
    
    final Object lock = new Object();
    Runnable notification;
    Runnable writeNotification;
    Boolean connection = Boolean.FALSE;
    
    void setChangeNotification(Runnable notification) {
        synchronized (lock) {
            this.notification = notification;
        }
    }
    
    public void setWriteNotification(Runnable writeNotification) {
        synchronized (lock) {
            this.writeNotification = writeNotification;
        }
    }
    
    public abstract Class<T> getType();
    
    public abstract Collection<T> getValues();
    
    abstract void queueValue(T value);
    
    public abstract void updateConnection(boolean connection);
    
    public abstract void notifyError(Exception ex);
    
    public abstract void sendWriteSuccessful();
    
    public abstract void sendWriteFailed(Exception ex);
    
}
