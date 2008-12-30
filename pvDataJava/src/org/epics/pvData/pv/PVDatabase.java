/**
 * Copyright - See the COPYRIGHT that is included with this distribution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.pv;


/**
 * IOCDB (Input/Output Controller Database).
 * An IOCDB contains record instance definitions.
 * @author mrk
 *
 */
public interface PVDatabase extends Requester{
    /**
     * Get the master IOCDB.
     * @return The master IOCDB.
     */
    PVDatabase getMaster();
    /**
     * Get the name of the IOCDB.
     * @return The name.
     */
    String getName();
    /**
     * Merge all definitions into the master IOCDB.
     * After the merge all definitions are cleared from this IOCDB and this IOCDB is removed from the IOCDBFactory list.
     */
    void mergeIntoMaster();
    /**
     * Find the interface for a record instance.
     * It will be returned if it resides in this IOCDB or in the master IOCDB.
     * @param recordName The instance name.
     * @return The interface or null if the record is not located.
     */
    PVRecord findRecord(String recordName);
    /**
     * Add a new record instance.
     * @param record The record instance.
     * @return true if the record was created.
     */
    boolean addRecord(PVRecord record);
    /**
     * Remove a record instance.
     * @param record The record instance.
     * @return true if the record was removed and false otherwise.
     */
    boolean removeRecord(PVRecord record);
    /**
     * Get an array of record instances.
     * @return The array of record instances.
     */
    PVRecord[] getRecords();
    /**
     * find a structure.
     * @param structureName The structure name.
     * @return The interface or null if the structure is not located.
     */
    PVStructure findStructure(String structureName);
    /**
     * Add a new structure instance.
     * @param structure The structure instance.
     * @return true if the structure was added.
     */
    boolean addStructure(PVStructure structure);
    /**
     * Remove a structure instance.
     * @param structure The structure instance.
     * @return true if the structure was removed and false otherwise.
     */
    boolean removeStructure(PVStructure structure);
    /**
     * Get an array of structure instances.
     * @return The array of structure instances.
     */
    PVStructure[] getStructures();
    /**
     * Report a message.
     * If no listeners are registered the messages are sent to System.out (info) or System.err (all other message types).
     * If listeners are registered they are called.
     * @param message The message.
     * @param messageType The message type.
     */
    void message(String message, MessageType messageType);
    /**
     * Add a message requester.
     * @param requester The requester.
     */
    void addRequester(Requester requester);
    /**
     * Remove a message requester.
     * @param requester The requester.
     */
    void removeRequester(Requester requester);
    /**
     * Generate a list of record instance with names that match the regular expression.
     * @param regularExpression The regular expression.
     * @return A string array containing the list.
     */
    String[] recordList(String regularExpression);
    /**
     * Generate a list of structure instance with names that match the regular expression.
     * @param regularExpression The regular expression.
     * @return A string array containing the list.
     */
    String[] structureList(String regularExpression);
    /**
     * Dump all the record instances with names that match the regular expression.
     * @param regularExpression The regular expression.
     * @return A string containing the dump.
     */
    String recordToString(String regularExpression);
    /**
     * Dump all the structure instances with names that match the regular expression.
     * @param regularExpression The regular expression.
     * @return A string containing the dump.
     */
    String structureToString(String regularExpression);
}
