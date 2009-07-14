/**
 * Copyright - See the COPYRIGHT that is included with this disctibution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvData.channelAccess;

import org.epics.pvData.misc.Executor;
import org.epics.pvData.pv.PVField;
import org.epics.pvData.pv.PVStructure;
import org.epics.pvData.pv.Requester;


/**
 * Interface for accessing a channel.
 * A channel is created via a call to ChannelAccess.createChannel(String channelName).
 * @author mrk
 *
 */
public interface Channel extends Requester{
    /**
     * Get the name of the channel provider.
     * @return The name.
     */
    String getProviderName();
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
     * Get the channel requester.
     * @return The requester.
     */
    ChannelRequester getChannelRequester();
    /**
     * Is the channel connected?
     * @return (false,true) means (not, is) connected.
     */
    boolean isConnected();
    /**
     * Get a Field which describes the subField.
     * GetFieldRequester.getDone is called after both client and server have processed the getField request.
     * This is for clients that want to introspect a PVRecord via channel access.
     * MAJEJ Neither client or server needs to save this info.
     * @param requester The requester.
     * @param subField The name of the subField.
     * If this is null or an empty string the returned Field is for the entire record.
     */
    void getField(GetFieldRequester requester,String subField);
    /**
     * Get the access rights for a field of a PVStructure created via a call to createPVStructure.
     * MATEJ Channel access can store this info via auxInfo.
     * @param pvField The field for which access rights is desired.
     * @return The access rights.
     */
    AccessRights getAccessRights(PVField pvField);
    /**
     * Create a PVStructure for communication with the server.
     * CreatePVStructureRequester.createDone is called after both client and server have processed the create request.
     * @param channel The channel.
     * @param requester The requester.
     * @param pvRequest A structure describing the desired set of fields from the remote PVRecord.
     * This has the same form as a pvRequest to PVCopyFactory.create.
     * @param structureName The name to give to the created PVStructure.
     * MATEJ Should it be required that this is unique for each call to getStructure?
     * @param shareData On the remote side should the companion PVStructure share data with the PVRecord. 
     */
    void createPVStructure(
            Channel channel,
            CreatePVStructureRequester requester,PVStructure pvRequest,
            String structureName,
            boolean shareData);
    /**
     * Create a ChannelProcess.
     * ChannelProcessRequester.channelProcessReady is called after both client and server are ready for
     * the client to make a process request.
     * @param channel The channel.
     * @param channelProcessRequester The interface for notifying when this request is complete
     * and when channel completes processing.
     */
    void createChannelProcess(
            Channel channel,
            ChannelProcessRequester channelProcessRequester);
    /**
     * Create a ChannelGet.
     * ChannelGetRequester.channelGetReady is called after both client and server are ready for
     * the client to make a get request.
     * @param channel The channel.
     * @param channelGetRequester The interface for notifying when this request is complete
     * and when a channel get completes.
     * @param pvRequest A structure describing the desired set of fields from the remote PVRecord.
     * This has the same form as a pvRequest to PVCopyFactory.create.
     * @param structureName The name to give to the created PVStructure.
     * @param shareData On the remote side should the companion PVStructure share data with the PVRecord. 
     * @param process Process before getting data.
     */
    void createChannelGet(
            Channel channel,
            ChannelGetRequester channelGetRequester,PVStructure pvRequest,
            String structureName,
            boolean shareData, boolean process);
    /**
     * Create a ChannelPut.
     * ChannelPutRequester.channelPutReady is called after both client and server are ready for
     * the client to make a put request.
     * @param channel The channel.
     * @param channelPutRequester The interface for notifying when this request is complete
     * and when a channel get completes.
     * @param pvRequest A structure describing the desired set of fields from the remote PVRecord.
     * This has the same form as a pvRequest to PVCopyFactory.create.
     * @param structureName The name to give to the created PVStructure.
     * @param shareData On the remote side should the companion PVStructure share data with the PVRecord. 
     * @param process Process before getting data.
     */
    void createChannelPut(
        Channel channel,
        ChannelPutRequester channelPutRequester,PVStructure pvRequest,
        String structureName,
        boolean shareData, boolean process);
    /**
     * Create a ChannelPutGet.
     * ChannelPutGetRequester.channelPutGetReady is called after both client and server are ready for
     * the client to make a putGet request.
     * @param channel The channel.
     * @param channelPutGetRequester The interface for notifying when this request is complete
     * and when a channel get completes.
     * @param pvPutRequest A structure describing the desired set of fields from the remote PVRecord.
     * This has the same form as a pvRequest to PVCopyFactory.create.
     * @param putStructureName The name to give to the created PVStructure.
     * @param sharePutData On the remote side should the companion PVStructure share data with the PVRecord. 
     * @param pvGetRequest A structure describing the desired set of fields from the remote PVRecord.
     * This has the same form as a pvRequest to PVCopyFactory.create.
     * @param getStructureName The name to give to the created PVStructure.
     * @param shareGetData On the remote side should the companion PVStructure share data with the PVRecord. 
     * @param process Process after put and before get.
     */
    void createChannelPutGet(
        Channel channel,
        ChannelPutGetRequester channelPutGetRequester,PVStructure pvPutRequest,
        String putStructureName,
        boolean sharePutData,PVStructure pvGetRequest,
        String getStructureName,
        boolean shareGetData, boolean process);
    /**
     * Create a ChannelMonitor.
     * ChannelMonitorRequester.channelMonitorReady is called after both client and server are ready for
     * the client to make a monitor request.
     * @param channel The channel.
     * @param channelMonitorRequester The channelMonitorRequester.
     * @param pvRequest A structure describing the desired set of fields from the remote PVRecord.
     * This has the same form as a pvRequest to PVCopyFactory.create.
     * @param structureName The name to give to the created PVStructure.
     * @param pvOption Monitor options.
     * @param executor An executor for calling requester.
     */
    void createChannelMonitor(
        Channel channel,
        ChannelMonitorRequester channelMonitorRequester,PVStructure pvRequest,
        String structureName,
        PVStructure pvOption, Executor executor);
    
    /**
     * Create a ChannelArray.
     * @param channel The channel.
     * @param channelArrayRequester The ChannelArrayRequester
     * @param subField The subfield of the remote PVRecord.
     */
    void createChannelArray(
        Channel channel,
        ChannelArrayRequester channelArrayRequester, String subField);
}
