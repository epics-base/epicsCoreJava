/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;

/**
 * PVRecordField is for PVField that are part of a PVRecord.
 * Each PVType has an interface that extends PVField.
 * @author mrk
 *
 */
public interface PVRecordField{
    /**
     * Get the record.
     * @return The record interface.
     */
    PVRecord getPVRecord();
    /**
     * Add a listener to this field.
     * @param pvListener The pvListener to add to list for postPut notification.
     * @return (false,true) if the pvListener (was not,was) added.
     * If the listener was already in the list false is returned.
     */
    boolean addListener(PVListener pvListener);
    /**
     * remove a pvListener.
     * @param pvListener The listener to remove.
     */
    void removeListener(PVListener pvListener);
    /**
     * post that data has been modified.
     * This must be called by the code that implements put.
     */
    void postPut();
}
