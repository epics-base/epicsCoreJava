/*
 *
 */
package org.epics.pvaccess.client.rpc;

import org.epics.pvdata.pv.PVStructure;

/**
 * The factory to create a RPCClient.
 * @author mrk
 *
 */
public class RPCClientFactory {
	/**
	 * Create a RPCClient and connect to the service.
	 * @param serviceName The service name. This is the name of the channel that connects to the service.
	 * @return The RPCClient interface.
	 */
	public static RPCClient create(String serviceName) {
		return new RPCClientImpl(serviceName);
	}

	/**
	 * Create a RPCClient and connect to the service.
	 * @param serviceName The service name. This is the name of the channel that connects to the service.
	 * @param requester The RPCClientRequester interface implemented by the requester.
	 * @return The RPCClient interface.
	 */
	public static RPCClient create(String serviceName, RPCClientRequester requester) {
		return new RPCClientImpl(serviceName,requester);
	}

	/**
	 * Create a RPCClient and connect to the service.
	 * @param serviceName The service name. This is the name of the channel that connects to the service.
	 * @param pvRequest  The structure sent in the request to create the Channel RPC.
	 * @return The RPCClient interface.
	 */
	public static RPCClient create(String serviceName, PVStructure pvRequest) {
		return new RPCClientImpl(serviceName,pvRequest);
	}

	/**
	 * Create a RPCClient and connect to the service.
	 * @param serviceName The service name. This is the name of the channel that connects to the service.
	 * @param pvRequest  The structure sent in the request to create the Channel RPC.
	 * @param requester The RPCClientRequester interface implemented by the requester.
	 * @return The RPCClient interface.
	 */
	public static RPCClient create(String serviceName, PVStructure pvRequest, RPCClientRequester requester) {
		return new RPCClientImpl(serviceName,pvRequest,requester);
	}

}
