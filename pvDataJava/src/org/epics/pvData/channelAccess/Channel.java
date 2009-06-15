/**
 * Copyright - See the COPYRIGHT that is included with this disctibution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;


/**
 * Interface for accessing a channel.
 * A channel is created via a call to ChannelFactory.createChannel(String pvName, ...).
 * The pvName is of the form recordName.name.name...{options}
 * channel.getField returns name.name...
 * channel.gertProperty returns name.name....value if name.name... locates a structure
 * that has a field named "value", i.e. a structure that follows the data model.
 * For an IOC database a channel allows access to all the fields in a single record instance.
 * @author mrk
 *
 */
public interface Channel extends Requester{
    /**
     * Connect to data source.
     */
    void connect();
    /**
     * Disconnect from data source.
     */
    void disconnect();
    /**
     * Destroy the channel. It will not honor any further requests.
     */
    void destroy();
    /**
     * Get the channel name.
     * @return The name.
     */
    String getChannelName();
    /**
     * Get the channel listener.
     * @return The listener.
     */
    ChannelListener getChannelListener();
    /**
     * Is the channel connected?
     * @return (false,true) means (not, is) connected.
     */
    boolean isConnected();
    /**
     * Create a PVStructure for communication with the server.
     * @param pvRequest A structure describing the desired set of fields from the remote PVRecord.
     * This has the same form as a pvRequest to PVCopyFactory.create.
     * @param structureName The name to give to the created PVStructure
     * @param shareData On the remote side should the companion PVStructure share data with the PVRecord. 
     * @return The PVStructure. Note that a companion structure is created on the server.
     */
    PVStructure createPVStructure(PVStructure pvRequest,String structureName,boolean shareData);
    /**
     * Get the access rights for a field of a PVStructure created via a call to createPVStructure.
     * @param pvField The field for which access rights is desired.
     * @return The access rights.
     */
    AccessRights getAccessRights(PVField pvField);
    /**
     * Create a ChannelProcess.
     * @param channelProcessRequester The interface for notifying when channel completes processing.
     * @return An interface for the ChannelProcess or null if the caller can't process the record.
     */
    ChannelProcess createChannelProcess(
        ChannelProcessRequester channelProcessRequester);
    /**
     * Create a ChannelGet.
     * The channel will be processed before reading data if process is true.
     * @param pvStructure A PVStructure created via a call to createPVStructure.
     * @param channelGetRequester The channelGetRequester.
     * @param process Process before getting data.
     * @return An interface for the Get or null if the caller can't process the record.
     */
    ChannelGet createChannelGet(
        PVStructure pvStructure,ChannelGetRequester channelGetRequester,
        boolean process);
    /**
     * Create a ChannelPut.
     * @param pvStructure A PVStructure created via a call to createPVStructure.
     * @param channelPutRequester The channelPutRequester.
     * @param process Should record be processed after put.
     * @return An interface for the CDPut or null if the caller can't process the record.
     */
    ChannelPut createChannelPut(
            PVStructure pvStructure,ChannelPutRequester channelPutRequester,
        boolean process);
    /**
     * Create a ChannelPutGet.
     * @param pvPutStructure A PVStructure created via a call to createPVStructure.
     * @param pvGetStructure A PVStructure created via a call to createPVStructure.
     * @param channelPutGetRequester The channelPutGetRequester.
     * @param process Process after put and before get.
     * @return An interface for the ChannelPutGet or null if the caller can't process the record.
     */
    ChannelPutGet createChannelPutGet(
            PVStructure pvPutStructure,PVStructure pvGetStructure,
        ChannelPutGetRequester channelPutGetRequester,
        boolean process);
    /**
     * Create a ChannelMonitor.
     * @param pvStructure A PVStructure created via a call to createPVStructure.
     * This can be null in which case a monitor event will be issues whenever any field in the PVRecord is modified.
     * @param channelMonitorRequester The channelMonitorRequester.
     * @return The ChannelMonitor interface.
     */
    ChannelMonitor createChannelMonitor(
            PVStructure pvStructure,ChannelMonitorRequester channelMonitorRequester);
}
