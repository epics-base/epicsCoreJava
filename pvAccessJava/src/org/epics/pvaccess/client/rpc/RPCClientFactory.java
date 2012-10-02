/**
 * 
 */
package org.epics.pvaccess.client.rpc;


/**
 * The factory to create a RPCClient.
 * @author mrk
 *
 */
public class RPCClientFactory {
	/**
	 * Create a RPCClient and connect to the service.
	 * @param serviceName The service name. This is the name of a PVRecord with associated support that implements the service.
	 * @return The RPCClient interface.
	 */
	public static RPCClient create(String serviceName) {
		return new RPCClientImpl(serviceName);
	}

	/**
	 * Create a RPCClient and connect to the service.
	 * @param serviceName The service name. This is the name of a PVRecord with associated support that implements the service.
	 * @param requester The RPCClientRequester interface implemented by the requester.
	 * @return The RPCClient interface.
	 */
	public static RPCClient create(String serviceName, RPCClientRequester requester) {
		return new RPCClientImpl(serviceName,requester);
	}

}
