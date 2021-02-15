/*
 *
 */
package org.epics.pvaccess.client.rpc;

import org.epics.pvdata.pv.PVStructure;
import org.epics.pvdata.pv.Requester;
import org.epics.pvdata.pv.Status;


/**
 * The interface implemented by a service requester.
 * @author mrk
 */
public interface RPCClientRequester extends Requester {
	/**
	 * The connection request result.
	 * @param client The client issuing the request.
	 * @param status The status. Unless status.isOK is true then the connection failed.
	 */
	void connectResult(RPCClient client, Status status);
	/**
	 * The result returned for a sendRequest.
	 * @param client The client issuing the request.
	 * @param status The status. Unless status.isOK is true then the request failed.
	 * @param pvResult A pvStructure that hold the result of a service request.
	 */
	void requestResult(RPCClient client, Status status, PVStructure pvResult);
}
