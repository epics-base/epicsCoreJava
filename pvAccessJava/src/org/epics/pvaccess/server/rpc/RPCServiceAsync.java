/*
 *
 */
package org.epics.pvaccess.server.rpc;

import org.epics.pvdata.pv.PVStructure;

/**
 * Interface defining a asynchronous service.
 * @author msekoranja
 */
public interface RPCServiceAsync extends Service {

	/**
	 * Async. RPC call request.
	 * Implementation of this method should read arguments encoded in a <code>PVStructure</code>.
	 * A response must always be returned by calling <code>RPCResponseCallback</code> callback providing
	 * a completion status and a result structure in form of <code>PVStructure</code>.
	 * @param args RPC request arguments.
	 * @param callback a response callback.
	 */
	void request(PVStructure args, RPCResponseCallback callback);
}
