/*
 * Copyright - See the COPYRIGHT that is included with this disctibution.
 * EPICS JavaIOC is distributed subject to a Software License Agreement found
 * in file LICENSE that is included with this distribution.
 */
package org.epics.pvaccess.client;

import org.epics.pvdata.monitor.Monitor;
import org.epics.pvdata.monitor.MonitorRequester;
import org.epics.pvdata.pv.PVField;
import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Requester;


/**
 * Interface for accessing a channel.
 * A channel is created via a call to ChannelAccess.createChannel(String channelName).
 * @author mrk
 * @author msekoranja
 *
 */
public interface Channel extends Requester{

	/**
	 * Channel connection status.
	 */
	public enum ConnectionState {
		NEVER_CONNECTED, CONNECTED, DISCONNECTED, DESTROYED
	};

	/**
     * Get the the channel provider of this channel.
     * @return The channel provider.
     */
    ChannelProvider getProvider();
	/**
	 * Returns the channel's remote address, e.g. "/192.168.1.101:5064" or "#C0 S1".
	 * @return the channel's remote address.
	 **/
	String getRemoteAddress();
	/**
	 * Returns the connection state of this channel.
	 * @return the <code>ConnectionState</code> value.
	 **/
	ConnectionState getConnectionState();
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
     * Create a ChannelProcess.
     * ChannelProcessRequester.channelProcessReady is called after both client and server are ready for
     * the client to make a process request.
     * @param channelProcessRequester The interface for notifying when this request is complete
     * and when channel completes processing.
     * @param pvRequest Additional options (e.g. triggering).
     * @return <code>ChannelProcess</code> instance.
     */
    ChannelProcess createChannelProcess(
            ChannelProcessRequester channelProcessRequester,
    		PVStructure pvRequest);
    /**
     * Create a ChannelGet.
     * ChannelGetRequester.channelGetReady is called after both client and server are ready for
     * the client to make a get request.
     * @param channelGetRequester The interface for notifying when this request is complete
     * and when a channel get completes.
     * @param pvRequest A structure describing the desired set of fields from the remote PVRecord.
     * This has the same form as a pvRequest to PVCopyFactory.create.
     * @return <code>ChannelGet</code> instance.
     */
    ChannelGet createChannelGet(
            ChannelGetRequester channelGetRequester,
            PVStructure pvRequest);
    /**
     * Create a ChannelPut.
     * ChannelPutRequester.channelPutReady is called after both client and server are ready for
     * the client to make a put request.
     * @param channelPutRequester The interface for notifying when this request is complete
     * and when a channel get completes.
     * @param pvRequest A structure describing the desired set of fields from the remote PVRecord.
     * This has the same form as a pvRequest to PVCopyFactory.create.
     * @return <code>ChannelPut</code> instance.
     */
    ChannelPut createChannelPut(
        ChannelPutRequester channelPutRequester,
        PVStructure pvRequest);
    /**
     * Create a ChannelPutGet.
     * ChannelPutGetRequester.channelPutGetReady is called after both client and server are ready for
     * the client to make a putGet request.
     * @param channelPutGetRequester The interface for notifying when this request is complete
     * and when a channel get completes.
     * @param pvRequest A structure describing the desired set of fields from the remote PVRecord.
     * This has the same form as a pvRequest to PVCopyFactory.create.
     * @return <code>ChannelPutGet</code> instance.
     */
    ChannelPutGet createChannelPutGet(
        ChannelPutGetRequester channelPutGetRequester,
        PVStructure pvRequest);
    /**
     * Create a ChannelRPC (Remote Procedure Call).
     * @param channelRPCRequester The requester.
     * @param pvRequest Request options.
     * @return <code>ChannelRPC</code> instance.
     */
    ChannelRPC createChannelRPC(ChannelRPCRequester channelRPCRequester,PVStructure pvRequest);
    /**
     * Create a Monitor.
     * @param monitorRequester The requester.
     * @param pvRequest A structure describing the desired set of fields from the remote PVRecord.
     * This has the same form as a pvRequest to PVCopyFactory.create.
     * @return <code>Monitor</code> instance.
     */
    Monitor createMonitor(
        MonitorRequester monitorRequester,
        PVStructure pvRequest);

    /**
     * Create a ChannelArray.
     * @param channelArrayRequester The ChannelArrayRequester
     * @param pvRequest Additional options (e.g. triggering).
     * @return <code>ChannelArray</code> instance.
     */
    ChannelArray createChannelArray(
        ChannelArrayRequester channelArrayRequester,
        PVStructure pvRequest);
}
