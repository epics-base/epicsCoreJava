/*
 *
 */
package org.epics.pvaccess.client.rpc;
import org.epics.pvaccess.server.rpc.RPCRequestException;
import org.epics.pvdata.pv.PVStructure;

/**
 * Interface that is called by a service client.
 * @author mrk
 */
public interface RPCClient {
	/**
	 * Called by client when the service is no longer required.
	 */
	void destroy();
	/**
	 * Called by client to wait for connection to the service.
	 * This call blocks until a connection is made or until a timeout occurs.
	 * A connection means that a channel connects and a ChannelRPC has been created.
	 * @param timeout The time in seconds to wait for the connection.
	 * @return true on connect, false on timeout.
	 */
	boolean waitConnect(double timeout);
	/**
	 * Send a request and wait for the response or until timeout occurs.
	 * This method will also wait for client to connect, if necessary.
	 * @param pvArgument The argument for the rpc.
	 * @param timeout The time in seconds to wait for the response.
	 * @return request response.
	 * @throws RPCRequestException thrown in case of an server-side error, check RPCRequestException.getStatus() for details.
	 */
	PVStructure request(PVStructure pvArgument, double timeout) throws RPCRequestException;
	/**
	 * Send a request.
	 * The client must be connected at the time this method is called.
	 * @param pvArgument The argument for the rpc.
	 */
	void sendRequest(PVStructure pvArgument);
	/**
	 * Wait for the request to finish.
	 * @param timeout The time in seconds to wait for the response.
	 * @return true on connect, false on timeout.
	 */
	boolean waitResponse(double timeout);
}
