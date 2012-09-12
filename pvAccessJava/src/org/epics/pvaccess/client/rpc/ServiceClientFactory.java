/**
 * 
 */
package org.epics.pvaccess.client.rpc;


/**
 * The factory to create a ServiceClient.
 * @author mrk
 *
 */
public class ServiceClientFactory {
	/**
	 * Create a ServiceClient and connect to the service.
	 * @param serviceName The service name. This is the name of a PVRecord with associated support that implements the service.
	 * @return The ServiceClient interface.
	 */
	public static ServiceClient create(String serviceName) {
		return new ServiceClientImpl(serviceName);
	}

	/**
	 * Create a ServiceClient and connect to the service.
	 * @param serviceName The service name. This is the name of a PVRecord with associated support that implements the service.
	 * @param requester The ServiceClientRequester interface implemented by the requester.
	 * @return The ServiceClient interface.
	 */
	public static ServiceClient create(String serviceName, ServiceClientRequester requester) {
		return new ServiceClientImpl(serviceName,requester);
	}

}
