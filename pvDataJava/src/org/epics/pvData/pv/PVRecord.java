/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;



/**
 * PVRecord interrace.
 * @author mrk
 *
 */
public interface PVRecord extends PVStructure {
    /**
     * Get the PVStructure.
     * @return The PVStructure interface.
     */
    PVStructure getPVStructure();
    /**
     * Get the record instance name.
     * @return The name.
     */
    String getRecordName();
    /**
     * Report a message.
     * The record name will be appended to the message.
     * @param message The message.
     * @param messageType The message type.
     */
    void message(String message, MessageType messageType);
    /**
     * Add a requester to receive messages.
     * @param requester The requester to add.
     */
    void addRequester(Requester requester);
    /**
     * Remove a message requester.
     * @param requester The requester to remove.
     */
    void removeRequester(Requester requester);
    /**
     * Lock the record instance.
     * This must be called before accessing the record.
     */
    void lock();
    /**
     * Unlock the record.
     */
    void unlock();
    /**
     * While holding lock on this record lock another record.
     * If the other record is already locked than this record may be unlocked.
     * The caller must call the unlock method of the other record when done with it.
     * @param otherRecord the other record.
     */
    void lockOtherRecord(PVRecord otherRecord);
    /**
     * Begin a group of related puts.
     */
    void beginGroupPut();
    /**
     * End of a group of related puts.
     */
    void endGroupPut();
    /**
     * Register a PVListener. This must be called before pvField.addListener.
     * @param pvListener The listener.
     */
    void registerListener(PVListener pvListener);
    /**
     * Unregister a PVListener.
     * @param pvListener The listener.
     */
    void unregisterListener(PVListener pvListener);
    /**
     * Is this pvListener registered? This is called by AbstractPVField. addListener.
     * @param pvListener The listener.
     * @return (false,true) if the listener (is not, is) registered.
     */
    boolean isRegisteredListener(PVListener pvListener);
    /**
     * Remove every PVListener.
     */
    void removeEveryListener();
}
